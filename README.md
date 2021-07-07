## Templates
#### A uniform solution for storing and applying item styles
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.github.the-h-team/templates?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/#nexus-search;gav~com.github.the-h-team~templates~~~)

### Relocation (READ THIS SECTION)
It is important to include a valid `maven-shade-plugin` configuration to avoid
colliding with other plugin jars that also use and provide this resource.
Take moment to look over this example:
```xml
    <!-- In your pom.xml -->
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-shade-plugin</artifactId>
          <version>3.1.0</version>
          <!-- This is the important element -->
          <configuration>
            <relocations>
              <relocation>
                <pattern>com.github.sanctum.templates</pattern>
                <!-- Replace this with your package! -->
                <shadedPattern>com.github.ms5984.anotherplugin.templates</shadedPattern>
              </relocation>
            </relocations>
          </configuration>
          <executions>
            <execution>
              <phase>package</phase>
              <goals>
                <goal>shade</goal>
              </goals>
              <!-- Note that there are two areas labeled configuration! -->
              <!-- Relocations must go up in the first one -->
              <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
```