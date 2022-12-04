package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object JavaAppBuild : BuildType({
    name = "Java_APP_build"

    artifactRules = "target => target"

    vcs {
        root(HttpsGithubComJenkinsDocsSimpleJavaMavenAppGit)
    }

    steps {
        script {
            name = "Config_Java_build"
            scriptContent = """
                cat <<EOF > pom.xml
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>com.mycompany.app</groupId>
                  <artifactId>my-app</artifactId>
                  <packaging>jar</packaging>
                  <version>v0.0.1</version>
                  <name>my-app</name>
                  <url>http://maven.apache.org</url>
                  <distributionManagement>
                    <repository>
                      <id>nexus</id>
                      <name>Nexus Releases</name>
                      <url>http://nexus:8081/repository/maven-releases</url>
                    </repository>
                  </distributionManagement>
                  <dependencies>
                    <dependency>
                      <groupId>junit</groupId>
                      <artifactId>junit</artifactId>
                      <version>4.13.2</version>
                      <scope>test</scope>
                    </dependency>
                  </dependencies>
                  <properties>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                  </properties>
                  <build>
                    <pluginManagement>
                      <plugins>
                        <plugin>
                          <groupId>org.apache.maven.plugins</groupId>
                          <artifactId>maven-compiler-plugin</artifactId>
                          <version>3.8.1</version>
                        </plugin>
                      </plugins>
                    </pluginManagement>
                    <plugins>
                      <plugin>
                        <!-- Build an executable JAR -->
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-jar-plugin</artifactId>
                        <version>3.2.0</version>
                        <configuration>
                          <archive>
                            <manifest>
                              <addClasspath>true</addClasspath>
                              <classpathPrefix>lib/</classpathPrefix>
                              <mainClass>com.mycompany.app.App</mainClass>
                            </manifest>
                          </archive>
                        </configuration>
                      </plugin>
                      <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-enforcer-plugin</artifactId>
                        <version>3.0.0-M3</version>
                        <executions>
                          <execution>
                            <id>enforce-maven</id>
                            <goals>
                              <goal>enforce</goal>
                            </goals>
                            <configuration>
                              <rules>
                                <requireMavenVersion>
                                  <version>[3.5.4,)</version>
                                </requireMavenVersion>
                              </rules>
                            </configuration>
                          </execution>
                        </executions>
                      </plugin>
                    </plugins>
                  </build>
                </project>
                EOF
            """.trimIndent()
        }
        maven {
            name = "Test_Java_APP"
            runnerArgs = "test"
        }
        maven {
            name = "Build_Java_APP"
            goals = "clean compile install"
            runnerArgs = "-B -DskipTests clean package"
        }
        maven {
            name = "Push_Java_APP_to_Nexus"
            runnerArgs = "deploy"
            userSettingsSelection = "settings.xml"
        }
        script {
            name = "Check_Java_APP"
            scriptContent = "java -jar target/my-app-v0.0.%build.number%.jar"
        }
    }
})
