plugins { id("mobimon.android.compose") }

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(project(":core:core-domain"))
    "implementation"(project(":core:core-ui"))
    "implementation"(project(":core:core-navigation"))
    "implementation"(project(":core:core-presentation"))
    "implementation"(catalog.findLibrary("androidx-lifecycle-viewmodel").get())
    "implementation"(catalog.findLibrary("androidx-lifecycle-runtime-compose").get())
    "implementation"(catalog.findLibrary("androidx-lifecycle-viewmodel-compose").get())
}
