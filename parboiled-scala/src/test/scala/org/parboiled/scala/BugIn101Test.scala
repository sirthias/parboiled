package org.parboiled.scala

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

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import testing.ParboiledTest

class BugIn101Test extends ParboiledTest with TestNGSuite {

  type Result = Int

  class MyParser extends Parser {
     def A = rule { nTimes(1, "a") }
 }

  val parser = new MyParser() {
    override val buildParseTree = true
  }

  @Test
  def testNTimes() {
    parse(ReportingParseRunner(parser.A), "any") {
      assertEquals(parseTree,"[A] 'a'\n")
    }
  }

}