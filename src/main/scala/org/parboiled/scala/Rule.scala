package org.parboiled.scala

import org.parboiled.matchers._
import org.parboiled.common.StringUtils.escape
import annotation.unchecked.uncheckedVariance

abstract class Rule(val creator: MatcherCreator) {

  def toMatcher = creator.toMatcher

  def unary_!() = new Rule0(new UnaryCreator(creator, new TestNotMatcher(_).label("TestNot")))

  def withLabel(label: String): this.type = { creator.label = label; this}
  def withNodeSuppressed(): this.type = { creator.suppressNode = true; this}
  def withSubnodesSuppressed(): this.type = { creator.suppressSubnodes = true; this}
  def withNodeSkipped(): this.type = { creator.skipNode = true; this}

  override def toString = creator.toString + ": " + getClass.getSimpleName

  protected def append(other: Rule) = creator.appendSeq(other.creator)
}

abstract class ReductionRule(creator: MatcherCreator) extends Rule(creator)

class ReductionRule1[Z, R](creator: MatcherCreator) extends ReductionRule(creator) {
  def |(other: ReductionRule1[Z, R]) = new ReductionRule1[Z, R](creator.appendChoice(other.creator))
}

class ReductionRule2[Y, Z, R](creator: MatcherCreator) extends ReductionRule(creator) {
  def |(other: ReductionRule2[Y, Z, R]) = new ReductionRule2[Y, Z, R](creator.appendChoice(other.creator))
}

class ReductionRule3[X, Y, Z, R](creator: MatcherCreator) extends ReductionRule(creator) {
  def |(other: ReductionRule3[X, Y, Z, R]) = new ReductionRule3[X, Y, Z, R](creator.appendChoice(other.creator))
}

abstract class PopRule(creator: MatcherCreator) extends Rule(creator)

class PopRule1[Z](creator: MatcherCreator) extends PopRule(creator) {
  def |(other: PopRule1[Z]) = new PopRule1[Z](creator.appendChoice(other.creator))
}

class PopRule2[Y, Z](creator: MatcherCreator) extends PopRule(creator) {
  def |(other: PopRule2[Y, Z]) = new PopRule2[Y, Z](creator.appendChoice(other.creator))
}

class PopRule3[X, Y, Z](creator: MatcherCreator) extends PopRule(creator) {
  def |(other: PopRule3[X, Y, Z]) = new PopRule3[X, Y, Z](creator.appendChoice(other.creator))
}

class PopRuleN1(creator: MatcherCreator) extends PopRule(creator) {
  def |(other: PopRuleN1) = new PopRuleN1(creator.appendChoice(other.creator))
}

class PopRuleN2(creator: MatcherCreator) extends PopRule(creator) {
  def |(other: PopRuleN2) = new PopRuleN2(creator.appendChoice(other.creator))
}

class PopRuleN3(creator: MatcherCreator) extends PopRule(creator) {
  def |(other: PopRuleN3) = new PopRuleN3(creator.appendChoice(other.creator))
}

