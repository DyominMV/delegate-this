package mikhail.dyomin.delegatethis

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import java.nio.file.Path
import kotlin.io.path.absolute

abstract class DelegateThisMojoBase : AbstractMojo() {
    protected lateinit var directoriesWithClasses: List<Path>

    override fun execute() = DelegateThis(directoriesWithClasses).execute()
}

/**
 * @goal transform-delegators
 * @phase process-classes
 */
@Mojo(name = "transform-delegators", requiresDependencyCollection = ResolutionScope.RUNTIME_PLUS_SYSTEM)
class DelegateThisMojo : DelegateThisMojoBase() {
    /**
     * directories with `*.class` files produced by compiler (default value: `${project.build.outputDirectory}`)
     */
    @Parameter(readonly = false, defaultValue = "\${project.build.outputDirectory}")
    fun setBuildOutputDirectories(dirs: List<String>) {
        this.directoriesWithClasses = dirs.map { Path.of(it).absolute() }
    }
}

/**
 * @goal transform-delegators-test
 * @phase process-test-classes
 */
@Mojo(name = "transform-delegators-test", requiresDependencyCollection = ResolutionScope.TEST)
class DelegateThisTestsMojo : DelegateThisMojoBase() {
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