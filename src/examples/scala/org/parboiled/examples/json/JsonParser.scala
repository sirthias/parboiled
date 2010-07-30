package org.parboiled.examples.json

import org.parboiled.Scala._

sealed abstract class AstNode
case class ObjectNode(members: List[MemberNode]) extends AstNode
case class MemberNode(key: String, value: AstNode) extends AstNode
case class ArrayNode(elements: Array[AstNode]) extends AstNode
case class StringNode(text: String) extends AstNode
case class NumberNode(value: BigDecimal) extends AstNode
case object True extends AstNode
case object False extends AstNode
case object Null extends AstNode

class JsonParser extends Parser[AstNode] {

  def Object: Rule = rule { WS ~ ws('{') ~ optional(Members) ~ ws('}') }
  def Members = rule {
    var members = List.empty[MemberNode]
    Pair_ ~ (members += pop().asInstanceOf[MemberNode]) ~ zeroOrMore(ws(',') ~ Pair_)
  }
  def Pair_ = rule { String_ ~ ws(':') ~ Value ~ push(MemberNode(pop(1).asInstanceOf[StringNode].text, pop())) }
  def Value: Rule = rule { String_ | Number_ | Object | Array_ | ws("true") | ws("false") | ws("null") }
  def String_ = rule { '"' ~ zeroOrMore(Char_) ~ ws('"') }
  def Number_ = rule { Int_ ~ ws(optional(Frac ~ optional(Exp))) }
  def Array_ = rule { ws('[') ~ Elements ~ ws(']') }
  def Elements = rule { Value ~ zeroOrMore(ws(',') ~ Value) }
  def Char_ = rule { EscapedChar | NormalChar }
  def EscapedChar = rule { '\\' ~ (anyOf("\"\\/bfnrt") | Unicode) }
  def NormalChar = rule { !anyOf("\"\\") ~ ANY }
  def Unicode = rule { 'u' ~ HexDigit ~ HexDigit ~ HexDigit ~ HexDigit }
  def Int_ = rule { optional('-') ~ (('1' -- '9') ~ Digits | Digit) }
  def Digits = rule { oneOrMore(Digit) }
  def Digit = rule { '0' -- '9' }
  def HexDigit = rule { '0' -- '9' | 'a' -- 'f' | 'A' -- 'Z' }
  def Frac = rule { '.' ~ Digits }
  def Exp = rule { ignoreCase('e') ~ optional(anyOf("+-")) }
  def WS = rule { zeroOrMore(anyOf(" \n\r\t\f")) }

  def ws(sub: Rule) = sub ~ WS

}