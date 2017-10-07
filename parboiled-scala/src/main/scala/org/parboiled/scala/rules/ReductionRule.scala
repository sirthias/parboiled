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
import Rule._

/**
 * The base class of all reduction rules, which take a certain number of input values and produce one output value.
 */
abstract class ReductionRule extends Rule

/**
 * A rule taking one value off the value stack and replacing it with another value.
 */
class ReductionRule1[-Z, +R](val matcher: Matcher) extends ReductionRule {
  def ~[X, Y](other: PopRule3[X, Y, R]) = new PopRule3[X, Y, Z](append(other))
  def ~[Y](other: PopRule2[Y, R]) = new PopRule2[Y, Z](append(other))
  def ~(other: PopRule1[R]) = new PopRule1[Z](append(other))
  def ~[X, Y, T](other: ReductionRule3[X, Y, R, T]) = new ReductionRule3[X, Y, Z, T](append(other))
  def ~[Y, T](other: ReductionRule2[Y, R, T]) = new ReductionRule2[Y, Z, T](append(other))
  def ~[T](other: ReductionRule1[R, T]) = new ReductionRule1[Z, T](append(other))
  def ~~>[X, Y, A](f: (X, Y, R) => A) = new ReductionRule3[X, Y, Z, A](append(push(exec(stack3(Pop), f))))
  def ~~>[Y, A](f: (Y, R) => A) = new ReductionRule2[Y, Z, A](append(push(exec(stack2(Pop), f))))
  def ~~>[A](f: R => A) = new ReductionRule1[Z, A](append(push(exec(stack1(Pop), f))))
  def ~~?[X, Y](f: (X, Y, R) => Boolean) = new PopRule3[X, Y, Z](append(exec(stack3(Pop), f)))
  def ~~?[Y](f: (Y, R) => Boolean) = new PopRule2[Y, Z](append(exec(stack2(Pop), f)))
  def ~~?(f: R => Boolean) = new PopRule1[Z](append(exec(stack1(Pop), f)))
  def ~~%[X, Y](f: (X, Y, R) => Unit) = new PopRule3[X, Y, Z](append(ok(exec(stack3(Pop), f))))
  def ~~%[Y](f: (Y, R) => Unit) = new PopRule2[Y, Z](append(ok(exec(stack2(Pop), f))))
  def ~~%(f: R => Unit) = new PopRule1[Z](append(ok(exec(stack1(Pop), f))))
  def ~~~?(f: R => Boolean) = withMatcher(append(exec(stack1(Peek), f)))
  def ~~~%(f: R => Unit) = withMatcher(append(ok(exec(stack1(Peek), f))))
  def |[ZZ <: Z, RR >: R](other: ReductionRule1[ZZ, RR]) = new ReductionRule1[ZZ, RR](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule1[Z, R](matcher).asInstanceOf[this.type]
}

/**
 * A rule taking two values off the value stack and replacing them with one other value.
 */
class ReductionRule2[-Y, -Z, +R](val matcher: Matcher) extends ReductionRule {
  def ~[X](other: PopRule2[X, R]) = new PopRule3[X, Y, Z](append(other))
  def ~(other: PopRule1[R]) = new PopRule2[Y, Z](append(other))
  def ~[X, T](other: ReductionRule2[X, R, T]) = new ReductionRule3[X, Y, Z, T](append(other))
  def ~[T](other: ReductionRule1[R, T]) = new ReductionRule2[Y, Z, T](append(other))
  def ~~>[X, A](f: (X, R) => A) = new ReductionRule3[X, Y, Z, A](append(push(exec(stack2(Pop), f))))
  def ~~>[A](f: R => A) = new ReductionRule2[Y, Z, A](append(push(exec(stack1(Pop), f))))
  def ~~?[X](f: (X, R) => Boolean) = new PopRule3[X, Y, Z](append(exec(stack2(Pop), f)))
  def ~~?(f: R => Boolean) = new PopRule2[Y, Z](append(exec(stack1(Pop), f)))
  def ~~%[X](f: (X, R) => Unit) = new PopRule3[X, Y, Z](append(ok(exec(stack2(Pop), f))))
  def ~~%(f: R => Unit) = new PopRule2[Y, Z](append(ok(exec(stack1(Pop), f))))
  def ~~~?(f: R => Boolean) = withMatcher(append(exec(stack1(Peek), f)))
  def ~~~%(f: R => Unit) = withMatcher(append(ok(exec(stack1(Peek), f))))
  def |[YY <: Y, ZZ <: Z, RR >: R](other: ReductionRule2[YY, ZZ, RR]) = new ReductionRule2[YY, ZZ, RR](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule2[Y, Z, R](matcher).asInstanceOf[this.type]
}

/**
 * A rule taking three values off the value stack and replacing them with one other value.
 */
class ReductionRule3[-X, -Y, -Z, +R](val matcher: Matcher) extends ReductionRule {
  def ~(other: PopRule1[R]) = new PopRule3[X, Y, Z](append(other))
  def ~[T](other: ReductionRule1[R, T]) = new ReductionRule3[X, Y, Z, T](append(other))
  def ~~>[A](f: R => A) = new ReductionRule3[X, Y, Z, A](append(push(exec(stack1(Pop), f))))
  def ~~?(f: R => Boolean) = new PopRule3[X, Y, Z](append(exec(stack1(Pop), f)))
  def ~~%(f: R => Unit) = new PopRule3[X, Y, Z](append(ok(exec(stack1(Pop), f))))
  def ~~~?(f: R => Boolean) = withMatcher(append(exec(stack1(Peek), f)))
  def ~~~%(f: R => Unit) = withMatcher(append(ok(exec(stack1(Peek), f))))
  def |[XX <: X, YY <: Y, ZZ <: Z, RR >: R](other: ReductionRule3[XX, YY, ZZ, RR]) = new ReductionRule3[XX, YY, ZZ, RR](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule3[X, Y, Z, R](matcher).asInstanceOf[this.type]
}
