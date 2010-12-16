package org.parboiled.examples.calculators

import org.parboiled.scala._
import org.parboiled.errors.{ErrorUtils, ParsingException}

/**
 * A parser for a simple calculator language supporting the 4 basic calculation types on integers.
 * The actual calculations are performed by inline parser actions using the parsers value stack as temporary storage.
 */
class SimpleCalculator1 extends Parser {

  def InputLine = rule { Expression ~ EOI }

  def Expression: Rule1[Int] = rule {
    Term ~ zeroOrMore(
      "+" ~ Term ~~> ((a:Int, b) => a + b)
    | "-" ~ Term ~~> ((a:Int, b) => a - b)
    )
  }

  def Term = rule {
    Factor ~ zeroOrMore(
      "*" ~ Factor ~~> ((a:Int, b) => a * b)
    | "/" ~ Factor ~~> ((a:Int, b) => a / b)
    )
  }

  def Factor = rule { Number | Parens }

  def Parens = rule { "(" ~ Expression ~ ")" }

  def Number = rule { Digits ~> (_.toInt) }

  def Digits = rule { oneOrMore(Digit) }

  def Digit = rule { "0" - "9" }

  /**
   * The main parsing method. Uses a ReportingParseRunner (which only reports the first error) for simplicity.
   */
  def calculate(expression: String): Int = {
    val parsingResult = ReportingParseRunner(InputLine).run(expression)
    parsingResult.resultOption match {
      case Some(i) => i
      case None => throw new ParsingException("Invalid calculation expression:\n" +
              ErrorUtils.printParseErrors(parsingResult)) 
    }
  }
}