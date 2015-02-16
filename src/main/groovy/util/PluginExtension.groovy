package util
import org.gradle.api.*;
class PluginExtension {
    static final String DEFAULT_VERSION = "PROJECT"
    def projectVersion = null
    String javaVersion = DEFAULT_VERSION // specific java version
    boolean isMultiProject = false
    boolean applyEclipseFix = true
    void apply(Project project, plugin plugin) {
        isMultiProject = !project.subprojects.isEmpty()
        project.afterEvaluate {
            projectVersion = checkVersion(project)
            if (javaVersion == DEFAULT_VERSION) {
                javaVersion = projectVersion["java"]
            } else if (javaVersion instanceof CharSequence) {
                def both = javaVersion.toString()
                javaVersion = [src: both, target: both]
            }
            plugin.applyEclipseClasspathMod(project, this)
        }
    }
    Object checkVersion(Project project) {
        def data = [:]
        if (project.plugins.hasPlugin("java")) {
            def cJava = project.tasks.getByName("compileJava")
            data["java"] = [src: cJava.sourceCompatibility, target: cJava.targetCompatibility]
        }
        if (project.plugins.hasPlugin("eclipse")) {
            def jdt = project.eclipse.jdt
            data["eclipse"] = [src: jdt.sourceCompatibility, target: jdt.targetCompatibility]
        }
        if (data["java"] && data["eclipse"]) {
            if (data["java"] != data["eclipse"]) {
                print data["java"]["src"].getClass()
                print data["java"]["target"].getClass()
                print data["eclipse"]["src"].getClass()
                print data["eclipse"]["target"].getClass()
                throw new InvalidUserDataException("Unequal java versions for " + data)
            }
        }
        return data
    }
}
