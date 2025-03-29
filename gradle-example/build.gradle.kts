plugins {
    kotlin("jvm") version "2.1.10"
    id("mikhail.dyomin.delegate-this-gradle-plugin") version "1.0.0"
}

group = "mikhail.dyomin"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("mikhail.dyomin", "by-computed", "1.0.0")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.1")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.8.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}