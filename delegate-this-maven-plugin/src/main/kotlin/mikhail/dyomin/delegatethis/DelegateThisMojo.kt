package mikhail.dyomin.delegatethis

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import kotlin.io.path.Path

/**
 * @goal transform-delegators
 * @phase process-classes
 * @phase process-test-classes
 */
@Mojo(name = "transform-delegators", requiresDependencyCollection = ResolutionScope.TEST)
class DelegateThisMojo : AbstractMojo() {
    @Parameter(readonly = true, defaultValue = "\${project}")
    private lateinit var project: MavenProject

    override fun execute() = DelegateThis(Path(project.build.outputDirectory)).execute()
}