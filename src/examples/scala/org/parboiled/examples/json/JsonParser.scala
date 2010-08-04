package org.parboiled.examples.json

import org.parboiled.scala._

class JsonParser extends Parser {
  sealed abstract class AstNode
  case class ObjectNode(members: List[MemberNode]) extends AstNode
  case class MemberNode(key: String, value: AstNode) extends AstNode
  case class ArrayNode(elements: Array[AstNode]) extends AstNode
  case class StringNode(text: String) extends AstNode
  case class NumberNode(value: BigDecimal) extends AstNode
  case object True extends AstNode
  case object False extends AstNode
  case object Null extends AstNode

  def Object: Rule1[AstNode] = rule {
    WS ~ ws('{') ~ (Members | push(List.empty[MemberNode])) ~ ws('}') --> (ObjectNode(_))
  }

  def Members = rule {
    (Pair_ --> (List(_))) ~ zeroOrMore(ws(',') ~ Pair_ --> ((list: List[MemberNode], p) => p :: list))
  }

  def Pair_ = rule {
    String_ ~ ws(':') ~ Value --> ((k, v) => MemberNode(k.text, v))
  }

  def Value: Rule1[AstNode] = rule {
    String_ | Number_ | Object | Array_ | True_ | False_ | Null_
  }

  def String_ = rule {
    '"' ~ zeroOrMore(Char_) ~> (StringNode(_)) ~ ws('"')
  }

  def Number_ = rule {
    (Int_ ~ ws(optional(Frac ~ optional(Exp)))) ~> (s => NumberNode(BigDecimal(s)))
  }

  def Array_ = rule {
    ws('[') ~ Elements ~ ws(']') --> (l => ArrayNode(l.toArray))
  }

  def Elements = rule {
    (Value --> (List(_))) ~ zeroOrMore(ws(',') ~ Value --> ((list: List[AstNode], v) => v :: list))
  }

  def Char_ = rule {EscapedChar | NormalChar}

  def EscapedChar = rule {'\\' ~ (anyOf("\"\\/bfnrt") | Unicode)}

  def NormalChar = rule {!anyOf("\"\\") ~ ANY}

  def Unicode = rule {'u' ~ HexDigit ~ HexDigit ~ HexDigit ~ HexDigit}

  def Int_ = rule {optional('-') ~ (('1' -- '9') ~ Digits | Digit)}

  def Digits = rule {oneOrMore(Digit)}

  def Digit = rule {'0' -- '9'}

  def HexDigit = rule {'0' -- '9' | 'a' -- 'f' | 'A' -- 'Z'}

  def Frac = rule {'.' ~ Digits}

  def Exp = rule {ignoreCase('e') ~ optional(anyOf("+-"))}

  def True_ = ws("true") ~ push(True)

  def False_ = ws("false") ~ push(False)

  def Null_ = ws("null") ~ push(Null)

  def WS = rule {zeroOrMore(anyOf(" \n\r\t\f"))}

  def ws(sub: Rule0) = sub ~ WS

}