package org.parboiled.scala

import org.parboiled.Scala._

class SimpleParser extends Parser[Int] {

  def InputLine = rule {
    Expression ~ EOI
  }

  def Expression:Rule = rule {
    var a:Int = 0
    Term ~ withValue(a = _) ~
            zeroOrMore(
              '+' ~ Term ~ withValue(a + _) |
              '-' ~ Term ~ withValue(a - _)
            )
  }

  def Term = rule {
    var a:Int = 0
    Factor ~ withValue(a = _) ~
            zeroOrMore(
              '*' ~ Factor ~ withValue(a * _) |
              '/' ~ Factor ~ withValue(a / _)
            )
  }

  def Factor = rule {
    Number | Parens
  }

  def Parens = rule {
    '(' ~ Expression ~ ')'
  }

  def Number = rule {    
    Digits ~ withMatch(_.toInt)
  }

  def Digits = rule {
    oneOrMore(Digit)
  }

  def Digit = rule {
    '0' -- '9'
  }
}