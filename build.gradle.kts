plugins {
    kotlin("jvm") version "1.3.70-eap-42"
    kotlin("plugin.serialization") version "1.3.70-eap-42"
}

group = "net.mready"
version = "1.0-SNAPSHOT"


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
