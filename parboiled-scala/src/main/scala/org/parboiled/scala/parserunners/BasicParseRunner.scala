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
package parserunners

import org.parboiled.parserunners.{BasicParseRunner => PBasicParseRunner}

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