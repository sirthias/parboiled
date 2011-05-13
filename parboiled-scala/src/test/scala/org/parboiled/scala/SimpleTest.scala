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
import org.parboiled.matchers.Matcher
import org.parboiled.support.ToStringFormatter
import org.parboiled.trees.GraphUtils
import testing.ParboiledTest

class SimpleTest extends ParboiledTest with TestNGSuite {

  class SimpleParser extends Parser {
    def Clause = rule {Digit ~ ClauseRest ~ EOI}

    def ClauseRest = rule {zeroOrMore(Operator ~ Digit) ~ anyOf("abcd")}

    def Operator = rule {"+" | "-"}

    def Digit = rule {"0" - "9"}
  }

  val parser = new SimpleParser() {
    override val buildParseTree = true
  }

  @Test
  def testRuleTreeConstruction() {
    val rule = parser.Clause
    assertEquals(GraphUtils.printTree(rule.matcher, new ToStringFormatter[Matcher]),
       """|Clause
          |  Digit
          |  ClauseRest
          |    ZeroOrMore
          |      Sequence
          |        Operator
          |          '+'
          |          '-'
          |        Digit
          |    [abcd]
          |  EOI
          |""".stripMargin);
  }

  @Test
  def testSimpleParse() {
    parse(ReportingParseRunner(parser.Clause), "1+2a") {
      assertEquals(parseTree,
         """|[Clause] '1+2a'
            |  [Digit] '1'
            |  [ClauseRest] '+2a'
            |    [ZeroOrMore] '+2'
            |      [Sequence] '+2'
            |        [Operator] '+'
            |          ['+'] '+'
            |        [Digit] '2'
            |    [[abcd]] 'a'
            |  [EOI]
            |""".stripMargin)
    }
  }

}