// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

allprojects {
    val userHome = System.getProperty("user.home").replace('\\', '/')
    layout.buildDirectory.set(file("$userHome/.gradle_builds/SecureDroid/${project.name}"))
}