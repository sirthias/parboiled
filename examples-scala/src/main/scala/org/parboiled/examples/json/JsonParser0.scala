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

/**
 * A complete JSON recognizer without any parser actions. When run with the RecoveringParseRunner this parser could be
 * used to report all syntax errors in a piece of JSON source.
 */
class JsonParser0 extends Parser {

  def Json = rule { JsonObject | JsonArray }

  def JsonObject: Rule0 = rule { WhiteSpace ~ "{ " ~ zeroOrMore(Pair, separator = ", ") ~ "} " }

  def Pair = rule { JsonString ~ ": " ~ Value }

  def Value: Rule0 = rule { JsonString | JsonNumber | JsonObject | JsonArray | "true " | "false " | "null " }

  def JsonString = rule { "\"" ~ zeroOrMore(Character) ~ "\" " }

  def JsonNumber = rule { Integer ~ optional(Frac ~ optional(Exp)) ~ WhiteSpace }

  def JsonArray = rule { "[ " ~ zeroOrMore(Value, separator = ", ") ~ "] " }

  def Character = rule { EscapedChar | NormalChar }

  def EscapedChar = rule { "\\" ~ (anyOf("\"\\/bfnrt") | Unicode) }

  def NormalChar = rule { !anyOf("\"\\") ~ ANY }

  def Unicode = rule { "u" ~ HexDigit ~ HexDigit ~ HexDigit ~ HexDigit }

  def Integer = rule { optional("-") ~ (("1" - "9") ~ Digits | Digit) }

  def Digits = rule { oneOrMore(Digit) }

  def Digit = rule { "0" - "9" }

  def HexDigit = rule { "0" - "9" | "a" - "f" | "A" - "Z" }

  def Frac = rule { "." ~ Digits }

  def Exp = rule { ignoreCase("e") ~ optional(anyOf("+-")) ~ Digits }

  def WhiteSpace = rule { zeroOrMore(anyOf(" \n\r\t\f")) }

  /**
   * We redefine the default string-to-rule conversion to also match trailing whitespace if the string ends with
   * a blank, this keeps the rules free from most whitespace matching clutter
   */
  override implicit def toRule(string: String) =
    if (string.endsWith(" "))
      str(string.trim) ~ WhiteSpace
    else
      str(string)

}