plugins {
    id("mobimon.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android { buildFeatures { compose = true } }

dependencies {
    "implementation"(platform(catalog.findLibrary("androidx-compose-bom").get()))
    "implementation"(catalog.findLibrary("androidx-compose-ui").get())
    "implementation"(catalog.findLibrary("androidx-compose-foundation").get())
    "implementation"(catalog.findLibrary("androidx-compose-material3").get())
    "implementation"(catalog.findLibrary("androidx-compose-ui-tooling-preview").get())
    "implementation"(catalog.findLibrary("androidx-activity-compose").get())
    "implementation"(catalog.findLibrary("kotlinx-coroutines-android").get())
    "testImplementation"(platform(catalog.findLibrary("androidx-compose-bom").get()))
    "testImplementation"(catalog.findLibrary("androidx-compose-ui-test-junit4").get())
    "debugImplementation"(catalog.findLibrary("androidx-compose-ui-test-manifest").get())
    "testImplementation"(catalog.findLibrary("junit").get())
    "testImplementation"(catalog.findLibrary("kotlinx-coroutines-test").get())
    "testImplementation"(catalog.findLibrary("robolectric").get())
    "testImplementation"(catalog.findLibrary("androidx-test-core").get())
    "testImplementation"(catalog.findLibrary("androidx-test-junit").get())
    "androidTestImplementation"(catalog.findLibrary("androidx-test-runner").get())
    "androidTestImplementation"(catalog.findLibrary("androidx-test-junit").get())
    "androidTestImplementation"(catalog.findLibrary("kotlinx-coroutines-test").get())
}
