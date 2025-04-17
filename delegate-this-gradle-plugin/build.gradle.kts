plugins {
    id("com.gradle.plugin-publish") version "1.3.1"
    signing
    kotlin("jvm") version "2.1.0"
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
        create("gradletestPlugin") {
            id = "io.github.dyominmv.delegate-this-gradle-plugin"
            implementationClass = "io.github.dyominmv.delegatethis.DelegateThisPlugin"
            displayName = "delegate-this"
            description = "Gradle plugin to transform delegators in order to initialize instances of Delegate"
            tags = listOf("kotlin", "delegate")
        }
    }
}

publishing {
    repositories { mavenLocal { name = "local" } }
    publications {
        create<MavenPublication>("snapshot") {
            groupId = "io.github.dyominmv"
            artifactId = "delegate-this-gradle-plugin"
            version = rootProject.version.toString()

            from(components["kotlin"])
        }
    }
}