class Rule0(creator: MatcherCreator) extends Rule(creator) {
  def this(creator: => Matcher) = this(new SimpleCreator(() => creator))
  def ~[X, Y, Z](other: PopRule3[X, Y, Z]) = new PopRule3[X, Y, Z](append(other))
  def ~[Y, Z](other: PopRule2[Y, Z]) = new PopRule2[Y, Z](append(other))
  def ~[Z](other: PopRule1[Z]) = new PopRule1[Z](append(other))
  def ~(other: PopRuleN3) = new PopRuleN3(append(other))
  def ~(other: PopRuleN2) = new PopRuleN2(append(other))
  def ~(other: PopRuleN1) = new PopRuleN1(append(other))
  def ~[X, Y, Z, R](other: ReductionRule3[X, Y, Z, R]) = new ReductionRule3[X, Y, Z, R](append(other))
  def ~[Y, Z, R](other: ReductionRule2[Y, Z, R]) = new ReductionRule2[Y, Z, R](append(other))
  def ~[Z, R](other: ReductionRule1[Z, R]) = new ReductionRule1[Z, R](append(other))
  def ~(other: Rule0) = new Rule0(append(other))
  def ~[A](other: Rule1[A]) = new Rule1[A](append(other))
  def ~[A, B](other: Rule2[A, B]) = new Rule2[A, B](append(other))
  def ~[A, B, C](other: Rule3[A, B, C]) = new Rule3[A, B, C](append(other))
  def ~[A, B, C, D](other: Rule4[A, B, C, D]) = new Rule4[A, B, C, D](append(other))
  def ~[A, B, C, D, E](other: Rule5[A, B, C, D, E]) = new Rule5[A, B, C, D, E](append(other))
  def ~[A, B, C, D, E, F](other: Rule6[A, B, C, D, E, F]) = new Rule6[A, B, C, D, E, F](append(other))
  def ~[A, B, C, D, E, F, G](other: Rule7[A, B, C, D, E, F, G]) = new Rule7[A, B, C, D, E, F, G](append(other))
  def -->[Z, R](f: Z => R) = new ReductionRule1[Z, R](creator.appendSeq1(f))
  def -->[Y, Z, R](f: (Y, Z) => R) = new ReductionRule2[Y, Z, R](creator.appendSeq2(f))
  def -->[X, Y, Z, R](f: (X, Y, Z) => R) = new ReductionRule3[X, Y, Z, R](creator.appendSeq3(f))
  def ~>[R](f: String => R) = new Rule1[R](creator.appendSeqS(f))
  def |(other: Rule0) = new Rule0(creator.appendChoice(other.creator))
}

sealed abstract class PushRule(creator: MatcherCreator) extends Rule(creator)

class Rule1[+A](creator: MatcherCreator) extends PushRule(creator: MatcherCreator) {
  def ~[Y, Z, AA >: A](other: PopRule3[Y, Z, AA]) = new PopRule2[Y, Z](append(other))
  def ~[Z, AA >: A](other: PopRule2[Z, AA]) = new PopRule1[Z](append(other))
  def ~[AA >: A](other: PopRule1[AA]) = new Rule0(append(other))
  def ~(other: PopRuleN3) = new PopRuleN2(append(other))
  def ~(other: PopRuleN2) = new PopRuleN1(append(other))
  def ~(other: PopRuleN1) = new Rule0(append(other))
  def ~[X, Y, AA >: A, R](other: ReductionRule3[X, Y, AA, R]) = new ReductionRule2[X, Y, R](append(other))
  def ~[Y, AA >: A, R](other: ReductionRule2[Y, AA, R]) = new ReductionRule1[Y, R](append(other))
  def ~[AA >: A, R](other: ReductionRule1[AA, R]) = new Rule1[R](append(other))
  def ~(other: Rule0) = new Rule1[A](append(other))
  def ~[B](other: Rule1[B]): Rule2[A, B] @uncheckedVariance = new Rule2[A, B](append(other))
  def ~[B, C](other: Rule2[B, C]): Rule3[A, B, C] @uncheckedVariance = new Rule3[A, B, C](append(other))
  def ~[B, C, D](other: Rule3[B, C, D]): Rule4[A, B, C, D] @uncheckedVariance = new Rule4[A, B, C, D](append(other))
  def ~[B, C, D, E](other: Rule4[B, C, D, E]): Rule5[A, B, C, D, E] @uncheckedVariance = new Rule5[A, B, C, D, E](append(other))
  def ~[B, C, D, E, F](other: Rule5[B, C, D, E, F]): Rule6[A, B, C, D, E, F] @uncheckedVariance = new Rule6[A, B, C, D, E, F](append(other))
  def ~[B, C, D, E, F, G](other: Rule6[B, C, D, E, F, G]): Rule7[A, B, C, D, E, F, G] @uncheckedVariance = new Rule7[A, B, C, D, E, F, G](append(other))
  def -->[R](f: A => R) = new Rule1[R](creator.appendSeq1(f))
  def -->[Z, R](f: (Z, A) => R) = new ReductionRule1[Z, R](creator.appendSeq2(f))
  def -->[Y, Z, R](f: (Y, Z, A) => R) = new ReductionRule2[Y, Z, R](creator.appendSeq3(f))
  def -->[X, Y, Z, R](f: (X, Y, Z, A) => R) = new ReductionRule3[X, Y, Z, R](creator.appendSeq4(f))
  def ~>[R](f: String => R): Rule2[A, R] @uncheckedVariance = new Rule2[A, R](creator.appendSeqS(f))
  def |[AA >: A](other: Rule1[AA]) = new Rule1[AA](creator.appendChoice(other.creator))
}

