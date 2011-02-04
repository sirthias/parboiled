package org.parboiled.examples.calculators

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.scala.parserunners._
import org.parboiled.scala.testing.ParboiledTest
import org.parboiled.common.StringBuilderSink

class TracingTest extends ParboiledTest with TestNGSuite {

  val parser = new SimpleCalculator1

  type Result = Int

  @Test
  def testTraceParse() {
    val log = new StringBuilderSink 
    parse(TracingParseRunner(parser.InputLine).filter(Rules.only(parser.Factor, parser.Term)).log(log), "1+2") {
      assertEquals(log.toString,
         """Starting new parsing run
InputLine/Expression/Term/Factor, matched, cursor at 1:2 after "1"
..(2)../Term, matched, cursor at 1:2 after "1"
..(1)../Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor, matched, cursor at 1:4 after "1+2"
..(5)../Term, matched, cursor at 1:4 after "1+2"
""")
    }
  }
    
  @Test
  def testTraceParse2() {
    val log = new StringBuilderSink 
    parse(TracingParseRunner(parser.InputLine).filter(Rules.below(parser.Factor) && !Rules.below(parser.Digits)).log(log), "1+2") {
      assertEquals(log.toString,
         """Starting new parsing run
InputLine/Expression/Term/Factor/Number/Digits, matched, cursor at 1:2 after "1"
..(4)../Number/NumberAction1, matched, cursor at 1:2 after "1"
..(4)../Number, matched, cursor at 1:2 after "1"
..(1)../Expression/ZeroOrMore/FirstOf/Sequence/Term/Factor/Number/Digits, matched, cursor at 1:4 after "1+2"
..(7)../Number/NumberAction1, matched, cursor at 1:4 after "1+2"
..(7)../Number, matched, cursor at 1:4 after "1+2"
""")
    }
  }

}