/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  val parser = new NTimesParser() {
    override val buildParseTree = true
  }

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