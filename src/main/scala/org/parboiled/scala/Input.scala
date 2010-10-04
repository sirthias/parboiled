package org.parboiled.scala

import org.parboiled.buffers.{InputBuffer, IndentDedentInputBuffer, DefaultInputBuffer}
import org.parboiled.support.Chars
import annotation.tailrec

/**
 * Simple Input abstraction serving as the target of a number of implicit conversions defined in the
 * org.parboiled.scala package object.
 */
class Input(val input: Array[Char], bufferCreator: (Array[Char] => InputBuffer) = new DefaultInputBuffer(_)) {
  def inputBuffer: InputBuffer = bufferCreator(input)

  /**
   * Causes the input to be wrapped with a IndentDedentInputBuffer.
   */
  def transformIndents(tabStop: Int = 2): Input = new Input(input, new IndentDedentInputBuffer(_, tabStop))

  def collectContents: String = contents(inputBuffer, new StringBuilder, 0).toString

  @tailrec
  private def contents(buf: InputBuffer, sb: StringBuilder, ix: Int): StringBuilder =
    buf.charAt(ix) match {
      case Chars.EOI => sb
      case Chars.INDENT => { sb.append('→'); contents(buf, sb, ix + 1) }
      case Chars.DEDENT => { sb.append('←'); contents(buf, sb, ix + 1) }
      case c => { sb.append(c); contents(buf, sb, ix + 1) }
    }
}
