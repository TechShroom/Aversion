package maven
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.artifacts.ProjectDependency
class MavenPlugin implements Plugin<Project> {
    void apply(Project project) {
        // Add source/jd tasks
        project.apply plugin: 'java'
        task sourcesJar(type: Jar, dependsOn: classes) {
            classifier = 'sources'
            from sourceSets.main.allSource
        }

        task javadocJar(type: Jar, dependsOn: javadoc) {
            classifier = 'javadoc'
            from javadoc.destinationDir
        }

        artifacts {
            archives sourcesJar
            archives javadocJar
        }

        def cfg = project.extensions.create("mavencfg", PluginExtension, project)
        if (!project.hasProperty('ossrhPassword')) {
            project.ext.ossrhPassword = System.getenv('PASSWORD');
            println('Captured password from $PASSWORD')
        }
        // skip everything if data missing
        if (!(project.hasProperty('ossrhUsername') && project.hasProperty('ossrhPassword') && project.property('ossrhPassword'))) {
            return;
        }

        project.plugins.withId('net.researchgate.release') {
            project.afterReleaseBuild.dependsOn uploadArchives
        }
        println("[aversion-maven] username:password=${project.ossrhUsername}:REDACTED")
        project.apply plugin: 'maven'
        project.afterEvaluate {
            cfg.validate();
            if (cfg.snapshotRepo) {
                project.uploadArchives {
                    repositories {
                        mavenDeployer {
                            snapshotRepository(url: cfg.snapshotRepo) {
                                authentication(userName: project.ossrhUsername, password: project.ossrhPassword)
                            }
                        }
                    }
                }
            }
            project.uploadArchives {
                repositories {
                    mavenDeployer {
                        repository(url: cfg.repo) {
                            authentication(userName: project.ossrhUsername, password: project.ossrhPassword)
                        }

                        pom.project {
                            name project.name
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
                                    url "https://github.com${project.coord}/blob/master/LICENSE"
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
                }
            }
            if (cfg.doSigning) {
                project.apply plugin: 'signing'
                project.signing {
                    sign configurations.archives
                }
                uploadArchives {
                    repositories {
                        mavenDeployer {
                            beforeDeployment { deployment -> signing.signPom(deployment) }
                        }
                    }
                }
            }
        }
    }
}
