@Grab("org.kohsuke:github-api:1.70")
import org.kohsuke.github.*

gitHub = GitHub.connectUsingOAuth(System.getenv("GH_TOKEN"))
repository = gitHub.getRepository("tschulte/github-releasetest")

boolean tagExists(String tag) {
    try {
        repository.getRef("tags/$tag")
        println "tag exists"
        return true
    } catch(Throwable t) {
        println "tag does not exist"
        return false
    }
}

for (String version in args) {
    String tag = "v$version"
    ['git', 'tag', tag, '-m', "Release of $version"].execute().waitForProcessOutput(System.out, System.err)
    ['git', 'push', 'origin', tag].execute().waitForProcessOutput(System.out, System.err)
    while(!tagExists(tag)) {}
    repository.createRelease(tag).body("# Changelog of $version").create()
}
