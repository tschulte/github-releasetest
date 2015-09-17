I have a problem uploading releasenotes for an existing tag, if the tag was just created (https://github.com/tschulte/gradle-semantic-release-plugin/issues/3). My code does first create an annotated tag and pushes it. Directly after that, it uses the GitHub API to set the body of the just created release. This does fail often -- less often on a new repository with few commits, more often, if the repository contains more commits.

I used the following groovy code to reproduce this behavior

```groovy
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
```

Using `GH_TOKEN=... groovy issue3.groovy 1 2 3 4 5 6 7 8 9` I get

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
To git@github.com:tschulte/github-releasetest.git
 * [new tag]         v1 -> v1
To git@github.com:tschulte/github-releasetest.git
 * [new tag]         v2 -> v2
Caught: java.lang.AssertionError: HTTP response status is not equal to 201:
422 Unprocessable Entity [https://api.github.com/repos/tschulte/github-releasetest/releases]
Server: GitHub.com
Date: Thu, 17 Sep 2015 19:27:59 GMT
Content-Type: application/json; charset=utf-8
Content-Length: 221
Status: 422 Unprocessable Entity
X-RateLimit-Limit: 5000
X-RateLimit-Remaining: 4989
X-RateLimit-Reset: 1442521197
X-OAuth-Scopes: public_repo
X-Accepted-OAuth-Scopes:
X-GitHub-Media-Type: github.v3
X-XSS-Protection: 1; mode=block
X-Frame-Options: deny
Content-Security-Policy: default-src 'none'
Access-Control-Allow-Credentials: true
Access-Control-Expose-Headers: ETag, Link, X-GitHub-OTP, X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset, X-OAuth-Scopes, X-Accepted-OAuth-Scopes, X-Poll-Interval
Access-Control-Allow-Origin: *
X-GitHub-Request-Id: 508AF4CF:C025:5BE1F27:55FB143F
Strict-Transport-Security: max-age=31536000; includeSubdomains; preload
X-Content-Type-Options: nosniff

{"message":"Validation Failed","errors":[{"resource":"Release","code":"custom","message":"Published releases must have a valid tag"}],"documentation_url":"https://developer.github.com/v3/repos/releases/#create-a-release"}
Expected: HTTP response with status 201
     but: was <422 Unprocessable Entity [https://api.github.com/repos/tschulte/github-releasetest/releases]
Server: GitHub.com
Date: Thu, 17 Sep 2015 19:27:59 GMT
Content-Type: application/json; charset=utf-8
Content-Length: 221
Status: 422 Unprocessable Entity
X-RateLimit-Limit: 5000
X-RateLimit-Remaining: 4989
X-RateLimit-Reset: 1442521197
X-OAuth-Scopes: public_repo
X-Accepted-OAuth-Scopes:
X-GitHub-Media-Type: github.v3
X-XSS-Protection: 1; mode=block
X-Frame-Options: deny
Content-Security-Policy: default-src 'none'
Access-Control-Allow-Credentials: true
Access-Control-Expose-Headers: ETag, Link, X-GitHub-OTP, X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset, X-OAuth-Scopes, X-Accepted-OAuth-Scopes, X-Poll-Interval
Access-Control-Allow-Origin: *
X-GitHub-Request-Id: 508AF4CF:C025:5BE1F27:55FB143F
Strict-Transport-Security: max-age=31536000; includeSubdomains; preload
X-Content-Type-Options: nosniff
[...]
```

If I change the script to call `releases.find(tag)` directly after pushing the tag, it does not seem to fail -- maybe just introducing `Thread.sleep(1000)` would also "fix" the problem.

I know I could fix my code by not using `git tag` and `git push` and instead using the GitHub API to also create the tag and not just to set the releasenotes text. But the upload of the releasenotes is an addon to the existing behaviour, which I don't want to change.

Is there anything else I can do? Or is this a bug in the API?