class Rule2[+A, +B](creator: MatcherCreator) extends PushRule(creator: MatcherCreator) {
  def ~[Z](other: PopRule3[Z, A, B] @uncheckedVariance) = new PopRule1[Z](append(other))
  def ~(other: PopRule2[A, B] @uncheckedVariance) = new Rule0(append(other))
  def ~(other: PopRule1[B] @uncheckedVariance) = new Rule1[A](append(other))
  def ~(other: PopRuleN3) = new PopRuleN1(append(other))
  def ~(other: PopRuleN2) = new Rule0(append(other))
  def ~(other: PopRuleN1) = new Rule1[A](append(other))
  def ~[X, R](other: ReductionRule3[X, A, B, R] @uncheckedVariance) = new ReductionRule1[X, R](append(other))
  def ~[R](other: ReductionRule2[A, B, R] @uncheckedVariance) = new Rule1[R](append(other))
  def ~[R](other: ReductionRule1[B, R] @uncheckedVariance) = new Rule2[A, R](append(other))
  def ~(other: Rule0) = new Rule2[A, B](append(other))
  def ~[C](other: Rule1[C]): Rule3[A, B, C] @uncheckedVariance = new Rule3[A, B, C](append(other))
  def ~[C, D](other: Rule2[C, D]): Rule4[A, B, C, D] @uncheckedVariance = new Rule4[A, B, C, D](append(other))
  def ~[C, D, E](other: Rule3[C, D, E]): Rule5[A, B, C, D, E] @uncheckedVariance = new Rule5[A, B, C, D, E](append(other))
  def ~[C, D, E, F](other: Rule4[C, D, E, F]): Rule6[A, B, C, D, E, F] @uncheckedVariance = new Rule6[A, B, C, D, E, F](append(other))
  def ~[C, D, E, F, G](other: Rule5[C, D, E, F, G]): Rule7[A, B, C, D, E, F, G] @uncheckedVariance = new Rule7[A, B, C, D, E, F, G](append(other))
  def -->[R](f: B => R) = new Rule2[A, R](creator.appendSeq1(f))
  def -->[R](f: (A, B) => R) = new Rule1[R](creator.appendSeq2(f))
  def -->[Z, R](f: (Z, A, B) => R) = new ReductionRule1[Z, R](creator.appendSeq3(f))
  def -->[Y, Z, R](f: (Y, Z, A, B) => R) = new ReductionRule2[Y, Z, R](creator.appendSeq4(f))
  def -->[X, Y, Z, R](f: (X, Y, Z, A, B) => R) = new ReductionRule3[X, Y, Z, R](creator.appendSeq5(f))
  def ~>[R](f: String => R): Rule3[A, B, R] @uncheckedVariance = new Rule3[A, B, R](creator.appendSeqS(f))
  def |[AA >: A, BB >: B](other: Rule2[AA, BB]) = new Rule2[AA, BB](creator.appendChoice(other.creator))
}

