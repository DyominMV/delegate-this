plugins {
    id("com.gradle.plugin-publish") version "1.3.1"
    signing
    kotlin("jvm") version "2.3.20"
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
version = "1.1.1"

dependencies {
    api("io.github.dyominmv:delegate-this-plugin-base:$version")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20")
}

gradlePlugin {
    website = "https://github.com/dyominmv/delegate-this/tree/master/delegate-this-gradle-plugin"
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
    repositories {
        maven {
            url = uri("file:///${System.getProperty("user.home")}/local-repository")
            name = "dev"
        }
    }
    publications {
        withType<MavenPublication> {
            pom {
                name = rootProject.name
                groupId = rootProject.group.toString()
                licenses {
                    license {
                        name = "MIT License"
                        url = "http://www.opensource.org/licenses/mit-license.php"
                    }
                }
                developers {
                    developer {
                        name = "Mikhail Dyomin"
                        email = "m.v.dyomin@mail.ru"
                        organizationUrl = "https://github.com/DyominMV"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/DyominMV/delegate-this.git"
                    developerConnection = "scm:git:ssh://github.com:DyominMV/delegate-this.git"
                    url = "https://github.com/DyominMV/delegate-this/tree/master"
                }
                url = "https://github.com/DyominMV/delegate-this/tree/master/"
                description = "Gradle plugin to transform delegators in order to initialize instances of Delegate"
            }
        }
    }
}

signing { useGpgCmd() }