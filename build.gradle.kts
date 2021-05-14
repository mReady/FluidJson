plugins {
    kotlin("multiplatform") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
}

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    jcenter()
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            useExperimentalAnnotation("kotlin.RequiresOptIn")
            useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        }
    }

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

//    js(IR) {
//        browser()
//        nodejs()
//    }

    ios {
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
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
