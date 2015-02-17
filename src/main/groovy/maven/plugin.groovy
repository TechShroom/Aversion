package maven
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.artifacts.ProjectDependency
class plugin implements Plugin<Project> {
    void apply(Project project) {
        def cfg = project.extensions.create("mavencfg", PluginExtension, project)
        project.apply plugin: 'maven'
        project.afterEvaluate {
            if (cfg.snapshotRepo) {
                project.uploadArchives {
                    repositories {
                        mavenDeployer {
                            snapshotRepository(url: cfg.snapshotRepo)
                        }
                    }
                }
            }
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: cfg.repo)
                    }
                }
            }
        }
    }
}
