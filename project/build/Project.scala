import sbt._
import scala.io.Source
import java.io.File

/**
 * The SBT "buildfile" for parboiled
 */
class Project(info: ProjectInfo) extends ParentProject(info) with IdeaProject {
  
  // -------------------------------------------------------------------------------------------------------------------
  // Dependencies
  // -------------------------------------------------------------------------------------------------------------------
  object Deps {
    lazy val asm_all      = "asm" % "asm-all" % "3.3" % "compile" withSources()
    lazy val collections  = "com.google.collections" % "google-collections" % "1.0" % "compile" withSources() withJavadoc()

    lazy val scalatest    = "org.scalatest" % "scalatest" % "1.2" % "test" withSources()
    lazy val testng       = "org.testng" % "testng" % "5.14.1" % "test" withSources()
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Subprojects
  // -------------------------------------------------------------------------------------------------------------------
  lazy val mainProject = project("Main", "Main", new MainProject(_))
  lazy val examplesProject  = project("Examples", "Examples", new ExamplesProject(_))
  
  abstract class ModuleProject(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {
    override def compileOptions = super.compileOptions ++ Seq("-deprecation", "-encoding", "utf8").map(CompileOption(_))
  }

  class MainProject(info: ProjectInfo) extends ModuleProject(info) {
    val asm_all = Deps.asm_all
    val collections = Deps.collections
    
    val scalatest = Deps.scalatest
    val testng    = Deps.testng
  }

  class ExamplesProject(info: ProjectInfo) extends ModuleProject(info) {
    val main = mainProject
  }
}
