package org.parboiled.scala.utils

import org.parboiled.common.{Predicate => PPredicate}
import annotation.unchecked.uncheckedVariance

object Predicate {
  implicit def f2Predicate[A](f: A => Boolean): Predicate[A] = new Predicate[A](f)
}

/**
 * A simple wrapper around predicate functions providing for basic composability.
 * Functions (A => Boolean) are automatically wrapped with this class if they "demand" to be ANDed, ORed or NOTted.
 */
class Predicate[-A](f: A => Boolean) extends (A => Boolean) with PPredicate[A @uncheckedVariance] {
  def apply(a: A) = f(a)
  def &&[B <: A](other: Predicate[B]) = new Predicate[B](a => f(a) && other(a))
  def ||[B <: A](other: Predicate[B]) = new Predicate[B](a => f(a) && other(a))
  def unary_!(): Predicate[A] = new Predicate[A](a => !f(a))
}