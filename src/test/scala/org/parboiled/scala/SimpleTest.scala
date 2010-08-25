package org.parboiled.scala

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.matchers.Matcher
import org.parboiled.support.ToStringFormatter
import org.parboiled.trees.GraphUtils
import test.ParboiledTest

class SimpleTest extends ParboiledTest with TestNGSuite {

  class SimpleParser extends Parser {
    def Clause = rule {Digit ~ ClauseRest ~ EOI}

    def ClauseRest = rule {zeroOrMore(Operator ~ Digit) ~ anyOf("abcd")}

    def Operator = rule {"+" | "-"}

    def Digit = rule {"0" - "9"}
  }

  val parser = new SimpleParser().withParseTreeBuilding()

  @Test
  def testRuleTreeConstruction() {
    val rule = parser.Clause
    assertEquals(GraphUtils.printTree(rule.matcher, new ToStringFormatter[Matcher]),
       """|Clause
          |  Digit
          |  ClauseRest
          |    ZeroOrMore
          |      Sequence
          |        Operator
          |          '+'
          |          '-'
          |        Digit
          |    [abcd]
          |  EOI
          |""".stripMargin);
  }

  @Test
  def testSimpleParse() {
    parse(parser.Clause, "1+2a") {
      assertEquals(parseTree,
         """|[Clause] '1+2a'
            |  [Digit] '1'
            |  [ClauseRest] '+2a'
            |    [ZeroOrMore] '+2'
            |      [Sequence] '+2'
            |        [Operator] '+'
            |          ['+'] '+'
            |        [Digit] '2'
            |    [[abcd]] 'a'
            |  [EOI]
            |""".stripMargin)
    }
  }

}