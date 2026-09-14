plugins {
    id("mobimon.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.monsters.mobimon.core.database"
    sourceSets {
        listOf("test", "androidTest").forEach { sourceSet ->
            getByName(sourceSet) {
                assets.srcDirs("schemas", "src/migrationTest/assets")
            }
        }
    }
}

kotlin {
    sourceSets {
        listOf("test", "androidTest").forEach { sourceSet ->
            getByName(sourceSet).kotlin.srcDir("src/migrationTest/java")
        }
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    // Align AGP's compile/runtime resolution with the graph used to generate strict locks.
    runtimeOnly(libs.kotlin.stdlib.common)
    implementation(project(":core:core-domain"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.room.testing)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
