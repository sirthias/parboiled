package org.parboiled.scala.rules

import org.parboiled.matchers._
/**
 * The base class of all reduction rules, which take a certain number of input values and produce one output value.
 */
abstract class ReductionRule(matcher: Matcher) extends Rule(matcher)

/**
 * A rule taking one value off the value stack and replacing it with another value.
 */
class ReductionRule1[Z, R](matcher: Matcher) extends ReductionRule(matcher) {
  def ~[X, Y](other: PopRule3[X, Y, R]) = new PopRule3[X, Y, Z](append(other))
  def ~[Y](other: PopRule2[Y, R]) = new PopRule2[Y, Z](append(other))
  def ~(other: PopRule1[R]) = new PopRule1[Z](append(other))
  def ~(other: PopRuleN3) = new PopRuleN3(append(other))
  def ~(other: PopRuleN2) = new PopRuleN2(append(other))
  def ~(other: PopRuleN1) = new PopRule1[Z](append(other))
  def ~[X, Y, T](other: ReductionRule3[X, Y, R, T]) = new ReductionRule3[X, Y, Z, T](append(other))
  def ~[Y, T](other: ReductionRule2[Y, R, T]) = new ReductionRule2[Y, Z, T](append(other))
  def ~[T](other: ReductionRule1[R, T]) = new ReductionRule1[Z, T](append(other))
  def ~(other: Rule0) = new ReductionRule1[Z, R](append(other))
  def |(other: ReductionRule1[Z, R]) = new ReductionRule1[Z, R](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule1[Z, R](matcher).asInstanceOf[this.type]
}

/**
 * A rule taking two values off the value stack and replacing them with one other value.
 */
class ReductionRule2[Y, Z, R](matcher: Matcher) extends ReductionRule(matcher) {
  def ~[X](other: PopRule2[X, R]) = new PopRule3[X, Y, Z](append(other))
  def ~(other: PopRule1[R]) = new PopRule2[Y, Z](append(other))
  def ~(other: PopRuleN2) = new PopRuleN3(append(other))
  def ~(other: PopRuleN1) = new PopRuleN2(append(other))
  def ~[X, T](other: ReductionRule2[X, R, T]) = new ReductionRule3[X, Y, Z, T](append(other))
  def ~[T](other: ReductionRule1[R, T]) = new ReductionRule2[Y, Z, T](append(other))
  def ~(other: Rule0) = new ReductionRule2[Y, Z, R](append(other))
  def |(other: ReductionRule2[Y, Z, R]) = new ReductionRule2[Y, Z, R](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule2[Y, Z, R](matcher).asInstanceOf[this.type]
}

/**
 * A rule taking three values off the value stack and replacing them with one other value.
 */
class ReductionRule3[X, Y, Z, R](matcher: Matcher) extends ReductionRule(matcher) {
  def ~(other: PopRule1[R]) = new PopRule3[X, Y, Z](append(other))
  def ~(other: PopRuleN1) = new PopRuleN3(append(other))
  def ~[T](other: ReductionRule1[R, T]) = new ReductionRule3[X, Y, Z, T](append(other))
  def ~(other: Rule0) = new ReductionRule3[X, Y, Z, R](append(other))
  def |(other: ReductionRule3[X, Y, Z, R]) = new ReductionRule3[X, Y, Z, R](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule3[X, Y, Z, R](matcher).asInstanceOf[this.type]
}