class Rule3[+A, +B, +C](creator: MatcherCreator) extends PushRule(creator: MatcherCreator) {
  def ~(other: PopRule3[A, B, C] @uncheckedVariance) = new Rule0(append(other))
  def ~(other: PopRule2[B, C] @uncheckedVariance) = new Rule1[A](append(other))
  def ~(other: PopRule1[C] @uncheckedVariance) = new Rule2[A, B](append(other))
  def ~(other: PopRuleN3) = new Rule0(append(other))
  def ~(other: PopRuleN2) = new Rule1[A](append(other))
  def ~(other: PopRuleN1) = new Rule2[A, B](append(other))
  def ~[R](other: ReductionRule3[A, B, C, R] @uncheckedVariance) = new Rule1[R](append(other))
  def ~[R](other: ReductionRule2[B, C, R] @uncheckedVariance) = new Rule2[A, R](append(other))
  def ~[R](other: ReductionRule1[C, R] @uncheckedVariance) = new Rule3[A, B, R](append(other))
  def ~(other: Rule0): Rule3[A, B, C] @uncheckedVariance = new Rule3[A, B, C](append(other))
  def ~[D](other: Rule1[D]): Rule4[A, B, C, D] @uncheckedVariance = new Rule4[A, B, C, D](append(other))
  def ~[D, E](other: Rule2[D, E]): Rule5[A, B, C, D, E] @uncheckedVariance = new Rule5[A, B, C, D, E](append(other))
  def ~[D, E, F](other: Rule3[D, E, F]): Rule6[A, B, C, D, E, F] @uncheckedVariance = new Rule6[A, B, C, D, E, F](append(other))
  def ~[D, E, F, G](other: Rule4[D, E, F, G]): Rule7[A, B, C, D, E, F, G] @uncheckedVariance = new Rule7[A, B, C, D, E, F, G](append(other))
  def -->[R](f: C => R) = new Rule3[A, B, R](creator.appendSeq1(f))
  def -->[R](f: (B, C) => R) = new Rule2[A, R](creator.appendSeq2(f))
  def -->[R](f: (A, B, C) => R) = new Rule1[R](creator.appendSeq3(f))
  def -->[Z, R](f: (Z, A, B, C) => R) = new ReductionRule1[Z, R](creator.appendSeq4(f))
  def -->[Y, Z, R](f: (Y, Z, A, B, C) => R) = new ReductionRule2[Y, Z, R](creator.appendSeq5(f))
  def -->[X, Y, Z, R](f: (X, Y, Z, A, B, C) => R) = new ReductionRule3[X, Y, Z, R](creator.appendSeq6(f))
  def ~>[R](f: String => R): Rule4[A, B, C, R] @uncheckedVariance = new Rule4[A, B, C, R](creator.appendSeqS(f))
  def |[AA >: A, BB >: B, CC >: C](other: Rule3[AA, BB, CC]) = new Rule3[AA, BB, CC](creator.appendChoice(other.creator))
}

class Rule4[A, B, C, D](creator: MatcherCreator) extends PushRule(creator: MatcherCreator) {
  def ~(other: PopRule3[B, C, D]) = new Rule1[A](append(other))
  def ~(other: PopRule2[C, D]) = new Rule2[A, B](append(other))
  def ~(other: PopRule1[D]) = new Rule3[A, B, C](append(other))
  def ~(other: PopRuleN3) = new Rule1[A](append(other))
  def ~(other: PopRuleN2) = new Rule2[A, B](append(other))
  def ~(other: PopRuleN1) = new Rule3[A, B, C](append(other))
  def ~[R](other: ReductionRule3[B, C, D, R]) = new Rule2[A, R](append(other))
  def ~[R](other: ReductionRule2[C, D, R]) = new Rule3[A, B, R](append(other))
  def ~[R](other: ReductionRule1[D, R]) = new Rule4[A, B, C, R](append(other))
  def ~(other: Rule0) = new Rule4[A, B, C, D](append(other))
  def ~[E](other: Rule1[E]) = new Rule5[A, B, C, D, E](append(other))
  def ~[E, F](other: Rule2[E, F]) = new Rule6[A, B, C, D, E, F](append(other))
  def ~[E, F, G](other: Rule3[E, F, G]) = new Rule7[A, B, C, D, E, F, G](append(other))
  def -->[R](f: D => R) = new Rule4[A, B, C, R](creator.appendSeq1(f))
  def -->[R](f: (C, D) => R) = new Rule3[A, B, R](creator.appendSeq2(f))
  def -->[R](f: (B, C, D) => R) = new Rule2[A, R](creator.appendSeq3(f))
  def -->[R](f: (A, B, C, D) => R) = new Rule1[R](creator.appendSeq4(f))
  def -->[Z, R](f: (Z, A, B, C, D) => R) = new ReductionRule1[Z, R](creator.appendSeq5(f))
  def -->[Y, Z, R](f: (Y, Z, A, B, C, D) => R) = new ReductionRule2[Y, Z, R](creator.appendSeq6(f))
  def -->[X, Y, Z, R](f: (X, Y, Z, A, B, C, D) => R) = new ReductionRule3[X, Y, Z, R](creator.appendSeq7(f))
  def ~>[R](f: String => R) = new Rule5[A, B, C, D, R](creator.appendSeqS(f))
  def |(other: Rule4[A, B, C, D]) = new Rule4[A, B, C, D](creator.appendChoice(other.creator))
}

