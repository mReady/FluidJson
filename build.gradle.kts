import java.util.Properties

plugins {
    kotlin("jvm") version "1.4-M3"
    kotlin("plugin.serialization") version "1.4-M3"
    id("com.vanniktech.maven.publish") version "0.9.0"
}

tasks {
    compileKotlin {
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.ExperimentalStdlibApi",
            "-XXLanguage:+InlineClasses",
            "-progressive"
        )
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.ExperimentalStdlibApi",
            "-XXLanguage:+InlineClasses",
            "-progressive"
        )
        kotlinOptions.jvmTarget = "1.8"
    }

    mavenPublish {
        releaseSigningEnabled = false
        targets {
            named("uploadArchives") {
                val properties = Properties()
                project.rootProject.file("local.properties").inputStream().use {
                    properties.load(it)
                }

                releaseRepositoryUrl = properties.getProperty("NEXUS_RELEASE_URL")
                snapshotRepositoryUrl = properties.getProperty("NEXUS_SNAPSHOT_URL")
                repositoryUsername = properties.getProperty("NEXUS_USERNAME")
                repositoryPassword = properties.getProperty("NEXUS_PASSWORD")
                signing = false
            }
        }
    }
}


repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0-1.4-M3")

    testImplementation("junit:junit:4.12")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("test-junit"))
}
