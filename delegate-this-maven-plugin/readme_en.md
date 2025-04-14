# delegate-this-maven-plugin

This plugin contains goals for modifying the bytecode of classes in kotlin so that the trick with
[delegate-this](../delegate-this/readme_en.md) works.

For an example of connecting the plugin, see [the by-computed library](../by-computed/pom.xml).

Unfortunately, for some reason the goals do not run automatically when running tests via the green triangle in the 
intellij idea, so you have to click on this checkbox in the settings:
`settings -> Build, Execution, Deployment -> Maven -> Runner -> delegate IDE build/run actions to Maven`