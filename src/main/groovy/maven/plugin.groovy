package maven
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.artifacts.ProjectDependency
class projectData {
    def boolean gitio = false
    def List<String> otherLocations = []
}

public class plugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("mavencfg", maven.projectData)
        project.apply plugin: 'maven'
        if (project.mavencfg.gitio) {
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: file("../Techshroom.github.io/downloads/maven").toURI().toURL())
                    }
                }
            }
        }
        project.mavencfg.otherLocations.all {
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
