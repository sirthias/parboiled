import Dependencies._

val basicSettings = Seq(
  shellPrompt           := { s => Project.extract(s).currentProject.id + " > " },
  version               := "1.3.2-SNAPSHOT",
  scalaVersion          := "2.13.4",
  homepage              := Some(new URL("http://parboiled.org")),
  organization          := "org.parboiled",
  organizationHomepage  := Some(new URL("http://parboiled.org")),
  description           := "Elegant parsing in Java and Scala - lightweight, easy-to-use, powerful",
  startYear             := Some(2009),
  licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  resolvers             ++= resolutionRepos,

  javacOptions          ++= Seq(
    "-deprecation",
    "-target", "1.7",
    "-source", "1.7",
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

  libraryDependencies   ++= Dependencies.test(testNG),
  libraryDependencies   ++= Dependencies.test(scalatest(scalaVersion.value): _*),

  // scaladoc settings
  (scalacOptions in doc) ++= Seq("-doc-title", name.value, "-doc-version", version.value),

  // publishing
  crossScalaVersions := Seq("2.11.12", "2.12.13", "2.13.4"),
  scalaBinaryVersion := {
    if (CrossVersion.isScalaApiCompatible(scalaVersion.value)) CrossVersion.binaryScalaVersion(scalaVersion.value)
    else scalaVersion.value
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
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

val noPublishing = Seq(
  publishArtifact := false,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
  publishConfiguration := publishConfiguration.value.withOverwrite(true)
)

def javaDoc = Seq(
  doc in Compile := {
    val cp = (fullClasspath in Compile in doc).value
    val docTarget = (target in Compile in doc).value
    val compileSrc = (javaSource in Compile).value
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
  .settings(
    libraryDependencies ++= Dependencies.compile(asm, asmTree, asmAnalysis, asmUtil),
    javacOptions in Test += "-g", // needed for bytecode rewriting
    crossPaths := false,
    autoScalaLibrary := false
  )

lazy val parboiledScala = Project("parboiled-scala", file("parboiled-scala"))
  .dependsOn(parboiledCore)
  .settings(basicSettings: _*)


lazy val examplesJava = Project("examples-java", file("examples-java"))
  .dependsOn(parboiledJava % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(noPublishing: _*)
  .settings(javacOptions += "-g") // needed for bytecode rewriting


lazy val examplesScala = Project("examples-scala", file("examples-scala"))
  .dependsOn(parboiledScala % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(noPublishing: _*)
