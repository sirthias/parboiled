package org.parboiled.scala

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import parserunners.Rules
import testing.ParboiledTest
import org.parboiled.examples.calculators.SimpleCalculator1

class TracingTest extends ParboiledTest with TestNGSuite {

  val parser = new SimpleCalculator1

  type Result = Int

  @Test
  def testTraceParse() {
    parse(TracingParseRunner(parser.InputLine).filter(Rules.only(parser.Factor, parser.Term)), "1+2") {
      assertEquals(traceLog,
         """Starting match on rule 'InputLine'
InputLine/Expression/Term/Factor: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term: matched, cursor is at line 1, col 4: "1+2"
""")
    }

    parse(TracingParseRunner(parser.InputLine).filter(Rules.below(parser.Factor) && !Rules.below(parser.Digits)), "1+2") {
      assertEquals(traceLog,
         """Starting match on rule 'InputLine'
InputLine/Expression/Term/Factor/Number/Digits: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/Factor/Number/Action: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/Term/Factor/Number: matched, cursor is at line 1, col 2: "1"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number/Digits: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number/Action: matched, cursor is at line 1, col 4: "1+2"
InputLine/Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number: matched, cursor is at line 1, col 4: "1+2"
""")
    }
  }

}