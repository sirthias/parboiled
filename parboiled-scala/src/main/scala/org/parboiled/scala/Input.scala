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

import org.parboiled.buffers.{InputBuffer, IndentDedentInputBuffer, DefaultInputBuffer}

/**
 * Simple Input abstraction serving as the target of a number of implicit conversions defined in the
 * org.parboiled.scala package object.
 */
class Input(val input: Array[Char], bufferCreator: (Array[Char] => InputBuffer) = new DefaultInputBuffer(_)) {
  lazy val inputBuffer: InputBuffer = bufferCreator(input)

  /**
   * Causes the input to be wrapped with a IndentDedentInputBuffer.
   * @param tabStop          the number of characters in a tab stop.
   * @param lineCommentStart the string starting a line comment or null, if line comments are not defined
   * @param strict           signals whether an IllegalIndentationException should be thrown on
   * "semi-dedents", if false the buffer silently accepts these
   */
  def transformIndents(tabStop: Int = 2, lineCommentStart: String = null, strict: Boolean = false): Input =
    new Input(input, new IndentDedentInputBuffer(_, tabStop, lineCommentStart, strict))
}
