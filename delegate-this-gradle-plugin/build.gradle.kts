plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.1.0"
    `maven-publish`
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
    plugins {
        create("simplePlugin") {
            id = "io.github.dyominmv.delegate-this-gradle-plugin"
            implementationClass = "io.github.dyominmv.delegatethis.DelegateThisPlugin"
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.dyominmv"
            artifactId = "delegate-this-gradle-plugin"
            version = rootProject.version.toString()

            from(components["kotlin"])
        }
    }

    repositories {
        mavenLocal()
    }
}