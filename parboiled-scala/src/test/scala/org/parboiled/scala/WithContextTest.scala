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

class WithContextTest extends ParboiledTest with TestNGSuite {

  class TestParser extends Parser {
    def Clause = rule (
      "A"
      ~ pushFromContext(_.getCurrentIndex)
      ~ Digit
      ~~> withContext((_:Int) + (_:Int) + _.getCurrentIndex)
      ~% withContext(_.toString + _.getCurrentIndex)
      ~ EOI
    )

    def Digit = rule { ("0" - "9") ~> (_.toInt) }
  }

  type Result = Int

  val parser = new TestParser() {
    override val buildParseTree = true
  }

  @Test
  def testWithContext() {
    parse(ReportingParseRunner(parser.Clause), "A5") {
      assertEquals(parseTree,
         """|[Clause, {8}] 'A5'
            |  ['A'] 'A'
            |  [Digit, {5}] '5'
            |    [0..9, {1}] '5'
            |  [EOI, {8}]
            |""".stripMargin)
    }
  }

}