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

package org.parboiled.examples.json

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.scala.testing.ParboiledTest
import org.parboiled.scala.parserunners.ReportingParseRunner

class JsonParserTest extends ParboiledTest with TestNGSuite {
  val parser = new JsonParser1()

  type Result = parser.AstNode

  @Test
  def testJsonParser() {
    val json = """|{
                  |  "simpleKey" : "some value",
                  |  "key with spaces": null,
                  |  "zero": 0,
                  |  "number": -1.2323424E-5,
                  |  "Boolean yes":true,
                  |  "Boolean no": false,
                  |  "Unic\u00f8de" :  "Long string with newline\nescape",
                  |  "key with \"quotes\"" : "string",
                  |  "sub object" : {
                  |    "sub key": 26.5,
                  |    "a": "b",
                  |    "array": [1, 2, { "yes":1, "no":0 }, ["a", "b", null], false]
                  |  }
                  |}""".stripMargin

    val rootNode = parser.parseJson(json)
    assertEquals(printAst(rootNode),
       """|{
          |  "simpleKey" : "some value"
          |  "key with spaces" : null
          |  "zero" : 0
          |  "number" : -0.000012323424
          |  "Boolean yes" : true
          |  "Boolean no" : false
          |  "Unicøde" : "Long string with newline\nescape"
          |  "key with \"quotes\"" : "string"
          |  "sub object" : {
          |    "sub key" : 26.5
          |    "a" : "b"
          |    "array" : [1, 2, {
          |        "yes" : 1
          |        "no" : 0
          |      }, ["a", "b", null], false]
          |  }
          |}""".stripMargin)
  }

  @Test
  def testJsonParserError() {
    failParse(ReportingParseRunner(parser.Json), "XYZ") {
      assertEquals(errors,
        """|Invalid input 'X', expected Json (line 1, pos 1):
           |XYZ
           |^
           |""".stripMargin
      )
    }
  }

  def printAst(node: JsonParser1#AstNode, indent: String = ""): String = node match {
    case n: JsonParser1#ObjectNode => "{\n" + (for (sub <- n.members) yield printAst(sub, indent + "  ")).mkString + indent + "}"
    case n: JsonParser1#MemberNode => indent + '"' + n.key + "\" : " + printAst(n.value, indent) + "\n"
    case n: JsonParser1#ArrayNode => '[' + (for (sub <- n.elements) yield printAst(sub, indent + "  ")).mkString(", ") + "]"
    case n: JsonParser1#StringNode => '"' + n.text + '"'
    case n: JsonParser1#NumberNode => n.value.toString
    case parser.True => "true"
    case parser.False => "false"
    case parser.Null => "null"
  }

}