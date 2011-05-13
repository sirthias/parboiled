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
import org.parboiled.trees.GraphUtils
import testing.ParboiledTest
import org.parboiled.support.{Filters, ToStringFormatter}
import org.parboiled.common.Predicates

class RecursionTest extends ParboiledTest with TestNGSuite {

  class RecursionParser extends Parser {
    def LotsOfAs: Rule0 = rule {ignoreCase('a') ~ optional(LotsOfAs)}
  }

  val parser = new RecursionParser() {
    override val buildParseTree = true
  }

  @Test
  def testRuleTreeConstruction() {
    val rule = parser.LotsOfAs
    assertEquals(GraphUtils.printTree(rule.matcher, new ToStringFormatter[Matcher], Predicates.alwaysTrue(),
      Filters.preventLoops()),
      """|LotsOfAs
         |  'a/A'
         |  Optional
         |    LotsOfAs
      |""".stripMargin);
  }

  @Test
  def testRecursion() {
    parse(ReportingParseRunner(parser.LotsOfAs), "aAAAa") {
      assertEquals(parseTree,
         """|[LotsOfAs] 'aAAAa'
            |  ['a/A'] 'a'
            |  [Optional] 'AAAa'
            |    [LotsOfAs] 'AAAa'
            |      ['a/A'] 'A'
            |      [Optional] 'AAa'
            |        [LotsOfAs] 'AAa'
            |          ['a/A'] 'A'
            |          [Optional] 'Aa'
            |            [LotsOfAs] 'Aa'
            |              ['a/A'] 'A'
            |              [Optional] 'a'
            |                [LotsOfAs] 'a'
            |                  ['a/A'] 'a'
            |                  [Optional]
            |""".stripMargin)
    }
  }

}