import org.gradle.api.artifacts.dsl.LockMode

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

kotlin { jvmToolchain(17) }

ktlint { version.set(libs.versions.ktlint.engine) }

configurations.configureEach {
    if (name.endsWith("CompileClasspath") ||
        name.endsWith("RuntimeClasspath") ||
        name in listOf("compileClasspath", "runtimeClasspath", "testCompileClasspath", "testRuntimeClasspath")
    ) {
        resolutionStrategy.activateDependencyLocking()
    }
}
dependencyLocking { lockMode.set(LockMode.STRICT) }

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
