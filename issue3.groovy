@Grab("com.jcabi:jcabi-github:0.24")
import com.jcabi.github.*

Github github = new RtGithub(System.getenv("GH_TOKEN"))
Repo repo = github.repos().get(new Coordinates.Simple('tschulte/github-releasetest'))
Releases.Smart releases = new Releases.Smart(repo.releases())

for (String version in args) {
    String tag = "v$version"
    ['git', 'tag', tag, '-m', "Release of $version"].execute().waitForProcessOutput(System.out, System.err)
    ['git', 'push', 'origin', tag].execute().waitForProcessOutput(System.out, System.err)
    Release release = releases.create(tag)
    new Release.Smart(release).body("# Changelog of $version")
}
