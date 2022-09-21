plugins {
  id("org.jetbrains.kotlin.jvm").version("1.7.10")
  id("maven-publish")
  id("com.gradleup.gr8").version("0.6")
  id("java-gradle-plugin")
  id("signing")
}

group = "net.mbonnin.golatac"
version = "0.0.2"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.akuleshov7:ktoml-core:0.2.13")
  compileOnly("dev.gradleplugins:gradle-api:7.0")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
}

gr8 {
  removeGradleApiFromApi()
}

gradlePlugin {
  plugins {
    create("golatac") {
      id = "net.mbonnin.golatac"
      implementationClass = "golatac.GolatacPlugin"
    }
  }
}

publishing {
  repositories {
    maven {
      name = "ossStaging"
      url = project.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = System.getenv("OSSRH_USER")
        password = System.getenv("OSSRH_PASSWORD")
      }
    }
  }
}

project.extensions.getByType(SigningExtension::class.java).apply {
  useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PRIVATE_KEY_PASSWORD"))
  sign(project.publishing.publications)
}

project.tasks.withType(Sign::class.java).configureEach {
  isEnabled = !System.getenv("GPG_PRIVATE_KEY").isNullOrBlank()
}

tasks.register("emptyJavadocJar", Jar::class.java) {
  archiveClassifier.set("javadoc")
}

project.publishing.apply {
  publications {
    afterEvaluate {
      getByName("pluginMaven") {
        this as MavenPublication

        artifact(project.tasks.named("emptyJavadocJar"))
        artifact(project.tasks.named("kotlinSourcesJar"))

        configurePublication(this)
      }
      getByName("golatacPluginMarkerMaven") {
        this as MavenPublication

//        artifact(project.tasks.named("emptyJavadocJar"))
//        artifact(project.tasks.named("kotlinSourcesJar"))

        configurePublication(this)
      }
    }
  }
}

fun configurePublication(mavenPublication: MavenPublication) {
  with(mavenPublication) {
    val lArtifactId = project.name
    val lGroupId = project.group.toString().takeIf { it.isNotBlank() }
    val lVersion = project.version.toString()
    val lPomName = lArtifactId
    val lPomDescription = lArtifactId
    val lPomAuthors = "golatac authors"
    val lGithubRepository = "martinbonnin/golatac"
    val lGithubLicensePath = "blob/main/LICENSE"

//    artifactId = lArtifactId
//    groupId = lGroupId
//    version = lVersion

    pom {
      name.set(lPomName)
      description.set(lPomDescription)

      val githubUrl = "https://github.com/$lGithubRepository"

      url.set(githubUrl)

      scm {
        url.set(githubUrl)
        connection.set(githubUrl)
        developerConnection.set(githubUrl)
      }

      licenses {
        license {
          name.set("MIT License")
          url.set("$githubUrl/$lGithubLicensePath")
        }
      }

      developers {
        developer {
          id.set(lPomAuthors)
          name.set(lPomAuthors)
        }
      }
    }
  }
}