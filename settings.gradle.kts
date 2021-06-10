pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-atomicfu") {
                useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${requested.version}")
            }
        }
    }

    repositories {
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = "FluidJson"

