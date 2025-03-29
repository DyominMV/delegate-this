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

group = "mikhail.dyomin"
version = "1.0.0"

dependencies {
    api("mikhail.dyomin", "delegate-this-plugin-base", version.toString())
    implementation("org.jetbrains.kotlin", "kotlin-gradle-plugin", "2.1.0")
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "mikhail.dyomin.delegate-this-gradle-plugin"
            implementationClass = "mikhail.dyomin.delegatethis.DelegateThisPlugin"
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
            groupId = "mikhail.dyomin"
            artifactId = "delegate-this-gradle-plugin"
            version = rootProject.version.toString()

            from(components["kotlin"])
        }
    }

    repositories {
        mavenLocal()
    }
}