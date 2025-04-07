package com.github.dyominmv.delegatethis

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.absolute

abstract class DelegateThisMojoBase : AbstractMojo() {
    protected lateinit var directoriesWithClasses: List<Path>

    protected lateinit var classpath: List<String>

    private fun createClassLoader() = URLClassLoader(
        classpath.map { Path.of(it).toUri().toURL() }.toTypedArray(),
        getSystemClassLoader()
    )

    override fun execute() = DelegateThis(directoriesWithClasses, createClassLoader()).execute()
}

/**
 * @goal transform-delegators
 * @phase process-classes
 */
@Mojo(name = "transform-delegators")
class DelegateThisMojo : DelegateThisMojoBase() {

    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
    fun setProject(project: MavenProject) {
        this.classpath = project.runtimeClasspathElements.map { it as String }
    }

    /**
     * directories with `*.class` files produced by compiler (default value: `${project.build.outputDirectory}`)
     */
    @Parameter(readonly = false, defaultValue = "\${project.build.outputDirectory}")
    fun setBuildOutputDirectories(dirs: List<String>) {
        this.directoriesWithClasses = dirs.map { Path.of(it).absolute() }
    }
}

/**
 * @goal transform-test-delegators
 * @phase process-test-classes
 */
@Mojo(name = "transform-test-delegators")
class DelegateThisTestsMojo : DelegateThisMojoBase() {

    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
    fun setProject(project: MavenProject) {
        this.classpath = project.testClasspathElements.map { it as String }
    }

    /**
     * directories with `*.class` files produced by compiler (default value:
     * `${project.build.testOutputDirectory},${project.build.outputDirectory}`)
     */
    @Parameter(
        readonly = false,
        defaultValue = "\${project.build.testOutputDirectory},\${project.build.outputDirectory}"
    )
    fun setBuildTestOutputDirectories(dirs: List<String>) {
        this.directoriesWithClasses = dirs.map { Path.of(it).absolute() }
    }
}