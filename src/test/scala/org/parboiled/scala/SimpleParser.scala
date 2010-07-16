package org.parboiled.scala

import Parboiled._

class SimpleParser extends Parser {

  def InputLine = rule {
    Expression & EOI
  }

  def Expression:Rule[Int] = rule {
    Term & (anyOf("+-") & Term)*
  }

  def Term = rule {
    Factor & (anyOf("*-") & Factor)*
  }

  def Factor = rule {
    Number | Parens
  }

  def Parens = rule {
    '(' & Expression & ')'
  }

  def Number = rule {
    Digits
  }

  def Digits = rule {
    Digit+
  }

  def Digit = rule {
    '0' -- '9'
  }
}