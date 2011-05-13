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

class SemanticPredicateTest extends ParboiledTest with TestNGSuite {

  class TestParser extends Parser {
    def Clause = rule { Number ~ " " ~ Number ~ " " ~ Number ~~~? (_ + _ == _) ~ EOI ~~> (_ + _ - _) }

    def Number = rule { oneOrMore("0" - "9") ~> (_.toInt) }
  }

  type Result = Int

  val parser = new TestParser() {
    override val buildParseTree = true
  }

  @Test
  def testWithContext() {
    parse(ReportingParseRunner(parser.Clause), "2 3 5") {
      assertEquals(parseTree,
         """|[Clause, {0}] '2 3 5'
            |  [Number, {2}] '2'
            |    [OneOrMore] '2'
            |      [0..9] '2'
            |  [' ', {2}] ' '
            |  [Number, {3}] '3'
            |    [OneOrMore, {2}] '3'
            |      [0..9, {2}] '3'
            |  [' ', {3}] ' '
            |  [Number, {5}] '5'
            |    [OneOrMore, {3}] '5'
            |      [0..9, {3}] '5'
            |  [EOI, {5}]
            |""".stripMargin)
    }
  }

}