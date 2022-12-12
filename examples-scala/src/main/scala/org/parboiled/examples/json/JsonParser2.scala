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

import org.parboiled.scala._
import java.lang.String
import org.parboiled.errors.{ErrorUtils, ParsingException}

/**
 * <p>A complete JSON parser producing an AST representation of the parsed JSON source.</p>
 * <p>The syntactical grammar is identical to the {@link JsonParser0} example, however this parser adds the parser
 * actions required to build an AST during the parsing run.</p>
 * <p>As opposed to the functionally equivalent JsonParser1 class this class contains type specifications that could
 * be left out due to Scalas type inference but might make the parsers workings easier to understand for first-time
 * users.</p>
 */
class JsonParser2 extends Parser {

  /**
   * These case classes form the nodes of the AST.
   */
  sealed abstract class AstNode
  case class ObjectNode(members: List[MemberNode]) extends AstNode
  case class MemberNode(key: String, value: AstNode) extends AstNode
  case class ArrayNode(elements: List[AstNode]) extends AstNode
  case class StringNode(text: String) extends AstNode
  case class NumberNode(value: BigDecimal) extends AstNode
  case object True extends AstNode
  case object False extends AstNode
  case object Null extends AstNode

  // the root rule
  def Json: Rule1[AstNode] = rule { WhiteSpace ~ (JsonObject | JsonArray) ~ EOI }
  
  def JsonObject: Rule1[ObjectNode] = rule {
    "{ " ~ zeroOrMore(Pair, separator = ", ") ~ "} " ~~> ObjectNode.apply
  }

  def Pair: Rule1[MemberNode] = rule {
    JsonString ~~> (_.text) ~ ": " ~ Value ~~> MemberNode.apply
  }

  def Value: Rule1[AstNode] = rule {
    JsonString | JsonNumber | JsonObject | JsonArray | JsonTrue | JsonFalse | JsonNull
  }

  def JsonString: Rule1[StringNode] = rule {
    "\"" ~ zeroOrMore(Character) ~> StringNode.apply ~ "\" "
  }

  def JsonNumber: Rule1[NumberNode] = rule {
    group(Integer ~ optional(Frac ~ optional(Exp))) ~> ((matched) => NumberNode(BigDecimal(matched))) ~ WhiteSpace
  }

  def JsonArray: Rule1[ArrayNode] = rule {
    "[ " ~ zeroOrMore(Value, separator = ", ") ~ "] " ~~> ArrayNode.apply
  }

  def Character: Rule0 = rule { EscapedChar | NormalChar }

  def EscapedChar: Rule0 = rule { "\\" ~ (anyOf("\"\\/bfnrt") | Unicode) }

  def NormalChar: Rule0 = rule { !anyOf("\"\\") ~ ANY }

  def Unicode: Rule0 = rule { "u" ~ HexDigit ~ HexDigit ~ HexDigit ~ HexDigit }

  def Integer: Rule0 = rule { optional("-") ~ (("1" - "9") ~ Digits | Digit) }

  def Digits: Rule0 = rule { oneOrMore(Digit) }

  def Digit: Rule0 = rule { "0" - "9" }

  def HexDigit: Rule0 = rule { "0" - "9" | "a" - "f" | "A" - "F" }

  def Frac: Rule0 = rule { "." ~ Digits }

  def Exp: Rule0 = rule { ignoreCase("e") ~ optional(anyOf("+-")) ~ Digits }

  def JsonTrue: Rule1[AstNode] = rule { "true " ~ push(True) }

  def JsonFalse: Rule1[AstNode] = rule { "false " ~ push(False) }

  def JsonNull: Rule1[AstNode] = rule { "null " ~ push(Null) }

  def WhiteSpace: Rule0 = rule { zeroOrMore(anyOf(" \n\r\t\f")) }

  /**
   * We redefine the default string-to-rule conversion to also match trailing whitespace if the string ends with
   * a blank, this keeps the rules free from most whitespace matching clutter
   */
  override implicit def toRule(string: String) =
    if (string.endsWith(" "))
      str(string.trim) ~ WhiteSpace
    else
      str(string)

  /**
   * The main parsing method. Uses a ReportingParseRunner (which only reports the first error) for simplicity.
   */
  def parseJson(json: String): AstNode = {
    val parsingResult = ReportingParseRunner(Json).run(json)
    parsingResult.result match {
      case Some(astRoot) => astRoot
      case None => throw new ParsingException("Invalid JSON source:\n" +
              ErrorUtils.printParseErrors(parsingResult)) 
    }
  }

}
