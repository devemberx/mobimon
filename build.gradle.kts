plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ktlint)
}

ktlint {
    version.set(libs.versions.ktlint.engine)
}

tasks.register("verifyModuleBoundaries") {
    group = "verification"
    description = "Rejects feature coupling and dependencies that cross architecture boundaries."
    doLast {
        val featureDependencies =
            setOf(":core:core-domain", ":core:core-ui", ":core:core-navigation", ":core:core-presentation")
        val domainOnly = setOf(":core:core-domain")
        subprojects.forEach { module ->
            val allowed =
                when {
                    module.path == ":app" -> subprojects.map { it.path }.toSet()
                    module.path.startsWith(":feature:") -> featureDependencies
                    module.path in
                        setOf(
                            ":core:core-database",
                            ":core:core-auth",
                            ":core:core-vss",
                            ":core:core-presentation",
                        )
                    -> domainOnly
                    else -> emptySet()
                }
            module.configurations.forEach { configuration ->
                configuration.dependencies.withType<ProjectDependency>().forEach { dependency ->
                    val target = dependency.dependencyProject.path
                    check(target == module.path || target in allowed) {
                        "${module.path}:${configuration.name} must not depend on $target"
                    }
                }
                val productionDependency =
                    Regex(
                        "^(api|implementation|compileOnly|runtimeOnly|.*(Api|Implementation|CompileOnly|RuntimeOnly))$",
                    ).matches(configuration.name) &&
                        !configuration.name.contains("test", ignoreCase = true)
                if (productionDependency) {
                    configuration.dependencies.withType<ExternalModuleDependency>().forEach { dependency ->
                        if (module.path in setOf(":core:core-domain", ":core:core-vss")) {
                            check(dependency.group in setOf("org.jetbrains.kotlin", "org.jetbrains.kotlinx")) {
                                "${module.path}:${configuration.name} must remain platform independent: $dependency"
                            }
                        }
                        if (module.path.startsWith(":feature:") ||
                            module.path in setOf(":core:core-ui", ":core:core-navigation", ":core:core-presentation")
                        ) {
                            check(
                                dependency.group !in setOf("androidx.room", "androidx.datastore", "com.google.dagger"),
                            ) {
                                "${module.path}:${configuration.name} must not own storage or DI implementations: $dependency"
                            }
                        }
                    }
                }
            }
            val pure = module.path in setOf(":core:core-domain", ":core:core-vss")
            if (pure) {
                check(
                    !module.plugins.hasPlugin("com.android.library") &&
                        !module.plugins.hasPlugin("com.android.application"),
                ) {
                    "${module.path} must remain plain Kotlin"
                }
                listOf("compileClasspath", "runtimeClasspath").forEach { name ->
                    module.configurations.getByName(name).incoming.resolutionResult.allComponents.forEach { component ->
                        val id = component.id as? org.gradle.api.artifacts.component.ModuleComponentIdentifier
                        check(
                            id == null ||
                                id.group in setOf("org.jetbrains", "org.jetbrains.kotlin", "org.jetbrains.kotlinx"),
                        ) {
                            "${module.path} has a platform dependency through its resolved graph: $id"
                        }
                    }
                }
            }
            val forbidden =
                when {
                    pure ->
                        listOf("android.", "androidx.", "dagger.", "com.monsters.mobimon.feature.") +
                            listOf(
                                "database",
                                "auth",
                                "ui",
                                "presentation",
                                "navigation",
                            ).map { "com.monsters.mobimon.core.$it." }
                    module.path in setOf(":core:core-ui", ":core:core-navigation") ->
                        listOf("androidx.room.", "androidx.datastore.", "androidx.lifecycle.ViewModel", "dagger.") +
                            listOf(
                                "domain",
                                "database",
                                "auth",
                                "presentation",
                                "vss",
                            ).map { "com.monsters.mobimon.core.$it." } +
                            "com.monsters.mobimon.feature."
                    module.path.startsWith(":feature:") || module.path == ":core:core-presentation" ->
                        listOf(
                            "androidx.room.",
                            "androidx.datastore.",
                            "dagger.",
                            "com.monsters.mobimon.core.database.",
                            "com.monsters.mobimon.core.auth.",
                            "com.monsters.mobimon.core.vss.",
                        )
                    else -> emptyList()
                }
            module
                .fileTree("src")
                .matching {
                    include("**/*.kt", "**/*.java")
                    exclude("test*/**", "androidTest/**", "journeyTest/**", "migrationTest/**")
                }.forEach { source ->
                    source.readLines().map(String::trimStart).filter { it.startsWith("import ") }.forEach { line ->
                        val imported = line.removePrefix("import ").removePrefix("static ")
                        check(
                            forbidden.none(imported::startsWith),
                        ) { "Forbidden architecture import in $source: $imported" }
                    }
                }
        }
    }
}

tasks.register("resolveDependencies") {
    group = "build setup"
    description = "Resolves every module for an explicit --write-locks refresh."
    dependsOn(subprojects.map { "${it.path}:dependencies" })
}
