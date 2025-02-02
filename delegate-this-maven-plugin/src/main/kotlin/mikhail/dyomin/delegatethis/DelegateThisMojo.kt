package mikhail.dyomin.delegatethis

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import java.nio.file.Path
import kotlin.io.path.absolute

abstract class DelegateThisMojoBase : AbstractMojo() {
    protected lateinit var directoryWithClasses: Path

    override fun execute() = DelegateThis(directoryWithClasses).execute()
}

/**
 * @goal transform-delegators
 * @phase process-classes
 */
@Mojo(name = "transform-delegators", requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DelegateThisMojo : DelegateThisMojoBase() {
    /**
     * directory with `*.class` files produced by compiler (default value: `${project.build.outputDirectory}`)
     */
    @Parameter(readonly = false, defaultValue = "\${project.build.outputDirectory}")
    private fun setBuildOutputDirectory(dir: String) {
        this.directoryWithClasses = Path.of(dir).absolute()
    }
}

/**
 * @goal transform-delegators-test
 * @phase process-test-classes
 */
@Mojo(name = "transform-delegators-test", requiresDependencyCollection = ResolutionScope.TEST)
class DelegateThisTestsMojo : DelegateThisMojoBase() {
    /**
     * directory with `*.class` files produced by compiler (default value: `${project.build.testOutputDirectory}`)
     */
    @Parameter(readonly = false, defaultValue = "\${project.build.testOutputDirectory}")
    private fun setBuildTestOutputDirectory(dir: String) {
        this.directoryWithClasses = Path.of(dir).absolute()
    }
}