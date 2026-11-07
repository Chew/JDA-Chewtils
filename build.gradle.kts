/*
 * Copyright 2016-2021 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import java.util.Locale

plugins {
    `maven-publish`
    id("com.github.ben-manes.versions") version "0.54.0"
    id("com.gradleup.shadow") version "9.6.1"
}

val versionInfo = mapOf("major" to "2", "minor" to (System.getenv("BUILD_NUMBER") ?: "0-SNAPSHOT"))

val Project.artifactId: String
    get() = extra["artifactId"] as String

val Project.moduleName: String
    get() = extra["moduleName"] as String

tasks.register("build") {
    group = "build"
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    group = "pw.chew"
    version = versionInfo.values.joinToString(".")

    extra["artifactId"] = if (this == rootProject) name.lowercase(Locale.ROOT) else "${rootProject.name}-$name".lowercase(Locale.ROOT)
    extra["moduleName"] = "$group.jdautilities" + (if (this == rootProject) "" else ".${name.lowercase(Locale.ROOT)}")

    repositories {
        mavenCentral()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.artifactId
                version = project.version.toString()
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = "https://m2.chew.pro/releases"
                val snapshotsRepoUrl = "https://m2.chew.pro/snapshots"
                url = uri(if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = project.property("mchew-username") as String
                    password = project.property("mchew-password") as String
                }
            }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.github.ben-manes.versions")

    the<JavaPluginExtension>().apply {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    extra["includeInParent"] = true

    val main = the<SourceSetContainer>().getByName("main")

    the<SourceSetContainer>().getByName("test") {
        // add main classpath to test classpath
        java.srcDir("src/test/java")
        compileClasspath += main.compileClasspath
        runtimeClasspath += main.runtimeClasspath
    }

    val filteredSrcDir = layout.buildDirectory.dir("filteredSrc")

    val sourcesForRelease = tasks.register<Copy>("sourcesForRelease") {
        from("src/main/java") {
            filter(
                mapOf(
                    "tokens" to mapOf(
                        "VERSION_MAJOR" to versionInfo["major"],
                        "VERSION_MINOR" to versionInfo["minor"]
                    )
                ),
                ReplaceTokens::class.java
            )
        }
        into(filteredSrcDir)
    }

    tasks.named<JavaCompile>("compileJava") {
        setSource(filteredSrcDir)
        classpath = main.compileClasspath

        dependsOn(sourcesForRelease)
    }

    tasks.named<Javadoc>("javadoc") {
        destinationDir = layout.buildDirectory.dir("docs").get().asFile

        (options as StandardJavadocDocletOptions).apply {
            author()
            encoding = "UTF-8"
            memberLevel = JavadocMemberLevel.PROTECTED
            tags("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:", "implNote:a:Implementation Note:")
        }
    }

    tasks.named<Jar>("jar") {
        archiveBaseName.set(project.artifactId)
        archiveVersion.set(project.version.toString())
        archiveExtension.set("jar")
        manifest {
            attributes(
                "Implementation-Title" to project.artifactId,
                "Implementation-Version" to project.version,
                "Automatic-Module-Name" to project.moduleName
            )
        }
    }

    tasks.named<ShadowJar>("shadowJar") {
        archiveBaseName.set(project.artifactId)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("all")
        archiveExtension.set("jar")
        manifest {
            attributes(
                "Implementation-Title" to project.artifactId,
                "Implementation-Version" to project.version,
                "Automatic-Module-Name" to project.moduleName
            )
        }
    }

    tasks.register<Jar>("javadocJar") {
        group = "build"
        dependsOn("javadoc")
        from(tasks.named<Javadoc>("javadoc").map { it.destinationDir })

        archiveBaseName.set(project.artifactId)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("javadoc")
        archiveExtension.set("jar")
        manifest {
            attributes(
                "Implementation-Title" to project.artifactId,
                "Implementation-Version" to project.version,
                "Automatic-Module-Name" to project.moduleName
            )
        }
    }

    tasks.register<Jar>("sourceJar") {
        group = "build"
        dependsOn("classes")
        from(main.allSource)

        archiveBaseName.set(project.artifactId)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("sources")
        archiveExtension.set("jar")
        manifest {
            attributes(
                "Implementation-Title" to project.artifactId,
                "Implementation-Version" to project.version,
                "Automatic-Module-Name" to project.moduleName
            )
        }
    }

    tasks.named("build") {
        dependsOn("javadocJar", "sourceJar", "shadowJar")
    }

    publishing {
        publications {
            named<MavenPublication>("maven") {
                from(components["java"])
                artifact(tasks.named("javadocJar"))
                artifact(tasks.named("sourceJar"))
            }
        }
    }

    afterEvaluate {
        if (extra["includeInParent"] == true) {
            rootProject.dependencies.add("implementation", rootProject.dependencies.project(mapOf("path" to path)))
        }
    }
}

configurations {
    create("implementation") {
        description = "implementation"
        isTransitive = true
    }
}

tasks.register<Javadoc>("javadoc") {
    group = "documentation"
    dependsOn(subprojects.map { it.tasks.named("javadoc") })

    source(subprojects.map { it.the<SourceSetContainer>().getByName("main").allJava })
    classpath = files(subprojects.map { it.the<SourceSetContainer>().getByName("main").compileClasspath })

    destinationDir = layout.buildDirectory.dir("docs").get().asFile

    (options as StandardJavadocDocletOptions).apply {
        author()
        encoding = "UTF-8"
        memberLevel = JavadocMemberLevel.PROTECTED
        tags("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:", "implNote:a:Implementation Note:")
    }
}

// Jar tasks

tasks.register<ShadowJar>("jar") {
    group = "build"

    archiveBaseName.set(project.artifactId)
    archiveVersion.set(project.version.toString())
    archiveExtension.set("jar")
    manifest {
        attributes(
            "Implementation-Title" to project.artifactId,
            "Implementation-Version" to project.version,
            "Automatic-Module-Name" to project.moduleName
        )
    }

    from(subprojects.map { it.tasks.named("jar") })
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.register<ShadowJar>("shadowJar") {
    group = "shadow"
    dependsOn("jar")

    configurations.add(project.configurations.getByName("implementation"))
    archiveBaseName.set(project.artifactId)
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("all")
    archiveExtension.set("jar")
    manifest {
        attributes(
            "Implementation-Title" to project.artifactId,
            "Implementation-Version" to project.version,
            "Automatic-Module-Name" to project.moduleName
        )
    }

    from(subprojects.map { it.tasks.named("jar") })
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.register<Jar>("javadocJar") {
    group = "build"
    dependsOn("javadoc")

    archiveBaseName.set(project.artifactId)
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("javadoc")
    archiveExtension.set("jar")

    from(tasks.named<Javadoc>("javadoc").map { it.destinationDir })
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.register<Jar>("sourceJar") {
    group = "build"
    dependsOn(subprojects.map { it.tasks.named("classes") })

    archiveBaseName.set(project.artifactId)
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("sources")
    archiveExtension.set("jar")

    from(subprojects.map { it.the<SourceSetContainer>().getByName("main").allSource })
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.register<Delete>("clean") {
    group = "build"

    delete(layout.buildDirectory)
}

tasks.named("build") {
    dependsOn("javadocJar", "sourceJar", "shadowJar")
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            pom.withXml {
                val repositoriesNode = asNode().appendNode("repositories")
                val repositoryNode = repositoriesNode.appendNode("repository")

                repositoryNode.appendNode("name", "m2-chew")
                repositoryNode.appendNode("id", "m2-chew")
                repositoryNode.appendNode("url", "https://m2.chew.pro")

                val dependenciesNode = asNode().appendNode("dependencies")
                configurations["implementation"].allDependencies.forEach { dependency ->
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", dependency.group)
                    dependencyNode.appendNode(
                        "artifactId",
                        if (dependency is ProjectDependency) "${rootProject.name}-${dependency.name}".lowercase(Locale.ROOT) else dependency.name
                    )
                    dependencyNode.appendNode("version", dependency.version)
                    dependencyNode.appendNode("scope", "compile")
                }
            }
        }
    }
}
