package maven
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.artifacts.ProjectDependency
class plugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("mavencfg", PluginExtension)
        project.apply plugin: 'maven'
        project.afterEvaluate {
            if (project.mavencfg.gitio) {
                project.uploadArchives {
                    repositories {
                        mavenDeployer {
                            repository(url: file("../Techshroom.github.io/downloads/maven").toURI().toURL())
                        }
                    }
                }
            }
            project.mavencfg.otherLocations.each {
                project.uploadArchives {
                    repositories {
                        mavenDeployer {
                            repository(url: it)
                        }
                    }
                }
            }
        }
    }
}
