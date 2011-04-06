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