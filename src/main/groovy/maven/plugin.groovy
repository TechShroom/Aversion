package maven
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.artifacts.ProjectDependency
public class projectData {
    boolean gitio = false
    List<String> otherLocations = []
}

public class plugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("mavencfg", projectData)
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
