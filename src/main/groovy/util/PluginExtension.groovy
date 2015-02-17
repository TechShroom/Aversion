package util
import org.gradle.api.*;
class PluginExtension {
    static final String DEFAULT_VERSION = "PROJECT"
    def projectVersion = null
    def javaVersion = DEFAULT_VERSION // specific java version
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
            } else {
                throw new InvalidUserDataException("Couldn't convert " + javaVersion + " to a list, class type " + javaVersion.getClass().name)
            }
            plugin.applyEclipseClasspathMod(project, this)
        }
    }
    private JavaVersion merge(Object version) {
        return (version instanceof JavaVersion) ? version : JavaVersion.toVersion(version)
    }
    Object checkVersion(Project project) {
        def data = [:]
        if (project.plugins.hasPlugin("java")) {
            def cJava = project.tasks.getByName("compileJava")
            data["java"] = [src: merge(cJava.sourceCompatibility), target: merge(cJava.targetCompatibility)]
        }
        if (project.plugins.hasPlugin("eclipse")) {
            def jdt = project.eclipse.jdt
            data["eclipse"] = [src: merge(jdt.sourceCompatibility), target: merge(jdt.targetCompatibility)]
        }
        if (data["java"] && data["eclipse"]) {
            if (data["java"] != data["eclipse"]) {
                throw new InvalidUserDataException("Unequal java versions for " + data)
            }
        }
        return data
    }
}
