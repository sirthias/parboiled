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