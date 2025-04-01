package com.github.dyominmv.delegatethis

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URLClassLoader

private fun ConfigurableFileCollection.createClassLoader() = URLClassLoader(
    files.map { it.toURI().toURL() }.toTypedArray(),
    getSystemClassLoader()
)

abstract class DelegateThisTask : DefaultTask() {
    @TaskAction
    fun execute() = listOf(classesDirectory).map {
        it.get().asFile.toPath()
    }.let { DelegateThis(it, classpath.createClassLoader()).execute() }

    @get:OutputDirectory
    abstract val classesDirectory: DirectoryProperty

    @get:InputFiles
    @get:Classpath
    abstract val classpath: ConfigurableFileCollection
}

abstract class DelegateThisTestTask : DefaultTask() {
    @TaskAction
    fun execute() = listOf(classesDirectory, testClassesDirectory).map {
        it.get().asFile.toPath()
    }.let { DelegateThis(it, classpath.createClassLoader()).execute() }

    @get:OutputDirectory
    abstract val classesDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val testClassesDirectory: DirectoryProperty

    @get:InputFiles
    @get:Classpath
    abstract val classpath: ConfigurableFileCollection
}

class DelegateThisPlugin : Plugin<Project> {

    private fun Project.addTasks() {
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)
        val mainClassPath = sourceSets.named("main").map { it.runtimeClasspath }
        val testClassPath = sourceSets.named("test").map { it.runtimeClasspath }

        val compileKotlin = tasks.named("compileKotlin", KotlinCompile::class.java)
        val jar = tasks.named("jar", Jar::class.java)
        val transformDelegators = tasks.register("transformDelegators", DelegateThisTask::class.java)

        compileKotlin.configure { it.finalizedBy(transformDelegators) }
        jar.configure { it.dependsOn(transformDelegators) }
        transformDelegators.configure { task ->
            task.classesDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
            task.classpath.setFrom(mainClassPath)
        }

        val compileTestKotlin = tasks.named("compileTestKotlin", KotlinCompile::class.java)
        val transformTestDelegators = tasks.register("transformTestDelegators", DelegateThisTestTask::class.java)

        compileTestKotlin.configure { it.finalizedBy(transformTestDelegators) }
        transformTestDelegators.configure { task ->
            task.classesDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
            task.testClassesDirectory.set(compileTestKotlin.flatMap { it.destinationDirectory })
            task.classpath.setFrom(testClassPath)
        }
    }

    override fun apply(target: Project) = target.addTasks()
}