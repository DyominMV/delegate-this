package io.github.dyominmv.delegatethis

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URLClassLoader

private fun ConfigurableFileCollection.createClassLoader() = URLClassLoader(
    files.map { it.toURI().toURL() }.toTypedArray(),
    getSystemClassLoader()
)

abstract class TransformDelegators : DefaultTask() {
    @TaskAction
    fun execute() = classDirectories.get().map { it.asFile.toPath() }
        .let { DelegateThis(it, classpath.createClassLoader()).execute() }

    @get:OutputDirectories
    abstract val classDirectories: ListProperty<Directory>

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
        val transformDelegators = tasks.register("transformDelegators", TransformDelegators::class.java)

        compileKotlin.configure { it.finalizedBy(transformDelegators) }
        jar.configure { it.dependsOn(transformDelegators) }
        transformDelegators.configure { task ->
            task.classDirectories.convention(
                compileKotlin.flatMap { it.destinationDirectory }.map { listOf(it) }
            )
            task.classpath.convention(mainClassPath)
        }

        val compileTestKotlin = tasks.named("compileTestKotlin", KotlinCompile::class.java)
        val transformTestDelegators = tasks.register("transformTestDelegators", TransformDelegators::class.java)

        compileTestKotlin.configure { it.finalizedBy(transformTestDelegators) }
        transformTestDelegators.configure { task ->
            val classes = compileKotlin.flatMap { it.destinationDirectory }
            val testClasses = compileTestKotlin.flatMap { it.destinationDirectory }
            task.classDirectories.convention(classes.zip(testClasses) { a,b -> listOf(a,b) } )
            task.classpath.convention(testClassPath)
        }
    }

    override fun apply(target: Project) = target.addTasks()
}