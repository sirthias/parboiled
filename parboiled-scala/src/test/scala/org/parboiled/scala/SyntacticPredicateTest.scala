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

class SyntacticPredicateTest extends ParboiledTest with TestNGSuiteLike {

  class TestParser extends Parser {
    def Clause = rule { Number ~ &("1") ~ Number ~ !"5" ~ Number }
    def Number = rule { "0" - "9" }
  }

  type Result = Int

  val parser = new TestParser() {
    override val buildParseTree = true
  }

  @Test
  def testSyntacticPredicates() {
    parse(ReportingParseRunner(parser.Clause), "216") {
      assertEquals(parseTree,
        """|[Clause] '216'
           |  [Number] '2'
           |  [Number] '1'
           |  [Number] '6'
           |""".stripMargin)
    }
  }

}