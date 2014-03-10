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
import org.scalatest.testng.TestNGSuiteLike
import org.testng.Assert.assertEquals
import testing.ParboiledTest

class LoopedReductionRuleTest extends ParboiledTest with TestNGSuiteLike {

  type Result = Expression

  trait Expression {
    def value: Double
  }
  case class IntExpression(intValue: Int) extends Expression {
    def value = intValue.toDouble
  }
  case class DoubleExpression(value: Double) extends Expression

  class TestParser extends Parser {

    def IntsAndDoubles: Rule1[DoubleExpression] = rule {
      "1" ~> (s => IntExpression(s.toInt)) ~ oneOrMore(" + " ~ AddDouble)
    }

    def IntsAndMaybeDoubles: Rule1[Expression] = rule {
      "1" ~> (s => IntExpression(s.toInt)) ~ zeroOrMore(" + " ~ AddDouble)
    }

    def AddDouble: ReductionRule1[Expression, DoubleExpression] = rule {
      "0.3" ~> (s => DoubleExpression(s.toDouble)) ~~> ((l: Expression, r: DoubleExpression) => DoubleExpression(l.value + r.value))
    }
  }

  val parser = new TestParser() {
    override val buildParseTree = true
  }

  @Test
  def testLoopedReductionRule() {
    parse(ReportingParseRunner(parser.IntsAndDoubles), "1 + 0.3 + 0.3 + 0.3") {
      assertEquals(parseTree,
        """[IntsAndDoubles, {DoubleExpression(1.9000000000000001)}] '1 + 0.3 + 0.3 + 0.3'
  ['1'] '1'
  [OneOrMore, {DoubleExpression(1.9000000000000001)}] ' + 0.3 + 0.3 + 0.3'
    [Sequence, {DoubleExpression(1.3)}] ' + 0.3'
      [" + ", {IntExpression(1)}] ' + '
      [AddDouble, {DoubleExpression(1.3)}] '0.3'
        ["0.3", {IntExpression(1)}] '0.3'
    [Sequence, {DoubleExpression(1.6)}] ' + 0.3'
      [" + ", {DoubleExpression(1.3)}] ' + '
      [AddDouble, {DoubleExpression(1.6)}] '0.3'
        ["0.3", {DoubleExpression(1.3)}] '0.3'
    [Sequence, {DoubleExpression(1.9000000000000001)}] ' + 0.3'
      [" + ", {DoubleExpression(1.6)}] ' + '
      [AddDouble, {DoubleExpression(1.9000000000000001)}] '0.3'
        ["0.3", {DoubleExpression(1.6)}] '0.3'
""".stripMargin)
    }
  }

}
