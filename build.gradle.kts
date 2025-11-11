@file:Suppress("PropertyName")


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.vaniktek.mavem.publish)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.kotlin.atomicfu)
    //id("maven-publish")
}

val VERSION_NAME: String by project

group = "net.mready.json"
version = VERSION_NAME

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

//    js(IR) {
//        browser()
//        nodejs()
//    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }

        commonTest {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
            }
        }

        jvmTest {
            dependencies {
                dependencies {
                    implementation("junit:junit:4.12")
                    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
                    implementation(kotlin("reflect"))
                    implementation(kotlin("test-junit"))
                }
            }
        }
    }
}

val isReleaseBuild: Boolean get() = !VERSION_NAME.contains("SNAPSHOT")

val POM_ARTIFACT_ID: String by project
val POM_NAME: String by project
val POM_DESCRIPTION: String by project
val POM_DEVELOPER_ID: String by project
val POM_DEVELOPER_NAME: String by project
val POM_DEVELOPER_EMAIL: String by project
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

mavenPublishing {
    //For publishing to mavenLocal comment the next line, uncomment the id("maven-publish") plugin
    // sync with gradle and run the task publishToMavenLocal
    signAllPublications()

    publishToMavenCentral()
    pom {
        name = POM_NAME
        description = POM_DESCRIPTION
        url = POM_URL

        licenses {
            license {
                name = POM_LICENCE_NAME
                url = POM_LICENCE_URL
                distribution = POM_LICENCE_DIST
            }
        }

        scm {
            url = POM_SCM_URL
            connection = POM_SCM_CONNECTION
            developerConnection = POM_SCM_DEV_CONNECTION
        }

        developers {
            developer {
                id = POM_DEVELOPER_ID
                name = POM_DEVELOPER_NAME
                email = POM_DEVELOPER_EMAIL
                url = POM_DEVELOPER_URL
                organization = POM_DEVELOPER_NAME
                organizationUrl = POM_DEVELOPER_URL
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