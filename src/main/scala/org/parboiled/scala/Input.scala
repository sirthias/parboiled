package org.parboiled.scala

import org.parboiled.common.FileUtils
import io.{Codec, Source}
import java.io.InputStream
import org.parboiled.buffers.DefaultInputBuffer

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
class Input(input: Array[Char]) {
  val inputBuffer = new DefaultInputBuffer(input)
}