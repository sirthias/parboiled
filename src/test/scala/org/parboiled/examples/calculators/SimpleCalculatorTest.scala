package org.parboiled.examples.calculators

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.matchers.Matcher
import org.parboiled.trees.{Filters, GraphUtils}
import org.parboiled.support.ToStringFormatter
import org.parboiled.scala.test.ParboiledTest

class SimpleCalculatorTest extends ParboiledTest with TestNGSuite {
  val parser = new SimpleCalculator1().withParseTreeBuilding()
  type Result = Int

  @Test
  def testSimpleCalculatorMatcherBuilding() {
    assertEquals(GraphUtils.printTree(parser.InputLine.matcher, new ToStringFormatter[Matcher](), Filters.preventLoops),
       """|InputLine
          |  Expression
          |    Term
          |      Factor
          |        Number
          |          Digits
          |            Digit
          |          Action
          |        Parens
          |          '('
          |          Expression
          |          ')'
          |      ZeroOrMore
          |        FirstOf
          |          Sequence
          |            '*'
          |            Factor
          |            Action
          |          Sequence
          |            '/'
          |            Factor
          |            Action
          |    ZeroOrMore
          |      FirstOf
          |        Sequence
          |          '+'
          |          Term
          |          Action
          |        Sequence
          |          '-'
          |          Term
          |          Action
          |  EOI
          |""".stripMargin)
  }

  @Test
  def testCalculations() {
    def test(input: String, expected: Int) {
      parse(parser.InputLine, input) {
        assertEquals(resultValue, expected)
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