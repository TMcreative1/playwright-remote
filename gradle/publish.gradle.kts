configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${project.group}"
            artifactId = "${project.name}"
            from(components["java"])
            versionMapping {
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name.set("${project.name}")
                description.set("Java library to automate Chromium, Firefox and WebKit with a single API. This is the main package that provides Playwright remote client")
                url.set("https://github.com/TMcreative1/playwright-remote")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("tmcreative1")
                        name.set("Tymur Mamedov")
                    }
                    developer {
                        id.set("Roman-Mitusov")
                        name.set("Roman Mitusov")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:TMcreative1/playwright-remote.git")
                    developerConnection.set("scm:git@github.com:TMcreative1/playwright-remote.git")
                    url.set("https://github.com/TMcreative1/playwright-remote")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if ("${project.version}".endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            if (project.hasProperty("sonatypeUsername")) {
                credentials {
                    username = project.property("sonatypeUsername") as String
                    password = project.property("sonatypePassword") as String
                }
            }
        }
    }
}