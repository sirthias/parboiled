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

  val asmVersion  = "9.9"
  val asm         = "org.ow2.asm"   %  "asm"           % asmVersion
  val asmTree     = "org.ow2.asm"   %  "asm-tree"      % asmVersion
  val asmAnalysis = "org.ow2.asm"   %  "asm-analysis"  % asmVersion
  val asmUtil     = "org.ow2.asm"   %  "asm-util"      % asmVersion
  val testNG      = "org.scalatestplus" %% "testng-7-10" % "3.2.19.0"
}
