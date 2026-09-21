plugins { id("mobimon.android.library") }

android { namespace = "com.monsters.mobimon.core.vss" }

val vssJar = file("libs/mobis.framework.core.jar")

dependencies {
    implementation(project(":core:core-domain"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    if (vssJar.exists()) {
        compileOnly(files(vssJar))
    }
}
