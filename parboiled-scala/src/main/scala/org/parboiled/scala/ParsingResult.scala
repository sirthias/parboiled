/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.scala

import org.parboiled.Node
import org.parboiled.support.ValueStack
import org.parboiled.errors.ParseError
import org.parboiled.support.{ParsingResult => PParsingResult}
import annotation.unchecked.uncheckedVariance
import org.parboiled.buffers.InputBuffer
import collection.JavaConversions._

object ParsingResult {
  def apply[V](result: PParsingResult[V])  = new ParsingResult[V](result)

  implicit def unwrap[V](result: ParsingResult[V]): PParsingResult[V] = result.inner
}

/**
 * The scala wrapper for the org.parboiled.support.ParsingResult class.
 */
class ParsingResult[+V](val inner: PParsingResult[V] @uncheckedVariance) {
  val matched: Boolean = inner.matched
  val result: Option[V] = Option(inner.resultValue)
  val parseErrors: List[ParseError] = inner.parseErrors.toList
  val parseTreeRoot: Node[V] @uncheckedVariance = inner.parseTreeRoot
  val valueStack: ValueStack[Any] = inner.valueStack.asInstanceOf[ValueStack[Any]]
  val inputBuffer: InputBuffer = inner.inputBuffer
}