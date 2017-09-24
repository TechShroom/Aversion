package maven
import org.gradle.api.*
class PluginExtension {
    boolean doSigning = false
    String repo = null
    String artifactClassifier = ""
    String snapshotRepo = null
    String projectDescription = null
    String coord = null
    String artifactName = null
    private Project project
    public PluginExtension(Project project) {
        this.project = project
        this.doSigning = !Boolean.parseBoolean(System.getenv('TRAVIS'))
        this.repo = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        this.snapshotRepo = "https://oss.sonatype.org/content/repositories/snapshots/"
        this.name = project.name
    }
    void setRepoFile(file) {
        repo = project.file(file).toURI().toURL()
    }
    void setSnapshotRepoFile(file) {
        snapshotRepo = project.file(file).toURI().toURL()
    }
    void description(desc) {
        projectDescription = desc
    }
    void coords(owner, repo) {
        coord = "/${owner}/${repo}"
    }
    void validate() {
        if (coord == null) {
            throw new IllegalArgumentException("Coords must be set (mavencfg.coords)");
        }
        if (artifactName == null) {
            throw new IllegalArgumentException("Name must be set (mavencfg.artifactName)");
        }
        if (!coord.startsWith('/')) {
            coord = '/' + coord
        }
        if (projectDescription == null) {
            throw new IllegalArgumentException("Description must be set (mavencfg.description)");
        }
    }
}
