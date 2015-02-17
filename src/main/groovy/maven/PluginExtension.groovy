package maven
import org.gradle.api.*
class PluginExtension {
    String repo = null
    String snapshotRepo = null
    private Project project
    public PluginExtension(Project project) {
        this.project = project
        repoFile = "../downloads/maven"
    }
    void setRepoFile(file) {
        repo = project.file(file).toURI().toURL()
    }
    void setSnapshotRepoFile(file) {
        snapshotRepo = project.file(file).toURI().toURL()
    }
}
