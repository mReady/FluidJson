import java.util.Properties

plugins {
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.serialization") version "1.3.61"
    id("com.vanniktech.maven.publish") version "0.9.0"
}

tasks {
    compileKotlin {
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xuse-experimental=kotlin.Experimental",
            "-XXLanguage:+InlineClasses",
            "-progressive"
        )
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.freeCompilerArgs = listOf(
            "-Xuse-experimental=kotlin.Experimental",
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

    testImplementation("junit:junit:4.12")
    testImplementation(kotlin("test-junit"))
}
