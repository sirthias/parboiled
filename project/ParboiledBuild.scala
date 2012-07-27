import sbt._
import Keys._

object ParboiledBuild extends Build {

  /*

  project.organization=org.parboiled
project.name=parboiled
sbt.version=0.7.7
project.version=1.1.0-SNAPSHOT
build.scala.versions=2.9.1
project.initialize=false

   */

  // -------------------------------------------------------------------------------------------------------------------
  // All repositories *must* go here! See ModuleConfigurations below.
  // -------------------------------------------------------------------------------------------------------------------
  object Repositories {
    // e.g. val JavaNetRepo = MavenRepository("java.net Repo", "http://download.java.net/maven/2")
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ModuleConfigurations
  // Every dependency that cannot be resolved from the built-in repositories (Maven Central and Scala Tools Releases)
  // must be resolved from a ModuleConfiguration. This will result in a significant acceleration of the update action.
  // Therefore, if repositories are defined, this must happen as def, not as val.
  // -------------------------------------------------------------------------------------------------------------------
  //import Repositories._
  //val parboiledModuleConfig = ModuleConfiguration("org.parboiled", ScalaToolsSnapshots)

  // -------------------------------------------------------------------------------------------------------------------
  // Dependencies
  // -------------------------------------------------------------------------------------------------------------------
  object Deps {
    // compile
    val asm         = "asm" % "asm" % "3.3.1" % "compile"
    val asmTree     = "asm" % "asm-tree" % "3.3.1" % "compile"
    val asmAnalysis = "asm" % "asm-analysis" % "3.3.1" % "compile"
    val asmUtil     = "asm" % "asm-util" % "3.3.1" % "compile"

    // test
    val testng    = "org.testng" % "testng" % "5.14.1" % "test"

    def scalaTest(scalaVersion: String) = scalaVersion match {
      case x if x startsWith "2.9" => "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test"
      case "2.10.0-M6" => "org.scalatest" % "scalatest_2.10.0-M6" % "1.9-2.10.0-M6-B2" % "test"
    }
  }
  import Deps._

  // -------------------------------------------------------------------------------------------------------------------
  // Compile settings
  // -------------------------------------------------------------------------------------------------------------------

  val javaCompileSettings = Seq(
    "-deprecation",
    "-target", "1.5",
    "-source", "1.5",
    "-encoding", "utf8",
    "-Xlint:unchecked"
  )
  val scalaCompileSettings = Seq(
    "-deprecation",
    //"-unchecked",
    "-encoding", "utf8"
  )

  lazy val rootProject =
    Project("root", file("."))
      .aggregate(parboiledCore, parboiledJava, parboiledScala, examplesJava, examplesScala)

  lazy val parboiledCore =
    Project("parboiled-core", file("parboiled-core"))
      .settings(commonSettings: _*)
      .settings(

      )

  lazy val parboiledJava =
    Project("parboiled-java", file("parboiled-java"))
      .settings(commonSettings: _*)
      .settings(
        libraryDependencies ++= Seq(
          asm,
          asmTree,
          asmAnalysis,
          asmUtil
        ),
        javacOptions in Test += "-g" // needed for bytecode rewriting
      )
      .dependsOn(parboiledCore)

  lazy val parboiledScala =
    Project("parboiled-scala", file("parboiled-scala"))
      .settings(commonSettings: _*)
      .settings(

      )
      .dependsOn(parboiledCore)

  lazy val examplesJava =
    Project("examples-java", file("examples-java"))
      .settings(commonSettings: _*)
      .settings(
        javacOptions += "-g" // needed for bytecode rewriting
      )
      .dependsOn(parboiledJava)

  lazy val examplesScala =
    Project("examples-scala", file("examples-scala"))
      .settings(commonSettings: _*)
      .settings(
      )
      .dependsOn(parboiledScala)

  val commonSettings = seq(
    scalaVersion := "2.10.0-M6",
    libraryDependencies ++= Seq(
      testng
    ),
    libraryDependencies <+= scalaVersion(scalaTest),
    javacOptions ++= javaCompileSettings,
    scalacOptions ++= scalaCompileSettings
  )

