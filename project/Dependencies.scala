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

  val asmVersion  = "8.0.1"
  val asm         = "org.ow2.asm"   %  "asm"           % asmVersion
  val asmTree     = "org.ow2.asm"   %  "asm-tree"      % asmVersion
  val asmAnalysis = "org.ow2.asm"   %  "asm-analysis"  % asmVersion
  val asmUtil     = "org.ow2.asm"   %  "asm-util"      % asmVersion
  val testNG      = "org.scalatestplus" %% "testng-6-7" % "3.2.0.0"

  def scalatest(scalaVersion: String) = {
    Seq("org.scalatest" %% "scalatest" % "3.2.0")
  }
}
