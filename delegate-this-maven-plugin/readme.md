# delegate-this-maven-plugin

Этот плагин содержит goal-ы для модификации байткода классов на котлине так, чтобы работала темка с
[delegate-this](../delegate-this/readme.md).

Пример подключения плагина см. в [библиотеке by-computed](../by-computed/pom.xml).

К сожалению, почему-то goal-ы не запускаются автоматически при запуске тестов через зелёный треугольник в идее, поэтому 
приходится прожимать такую галку в настройках: 
`settings -> Build, Execution, Deployment -> Maven -> Runner -> delegate IDE build/run actions to Maven`



