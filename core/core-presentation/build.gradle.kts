plugins { id("mobimon.android.compose") }

android { namespace = "com.monsters.mobimon.core.presentation" }

dependencies {
    implementation(project(":core:core-domain"))
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
