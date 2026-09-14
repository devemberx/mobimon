import org.gradle.api.artifacts.dsl.LockMode

plugins {
    `kotlin-dsl`
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(libs.build.android)
    implementation(libs.build.kotlin)
    implementation(libs.build.compose)
    implementation(libs.build.ktlint)
    implementation(libs.build.kover)
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.STRICT)
}
