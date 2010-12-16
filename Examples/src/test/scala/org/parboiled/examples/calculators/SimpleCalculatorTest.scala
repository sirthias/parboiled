package org.parboiled.examples.calculators

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.matchers.Matcher
import org.parboiled.trees.GraphUtils
import org.parboiled.scala.testing.ParboiledTest
import org.parboiled.support.{Filters, ToStringFormatter}
import org.parboiled.common.Predicates
import org.parboiled.scala.parserunners.ReportingParseRunner

class SimpleCalculatorTest extends ParboiledTest with TestNGSuite {
  val parser = new SimpleCalculator1().withParseTreeBuilding()
  type Result = Int

  @Test
  def testSimpleCalculatorMatcherBuilding() {
    assertEquals(GraphUtils.printTree(parser.InputLine.matcher, new ToStringFormatter[Matcher](),
      Predicates.alwaysTrue(), Filters.preventLoops),
       """|InputLine
          |  Expression
          |    Term
          |      Factor
          |        Number
          |          Digits
          |            Digit
          |          NumberAction1
          |        Parens
          |          '('
          |          Expression
          |          ')'
          |      ZeroOrMore
          |        FirstOf
          |          Sequence
          |            '*'
          |            Factor
          |            TermAction1
          |          Sequence
          |            '/'
          |            Factor
          |            TermAction2
          |    ZeroOrMore
          |      FirstOf
          |        Sequence
          |          '+'
          |          Term
          |          ExpressionAction1
          |        Sequence
          |          '-'
          |          Term
          |          ExpressionAction2
          |  EOI
          |""".stripMargin)
  }

  @Test
  def testCalculations() {
    def test(input: String, expected: Int) {
      parse(ReportingParseRunner(parser.InputLine), input) {
        assertEquals(result, expected)
      }
    }
    test("1+2", 3)
    test("1+2", 3)
    test("1+2-3+4", 4)
    test("1-2-3", -4)
    test("1-(2-3)", 2)
    test("1*2+3", 5)
    test("1+2*3", 7)
    test("1*2*3", 6)
    test("3*4/6", 2)
    test("24/6/2", 2)
    test("1-2*3-4", -9)
    test("1-2*3-4*5-6", -31)
    test("1-24/6/2-(5+7)", -13)
    test("((1+2)*3-(4-5))/5", 2)
  }

}