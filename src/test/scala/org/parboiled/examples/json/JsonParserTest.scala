package org.parboiled.examples.json

import org.testng.annotations.Test
import org.parboiled.test.AbstractTest
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.scala.ReportingParseRunner
import org.parboiled.errors.ErrorUtils.printParseErrors
import org.parboiled.support.ParseTreeUtils.printNodeTree

class JsonParserTest extends AbstractTest with TestNGSuite {
  val parser = new JsonParser()

  @Test
  def testJsonParser() = {
    val json = """{
  "simpleKey" : "some value",
  "key with spaces": null,
  "zero": 0,
  "number": -1.2323424E-5,
  "Boolean yes":true,
  "Boolean no": false,
  "Unic\u00f8de" :  "Long string with newline\nescape",
  "key with \"quotes\"" : "string",
  "sub object" : {
    "sub key": 26.5,
    "a": "b",
    "array": [1, 2, { "yes":1, "no":0 }, ["a", "b", null], false]
  }
}
"""

    val rootNode = parser.parseJson(json)
    assertEquals(printAst(rootNode, ""),
"""{
  "simpleKey" : "some value"
  "key with spaces" : null
  "zero" : 0
  "number" : -0.000012323424
  "Boolean yes" : true
  "Boolean no" : false
  "Unicøde" : "Long string with newline\nescape"
  "key with \"quotes\"" : "string"
  "sub object" : {
    "sub key" : 26.5
    "a" : "b"
    "array" : [1, 2, {
        "yes" : 1
        "no" : 0
      }, ["a", "b", null], false]
  }
}""")
  }

  private def printAst(node: JsonParser#AstNode, indent: String): String = node match {
    case n: JsonParser#ObjectNode => "{\n" + (for (sub <- n.members) yield printAst(sub, indent + "  ")).mkString + indent + "}"
    case n: JsonParser#MemberNode => indent + '"' + n.key + "\" : " + printAst(n.value, indent) + "\n"
    case n: JsonParser#ArrayNode => '[' + (for (sub <- n.elements) yield printAst(sub, indent + "  ")).mkString(", ") + "]"
    case n: JsonParser#StringNode => '"' + n.text + '"'
    case n: JsonParser#NumberNode => n.value.toString
    case parser.True => "true"
    case parser.False => "false"
    case parser.Null => "null"
  }

}