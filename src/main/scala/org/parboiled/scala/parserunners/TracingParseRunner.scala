package org.parboiled.scala.parserunners

import org.parboiled.parserunners.{TracingParseRunner => PTracingParseRunner}
import org.parboiled.scala._

/**
 * A simple wrapper for org.parboiled.parserunners.TracingParseRunner which returns a scala ParsingResult.
 * Note that the ParseRunner only accepts rules with zero or one value type parameter, as parsers leaving more
 * than one value on the value stack are considered to be bad style.
 */
object TracingParseRunner {
  def apply(rule: Rule0) = new TracingParseRunner[Nothing](new PTracingParseRunner[Nothing](rule))

  def apply[V](rule: Rule1[V]) = new TracingParseRunner[V](new PTracingParseRunner[V](rule))
}

class TracingParseRunner[V](val inner: PTracingParseRunner[V]) extends ParseRunner[V] {
  def run(input: Input): ParsingResult[V] = ParsingResult(inner.run(input.inputBuffer))

  def traceLog: String = inner.getLog
}