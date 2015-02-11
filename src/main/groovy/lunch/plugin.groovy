package lunch
import com.jcabi.http.request.*
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.artifacts.ProjectDependency
import org.jsoup.*
import org.jsoup.parser.*
public class plugin implements Plugin<Project> {
    void apply(Project project) {
        project.ext.resolveDepURL = { dep ->
            def search = project.repositories
            // only urls
            search = search.collect { it.url }
            def theResult = null
            search.each {
                if (theResult != null) {
                    return
                }
                println "RESOLVING AGAINST ${it} FOR ${dep.group}-${dep.name}-${dep.version}"
                def url = "$it${dep.group.replace('.','/')}/${dep.name}/${dep.version}/${dep.name}-${dep.version}.jar"
                if (dep.version.toUpperCase().endsWith('SNAPSHOT')) {
                    // snapshot things
                    url = "$it${dep.group.replace('.','/')}/${dep.name}/${dep.version}/maven-metadata.xml"
                    // ensure exists
                    def request = new ApacheRequest("$url").method("GET").fetch()
                    if (request.status() != 200) {
                        // not here
                        return
                    }
                    def soup = Parser.xmlParser().parse(request.body(), url)
                    // super target mode: on
                    def snaps = soup.select("metadata versioning snapshotVersions snapshotVersion extension")
                    def seld = null
                    snaps.each {
                        if (it.text() != "pom") {
                            // probably the target
                            seld = it.parent()
                        }
                    }
                    if (seld == null) {
                        return
                    }
                    url = "$it${dep.group.replace('.','/')}/${dep.name}/${dep.version}/${dep.name}-" + seld.select("value").text() + "." + seld.select("extension").text()
                }
                if (new ApacheRequest("$url").method("HEAD").fetch().status() == 200) {
                    println "URL == $url"
                    theResult = url
                } else {
                    println "URL != $url"
                }
            }
            if (theResult == null) {
                throw new IllegalStateException("Couldn't resolve ${dep.group}-${dep.name}-${dep.version} against $search")
            }
            return theResult
        }
        
        project.task("copyLunchWrapperLibFile", type: Copy, dependsOn: "createLunchWrapperLibFile") {
            description "Copies the lwprops file to the right location"
            from project.file("libs.lwprops")
            into project.file("src/main/resources/")
        }

        project.task("createLunchWrapperLibFile") {
            description "Creates the lwprops file for LunchWrapper to load libraries from."
            ext.outputFile = "libs.lwprops"
            doLast {
                def applicableConfigs = [project.configurations.compile]
                project.file(outputFile).withWriter { out ->
                    applicableConfigs.each { conf ->
                        println "Adding configuration.${conf.name}'s deps to $outputFile..."
                        conf.dependencies.each { dep ->
                            if (dep instanceof ProjectDependency) {
                                println "If your released thing has trouble with LW you need to ensure ${dep.dependencyProject.name} is there."
                                return
                            }
                            def url = project.resolveDepURL(dep)
                            out.writeLine("${dep.group}:${dep.name}:${dep.version}=$url")
                        }
                    }
                }
            }
        }

        project.jar.dependsOn project.copyLunchWrapperLibFile
    }
}
