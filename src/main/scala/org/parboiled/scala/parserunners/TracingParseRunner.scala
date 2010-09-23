package org.parboiled.scala.parserunners

import org.parboiled.parserunners.{TracingParseRunner => PTracingParseRunner}
import org.parboiled.scala._
import org.parboiled.Context
import utils.Predicate
/**
 * A wrapper for org.parboiled.parserunners.TracingParseRunner which returns a scala ParsingResult.
 * It provides for the ability to attach filter expressions for limiting the tracing printout to certain input and/or
 * grammar areas.
 * Note that the ParseRunner only accepts rules with zero or one value type parameter, as parsers leaving more
 * than one value on the value stack are considered to be bad style.
 */
object TracingParseRunner {
  def apply(rule: Rule0) = new TracingParseRunner[Nothing](new PTracingParseRunner[Nothing](rule))

  def apply[V](rule: Rule1[V]) = new TracingParseRunner[V](new PTracingParseRunner[V](rule))
}

class TracingParseRunner[V](val inner: PTracingParseRunner[V]) extends ParseRunner[V] {
  def run(input: Input): ParsingResult[V] = ParsingResult(inner.run(input.inputBuffer))

  /**
   * Returns a new TracingParseRunner with the attached filter predicate limiting the tracing printout.
   * Apart from providing a custom (Context[...] => Boolean) function you can also build a predicate using a small
   * DSL involving the builder methods provided in the Lines and Rules objects.
   * For example: "Lines(10 until 20) && Rules.below(parser.Factor)".
   */
  def filter(filter: Predicate[Context[_]]) =
    new TracingParseRunner[V](new PTracingParseRunner[V](inner.rootMatcher, filter))

  def traceLog: String = inner.getLog
}

object Lines {
  def from(firstLine: Int): Predicate[Context[_]] = new Predicate[Context[_]](
    c => c.getInputBuffer.getPosition(c.getCurrentIndex).line >= firstLine
  )

  def until(lastLine: Int): Predicate[Context[_]] = new Predicate[Context[_]](
    c => c.getInputBuffer.getPosition(c.getCurrentIndex).line <= lastLine
  )

  def apply(lineRange: Range): Predicate[Context[_]] = {
    require(lineRange.step == 1, "Ranges with step != 1 are not supported here")
    new Predicate[Context[_]](
      c => {
        val line = c.getInputBuffer.getPosition(c.getCurrentIndex).line
        lineRange.start <= line && line < lineRange.end
      }
    )
  }
}

object Rules {
  def only(rules: Rule*): Predicate[Context[_]] = {
    val matchers = rules.map(_.matcher)
    new Predicate[Context[_]](c => matchers.contains(c.getMatcher))
  }

  def below(rules: Rule*): Predicate[Context[_]] = {
    val matchers = rules.map(_.matcher)
    new Predicate[Context[_]]( c => {
      val path = c.getPath
      matchers.exists(m => (path.getHead ne m) && path.contains(m))
    })
  }

  def apply(rules: Rule*): Predicate[Context[_]] = {
    val matchers = rules.map(_.matcher)
    new Predicate[Context[_]]( c => {
      val path = c.getPath
      matchers.exists(path.contains)
    })
  }
}
