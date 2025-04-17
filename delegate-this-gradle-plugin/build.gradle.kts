plugins {
    id("com.gradle.plugin-publish") version "1.3.1"
    signing
    kotlin("jvm") version "2.1.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

group = "io.github.dyominmv"
version = "1.0.0"

dependencies {
    api("io.github.dyominmv", "delegate-this-plugin-base", version.toString())
    implementation("org.jetbrains.kotlin", "kotlin-gradle-plugin", "2.1.0")
}

gradlePlugin {
    website = "https://github.com/dyominmv/delegate-this"
    vcsUrl = "https://github.com/dyominmv/delegate-this.git"
    plugins {
        create("delegateThis") {
            id = "io.github.dyominmv.delegate-this-gradle-plugin"
            implementationClass = "io.github.dyominmv.delegatethis.DelegateThisPlugin"
            displayName = "delegate-this"
            description = "Gradle plugin to transform delegators in order to initialize instances of Delegate"
            tags = listOf("kotlin", "delegate")
        }
    }
}

publishing {
    repositories { mavenLocal() }
}

signing { useGpgCmd() }