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

  val asm         = "org.ow2.asm"   %  "asm"           % "5.2"
  val asmTree     = "org.ow2.asm"   %  "asm-tree"      % "5.2"
  val asmAnalysis = "org.ow2.asm"   %  "asm-analysis"  % "5.2"
  val asmUtil     = "org.ow2.asm"   %  "asm-util"      % "5.2"
  val testNG      = "org.testng"    %  "testng"        % "5.14.10"

  def scalatest(scalaVersion: String) = "org.scalatest" %% "scalatest" % "3.0.4"
}
