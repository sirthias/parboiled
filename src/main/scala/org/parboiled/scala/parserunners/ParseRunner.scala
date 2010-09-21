package org.parboiled.scala.parserunners

import org.parboiled.scala._

trait ParseRunner[V] {
  def run(input: Input): ParsingResult[V]
}