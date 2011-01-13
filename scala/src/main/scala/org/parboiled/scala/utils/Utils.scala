package org.parboiled.scala.utils

object Utils {
  def avec[A, U](a: A)(f: A => U): A = { f(a); a }
}