  /*
  // -------------------------------------------------------------------------------------------------------------------
  // Miscellaneous
  // -------------------------------------------------------------------------------------------------------------------
  lazy override val `package` = task { None }    // disable packaging
  lazy override val publishLocal = task { None } // and publishing
  lazy override val publish = task { None }      // the root project

  val pomExtras =
    <url>http://parboiled.org/</url>
    <inceptionYear>2009</inceptionYear>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <developers>
      <developer>
        <id>sirthias</id>
        <name>Mathias Doenitz</name>
        <timezone>+1</timezone>
        <email>mathias [at] parboiled.org</email>
      </developer>
    </developers>
    <scm>
      <url>http://github.com/sirthias/parboiled/</url>
    </scm>

  // -------------------------------------------------------------------------------------------------------------------
  // Subprojects
  // -------------------------------------------------------------------------------------------------------------------
  lazy val coreProject          = project("parboiled-core", "parboiled-core", new ParboiledCoreProject(_))
  lazy val javaProject          = project("parboiled-java", "parboiled-java", new ParboiledJavaProject(_))
  lazy val scalaProject         = project("parboiled-scala", "parboiled-scala", new ParboiledScalaProject(_))
  lazy val examplesJavaProject  = project("examples-java", "examples-java", new ExamplesJavaProject(_))
  lazy val examplesScalaProject = project("examples-scala", "examples-scala", new ExamplesScalaProject(_))

  abstract class ModuleProject(info: ProjectInfo) extends DefaultProject(info) {
    // Options
    override def compileOptions = super.compileOptions ++ scalaCompileSettings.map(CompileOption)
    override def javaCompileOptions = super.javaCompileOptions ++ javaCompileSettings.map(JavaCompileOption)
    override def documentOptions: Seq[ScaladocOption] = documentTitle(name + " " + version + " API") :: Nil
    override def pomExtra = pomExtras

    // Publishing
    val publishTo = "Scala Tools Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
    //val publishTo = "Scala Tools Releases" at "http://nexus.scala-tools.org/content/repositories/releases/"

    Credentials(Path.userHome / ".ivy2" / ".credentials", log)
    override def managedStyle = ManagedStyle.Maven
    override def packageDocsJar = defaultJarPath("-javadoc.jar")
    override def packageSrcJar = defaultJarPath("-sources.jar")
    lazy val sourceArtifact = Artifact(artifactID, "src", "jar", Some("sources"), Nil, None)
    lazy val docsArtifact = Artifact(artifactID, "docs", "jar", Some("javadoc"), Nil, None)
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageDocs, packageSrc)

    override def disableCrossPaths = true
  }

  abstract class JavaModuleProject(info: ProjectInfo) extends ModuleProject(info) {
    def docLink = ""
    override def docAction = fileTask(mainDocPath / "index.html" from mainSources) {
      val cmd = "javadoc" +
              " -sourcepath " + mainSourceRoots.absString +
              " -classpath " + docClasspath.absString +
              " -d " + mainDocPath +
              docLink +
              " -encoding utf8" +
              " -public" +
              " -windowtitle " + name + "_" + version +
              " -subpackages" +
              " org.parboiled"
      println(cmd)
      cmd !;
      None
    } dependsOn(compile) describedAs "Create Javadocs"
  }

  class ParboiledCoreProject(info: ProjectInfo) extends JavaModuleProject(info) {
    val testng = Deps.testng
    val scalaTest = Deps.scalaTest
  }

  class ParboiledJavaProject(info: ProjectInfo) extends JavaModuleProject(info) {
    val core = coreProject
    val asm1 = Deps.asm1
    val asm2 = Deps.asm2
    val asm3 = Deps.asm3
    val asm4 = Deps.asm4
    val testng = Deps.testng
    val scalaTest = Deps.scalaTest

    override def docLink = " -linkoffline http://www.decodified.com/parboiled/api/core " + coreProject.mainDocPath
  }

  class ParboiledScalaProject(info: ProjectInfo) extends ModuleProject(info) {
    val core = coreProject
    val scalaTest = Deps.scalaTest
  }

  class ExamplesJavaProject(info: ProjectInfo) extends ModuleProject(info) {
    var core = coreProject
    var java = javaProject
    val testng = Deps.testng
    val scalaTest = Deps.scalaTest

    override def deliverProjectDependencies = Nil

    // disable publishing
    lazy override val publishLocal = task { None }
    lazy override val publish = task { None }
  }

  class ExamplesScalaProject(info: ProjectInfo) extends ModuleProject(info) {
    var core = coreProject
    var scala = scalaProject

    val testng = Deps.testng
    val scalaTest = Deps.scalaTest

    override def deliverProjectDependencies = Nil

    // disable publishing
    lazy override val publishLocal = task { None }
    lazy override val publish = task { None }
  }*/

}
