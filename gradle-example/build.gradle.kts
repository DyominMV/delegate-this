plugins {
    kotlin("jvm") version "2.3.20"
    id("io.github.dyominmv.delegate-this-gradle-plugin") version "1.1.1"
}

group = "io.github.dyominmv"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.dyominmv:by-computed:1.1.1")
}

// required to run from intellij idea
tasks.withType<JavaExec> {
    dependsOn(tasks.transformDelegators)
}

kotlin {
    jvmToolchain(25)
}