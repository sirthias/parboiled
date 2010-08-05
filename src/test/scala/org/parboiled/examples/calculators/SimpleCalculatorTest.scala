package org.parboiled.examples.calculators

import org.testng.annotations.Test
import org.parboiled.test.AbstractTest
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.scala._
import org.parboiled.matchers.Matcher
import org.parboiled.support.ToStringFormatter
import org.parboiled.trees.{Filters, GraphUtils}

class SimpleCalculatorTest extends AbstractTest with TestNGSuite {
  val parser = new SimpleCalculator().withParseTreeBuilding()

  @Test
  def testSimpleCalculator() = {
    val rule = parser.InputLine

    assertEquals(Support.printRule(rule),
            """InputLine: SequenceCreator
  Factor: FirstOfCreator
    Number: SequenceCreator
      Digits: UnaryCreator
        Digit: SimpleCreator
      ActionCreator
    Parens: SequenceCreator
      '(': SimpleCreator
      ProxyCreator
        InputLine: SequenceCreator
      ')': SimpleCreator
  ZeroOrMore: UnaryCreator
    FirstOfCreator
      SequenceCreator
        '*': SimpleCreator
        Factor: FirstOfCreator
        ActionCreator
      SequenceCreator
        '/': SimpleCreator
        Factor: FirstOfCreator
        ActionCreator
  ZeroOrMore: UnaryCreator
    FirstOfCreator
      SequenceCreator
        '+': SimpleCreator
        InputLine: SequenceCreator
        ActionCreator
      SequenceCreator
        '-': SimpleCreator
        InputLine: SequenceCreator
        ActionCreator
  EOI: SimpleCreator
""")

    val matcher = rule.toMatcher

    assertEquals(GraphUtils.printTree(matcher.asInstanceOf[Matcher], new ToStringFormatter[Matcher](),
      Filters.preventLoops),
            """InputLine
  Expression
    Term
      Factor
        Number
          Digits
            Digit
          Action
        Parens
          '('
          Expression
          ')'
      ZeroOrMore
        FirstOf
          Sequence
            '*'
            Factor
            Action
          Sequence
            '/'
            Factor
            Action
    ZeroOrMore
      FirstOf
        Sequence
          '+'
          Term
          Action
        Sequence
          '-'
          Term
          Action
  EOI
""");

    val res = testWithoutRecovery(matcher, "1+2*(3-4)+5",
      """[InputLine, {4}] '1+2*(3-4)+5'
  [Expression, {4}] '1+2*(3-4)+5'
    [Term, {1}] '1'
      [Factor, {1}] '1'
        [Number, {1}] '1'
          [Digits] '1'
            [Digit] '1'
      [ZeroOrMore, {1}]
    [ZeroOrMore, {4}] '+2*(3-4)+5'
      [FirstOf, {-1}] '+2*(3-4)'
        [Sequence, {-1}] '+2*(3-4)'
          ['+', {1}] '+'
          [Term, {-2}] '2*(3-4)'
            [Factor, {2}] '2'
              [Number, {2}] '2'
                [Digits, {1}] '2'
                  [Digit, {1}] '2'
            [ZeroOrMore, {-2}] '*(3-4)'
              [FirstOf, {-2}] '*(3-4)'
                [Sequence, {-2}] '*(3-4)'
                  ['*', {2}] '*'
                  [Factor, {-1}] '(3-4)'
                    [Parens, {-1}] '(3-4)'
                      ['(', {2}] '('
                      [Expression, {-1}] '3-4'
                        [Term, {3}] '3'
                          [Factor, {3}] '3'
                            [Number, {3}] '3'
                              [Digits, {2}] '3'
                                [Digit, {2}] '3'
                          [ZeroOrMore, {3}]
                        [ZeroOrMore, {-1}] '-4'
                          [FirstOf, {-1}] '-4'
                            [Sequence, {-1}] '-4'
                              ['-', {3}] '-'
                              [Term, {4}] '4'
                                [Factor, {4}] '4'
                                  [Number, {4}] '4'
                                    [Digits, {3}] '4'
                                      [Digit, {3}] '4'
                                [ZeroOrMore, {4}]
                      [')', {-1}] ')'
      [FirstOf, {4}] '+5'
        [Sequence, {4}] '+5'
          ['+', {-1}] '+'
          [Term, {5}] '5'
            [Factor, {5}] '5'
              [Number, {5}] '5'
                [Digits, {-1}] '5'
                  [Digit, {-1}] '5'
            [ZeroOrMore, {5}]
  [EOI, {4}]
""")
  }

}