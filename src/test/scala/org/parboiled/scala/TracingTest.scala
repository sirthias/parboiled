package org.parboiled.scala

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.matchers.Matcher
import org.parboiled.support.ToStringFormatter
import org.parboiled.trees.GraphUtils
import testing.ParboiledTest
import org.parboiled.examples.calculators.SimpleCalculator1

class TracingTest extends ParboiledTest with TestNGSuite {

  val parser = new SimpleCalculator1

  type Result = Int

  @Test
  def testTraceParse() {
    traceParse(parser.InputLine, "1+2") {
      assertEquals(traceLog,
         """Starting match on rule 'InputLine'
InputLine/Expression/Term/Factor/Number/Digits/Digit: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/Factor/Number/Digits/Digit: failed, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/Factor/Number/Digits: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/Factor/Number/Action: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/Factor/Number: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/Factor: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/ZeroOrMore/FirstOf/Sequence/'*': failed, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/ZeroOrMore/FirstOf/Sequence: failed, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/ZeroOrMore/FirstOf/Sequence/'/': failed, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/ZeroOrMore/FirstOf/Sequence: failed, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/ZeroOrMore/FirstOf: failed, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/ZeroOrMore: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/'+': matched, cursor is at line 1, col 3: "1+"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number/Digits/Digit: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number/Digits/Digit: failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number/Digits: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number/Action: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/ZeroOrMore/FirstOf/Sequence/'*': failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/ZeroOrMore/FirstOf/Sequence: failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/ZeroOrMore/FirstOf/Sequence/'/': failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/ZeroOrMore/FirstOf/Sequence: failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/ZeroOrMore/FirstOf: failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/ZeroOrMore: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Action: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/'+': failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence: failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/'-': failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence: failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf: failed, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression: matched, cursor is at line 1, col 4: "1+2"
InputLine/EOI: matched, cursor is at line 1, col 4: "1+2"
InputLine: matched, cursor is at line 1, col 4: "1+2"
""")
    }
  }

}