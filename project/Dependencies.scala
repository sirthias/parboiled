import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    // "typesafe repo"   at "http://repo.typesafe.com/typesafe/releases/",
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val asm         = "org.ow2.asm"   %  "asm"           % "5.0.3"
  val asmTree     = "org.ow2.asm"   %  "asm-tree"      % "5.0.3"
  val asmAnalysis = "org.ow2.asm"   %  "asm-analysis"  % "5.0.3"
  val asmUtil     = "org.ow2.asm"   %  "asm-util"      % "5.0.3"
  val testNG      = "org.testng"    %  "testng"        % "5.14.10"

  def scalatest(scalaVersion: String) = "org.scalatest" %% "scalatest" % {
    scalaVersion match {
      case "2.9.2" => "1.9.2"
      case "2.9.3" => "1.9.2"
      case "2.10.4" => "2.2.4"
      case "2.11.5" => "2.2.4"
    }
  }
}
