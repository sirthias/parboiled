package org.parboiled.scala.parserunners

import org.parboiled.scala._

/**
 * The scala version of the org.parboiled.parserunners.ParseRunner.
 * Expects the parsing input as an "Input" object, which provides a number of implicit conversions from popular input
 * types.
 */
trait ParseRunner[V] {
  def run(input: Input): ParsingResult[V]
}