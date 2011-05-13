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
import java.lang.String
import Rule._
import org.parboiled.support.IndexRange

/**
 * A rule which does not affect the parsers value stack.
 */
class Rule0(val matcher: Matcher) extends Rule {
  def ~[X, Y, Z](other: PopRule3[X, Y, Z]) = new PopRule3[X, Y, Z](append(other))
  def ~[Y, Z](other: PopRule2[Y, Z]) = new PopRule2[Y, Z](append(other))
  def ~[Z](other: PopRule1[Z]) = new PopRule1[Z](append(other))
  def ~[X, Y, Z, R](other: ReductionRule3[X, Y, Z, R]) = new ReductionRule3[X, Y, Z, R](append(other))
  def ~[Y, Z, R](other: ReductionRule2[Y, Z, R]) = new ReductionRule2[Y, Z, R](append(other))
  def ~[Z, R](other: ReductionRule1[Z, R]) = new ReductionRule1[Z, R](append(other))
  def ~[A](other: Rule1[A]) = new Rule1[A](append(other))
  def ~[A, B](other: Rule2[A, B]) = new Rule2[A, B](append(other))
  def ~[A, B, C](other: Rule3[A, B, C]) = new Rule3[A, B, C](append(other))
  def ~[A, B, C, D](other: Rule4[A, B, C, D]) = new Rule4[A, B, C, D](append(other))
  def ~[A, B, C, D, E](other: Rule5[A, B, C, D, E]) = new Rule5[A, B, C, D, E](append(other))
  def ~[A, B, C, D, E, F](other: Rule6[A, B, C, D, E, F]) = new Rule6[A, B, C, D, E, F](append(other))
  def ~[A, B, C, D, E, F, G](other: Rule7[A, B, C, D, E, F, G]) = new Rule7[A, B, C, D, E, F, G](append(other))
  def ~:>[R](f: Char => R) = new Rule1[R](append(push(exec(GetMatchedChar, f))))
  def ~>>[R](f: IndexRange => R) = new Rule1[R](append(push(exec(GetMatchRange, f))))
  def ~>[R](f: String => R) = new Rule1[R](append(push(exec(GetMatch, f))))
  def ~~>[Z, R](f: Z => R) = new ReductionRule1[Z, R](append(push(exec(stack1(Pop), f))))
  def ~~>[Y, Z, R](f: (Y, Z) => R) = new ReductionRule2[Y, Z, R](append(push(exec(stack2(Pop), f))))
  def ~~>[X, Y, Z, R](f: (X, Y, Z) => R) = new ReductionRule3[X, Y, Z, R](append(push(exec(stack3(Pop), f))))
  def ~~?[Z](f: Z => Boolean) = new PopRule1(append(exec(stack1(Pop), f)))
  def ~~?[Y, Z](f: (Y, Z) => Boolean) = new PopRule2[Y, Z](append(exec(stack2(Pop), f)))
  def ~~?[X, Y, Z](f: (X, Y, Z) => Boolean) = new PopRule3[X, Y, Z](append(exec(stack3(Pop), f)))
  def ~~%[Z](f: Z => Unit) = new PopRule1[Z](append(ok(exec(stack1(Pop), f))))
  def ~~%[Y, Z](f: (Y, Z) => Unit) = new PopRule2[Y, Z](append(ok(exec(stack2(Pop), f))))
  def ~~%[X, Y, Z](f: (X, Y, Z) => Unit) = new PopRule3[X, Y, Z](append(ok(exec(stack3(Pop), f))))
  def |(other: Rule0) = new Rule0(appendChoice(other))
  def -(upperBound: String): Rule0 = throw new IllegalArgumentException("char range operator '-' only allowed on single character strings")
  protected def withMatcher(matcher: Matcher) = new Rule0(matcher).asInstanceOf[this.type]
}

object Rule0 {
  implicit def toRule(rule: Rule0): org.parboiled.Rule = rule.matcher
}

