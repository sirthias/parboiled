import sbt._
import Keys._
import com.typesafe.sbt._
import com.typesafe.sbt.osgi._


object Build extends Build {
  import Dependencies._

  // configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  val basicSettings = SbtPgp.settings ++ seq(
    version               := "1.1.6",
    scalaVersion          := "2.10.2",
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
    scalacOptions ++= {
      if (scalaVersion.value startsWith "2.9")
        Seq("-unchecked", "-deprecation", "-encoding", "utf8")
      else if (scalaVersion.value startsWith "2.10")
        Seq("-feature", "-language:implicitConversions", "-unchecked", "-deprecation", "-encoding", "utf8")
      else Seq.empty
    },

    libraryDependencies   ++= test(testNG, scalatest),

    // scaladoc settings
    (scalacOptions in doc) <++= (name, version).map { (n, v) => Seq("-doc-title", n, "-doc-version", v) },

    // publishing
    crossScalaVersions := Seq("2.9.3", "2.10.2"),
    scalaBinaryVersion <<= scalaVersion(sV => if (CrossVersion.isStable(sV)) CrossVersion.binaryScalaVersion(sV) else sV),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    SbtPgp.useGpg := true,
    publishTo <<= version { v: String =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
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

  val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )

  def javaDoc = seq(
    doc in Compile <<= (fullClasspath in Compile in doc, target in Compile in doc, javaSource in Compile,
      name, version, streams) map { (cp, docTarget, compileSrc, name, version, s) =>
      def replace(x: Any) = x.toString.replace("parboiled-java", "parboiled-core")
      def docLink = name match {
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
              " -windowtitle " + name + "_" + version +
              " -subpackages" +
              " org.parboiled"
      s.log.info(cmd)
      cmd ! s.log
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
    .settings(SbtOsgi.osgiSettings: _*)
    .settings(
      OsgiKeys.exportPackage := Seq(
        "org.parboiled",
        "org.parboiled.buffers",
        "org.parboiled.common",
        "org.parboiled.errors",
        "org.parboiled.matchers",
        "org.parboiled.matchervisitors",
        "org.parboiled.parserunners",
        "org.parboiled.support",
        "org.parboiled.trees")
    )


  lazy val parboiledJava = Project("parboiled-java", file("parboiled-java"))
    .dependsOn(parboiledCore)
    .settings(basicSettings: _*)
    .settings(javaDoc: _*)
    .settings(
      libraryDependencies ++= compile(asm, asmTree, asmAnalysis, asmUtil),
      javacOptions in Test += "-g", // needed for bytecode rewriting
      crossPaths := false,
      autoScalaLibrary := false
    )
    .settings(SbtOsgi.osgiSettings: _*)
    .settings(
        OsgiKeys.fragmentHost := Some("org.parboiled.core"),
        OsgiKeys.exportPackage := Seq(
          "org.parboiled",
          "org.parboiled.annotations",
          "org.parboiled.transform")
    )


  lazy val parboiledScala = Project("parboiled-scala", file("parboiled-scala"))
    .dependsOn(parboiledCore)
    .settings(basicSettings: _*)
    .settings(SbtOsgi.osgiSettings: _*)
    .settings(
      OsgiKeys.exportPackage := Seq(
        "org.parboiled.scala",
        "org.parboiled.scala.parserunners",
        "org.parboiled.scala.rules",
        "org.parboiled.scala.testing",
        "org.parboiled.scala.utils")
    )


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
