package org.parboiled.scala

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import testing.ParboiledTest

class WithContextTest extends ParboiledTest with TestNGSuite {

  class TestParser extends Parser {
    def Clause = rule {Digit ~> withContext(_.toInt + _.getCurrentIndex) ~ EOI}

    def Digit = rule {"0" - "9"}
  }

  type Result = Int

  val parser = new TestParser().withParseTreeBuilding()

  @Test
  def testWithContext() {
    parse(ReportingParseRunner(parser.Clause), "5") {
      assertEquals(parseTree,
         """|[Clause, {6}] '5'
            |  [Digit] '5'
            |  [EOI, {6}]
            |""".stripMargin)
    }
  }

}