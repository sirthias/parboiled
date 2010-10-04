package org.parboiled.scala

import org.parboiled.Node
import org.parboiled.support.ValueStack
import org.parboiled.errors.ParseError
import org.parboiled.support.{ParsingResult => PParsingResult}
import annotation.unchecked.uncheckedVariance
import org.parboiled.buffers.InputBuffer

object ParsingResult {
  def apply[V](result: PParsingResult[V])  = new ParsingResult[V](result)

  implicit def unwrap[V](result: ParsingResult[V]): PParsingResult[V] = result.inner
}

/**
 * The scala wrapper for the org.parboiled.support.ParsingResult class.
 */
class ParsingResult[+V](val inner: PParsingResult[V] @uncheckedVariance) {
  val matched: Boolean = inner.matched
  val result: V = inner.resultValue
  val resultOption: Option[V] = if (inner.resultValue != null) Some(inner.resultValue) else None
  val parseErrors: List[ParseError] = List(inner.parseErrors.toArray(new Array[ParseError](inner.parseErrors.size)): _*)
  val parseTreeRoot: Node[V] @uncheckedVariance = inner.parseTreeRoot
  val valueStack: ValueStack[Any] = inner.valueStack.asInstanceOf[ValueStack[Any]]
  val inputBuffer: InputBuffer = inner.inputBuffer

  def hasErrors = !parseErrors.isEmpty

}