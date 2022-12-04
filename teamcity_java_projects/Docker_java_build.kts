package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildSteps.DockerCommandStep
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object JavaBuild : BuildType({
    name = "Docker_java_build"

    params {
        password("DockerPass", "******")
        param("DockerUser", "admin")
    }

    vcs {
        root(DokcerSimpleJavaMavenApp)
    }

    steps {
        script {
            name = "Create_Dockerfile"
            scriptContent = """
                cat <<EOF > Dockerfile
                #
                # Build stage
                #
                FROM maven:3.6.3-jdk-11-slim AS build
                WORKDIR /usr/src/app
                COPY . ./
                RUN mvn clean package

                #
                # Package stage
                #
                FROM openjdk:11-jre-slim
                WORKDIR /usr/src/app
                COPY --from=build /usr/src/app/target/my-app-1.0-SNAPSHOT.jar ./app.jar
                CMD ["java","-jar", "./app.jar"]
                EOF
            """.trimIndent()
        }
        dockerCommand {
            name = "Build_docker_image"
            commandType = build {
                source = file {
                    path = "Dockerfile"
                }
                contextDir = "./"
                platform = DockerCommandStep.ImagePlatform.Linux
                namesAndTags = "nexus:8082/java-app:%build.number%"
                commandArgs = "--pull"
            }
        }
        script {
            name = "Check_docker_image"
            scriptContent = "sudo -E docker run --rm nexus:8082/java-app:%build.number%"
        }
        dockerCommand {
            name = "Push_docker_image"
            commandType = push {
                namesAndTags = "nexus:8082/java-app:%build.number%"
            }
        }
    }

    features {
        dockerSupport {
            cleanupPushedImages = true
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_2"
            }
        }
    }
})
