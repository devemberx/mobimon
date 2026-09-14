import org.gradle.api.artifacts.dsl.LockMode

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin { jvmToolchain(17) }
ktlint { version.set(catalog.findVersion("ktlint-engine").get().requiredVersion) }

configurations.configureEach {
    if (name in listOf("compileClasspath", "runtimeClasspath", "testCompileClasspath", "testRuntimeClasspath")) {
        resolutionStrategy.activateDependencyLocking()
    }
}
dependencyLocking { lockMode.set(LockMode.STRICT) }

dependencies {
    "implementation"(catalog.findLibrary("kotlinx-coroutines-core").get())
    "testImplementation"(catalog.findLibrary("junit").get())
    "testImplementation"(catalog.findLibrary("kotlinx-coroutines-test").get())
}
