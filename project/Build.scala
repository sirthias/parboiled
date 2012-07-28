import sbt._
import Keys._


object Build extends Build {
  import Dependencies._

  // configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  val basicSettings = seq(
    version               := "1.1-SNAPSHOT",
    scalaVersion          := "2.9.2", // "2.10.0-M6"
    homepage              := Some(new URL("http://parboiled.org")),
    organization          := "org.parboiled",
    organizationHomepage  := Some(new URL("http://parboiled.org")),
    description           := "Elegant parsing in Java and Scala - lightweight, easy-to-use, powerful",
    startYear             := Some(2009),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    resolvers             ++= resolutionRepos,

    javacOptions          ++= Seq(
      "-deprecation",
      "-target", "1.5",
      "-source", "1.5",
      "-encoding", "utf8",
      "-Xlint:unchecked"
    ),
    scalacOptions         := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),

    libraryDependencies   ++= test(testNG),
    libraryDependencies   <+= scalaVersion(scalaTest),

    // scaladoc settings
    (scalacOptions in doc) <++= (name, version).map { (n, v) => Seq("-doc-title", n, "-doc-version", v) },

    // publishing
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    crossPaths := false,
    publishMavenStyle := true,
    publishTo <<= version { version =>
      Some {
        "spray nexus" at {
          // public uri is repo.spray.cc, we use an SSH tunnel to the nexus here
          "http://localhost:42424/content/repositories/" + {
            if (version.trim.endsWith("SNAPSHOT")) "snapshots/" else "releases/"
          }
        }
      }
    }
  )

  val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )

  lazy val rootProject = Project("root", file("."))
    .aggregate(parboiledCore, parboiledJava, parboiledScala, examplesJava, examplesScala)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)


  lazy val parboiledCore = Project("parboiled-core", file("parboiled-core"))
    .settings(basicSettings: _*)


  lazy val parboiledJava = Project("parboiled-java", file("parboiled-java"))
    .dependsOn(parboiledCore)
    .settings(basicSettings: _*)
    .settings(
      libraryDependencies ++= compile(asm, asmTree, asmAnalysis, asmUtil),
      javacOptions in Test += "-g" // needed for bytecode rewriting
    )

  lazy val parboiledScala = Project("parboiled-scala", file("parboiled-scala"))
    .dependsOn(parboiledCore)
    .settings(basicSettings: _*)


  lazy val examplesJava = Project("examples-java", file("examples-java"))
    .dependsOn(parboiledJava)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)
    .settings(javacOptions += "-g") // needed for bytecode rewriting


  lazy val examplesScala = Project("examples-scala", file("examples-scala"))
    .dependsOn(parboiledScala)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)

}