class Rule5[A, B, C, D, E](creator: MatcherCreator) extends PushRule(creator: MatcherCreator) {
  def ~(other: PopRule3[C, D, E]) = new Rule2[A, B](append(other))
  def ~(other: PopRule2[D, E]) = new Rule3[A, B, C](append(other))
  def ~(other: PopRule1[E]) = new Rule4[A, B, C, D](append(other))
  def ~(other: PopRuleN3) = new Rule2[A, B](append(other))
  def ~(other: PopRuleN2) = new Rule3[A, B, C](append(other))
  def ~(other: PopRuleN1) = new Rule4[A, B, C, D](append(other))
  def ~[R](other: ReductionRule3[C, D, E, R]) = new Rule3[A, B, R](append(other))
  def ~[R](other: ReductionRule2[D, E, R]) = new Rule4[A, B, C, R](append(other))
  def ~[R](other: ReductionRule1[E, R]) = new Rule5[A, B, C, D, R](append(other))
  def ~(other: Rule0) = new Rule5[A, B, C, D, E](append(other))
  def ~[F](other: Rule1[F]) = new Rule6[A, B, C, D, E, F](append(other))
  def ~[F, G](other: Rule2[F, G]) = new Rule7[A, B, C, D, E, F, G](append(other))  
  def -->[R](f: E => R) = new Rule5[A, B, C, D, R](creator.appendSeq1(f))
  def -->[R](f: (D, E) => R) = new Rule4[A, B, C, R](creator.appendSeq2(f))
  def -->[R](f: (C, D, E) => R) = new Rule3[A, B, R](creator.appendSeq3(f))
  def -->[R](f: (B, C, D, E) => R) = new Rule2[A, R](creator.appendSeq4(f))
  def -->[R](f: (A, B, C, D, E) => R) = new Rule1[R](creator.appendSeq5(f))
  def -->[Z, R](f: (Z, A, B, C, D, E) => R) = new ReductionRule1[Z, R](creator.appendSeq6(f))
  def -->[Y, Z, R](f: (Y, Z, A, B, C, D, E) => R) = new ReductionRule2[Y, Z, R](creator.appendSeq7(f))
  def ~>[R](f: String => R) = new Rule6[A, B, C, D, E, R](creator.appendSeqS(f))
  def |(other: Rule5[A, B, C, D, E]) = new Rule5[A, B, C, D, E](creator.appendChoice(other.creator))
}

