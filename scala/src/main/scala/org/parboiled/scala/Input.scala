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
   */
  def transformIndents(tabStop: Int = 2, lineCommentStart: String = null): Input =
    new Input(input, new IndentDedentInputBuffer(_, tabStop, lineCommentStart))
}
