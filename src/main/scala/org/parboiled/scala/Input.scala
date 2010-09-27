package org.parboiled.scala

import org.parboiled.buffers.{InputBuffer, IndentDedentInputBuffer, DefaultInputBuffer}

/**
 * Simple wrapper around the default InputBuffer implementation to provide a place for the implicit conversions
 * defined in the companion object.
 */
class Input(val input: Array[Char], bufferCreator: (Array[Char] => InputBuffer) = new DefaultInputBuffer(_)) {
  def inputBuffer: InputBuffer = bufferCreator(input)

  def transformIndents(tabStop: Int = 2): Input = new Input(input, new IndentDedentInputBuffer(_, tabStop))
}