class Rule6[A, B, C, D, E, F](creator: MatcherCreator) extends PushRule(creator: MatcherCreator) {
  def ~(other: PopRule3[D, E, F]) = new Rule3[A, B, C](append(other))
  def ~(other: PopRule2[E, F]) = new Rule4[A, B, C, D](append(other))
  def ~(other: PopRule1[F]) = new Rule5[A, B, C, D, E](append(other))
  def ~(other: PopRuleN3) = new Rule3[A, B, C](append(other))
  def ~(other: PopRuleN2) = new Rule4[A, B, C, D](append(other))
  def ~(other: PopRuleN1) = new Rule5[A, B, C, D, E](append(other))
  def ~[R](other: ReductionRule3[D, E, F, R]) = new Rule4[A, B, C, R](append(other))
  def ~[R](other: ReductionRule2[E, F, R]) = new Rule5[A, B, C, D, R](append(other))
  def ~[R](other: ReductionRule1[F, R]) = new Rule6[A, B, C, D, E, R](append(other))
  def ~(other: Rule0) = new Rule6[A, B, C, D, E, F](append(other))
  def ~[G](other: Rule1[G]) = new Rule7[A, B, C, D, E, F, G](append(other))
  def -->[R](f: E => R) = new Rule6[A, B, C, D, E, R](creator.appendSeq1(f))
  def -->[R](f: (E, F) => R) = new Rule5[A, B, C, D, R](creator.appendSeq2(f))
  def -->[R](f: (D, E, F) => R) = new Rule4[A, B, C, R](creator.appendSeq3(f))
  def -->[R](f: (C, D, E, F) => R) = new Rule3[A, B, R](creator.appendSeq4(f))
  def -->[R](f: (B, C, D, E, F) => R) = new Rule2[A, R](creator.appendSeq5(f))
  def -->[R](f: (A, B, C, D, E, F) => R) = new Rule1[R](creator.appendSeq6(f))
  def -->[Z, R](f: (Z, A, B, C, D, E, F) => R) = new ReductionRule1[Z, R](creator.appendSeq7(f))
  def ~>[R](f: String => R) = new Rule7[A, B, C, D, E, F, R](creator.appendSeqS(f))
  def |(other: Rule6[A, B, C, D, E, F]) = new Rule6[A, B, C, D, E, F](creator.appendChoice(other.creator))
}

class Rule7[A, B, C, D, E, F, G](creator: MatcherCreator) extends PushRule(creator: MatcherCreator) {
  def ~(other: PopRule3[E, F, G]) = new Rule4[A, B, C, F](append(other))
  def ~(other: PopRule2[F, G]) = new Rule5[A, B, C, D, F](append(other))
  def ~(other: PopRule1[G]) = new Rule6[A, B, C, D, E, F](append(other))
  def ~(other: PopRuleN3) = new Rule4[A, B, C, D](append(other))
  def ~(other: PopRuleN2) = new Rule5[A, B, C, D, E](append(other))
  def ~(other: PopRuleN1) = new Rule6[A, B, C, D, E, F](append(other))
  def ~[R](other: ReductionRule3[E, F, G, R]) = new Rule5[A, B, C, D, R](append(other))
  def ~[R](other: ReductionRule2[F, G, R]) = new Rule6[A, B, C, D, E, R](append(other))
  def ~[R](other: ReductionRule1[G, R]) = new Rule7[A, B, C, D, E, F, R](append(other))
  def ~(other: Rule0) = new Rule7[A, B, C, D, E, F, G](append(other))
  def -->[R](f: G => R) = new Rule7[A, B, C, D, E, F, R](creator.appendSeq1(f))
  def -->[R](f: (F, G) => R) = new Rule6[A, B, C, D, E, R](creator.appendSeq2(f))
  def -->[R](f: (E, F, G) => R) = new Rule5[A, B, C, D, R](creator.appendSeq3(f))
  def -->[R](f: (D, E, F, G) => R) = new Rule4[A, B, C, R](creator.appendSeq4(f))
  def -->[R](f: (C, D, E, F, G) => R) = new Rule3[A, B, R](creator.appendSeq5(f))
  def -->[R](f: (B, C, D, E, F, G) => R) = new Rule2[A, R](creator.appendSeq6(f))
  def -->[R](f: (A, B, C, D, E, F, G) => R) = new Rule1[R](creator.appendSeq7(f))
  def |(other: Rule7[A, B, C, D, E, F, G]) = new Rule7[A, B, C, D, E, F, G](creator.appendChoice(other.creator))
}

class CharRule(val c: Char) extends Rule0(new CharMatcher(c).label('\'' + escape(c) + '\'')) {
  def --(upperBound: Char) = new Rule0(new CharRangeMatcher(c, upperBound).label(c + ".." + upperBound))
}

class ProxyRule extends Rule(new ProxyCreator) {
  private var inner: Rule = _
  
  creator.asInstanceOf[ProxyCreator].creator = () => {
    require(inner != null)
    val proxy = new ProxyMatcher()
    inner.creator.registerProxy(proxy)
    proxy
  }

  def arm(inner: Rule) { this.inner = inner }

}