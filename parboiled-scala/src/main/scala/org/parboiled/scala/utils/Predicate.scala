/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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