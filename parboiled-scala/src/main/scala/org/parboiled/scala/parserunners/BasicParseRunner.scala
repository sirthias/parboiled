package org.parboiled.scala.parserunners

import org.parboiled.parserunners.{BasicParseRunner => PBasicParseRunner}
import org.parboiled.scala._

/**
 * A simple wrapper for org.parboiled.parserunners.BasicParseRunner which returns a scala ParsingResult.
 * Note that the ParseRunner only accepts rules with zero or one value type parameter, as parsers leaving more
 * than one value on the value stack are considered to be bad style.
 */
object BasicParseRunner {
  def apply(rule: Rule0) = new BasicParseRunner[Nothing](new PBasicParseRunner[Nothing](rule))

  def apply[V](rule: Rule1[V]) = new BasicParseRunner[V](new PBasicParseRunner[V](rule))
}

class BasicParseRunner[V](val inner: PBasicParseRunner[V]) extends ParseRunner[V] {
  def run(input: Input): ParsingResult[V] = ParsingResult(inner.run(input.inputBuffer))
}