package org.parboiled.scala

import org.testng.annotations.Test
import org.parboiled.test.AbstractTest
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.scala.Parboiled._
import collection.mutable.Set

class SimpleScalaTest extends AbstractTest with TestNGSuite {
  val parser = new SimpleParser

  def printRule(rule: Rule[_], indent: String, printIndent: Boolean, printed: Set[Rule[_]]): String = {
    if (!printed.contains(rule)) {
      printed += rule
      val printSub = printRule(_: Rule[_], indent + "  ", true, printed)
      val ind = if (printIndent) indent else ""
      rule match {
        case r: LabelRule[_] => ind + r.label + ": " + printRule(r.sub, indent, false, printed)
        case r: BinaryRule[_, _, _] => ind + rule + '\n' + printSub(r.left) + printSub(r.right)
        case r: UnaryRule[_] => ind + rule + '\n' + printSub(r.sub)
        case r: LeafRule => ind + rule + ": " + r.matcher + '\n'
        case r: Rule[_] => ind + r + '\n'
      }
    } else ""
  }

  @Test
  def verifyEasy() = {
    val rule = parser.InputLine

    assertEquals(printRule(rule, "", false, Set.empty[Rule[_]]), "" +
            "InputLine: SequenceRule\n"+
            "  Expression: SequenceRule\n"+
            "    Term: SequenceRule\n"+
            "      Factor: FirstOfRule\n"+
            "        Number: SequenceRule\n"+
            "          Digits: OneOrMoreRule\n"+
            "            LeafRule: Digit\n"+
            "          ActionRule\n"+
            "        Parens: SequenceRule\n"+
            "          SequenceRule\n"+
            "            CharRule: '('\n"+
            "            ProxyRule\n"+
            "          CharRule: ')'\n"+
            "      ZeroOrMoreRule\n"+
            "        SequenceRule\n"+
            "          LeafRule: [*-]\n"+
            "    ZeroOrMoreRule\n"+
            "      SequenceRule\n"+
            "        LeafRule: [+-]\n"+
            "  LeafRule: EOI\n")

    val res = testWithoutRecovery(rule.toMatcher, "1+5*4*(1-6)", "" +
            "[Clause] '1+5b'\n" +
            "    [Digit] '1'\n" +
            "    [Operator] '+'\n" +
            "        ['+'] '+'\n" +
            "    [Digit] '5'\n" +
            "    [[abcd]] 'b'\n" +
            "    [EOI]\n")
  }

}