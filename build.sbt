import Dependencies._

val basicSettings = Seq(
  shellPrompt           := { s => Project.extract(s).currentProject.id + " > " },
  version               := "1.4.1",
  scalaVersion          := "2.13.14",
  homepage              := Some(url("http://parboiled.org")),
  organization          := "org.parboiled",
  organizationHomepage  := Some(url("http://parboiled.org")),
  description           := "Elegant parsing in Java and Scala - lightweight, easy-to-use, powerful",
  startYear             := Some(2009),
  licenses              := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  resolvers             ++= resolutionRepos,

  javacOptions          ++= Seq(
    "-deprecation",
    "-target", "11",
    "-source", "11",
    "-encoding", "utf8",
    "-Xlint:unchecked"
  ),
  scalacOptions ++= Seq(
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8"
  ),

  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq("-Xsource:3")
      case _ =>
        Nil
    }
  },

  libraryDependencies   ++= Dependencies.test(testNG),

  // scaladoc settings
  (doc / scalacOptions) ++= Seq("-doc-title", name.value, "-doc-version", version.value),

  // publishing
  crossScalaVersions := Seq("2.12.19", "2.13.14", "3.3.3"),
  scalaBinaryVersion := {
    if (CrossVersion.isScalaApiCompatible(scalaVersion.value)) CrossVersion.binaryScalaVersion(scalaVersion.value)
    else scalaVersion.value
  },
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra :=
    <scm>
      <url>git@github.com:sirthias/parboiled.git</url>
      <connection>scm:git:git@github.com:sirthias/parboiled.git</connection>
    </scm>
    <developers>
      <developer>
        <id>sirthias</id>
        <name>Mathias Doenitz</name>
      </developer>
    </developers>
)

val javaTestModulesSettings = 
  if (List("11", "17").exists(System.getProperty("java.version", "").startsWith)) 
    Seq(
      Test / fork := true,
      Test / javaOptions += "--add-opens=java.base/java.lang=ALL-UNNAMED"
    )
  else Seq.empty

val noPublishing = Seq(
  publishArtifact := false,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
  publishConfiguration := publishConfiguration.value.withOverwrite(true)
)

def javaDoc = Seq(
  (Compile / doc) := {
    val cp = (Compile / doc / fullClasspath).value
    val docTarget = (Compile / doc / target).value
    val compileSrc = (Compile / javaSource).value
    val s = streams.value
    def replace(x: Any) = x.toString.replace("parboiled-java", "parboiled-core")
    def docLink = name.value match {
      case "parboiled-java" => " -linkoffline http://www.decodified.com/parboiled/api/core " + replace(docTarget)
      case _ => ""
    }
    val cmd = "javadoc" +
            " -sourcepath " + compileSrc +
            " -classpath " + cp.map(_.data).mkString(":") +
            " -d " + docTarget +
            docLink +
            " -encoding utf8" +
            " -public" +
            " -windowtitle " + name.value + "_" + version.value +
            " -subpackages" +
            " org.parboiled"
    s.log.info(cmd)
    sys.process.Process(cmd) ! s.log
    docTarget
  }
)

lazy val root = Project("root", file("."))
  .aggregate(parboiledCore, parboiledJava, parboiledScala, examplesJava, examplesScala)
  .settings(basicSettings: _*)
  .settings(noPublishing: _*)


lazy val parboiledCore = Project("parboiled-core", file("parboiled-core"))
  .settings(basicSettings: _*)
  .settings(javaDoc: _*)
  .settings(
    crossPaths := false,
    autoScalaLibrary := false
  )

lazy val parboiledJava = Project("parboiled-java", file("parboiled-java"))
  .dependsOn(parboiledCore % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(javaDoc: _*)
  .settings(javaTestModulesSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.compile(asm, asmTree, asmAnalysis, asmUtil),
    Test / javacOptions += "-g", // needed for bytecode rewriting
    crossPaths := false,
    autoScalaLibrary := false,
    // java 17 patch module path by letting parboiled reflective access to java base package
    Compile / packageBin / packageOptions += Package.ManifestAttributes("Add-Opens" -> "java.base/java.lang")
  )

lazy val parboiledScala = Project("parboiled-scala", file("parboiled-scala"))
  .dependsOn(parboiledCore)
  .settings(basicSettings: _*)


lazy val examplesJava = Project("examples-java", file("examples-java"))
  .dependsOn(parboiledJava % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(noPublishing: _*)
  .settings(javaTestModulesSettings: _*)
  .settings(javacOptions += "-g") // needed for bytecode rewriting


lazy val examplesScala = Project("examples-scala", file("examples-scala"))
  .dependsOn(parboiledScala % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(noPublishing: _*)
