# delegate-this-maven-plugin

This plugin contains goals for modifying the bytecode of classes in kotlin so that the trick with
[delegate-this](../delegate-this) works.

For an example of connecting the plugin, see [the by-computed library](../by-computed/pom.xml).

Unfortunately, for some reason the goals do not run automatically when running tests via the green triangle in the 
intellij IDE, so you have to click on the following checkbox in the settings:

`settings -> Build, Execution, Deployment -> Maven -> Runner -> delegate IDE build/run actions to Maven`

## Usage

1. Add the following to the `build`/`plugins` section of your pom.xml:
   ```xml
   <plugin>
       <groupId>io.github.dyominmv</groupId>
       <artifactId>delegate-this-maven-plugin</artifactId>
       <version>1.0.0</version>
       <executions>
           <execution>
               <id>compile</id>
               <phase>compile</phase>
               <goals><goal>transform-delegators</goal></goals>
           </execution>
           <execution>
               <id>test-compile</id>
               <phase>test-compile</phase>
               <goals><goal>transform-test-delegators</goal></goals>
           </execution>
       </executions>
   </plugin>
   ```
2. By default, classes to be transformed are searched for in your projects build output directory, but it can be 
overridden by setting `buildOutputDirectories` (for compile phase) and `buildTestOutputDirectories` 
(for test-compile phase) like following: 
   ```xml
   <execution>
       <id>compile</id>
       <phase>compile</phase>
       <goals><goal>transform-delegators</goal></goals>
       <configuration>
           <buildOutputDirectories>
                <directory>custom-compile-directory-1</directory>
                <directory>custom-compile-directory-2</directory>
           </buildOutputDirectories>
       </configuration>
   </execution>
   <execution>
       <id>test-compile</id>
       <phase>test-compile</phase>
       <goals><goal>transform-test-delegators</goal></goals>
       <configuration>
           <buildTestOutputDirectories>
                <directory>custom-compile-directory-1</directory>
                <directory>custom-compile-directory-2</directory>
                <directory>custom-test-compile-directory</directory>
           </buildTestOutputDirectories>
       </configuration>
   </execution>
   ```