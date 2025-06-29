// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

}
buildscript {
    repositories {

        mavenCentral()
        maven(url = "file:///Users/muhammad.alfauzan/Development/DexGuard/DexGuard-9.10.1/lib")
        flatDir { dirs("libs") }
    }
    dependencies {
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.4")
        classpath ("com.github.dcendents:android-maven-gradle-plugin:1.4.1")
        classpath("com.guardsquare:dexguard-gradle-plugin:9.10.1")
    }
}