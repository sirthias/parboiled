package org.parboiled.scala

import org.parboiled.{BasicParseRunner => PBasicParseRunner}
import org.parboiled.support.InputBuffer

/**
 * A simple wrapper for org.parboiled.BasicParseRunner which returns a scala ParsingResult.
 * Note that the ParseRunner only accepts rules with zero or one value type parameter, as parsers leaving more
 * than one value on the value stack are considered to be bad style.
 */
object BasicParseRunner {
  def apply(rule: Rule0) = new BasicParseRunner[Nothing](new PBasicParseRunner[Nothing](rule))

  def apply[V](rule: Rule1[V]) = new BasicParseRunner[V](new PBasicParseRunner[V](rule))
}

class BasicParseRunner[V](val inner: PBasicParseRunner[V]) {
  def run(input: Input) = ParsingResult(inner.run(input.inputBuffer))
}