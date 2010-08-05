package org.parboiled.examples.calculators

import org.testng.annotations.Test
import org.parboiled.test.AbstractTest
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.scala._
import org.parboiled.matchers.Matcher
import org.parboiled.support.ToStringFormatter
import org.parboiled.trees.{Filters, GraphUtils}

class SimpleTest extends AbstractTest with TestNGSuite {

  class SimpleParser extends Parser {
    def Clause = rule {Digit ~ Operator ~ Digit ~ anyOf("abcd") ~ EOI}
    def Operator = rule { "+" | "-" }
    def Digit = rule { "0" - "9" }
  }

  val parser = new SimpleParser().withParseTreeBuilding()

  @Test
  def testSimpleParser() = {
    val rule = parser.Clause
    assertEquals(Support.printRule(rule),
      """Clause: SequenceCreator
  Digit: SimpleCreator
  Operator: FirstOfCreator
    '+': SimpleCreator
    '-': SimpleCreator
  Digit: SimpleCreator
  [abcd]: SimpleCreator
  EOI: SimpleCreator
""")

    assertEquals(GraphUtils.printTree(rule.toMatcher, new ToStringFormatter[Matcher]),
      """Clause
  Digit
  Operator
    '+'
    '-'
  Digit
  [abcd]
  EOI
""");

    val res = testWithoutRecovery(rule.toMatcher, "1+2a",
      """[Clause] '1+2a'
  [Digit] '1'
  [Operator] '+'
    ['+'] '+'
  [Digit] '2'
  [[abcd]] 'a'
  [EOI]
""")
  }

}