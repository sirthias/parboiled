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

  //@Test
  def verifyEasy() = {
    val rule = parser.InputLine

    /*assertEquals(printRule(rule), "" +
            "InputLine: SequenceRule\n"+
            "  Expression: SequenceRule\n"+
            "    Term: SequenceRule\n"+
            "      Factor: FirstOfRule\n"+
            "        Number: UnaryRule\n"+
            "          Digit: SimpleRule\n"+
            "        Parens: SequenceRule\n"+
            "          SequenceRule\n"+
            "            '(': CharRule\n"+
            "            ProxyRule\n"+
            "          ')': CharRule\n"+
            "      ZeroOrMore: UnaryRule\n"+
            "        FirstOfRule\n"+
            "          SequenceRule\n"+
            "            '*': CharRule\n"+
            "            Factor: FirstOfRule\n"+
            "          SequenceRule\n"+
            "            '/': CharRule\n"+
            "            Factor: FirstOfRule\n"+
            "    ZeroOrMore: UnaryRule\n"+
            "      FirstOfRule\n"+
            "        SequenceRule\n"+
            "          '+': CharRule\n"+
            "          Term: SequenceRule\n"+
            "        SequenceRule\n"+
            "          '-': CharRule\n"+
            "          Term: SequenceRule\n"+
            "  EOI: SimpleRule\n")*/

    val matcher = rule.toMatcher

    /*assertEquals(GraphUtils.printTree(matcher.asInstanceOf[Matcher[Int]], new ToStringFormatter[Matcher[Int]](),
      Filters.preventLoops[Int]), "" +
            "InputLine\n"+
            "    Expression\n"+
            "        Term\n"+
            "            Factor\n"+
            "                Number\n"+
            "                    Digit\n"+
            "                Parens\n"+
            "                    '('\n"+
            "                    Expression\n"+
            "                    ')'\n"+
            "            ZeroOrMore\n"+
            "                FirstOf\n"+
            "                    Sequence\n"+
            "                        '*'\n"+
            "                        Factor\n"+
            "                    Sequence\n"+
            "                        '/'\n"+
            "                        Factor\n"+
            "        ZeroOrMore\n"+
            "            FirstOf\n"+
            "                Sequence\n"+
            "                    '+'\n"+
            "                    Term\n"+
            "                Sequence\n"+
            "                    '-'\n"+
            "                    Term\n"+
            "    EOI\n");*/

    val res = testWithoutRecovery(matcher, "1+2*(3-4)", "" +
            "[InputLine, {-1}] '1+2*(3-4)'\n"+
            "    [Expression, {-1}] '1+2*(3-4)'\n"+
            "        [Term, {1}] '1'\n"+
            "            [Factor, {1}] '1'\n"+
            "                [Number, {1}] '1'\n"+
            "                    [Digits] '1'\n"+
            "                        [Digit] '1'\n"+
            "            [ZeroOrMore]\n"+
            "        [ZeroOrMore, {-1}] '+2*(3-4)'\n"+
            "            [FirstOf, {-1}] '+2*(3-4)'\n"+
            "                [Sequence, {-1}] '+2*(3-4)'\n"+
            "                    ['+'] '+'\n"+
            "                    [Term, {-4}] '2*(3-4)'\n"+
            "                        [Factor, {2}] '2'\n"+
            "                            [Number, {2}] '2'\n"+
            "                                [Digits] '2'\n"+
            "                                    [Digit] '2'\n"+
            "                        [ZeroOrMore, {-4}] '*(3-4)'\n"+
            "                            [FirstOf, {-4}] '*(3-4)'\n"+
            "                                [Sequence, {-4}] '*(3-4)'\n"+
            "                                    ['*'] '*'\n"+
            "                                    [Factor, {-1}] '(3-4)'\n"+
            "                                        [Parens, {-1}] '(3-4)'\n"+
            "                                            ['('] '('\n"+
            "                                            [Expression, {-1}] '3-4'\n"+
            "                                                [Term, {3}] '3'\n"+
            "                                                    [Factor, {3}] '3'\n"+
            "                                                        [Number, {3}] '3'\n"+
            "                                                            [Digits] '3'\n"+
            "                                                                [Digit] '3'\n"+
            "                                                    [ZeroOrMore]\n"+
            "                                                [ZeroOrMore, {-1}] '-4'\n"+
            "                                                    [FirstOf, {-1}] '-4'\n"+
            "                                                        [Sequence, {-1}] '-4'\n"+
            "                                                            ['-'] '-'\n"+
            "                                                            [Term, {4}] '4'\n"+
            "                                                                [Factor, {4}] '4'\n"+
            "                                                                    [Number, {4}] '4'\n"+
            "                                                                        [Digits] '4'\n"+
            "                                                                            [Digit] '4'\n"+
            "                                                                [ZeroOrMore]\n"+
            "                                            [')'] ')'\n"+
            "    [EOI]\n")
  }

}