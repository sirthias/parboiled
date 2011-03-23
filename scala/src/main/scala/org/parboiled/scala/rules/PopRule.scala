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

package org.parboiled.scala.rules

import org.parboiled.matchers._
/**
 * The base class of all rules simply removing a certain number of elements off the top of the value stack.
 */
abstract class PopRule extends Rule

/**
 * A rule removing the top value stack element with a given type.
 */
class PopRule1[-Z](val matcher: Matcher) extends PopRule {
  def ~[X, Y](other: PopRule2[X, Y]) = new PopRule3[X, Y, Z](append(other))
  def ~[Y](other: PopRule1[Y]) = new PopRule2[Y, Z](append(other))
  def ~[A](other: Rule1[A]) = new ReductionRule1[Z, A](append(other))
  def |[ZZ <: Z](other: PopRule1[ZZ]) = new PopRule1[ZZ](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule1[Z](matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top two value stack elements with given types.
 */
class PopRule2[-Y, -Z](val matcher: Matcher) extends PopRule {
  def ~[X](other: PopRule1[X]) = new PopRule3[X, Y, Z](append(other))
  def ~[A](other: Rule1[A]) = new ReductionRule2[Y, Z, A](append(other))
  def |[YY <: Y, ZZ <: Z](other: PopRule2[YY, ZZ]) = new PopRule2[YY, ZZ](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule2[Y, Z](matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top three value stack elements with given types.
 */
class PopRule3[-X, -Y, -Z](val matcher: Matcher) extends PopRule {
  def ~[A](other: Rule1[A]) = new ReductionRule3[X, Y, Z, A](append(other))
  def |[XX <: X, YY <: Y, ZZ <: Z](other: PopRule3[XX, YY, ZZ]) = new PopRule3[XX, YY, ZZ](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule3[X, Y, Z](matcher).asInstanceOf[this.type]
}