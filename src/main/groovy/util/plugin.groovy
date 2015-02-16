package util
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.artifacts.ProjectDependency
class plugin implements Plugin<Project> {
    void applyEclipseClasspathMod(Project project, PluginExtension ext) {
        project.apply plugin: 'eclipse'
        def eclipse = project.eclipse
        def cJava = project.tasks.getByName('compileJava')
        def cp ='org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-' + ext.javaVersion['target']
        eclipse.classpath.containers.clear()
        eclipse.classpath.containers cp
        eclipse.jdt.sourceCompatibility = cJava.sourceCompatibility = ext.javaVersion['src']
        eclipse.jdt.targetCompatibility = cJava.targetCompatibility = ext.javaVersion['target']
    }
    void apply(Project project) {
        def ext = project.extensions.create('util', PluginExtension)
        ext.apply(project, this)
        project.afterEvaluate {
            if (ext.applyEclipseFix) {
                // eclipse bug workaround
                project.tasks.eclipse.dependsOn('cleanEclipse')
            }
        }
        // return true if there is a property `prop` from Gradle, Java system properties, or environment, in that order.
        ext.ext.hasProperty = { prop ->
            def res = ext.property(prop)
            return res != null && res != ""
        }

        // get property from Gradle, Java system properties, or environment, in that order.
        ext.ext.property = { prop ->
            if (project.hasProperty(prop))
                return project.property(prop)
            def res = System.getProperty(prop, null)
            if (res)
                return res
            res = System.getenv(prop)
            if (res)
                return res
            return null
        }

        // true if we have a jdk rt.jar for the version
        ext.ext.hasJDKRT = { version ->
            return ext.getJDKRT(version) != null
        }

        // get the jdk rt.jar; or null if there is none
        ext.ext.getJDKRT = { version ->
            if (ext.hasProperty("JAVA$version_HOME")) {
                return ext.property("JAVA$version_HOME") + "/jre/lib/rt.jar"
            }
            if (ext.hasProperty("JAVA_HOME") && System.getProperty("java.version").startsWith("1.$version")) {
                return ext.property("JAVA_HOME") + "/jre/lib/rt.jar"
            }
            if (ext.hasProperty("JDK$version_RT")) {
                return ext.property("JDK$version_RT")
            }
            return null
        }
    }
}
