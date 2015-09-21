@Grab("com.jcabi:jcabi-github:0.24")
import com.jcabi.github.*

github = new RtGithub(System.getenv("GH_TOKEN"))
repo = github.repos().get(new Coordinates.Simple('tschulte/github-releasetest'))
releases = new Releases.Smart(repo.releases())

boolean tagExists(tag) {
    try {
        repo.git().references().get("refs/tags/$tag").json()
        println "tag exists"
        return true
    } catch (Throwable t) {
        println "tag does not exist"
        return false
    }
}
for (String version in args) {
    String tag = "v$version"
    ['git', 'tag', tag, '-m', "Release of $version"].execute().waitForProcessOutput(System.out, System.err)
    ['git', 'push', 'origin', tag].execute().waitForProcessOutput(System.out, System.err)
    while(!tagExists(tag)) {}
    Release release = releases.create(tag)
    new Release.Smart(release).body("# Changelog of $version")
}
