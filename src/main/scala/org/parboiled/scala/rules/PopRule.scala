package org.parboiled.scala.rules

import org.parboiled.matchers._
/**
 * The base class of all rules simply removing a certain number of elements off the top of the value stack.
 */
abstract class PopRule(matcher: Matcher) extends Rule(matcher)

/**
 * A rule removing the top value stack element with a given type.
 */
class PopRule1[Z](matcher: Matcher) extends PopRule(matcher) {
  def ~[X, Y](other: PopRule2[X, Y]) = new PopRule3[X, Y, Z](append(other))
  def ~[Y](other: PopRule1[Y]) = new PopRule2[Y, Z](append(other))
  def ~(other: PopRuleN2) = new PopRuleN3(append(other))
  def ~(other: PopRuleN1) = new PopRuleN2(append(other))
  def ~(other: Rule0) = new PopRule1[Z](append(other))
  def ~[A](other: Rule1[A]) = new ReductionRule1[Z, A](append(other))
  def |(other: PopRule1[Z]) = new PopRule1[Z](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule1[Z](matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top two value stack elements with given types.
 */
class PopRule2[Y, Z](matcher: Matcher) extends PopRule(matcher) {
  def ~[X](other: PopRule1[X]) = new PopRule3[X, Y, Z](append(other))
  def ~(other: PopRuleN1) = new PopRuleN3(append(other))
  def ~(other: Rule0) = new PopRule2[Y, Z](append(other))
  def ~[A](other: Rule1[A]) = new ReductionRule2[Y, Z, A](append(other))
  def |(other: PopRule2[Y, Z]) = new PopRule2[Y, Z](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule2[Y, Z](matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top three value stack elements with given types.
 */
class PopRule3[X, Y, Z](matcher: Matcher) extends PopRule(matcher) {
  def ~(other: Rule0) = new PopRule3[X, Y, Z](append(other))
  def ~[A](other: Rule1[A]) = new ReductionRule3[X, Y, Z, A](append(other))
  def |(other: PopRule3[X, Y, Z]) = new PopRule3[X, Y, Z](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule3[X, Y, Z](matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top value stack element independently of its type.
 */
class PopRuleN1(matcher: Matcher) extends PopRule(matcher) {
  def ~(other: PopRuleN2) = new PopRuleN3(append(other))
  def ~(other: PopRuleN1) = new PopRuleN2(append(other))
  def ~(other: Rule0) = new PopRuleN1(append(other))
  def |(other: PopRuleN1) = new PopRuleN1(appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRuleN1(matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top two value stack elements independently of their type.
 */
class PopRuleN2(matcher: Matcher) extends PopRule(matcher) {
  def ~(other: PopRuleN1) = new PopRuleN3(append(other))
  def ~(other: Rule0) = new PopRuleN2(append(other))
  def |(other: PopRuleN2) = new PopRuleN2(appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRuleN2(matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top three value stack elements independently of their type.
 */
class PopRuleN3(matcher: Matcher) extends PopRule(matcher) {
  def ~(other: Rule0) = new PopRuleN3(append(other))
  def |(other: PopRuleN3) = new PopRuleN3(appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRuleN3(matcher).asInstanceOf[this.type]
}