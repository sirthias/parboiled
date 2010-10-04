package org.parboiled.examples.calculators

import org.parboiled.scala._

/**
 * A recognizer for a very simple calculator language supporting the 4 basic calculation types on integers.
 * This grammar does not contain any actions, it only serves for determinining whether a given input conforms to
 * the language. The SimpleCalculator1 adds the actual calculation actions.
 */
class SimpleCalculator0 extends Parser {

  def InputLine = rule { Expression ~ EOI }

  def Expression: Rule0 = rule { Term ~ zeroOrMore(anyOf("+-") ~ Term) }

  def Term = rule { Factor ~ zeroOrMore(anyOf("*/") ~ Factor) }

  def Factor = rule { Digits | Parens }

  def Parens = rule { "(" ~ Expression ~ ")" }

  def Digits = rule { oneOrMore(Digit) }

  def Digit = rule { "0" - "9" }
}