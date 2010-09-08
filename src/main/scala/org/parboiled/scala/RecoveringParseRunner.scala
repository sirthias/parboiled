package org.parboiled.scala

import org.parboiled.{RecoveringParseRunner => PRecoveringParseRunner}

/**
 * A simple wrapper for org.parboiled.RecoveringParseRunner which returns a scala ParsingResult.
 * Note that the ParseRunner only accepts rules with zero or one value type parameter, as parsers leaving more
 * than one value on the value stack are considered to be bad style.
 */
object RecoveringParseRunner {
  def apply(rule: Rule0) = new RecoveringParseRunner[Nothing](new PRecoveringParseRunner[Nothing](rule))

  def apply[V](rule: Rule1[V]) = new RecoveringParseRunner[V](new PRecoveringParseRunner[V](rule))
}

class RecoveringParseRunner[V](val inner: PRecoveringParseRunner[V]) {
  def run(input: Input) = ParsingResult(inner.run(input.inputBuffer))
}