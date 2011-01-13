package org.parboiled.scala

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import testing.ParboiledTest

class WithContextTest extends ParboiledTest with TestNGSuite {

  class TestParser extends Parser {
    def Clause = rule {
      "A" ~ pushFromContext(_.getCurrentIndex) ~ Digit ~~> withContext((_:Int) + (_:Int) + _.getCurrentIndex) ~ EOI
    }

    def Digit = rule { ("0" - "9") ~> (_.toInt) }
  }

  type Result = Int

  val parser = new TestParser().withParseTreeBuilding()

  @Test
  def testWithContext() {
    parse(ReportingParseRunner(parser.Clause), "A5") {
      assertEquals(parseTree,
         """|[Clause, {8}] 'A5'
            |  ['A'] 'A'
            |  [Digit, {5}] '5'
            |    [0..9, {1}] '5'
            |  [EOI, {8}]
            |""".stripMargin)
    }
  }

}