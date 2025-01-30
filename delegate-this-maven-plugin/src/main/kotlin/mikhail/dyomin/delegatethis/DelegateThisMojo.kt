package mikhail.dyomin.delegatethis

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import kotlin.io.path.Path

/**
 * @goal transform-delegators
 * @phase process-classes
 */
@Mojo(name = "transform-delegators", requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
class DelegateThisMojo : AbstractMojo() {
    @Parameter(readonly = true, defaultValue = "\${project.build.outputDirectory}")
    private lateinit var buildOutputDirectory: String

    override fun execute() = DelegateThis(Path(buildOutputDirectory)).execute()
}

/**
 * @goal transform-delegators-test
 * @phase process-test-classes
 */
@Mojo(name = "transform-delegators-test", requiresDependencyCollection = ResolutionScope.TEST)
class DelegateThisTestsMojo : AbstractMojo() {
    @Parameter(readonly = true, defaultValue = "\${project.build.testOutputDirectory}")
    private lateinit var buildTestOutputDirectory: String

    override fun execute() = DelegateThis(Path(buildTestOutputDirectory)).execute()
}