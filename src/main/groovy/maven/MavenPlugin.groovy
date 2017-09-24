package maven
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.artifacts.ProjectDependency
class MavenPlugin implements Plugin<Project> {
    void apply(Project project) {
        // Add source/jd tasks
        project.apply plugin: 'java'
        project.task('sourcesJar', type: Jar, dependsOn: project.classes) {
            classifier = 'sources'
            from project.sourceSets.main.allSource
        }

        project.task('javadocJar', type: Jar, dependsOn: project.javadoc) {
            classifier = 'javadoc'
            from project.javadoc.destinationDir
        }

        project.artifacts {
            archives project.sourcesJar
            archives project.javadocJar
        }

        def cfg = project.extensions.create("mavencfg", PluginExtension, project)
        if (!project.hasProperty('ossrhPassword')) {
            project.ext.ossrhPassword = System.getenv('PASSWORD');
        }
        // skip everything if data missing
        if (!(project.hasProperty('ossrhUsername') && project.hasProperty('ossrhPassword') && project.property('ossrhPassword'))) {
            println('[aversion-maven] Missing data, aborting configuring maven uploads.')
            return;
        }

        project.plugins.withId('net.researchgate.release') {
            project.afterReleaseBuild.dependsOn(project.uploadArchives)
        }
        if (!project.version.endsWith('-SNAPSHOT') && Boolean.parseBoolean(System.getenv('TRAVIS'))) {
            println('Disabling uploads for non-SNAPSHOT Travis build.')
            project.uploadArchives.onlyIf { false }
        }
        println("[aversion-maven] username:password=${project.ossrhUsername}:REDACTED")
        project.apply plugin: 'maven'
        project.afterEvaluate {
            cfg.validate();
            def installer = project.install.repositories.mavenInstaller;
            def deployer = project.uploadArchives.repositories.mavenDeployer;
            if (cfg.snapshotRepo) {
                project.project.configure([installer, deployer]) {
                    snapshotRepository(url: cfg.snapshotRepo) {
                        authentication(userName: project.ossrhUsername, password: project.ossrhPassword)
                    }
                }
            }
            project.configure([installer, deployer]) {
                repository(url: cfg.repo) {
                    authentication(userName: project.ossrhUsername, password: project.ossrhPassword)
                }

                pom.project {
                    name cfg.name
                    if (cfg.classifier) {
                        classifier cfg.classifier
                    }
                    packaging 'jar'
                    description cfg.projectDescription
                    url 'https://github.com' + cfg.coord

                    scm {
                        connection 'git://github.com' + cfg.coord
                        developerConnection 'git://github.com' + cfg.coord
                        url 'https://github.com' + cfg.coord
                    }

                    licenses {
                        license {
                            name 'The MIT License'
                            url "https://github.com${cfg.coord}/blob/master/LICENSE"
                        }
                    }

                    developers {
                        developer {
                            id 'kenzierocks'
                            name 'Kenzie Togami'
                            email 'ket1999@gmail.com'
                        }
                    }
                }
            }
            if (cfg.doSigning) {
                project.apply plugin: 'signing'
                project.signing {
                    sign project.configurations.archives
                }
                project.configure([installer, deployer]) {
                    beforeDeployment { deployment -> project.signing.signPom(deployment) }
                }
            }
        }
    }
}
