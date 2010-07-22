package org.parboiled.scala

import org.parboiled.Scala._

class SimpleParser extends Parser[Int] {
  def InputLine = rule {
    Expression ~ EOI
  }

  def Expression: Rule = rule {
    Term ~ zeroOrMore(
      '+' ~ Term ~ push(pop() + pop())
    | '-' ~ Term ~ push(pop(1) - pop())
    )
  }

  def Term = rule {
    Factor ~ zeroOrMore(
      '*' ~ Factor ~ push(pop() * pop())
    | '/' ~ Factor ~ push(pop(1) / pop())
    )
  }

  def Factor = rule {
    Number | Parens
  }

  def Parens = rule {
    '(' ~ Expression ~ ')'
  }

  def Number = rule {
    Digits ~ push(_.toInt)
  }

  def Digits = rule {
    oneOrMore(Digit)
  }

  def Digit = rule {
    '0' -- '9'
  }
}