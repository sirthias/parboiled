package org.parboiled.scala

import org.parboiled.Scala._

class SimpleParser extends Parser {

  def InputLine = rule {
    Expression ~ EOI
  }

  def Expression:Rule[Int] = rule {
    var a:Int = 0
    Term ~ withValue(a = _:Int) ~
            zeroOrMore(
              '+' ~ Term ~ convertValue[Int](a + _) |
              '-' ~ Term ~ convertValue[Int](a - _)
            )
  }

  def Term = rule {
    var a:Int = 0
    Factor ~ withValue(a = _:Int) ~
            zeroOrMore(
              '*' ~ Factor ~ convertValue[Int](a * _) |
              '/' ~ Factor ~ convertValue[Int](a / _)
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