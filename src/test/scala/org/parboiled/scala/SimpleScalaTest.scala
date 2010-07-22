package org.parboiled.scala

import org.testng.annotations.Test
import org.parboiled.test.AbstractTest
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.Scala._
import org.parboiled.matchers.Matcher
import org.parboiled.support.ToStringFormatter
import org.parboiled.trees.{Filters, GraphUtils}

class SimpleScalaTest extends AbstractTest with TestNGSuite {
  val parser = new SimpleParser

  @Test
  def verifyEasy() = {
    val rule = parser.InputLine

    assertEquals(printRule(rule),
            """InputLine: SequenceRule
  Expression: SequenceRule
    Term: SequenceRule
      Factor: FirstOfRule
        Number: SequenceRule
          Digits: UnaryRule
            Digit: SimpleRule
          Action: SimpleRule
        Parens: SequenceRule
          SequenceRule
            '(': CharRule
            ProxyRule
          ')': CharRule
      ZeroOrMore: UnaryRule
        FirstOfRule
          SequenceRule
            SequenceRule
              '*': CharRule
              Factor: FirstOfRule
            Action: SimpleRule
          SequenceRule
            SequenceRule
              '/': CharRule
              Factor: FirstOfRule
            Action: SimpleRule
    ZeroOrMore: UnaryRule
      FirstOfRule
        SequenceRule
          SequenceRule
            '+': CharRule
            Term: SequenceRule
          Action: SimpleRule
        SequenceRule
          SequenceRule
            '-': CharRule
            Term: SequenceRule
          Action: SimpleRule
  EOI: SimpleRule
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