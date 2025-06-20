// settings.gradle.kts (Corrected Version)

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // THIS IS THE CORRECTED LINE:
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Budget Tracker"
include(":app")