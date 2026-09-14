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
            }
            if (module.path == ":core:core-domain") {
                check(!module.plugins.hasPlugin("com.android.library")) { "core-domain must remain plain Kotlin" }
                module.fileTree("src/main").matching { include("**/*.kt") }.forEach { source ->
                    val forbidden =
                        listOf("android.", "androidx.", "dagger.", "com.monsters.mobimon.feature.") +
                            listOf("database", "ui", "presentation", "navigation", "vss").map {
                                "com.monsters.mobimon.core.$it."
                            }
                    source.readLines().filter { it.startsWith("import ") }.forEach { line ->
                        check(forbidden.none { line.removePrefix("import ").startsWith(it) }) {
                            "Forbidden domain import in $source"
                        }
                    }
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
