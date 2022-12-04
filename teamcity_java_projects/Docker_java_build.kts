package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildSteps.DockerCommandStep
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object DockerJavaBuild : BuildType({
    name = "Docker_java_build"

    vcs {
        root(HttpsGithubComFred13SimpleJavaMavenAppGitRefsHeadsMain)
    }
    steps {
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
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_2"
            }
        }
    }
})
