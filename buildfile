repositories.remote << 'http://repo1.maven.org/maven2'
repositories.release_to[:url] = 'http://nexus.scala-tools.org/content/repositories/releases/'

Buildr.settings.build['scala.version'] = "2.8.1"
VERSION_NUMBER = "0.10.0"

require "buildr/scala"

desc "The parent project"
define "parboiled" do
  project.version = VERSION_NUMBER
  project.group = "org.parboiled"

  manifest["Built-By"] = "Mathias"
  manifest["Specification-Title"] = "parboiled"
  manifest["Specification-Version"] = VERSION_NUMBER
  manifest["Specification-Vendor"] = "parboiled.org"
  manifest["Implementation-Title"] = "parboiled"
  manifest["Implementation-Version"] = "${version}"
  manifest["Implementation-Vendor"] = "parboiled.org"
  manifest["Bundle-License"] = "http://www.apache.org/licenses/LICENSE-2.0.txt"
  manifest["Bundle-Version"] = VERSION_NUMBER
  manifest["Bundle-Description"] = "parboiled, a Java 1.5+/Scala 2.8 library providing a light-weight and easy-to-use, yet powerful PEG parsing facility"
  manifest["Bundle-Name"] = "parboiled"
  manifest["Bundle-DocURL"] = "http://www.parboiled.org"
  manifest["Bundle-Vendor"] = "parboiled.org"
  manifest["Bundle-SymbolicName"] = "org.parboiled"

  ASM = ["asm:asm:jar:3.3", "asm:asm-tree:jar:3.3", "asm:asm-analysis:jar:3.3", "asm:asm-util:jar:3.3"]
  SCALATEST = "org.scalatest:scalatest:jar:1.2"

  compile.using :deprecation => true, :target => "1.5", :other => ["-encoding", "UTF-8"], :lint=> "all"
  meta_inf << file('NOTICE')

  desc "The core parts of parboiled, depended on by everything else"
  define "core" do
    test.using :testng
    package(:jar).pom.from file("pom.xml")
    package :sources
    package :javadoc
  end

  desc "The Java DSL and supporting code"
  define "java" do
    compile.with ASM, project("core")
    test.using :testng
    package(:jar).pom.from file("pom.xml")
    package :sources
    package :javadoc
  end

  desc "The Scala DSL and supporting code"
  define "scala" do
    compile.with project("core")
    test.with SCALATEST
    test.using :testng
    package(:jar).pom.from file("pom.xml")
    package :sources
  end

  desc "Examples using the Java DSL"
  define "examples-java" do
    compile.with ASM, project("core"), project("java")
    test.using :testng
  end

  desc "Examples using the Scala DSL"
  define "examples-scala" do
    compile.with project("core"), project("scala")
    test.with SCALATEST
    test.using :testng
  end
end