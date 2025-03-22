package mikhail.dyomin.delegatethis

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class DelegateThisTask : DefaultTask() {
    @TaskAction
    fun execute() = listOf(classesDirectory).map {
        it.get().asFile.toPath()
    }.let { DelegateThis(it).execute() }

    @get:InputDirectory
    abstract val classesDirectory: DirectoryProperty
}

abstract class DelegateThisTestTask : DefaultTask() {
    @TaskAction
    fun execute() = listOf(classesDirectory, testClassesDirectory).map {
        it.get().asFile.toPath()
    }.let { DelegateThis(it).execute() }

    @get:InputDirectory
    abstract val classesDirectory: DirectoryProperty

    @get:InputDirectory
    abstract val testClassesDirectory: DirectoryProperty
}

class DelegateThisPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.register("transform delegators", DelegateThisTask::class.java) { task ->
            target.tasks.named("compileKotlin", KotlinCompile::class.java).map { it.destinationDirectory.get() }
                .also { task.classesDirectory.set(it) }
        }


        target.tasks.register("transform test delegators", DelegateThisTestTask::class.java) { task ->
            target.tasks.named("compileKotlin", KotlinCompile::class.java).map { it.destinationDirectory.get() }
                .also { task.classesDirectory.set(it) }
            target.tasks.named("compileTestKotlin", KotlinCompile::class.java).map { it.destinationDirectory.get() }
                .also { task.classesDirectory.set(it) }
        }
    }
}