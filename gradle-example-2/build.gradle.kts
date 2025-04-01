import com.github.dyominmv.delegatethis.DelegateThisTask
import com.github.dyominmv.delegatethis.DelegateThisTestTask
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("com.github.dyominmv:delegate-this-gradle-plugin:1.0.0")
    }
}

plugins {
    kotlin("jvm") version "2.1.10"
}

group = "com.github.dyominmv"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.github.dyominmv", "by-computed", "1.0.0")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.1")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.8.1")
}

val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")
val transformDelegators = tasks.register<DelegateThisTask>("transformDelegators") {
    classesDirectory = compileKotlin.flatMap { it.destinationDirectory }
    classpath.setFrom(sourceSets.main.map { it.runtimeClasspath })
}
compileKotlin { finalizedBy(transformDelegators) }
tasks.named<Jar>("jar") { dependsOn(transformDelegators) }

val compileTestKotlin = tasks.named<KotlinCompile>("compileTestKotlin")
val transformTestDelegators = tasks.register<DelegateThisTestTask>("transformTestDelegators") {
    classesDirectory = compileKotlin.flatMap { it.destinationDirectory }
    testClassesDirectory = compileTestKotlin.flatMap { it.destinationDirectory }
    classpath.setFrom(sourceSets.test.map { it.runtimeClasspath })
}
compileTestKotlin { finalizedBy(transformTestDelegators) }

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}