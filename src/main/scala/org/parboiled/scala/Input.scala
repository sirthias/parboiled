package org.parboiled.scala

import org.parboiled.common.FileUtils
import io.{Codec, Source}
import java.io.InputStream
import org.parboiled.buffers.{InputBuffer, IndentDedentInputBuffer, DefaultInputBuffer}

object Input {
  implicit def fromCharArray(input: Array[Char]) = new Input(input)

  implicit def fromString(input: String) = new Input(input.toCharArray)

  implicit def fromSource(input: Source) = new Input(input.toArray[Char])

  implicit def fromInputStream(input: InputStream)(implicit codec: Codec) =
    new Input(FileUtils.readAllChars(input, codec.charSet))
}

/**
 * Simple wrapper around the default InputBuffer implementation to provide a place for the implicit conversions
 * defined in the companion object.
 */
class Input(val input: Array[Char], bufferCreator: (Array[Char] => InputBuffer) = new DefaultInputBuffer(_)) {
  def inputBuffer: InputBuffer = bufferCreator(input)

  def transformIndents(tabStop: Int = 2): Input = new Input(input, new IndentDedentInputBuffer(_, tabStop))
}
