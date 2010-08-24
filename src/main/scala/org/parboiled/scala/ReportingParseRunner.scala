package org.parboiled.scala

import org.parboiled.{ReportingParseRunner => PReportingParseRunner}
import org.parboiled.support.InputBuffer

/**
 * A simple wrapper for org.parboiled.ReportingParseRunner which returns a scala ParsingResult.
 * Note that the ParseRunner only accepts rules with zero or one value type parameter, as parsers leaving more
 * than one value on the value stack are considered to be bad style.
 */
object ReportingParseRunner {
  def apply(rule: Rule0) = new ReportingParseRunner[Nothing](new PReportingParseRunner[Nothing](rule))

  def apply[V](rule: Rule1[V]) = new ReportingParseRunner[V](new PReportingParseRunner[V](rule))
}

class ReportingParseRunner[V](val inner: PReportingParseRunner[V]) {
  def run(input: Input) = ParsingResult(inner.run(input.inputBuffer))
}