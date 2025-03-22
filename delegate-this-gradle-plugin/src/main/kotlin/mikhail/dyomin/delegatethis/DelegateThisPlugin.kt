package mikhail.dyomin.delegatethis

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path

abstract class DelegateThisTaskBase : DefaultTask() {
    abstract val directoriesWithClasses: List<Path>

    @TaskAction
    fun execute() = DelegateThis(directoriesWithClasses).execute()
}

abstract class DelegateThisTask : DelegateThisTaskBase() {
    override val directoriesWithClasses: List<Path>
        get() = listOf(classesDirectory.asFile.get().toPath())

    @get:InputDirectory
    abstract val classesDirectory: DirectoryProperty
}

abstract class DelegateThisTestTask : DelegateThisTask() {
    override val directoriesWithClasses: List<Path>
        get() = listOf(classesDirectory, testClassesDirectory)
            .map { it.asFile.get().toPath() }

    @get:InputDirectory
    abstract val testClassesDirectory: DirectoryProperty
}

class DelegateThisPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.register("transform delegators", DelegateThisTask::class.java) { task ->
            target.tasks.named("compileKotlin", KotlinCompile::class.java)
                .map { it.destinationDirectory.get() }
                .also { task.classesDirectory.set(it) }
        }


        target.tasks.register("transform test delegators", DelegateThisTestTask::class.java) { task ->
            target.tasks.named("compileKotlin", KotlinCompile::class.java)
                .map { it.destinationDirectory.get() }
                .also { task.classesDirectory.set(it) }
            target.tasks.named("compileTestKotlin", KotlinCompile::class.java)
                .map { it.destinationDirectory.get() }
                .also { task.classesDirectory.set(it) }
        }
    }
}