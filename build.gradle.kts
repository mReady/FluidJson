@file:Suppress("PropertyName")

plugins {
    kotlin("multiplatform") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.21"
    id("kotlinx-atomicfu") version "0.18.5"
    id("maven-publish")
    id("signing")
}

val VERSION_NAME: String by project

group = "net.mready.json"
version = VERSION_NAME

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            optIn("kotlin.RequiresOptIn")
            optIn("kotlin.ExperimentalStdlibApi")
        }
    }

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()

//    js(IR) {
//        browser()
//        nodejs()
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:atomicfu:0.18.5")
            }
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting { }

        val jvmTest by getting {
            dependencies {
                implementation("junit:junit:4.12")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
                implementation(kotlin("reflect"))
                implementation(kotlin("test-junit"))
            }
        }

//        val jsMain by getting {
//        }
//
//        val jsTest by getting {
//        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val isReleaseBuild: Boolean get() = !VERSION_NAME.contains("SNAPSHOT")

val POM_ARTIFACT_ID: String by project
val POM_NAME: String by project
val POM_DESCRIPTION: String by project
val POM_DEVELOPER_ID: String by project
val POM_DEVELOPER_NAME: String by project
val POM_DEVELOPER_URL: String by project
val POM_URL: String by project
val POM_SCM_URL: String by project
val POM_SCM_CONNECTION: String by project
val POM_SCM_DEV_CONNECTION: String by project
val POM_LICENCE_NAME: String by project
val POM_LICENCE_URL: String by project
val POM_LICENCE_DIST: String by project

val RELEASE_REPOSITORY_URL: String
    get() = findProperty("RELEASE_REPOSITORY_URL") as String?
        ?: "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"

val SNAPSHOT_REPOSITORY_URL: String
    get() = findProperty("SNAPSHOT_REPOSITORY_URL") as String?
        ?: "https://s01.oss.sonatype.org/content/repositories/snapshots/"

val SONATYPE_NEXUS_USERNAME: String
    get() = findProperty("mavenCentralRepositoryUsername") as String? ?: ""

val SONATYPE_NEXUS_PASSWORD: String
    get() = findProperty("mavenCentralRepositoryPassword") as String? ?: ""

publishing {
    publications.withType<MavenPublication> {
        artifactId = artifactId.toLowerCase()

        artifact(javadocJar.get())
        pom {
            this.description.set(POM_DESCRIPTION)
            this.name.set(POM_NAME)
            this.url.set(POM_URL)
            licenses {
                license {
                    this.name.set(POM_LICENCE_NAME)
                    this.url.set(POM_LICENCE_URL)
                    this.distribution.set(POM_LICENCE_DIST)
                }
            }
            scm {
                this.url.set(POM_SCM_URL)
                this.connection.set(POM_SCM_CONNECTION)
                this.developerConnection.set(POM_SCM_DEV_CONNECTION)
            }
            developers {
                developer {
                    this.id.set(POM_DEVELOPER_ID)
                    this.name.set(POM_DEVELOPER_NAME)
                    this.url.set(POM_DEVELOPER_URL)
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            setUrl(if (isReleaseBuild) RELEASE_REPOSITORY_URL else SNAPSHOT_REPOSITORY_URL)
            credentials {
                username = SONATYPE_NEXUS_USERNAME
                password = SONATYPE_NEXUS_PASSWORD
            }
        }
    }
}

signing {
    setRequired { isReleaseBuild && gradle.taskGraph.hasTask("uploadArchives") }
    sign(publishing.publications)
}