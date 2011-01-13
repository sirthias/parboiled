package org.parboiled.scala

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import testing.ParboiledTest

class NTimesTest extends ParboiledTest with TestNGSuite {

  type Result = Int

  class NTimesParser extends Parser {
    def AsAndBs: Rule1[Int] = rule {
      "a" ~ nTimes(5, "b") ~ nTimes(3, Digit) ~~> (_.reduceLeft(_ + _))
    }

    def Digit = ("0" - "9") ~> (_.toInt)
  }

  val parser = new NTimesParser().withParseTreeBuilding()

  @Test
  def testNTimes() {
    parse(ReportingParseRunner(parser.AsAndBs), "abbbbb123") {
      assertEquals(parseTree,
         """[AsAndBs, {6}] 'abbbbb123'
  ['a'] 'a'
  [5-times] 'bbbbb'
    ['b'] 'b'
    ['b'] 'b'
    ['b'] 'b'
    ['b'] 'b'
    ['b'] 'b'
  [3-times, {List(1, 2, 3)}] '123'
    [0..9] '1'
    [Sequence, {2}] '2'
      [0..9, {List(1)}] '2'
    [Sequence, {3}] '3'
      [0..9, {List(2, 1)}] '3'
""".stripMargin)
    }
  }

}