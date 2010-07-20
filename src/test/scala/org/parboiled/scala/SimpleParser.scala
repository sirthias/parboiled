package org.parboiled.scala

import org.parboiled.Scala._

class SimpleParser extends Parser {

  def InputLine = rule {
    Expression ~ EOI
  }

  def Expression:Rule[Int] = rule {
    var a:Int = 0
    Term ~ (a = _) ~
            zeroOrMore(
              '+' ~ Term ~ setFromValue[Int, Int](a + _) |
              '-' ~ Term ~ convertValue[Int](a - _)
            )
  }

  def Term = rule {
    var a:Int = 0
    Factor ~ ((i:Val[Int]) => a = i.value) ~
            zeroOrMore(
              '*' ~ Factor ~ ((i:Val[Int]) => Val(a * i.value)) |
              '/' ~ Factor ~ ((i:Val[Int]) => Val(a / i.value))
            )
  }

  def Factor = rule {
    Number | Parens
  }

  def Parens = rule {
    '(' ~ Expression ~ ')'
  }

  def Number = rule {    
    Digits ~ setFromMatch(_.toInt)
  }

  def Digits = rule {
    oneOrMore(Digit)
  }

  def Digit = rule {
    '0' -- '9'
  }
}