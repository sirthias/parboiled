package org.parboiled.scala

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert._
import testing.ParboiledTest
import org.parboiled.matchers.FirstOfStringsMatcher

class FirstOfStringsTest extends ParboiledTest with TestNGSuite {

  class TestParser extends Parser {
    def Fast = rule { "Alpha" | "Bravo" | "Charlie" }

    def Slow1 = rule { EOI | "Alpha" | "Bravo" | "Charlie" }

    def Slow2 = rule { "Alpha" | EOI | "Bravo" | "Charlie" }

    def Slow3 = rule { "Alpha" | "Bravo" | "Charlie" | EOI }
  }

  val parser = new TestParser

  @Test
  def testFast() {
    assertTrue(parser.Fast.matcher.isInstanceOf[FirstOfStringsMatcher]);
    assertFalse(parser.Slow1.matcher.isInstanceOf[FirstOfStringsMatcher]);
    assertFalse(parser.Slow2.matcher.isInstanceOf[FirstOfStringsMatcher]);
    assertFalse(parser.Slow3.matcher.isInstanceOf[FirstOfStringsMatcher]);
  }

}