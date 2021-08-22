## Templates
#### A uniform solution for storing and applying item styles
[![Maven Central](https://img.shields.io/maven-central/v/com.github.the-h-team/templates)](https://search.maven.org/search?q=g:com.github.the-h-team%20a:templates)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.github.the-h-team/templates?server=https%3A%2F%2Fs01.oss.sonatype.org)](https://s01.oss.sonatype.org/#nexus-search;gav~com.github.the-h-team~templates~~~)

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

### Importing
#### Full release
None so far, but as we use OSSRH, it'd available directly in Maven Central
#### Snapshots (requires sonatype repository)
```xml
<project>
    <!-- For Sonatype Nexus snapshots (primary development here) -->
    <repositories>
        <repository>
            <id>sonatype-snapshots</id>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.github.the-h-team</groupId>
            <artifactId>templates</artifactId>
            <version><!--nexus snapshot version here--></version>
        </dependency>
    </dependencies>
</project>
```
#### GitHub+Jitpack (requires jitpack repository)
```xml
<project>
    <!-- For Jitpack pre-release, custom commit builds -->
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io/</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.github.the-h-team</groupId>
            <artifactId>Templates</artifactId>
            <!--commit hash; example below-->
            <version>b95d3f3</version>
        </dependency>
    </dependencies>
</project>
```
