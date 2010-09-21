package org.parboiled.scala.parserunners

import org.parboiled.parserunners.{RecoveringParseRunner => PRecoveringParseRunner}
import org.parboiled.scala._

/**
 * A simple wrapper for org.parboiled.parserunners.RecoveringParseRunner which returns a scala ParsingResult.
 * Note that the ParseRunner only accepts rules with zero or one value type parameter, as parsers leaving more
 * than one value on the value stack are considered to be bad style.
 */
object RecoveringParseRunner {
  def apply(rule: Rule0) = new RecoveringParseRunner[Nothing](new PRecoveringParseRunner[Nothing](rule))

  def apply[V](rule: Rule1[V]) = new RecoveringParseRunner[V](new PRecoveringParseRunner[V](rule))
}

class RecoveringParseRunner[V](val inner: PRecoveringParseRunner[V]) extends ParseRunner[V] {
  def run(input: Input): ParsingResult[V] = ParsingResult(inner.run(input.inputBuffer))
}