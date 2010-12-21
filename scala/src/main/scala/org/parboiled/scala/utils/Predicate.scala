package org.parboiled.scala.utils

object Predicate {
  implicit def apply[A](f: A => Boolean): Predicate[A] = new Predicate[A] {
    def apply(a: A) = f(a)
  }
}

/**
 * A simple wrapper around predicate functions providing for basic composability.
 * Functions (A => Boolean) are automatically wrapped with this class if they "demand" to be ANDed, ORed or NOTted.
 */
abstract class Predicate[-A] extends (A => Boolean) {
  def apply(a: A): Boolean
  def &&[B <: A](other: Predicate[B]) = Predicate[B](a => apply(a) && other(a))
  def ||[B <: A](other: Predicate[B]) = Predicate[B](a => apply(a) && other(a))
  def unary_!(): Predicate[A] = Predicate[A](a => !apply(a))
}