package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

object Build : BuildType({
    name = "Java_APP_build"

    vcs {
        root(HttpsGithubComFred13SimpleJavaMavenAppGitRefsHeadsMain)
    }

    steps {
        script {
            scriptContent = "sed -i 's/v0.0.1/v0.0.%build.number%/g' pom.xml"
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
        script {
            name = "Check_Java_APP"
            scriptContent = "java -jar target/my-app-v0.0.%build.number%.jar"
        }
        maven {
            name = "Push_Java_APP_to_Nexus"
            runnerArgs = "deploy"
            userSettingsSelection = "Nexus"
        }
    }

    triggers {
        vcs {
        }
    }
    features {
        perfmon {
        }
    }})
