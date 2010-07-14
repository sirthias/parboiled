package org.parboiled.scala

class SimpleParser extends Parser {

  def InputLine = rule {
    Expression & EOI
  }

  def Expression = rule {
    Term & (anyOf("+-") & Term) *
  }

  def Term = rule {
    Factor & (anyOf("*-") & Factor) *
  }

  def Factor = rule {
    Number | Parens
  }

  def Parens: Rule[Nothing] = rule {
    '(' & Expression & ')'
  }

  def Number = rule {
    Digits
  }

  def Digits = rule {
    Digit +
  }

  def Digit = rule {
    '0' -- '9'
  }
}