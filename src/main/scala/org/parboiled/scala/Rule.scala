package org.parboiled.scala

import org.parboiled.matchers._
import org.parboiled.common.StringUtils.escape
import annotation.unchecked.uncheckedVariance
import org.parboiled.Action
import org.parboiled.Context

/**
 * The base class of all scala parser rules.
 */
abstract class Rule(val matcher: Matcher) {

  /**
   * Creates a "NOT" syntactic predicate according to the PEG formalism.
   */
  def unary_!() = new Rule0(new TestNotMatcher(matcher))
  
  def label(label: String): this.type = withMatcher(matcher.label(label).asInstanceOf[Matcher])
  def suppressNode(): this.type = withMatcher(matcher.suppressNode().asInstanceOf[Matcher])
  def suppressSubnodes(): this.type = withMatcher(matcher.suppressSubnodes().asInstanceOf[Matcher])
  def skipNode(): this.type = withMatcher(matcher.skipNode().asInstanceOf[Matcher])
  def memoMismatches(): this.type = withMatcher(matcher.memoMismatches().asInstanceOf[Matcher])
  override def toString = getClass.getSimpleName +  ": " + matcher.toString

  protected def withMatcher(matcher: Matcher): this.type

  protected def append(action: Action[_]): Matcher = append(new ActionMatcher(action).label("Action"))
  protected def append(other: Rule): Matcher = append(other.matcher)
  protected def append(other: Matcher): Matcher = (matcher match {
    case m: SequenceMatcher if (m.getLabel == "Sequence") => new SequenceMatcher(addSub(m.getChildren, other))
    case _ => new SequenceMatcher(Array(matcher, other))
  }).label("Sequence")

  protected def appendChoice(other: Rule): Matcher = appendChoice(other.matcher)
  protected def appendChoice(other: Matcher): Matcher = (matcher match {
    case m: FirstOfMatcher if (m.getLabel == "FirstOf") => new FirstOfMatcher(addSub(m.getChildren, other))
    case _ => new FirstOfMatcher(Array(matcher, other))
  }).label("FirstOf")

  private def addSub(subs: java.util.List[Matcher], element: Matcher): Array[org.parboiled.Rule] = {
    val count = subs.size
    val array = new Array[org.parboiled.Rule](count + 1)
    subs.toArray(array)
    array(count) = element
    array
  }

  protected def appendSeqS[R](f: String => R): Matcher = append(new Action[Any] {
    def run(c: Context[Any]): Boolean = {
      c.getValueStack.push(f(c.getMatch)); true
    }
  })

  protected def appendSeq1[Z, R](f: Z => R) = append(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val r = f(z)
      vs.push(r)
      true
    }
  })

  protected def appendSeq2[Y, Z, R](f: (Y, Z) => R) = append(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val y = vs.pop.asInstanceOf[Y]
      val r = f(y, z)
      vs.push(r)
      true
    }
  })

  protected def appendSeq3[X, Y, Z, R](f: (X, Y, Z) => R) = append(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val y = vs.pop.asInstanceOf[Y]
      val x = vs.pop.asInstanceOf[X]
      val r = f(x, y, z)
      vs.push(r)
      true
    }
  })

  protected def appendSeq4[W, X, Y, Z, R](f: (W, X, Y, Z) => R) = append(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val y = vs.pop.asInstanceOf[Y]
      val x = vs.pop.asInstanceOf[X]
      val w = vs.pop.asInstanceOf[W]
      val r = f(w, x, y, z)
      vs.push(r)
      true
    }
  })

  protected def appendSeq5[V, W, X, Y, Z, R](f: (V, W, X, Y, Z) => R) = append(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val y = vs.pop.asInstanceOf[Y]
      val x = vs.pop.asInstanceOf[X]
      val w = vs.pop.asInstanceOf[W]
      val v = vs.pop.asInstanceOf[V]
      val r = f(v, w, x, y, z)
      vs.push(r)
      true
    }
  })

  protected def appendSeq6[U, V, W, X, Y, Z, R](f: (U, V, W, X, Y, Z) => R) = append(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val y = vs.pop.asInstanceOf[Y]
      val x = vs.pop.asInstanceOf[X]
      val w = vs.pop.asInstanceOf[W]
      val v = vs.pop.asInstanceOf[V]
      val u = vs.pop.asInstanceOf[U]
      val r = f(u, v, w, x, y, z)
      vs.push(r)
      true
    }
  })

  protected def appendSeq7[T, U, V, W, X, Y, Z, R](f: (T, U, V, W, X, Y, Z) => R) = append(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val y = vs.pop.asInstanceOf[Y]
      val x = vs.pop.asInstanceOf[X]
      val w = vs.pop.asInstanceOf[W]
      val v = vs.pop.asInstanceOf[V]
      val u = vs.pop.asInstanceOf[U]
      val t = vs.pop.asInstanceOf[T]
      val r = f(t, u, v, w, x, y, z)
      vs.push(r)
      true
    }
  })
}

/**
 * The base class of all reduction rules, which take a certain number of input values and produce one output value.
 */
abstract class ReductionRule(matcher: Matcher) extends Rule(matcher)

/**
 * A rule taking one value off the value stack and replacing it with another value.
 */
class ReductionRule1[Z, R](matcher: Matcher) extends ReductionRule(matcher) {
  def |(other: ReductionRule1[Z, R]) = new ReductionRule1[Z, R](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule1[Z, R](matcher).asInstanceOf[this.type]
}

/**
 * A rule taking two values off the value stack and replacing them with one other value.
 */
class ReductionRule2[Y, Z, R](matcher: Matcher) extends ReductionRule(matcher) {
  def |(other: ReductionRule2[Y, Z, R]) = new ReductionRule2[Y, Z, R](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule2[Y, Z, R](matcher).asInstanceOf[this.type]
}

/**
 * A rule taking three values off the value stack and replacing them with one other value.
 */
class ReductionRule3[X, Y, Z, R](matcher: Matcher) extends ReductionRule(matcher) {
  def |(other: ReductionRule3[X, Y, Z, R]) = new ReductionRule3[X, Y, Z, R](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new ReductionRule3[X, Y, Z, R](matcher).asInstanceOf[this.type]
}

/**
 * The base class of all rules simply removing a certain number of elements off the top of the value stack.
 */
abstract class PopRule(matcher: Matcher) extends Rule(matcher)

/**
 * A rule removing the top value stack element with a given type.
 */
class PopRule1[Z](matcher: Matcher) extends PopRule(matcher) {
  def |(other: PopRule1[Z]) = new PopRule1[Z](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule1[Z](matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top two value stack elements with given types.
 */
class PopRule2[Y, Z](matcher: Matcher) extends PopRule(matcher) {
  def |(other: PopRule2[Y, Z]) = new PopRule2[Y, Z](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule2[Y, Z](matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top three value stack elements with given types.
 */
class PopRule3[X, Y, Z](matcher: Matcher) extends PopRule(matcher) {
  def |(other: PopRule3[X, Y, Z]) = new PopRule3[X, Y, Z](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRule3[X, Y, Z](matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top value stack element independently of its type.
 */
class PopRuleN1(matcher: Matcher) extends PopRule(matcher) {
  def |(other: PopRuleN1) = new PopRuleN1(appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRuleN1(matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top two value stack elements independently of their type.
 */
class PopRuleN2(matcher: Matcher) extends PopRule(matcher) {
  def |(other: PopRuleN2) = new PopRuleN2(appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRuleN2(matcher).asInstanceOf[this.type]
}

/**
 * A rule removing the top three value stack elements independently of their type.
 */
class PopRuleN3(matcher: Matcher) extends PopRule(matcher) {
  def |(other: PopRuleN3) = new PopRuleN3(appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new PopRuleN3(matcher).asInstanceOf[this.type]
}

/**
 * A rule which does not affect the parsers value stack.
 */
class Rule0(matcher: Matcher) extends Rule(matcher) {
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
  def ~>[R](f: String => R) = new Rule1[R](appendSeqS(f))
  def ~~>[Z, R](f: Z => R) = new ReductionRule1[Z, R](appendSeq1(f))
  def ~~>[Y, Z, R](f: (Y, Z) => R) = new ReductionRule2[Y, Z, R](appendSeq2(f))
  def ~~>[X, Y, Z, R](f: (X, Y, Z) => R) = new ReductionRule3[X, Y, Z, R](appendSeq3(f))
  def |(other: Rule0) = new Rule0(appendChoice(other))
  def -(upperBound: String): Rule0 = throw new IllegalArgumentException("char range operator '-' only allowed on single character strings")
  protected def withMatcher(matcher: Matcher) = new Rule0(matcher).asInstanceOf[this.type]
}

/**
 * The base class of all rules pushing a certain number of elements onto the parser value stack.
 */
sealed abstract class PushRule(matcher: Matcher) extends Rule(matcher)

/**
 * A rule pushing one new value of a given type onto the parsers value stack.
 */
class Rule1[+A](matcher: Matcher) extends PushRule(matcher: Matcher) {
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
  def ~>[R](f: String => R): Rule2[A, R] @uncheckedVariance = new Rule2[A, R](appendSeqS(f))
  def ~~>[R](f: A => R) = new Rule1[R](appendSeq1(f))
  def ~~>[Z, R](f: (Z, A) => R) = new ReductionRule1[Z, R](appendSeq2(f))
  def ~~>[Y, Z, R](f: (Y, Z, A) => R) = new ReductionRule2[Y, Z, R](appendSeq3(f))
  def ~~>[X, Y, Z, R](f: (X, Y, Z, A) => R) = new ReductionRule3[X, Y, Z, R](appendSeq4(f))
  def |[AA >: A](other: Rule1[AA]) = new Rule1[AA](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new Rule1[A](matcher).asInstanceOf[this.type]
}

/**
 * A rule pushing two new values of given types onto the parsers value stack.
 */
class Rule2[+A, +B](matcher: Matcher) extends PushRule(matcher: Matcher) {
  def ~[Z, AA >: A, BB >: B](other: PopRule3[Z, AA, BB]) = new PopRule1[Z](append(other))
  def ~[AA >: A, BB >: B](other: PopRule2[AA, BB]) = new Rule0(append(other))
  def ~[BB >: B](other: PopRule1[BB]) = new Rule1[A](append(other))
  def ~(other: PopRuleN3) = new PopRuleN1(append(other))
  def ~(other: PopRuleN2) = new Rule0(append(other))
  def ~(other: PopRuleN1) = new Rule1[A](append(other))
  def ~[X, AA >: A, BB >: B, R](other: ReductionRule3[X, AA, BB, R]) = new ReductionRule1[X, R](append(other))
  def ~[AA >: A, BB >: B, R](other: ReductionRule2[AA, BB, R]) = new Rule1[R](append(other))
  def ~[BB >: B, R](other: ReductionRule1[BB, R]) = new Rule2[A, R](append(other))
  def ~(other: Rule0) = new Rule2[A, B](append(other))
  def ~[C](other: Rule1[C]): Rule3[A, B, C] @uncheckedVariance = new Rule3[A, B, C](append(other))
  def ~[C, D](other: Rule2[C, D]): Rule4[A, B, C, D] @uncheckedVariance = new Rule4[A, B, C, D](append(other))
  def ~[C, D, E](other: Rule3[C, D, E]): Rule5[A, B, C, D, E] @uncheckedVariance = new Rule5[A, B, C, D, E](append(other))
  def ~[C, D, E, F](other: Rule4[C, D, E, F]): Rule6[A, B, C, D, E, F] @uncheckedVariance = new Rule6[A, B, C, D, E, F](append(other))
  def ~[C, D, E, F, G](other: Rule5[C, D, E, F, G]): Rule7[A, B, C, D, E, F, G] @uncheckedVariance = new Rule7[A, B, C, D, E, F, G](append(other))
  def ~>[R](f: String => R): Rule3[A, B, R] @uncheckedVariance = new Rule3[A, B, R](appendSeqS(f))
  def ~~>[R](f: B => R) = new Rule2[A, R](appendSeq1(f))
  def ~~>[R](f: (A, B) => R) = new Rule1[R](appendSeq2(f))
  def ~~>[Z, R](f: (Z, A, B) => R) = new ReductionRule1[Z, R](appendSeq3(f))
  def ~~>[Y, Z, R](f: (Y, Z, A, B) => R) = new ReductionRule2[Y, Z, R](appendSeq4(f))
  def ~~>[X, Y, Z, R](f: (X, Y, Z, A, B) => R) = new ReductionRule3[X, Y, Z, R](appendSeq5(f))
  def |[AA >: A, BB >: B](other: Rule2[AA, BB]) = new Rule2[AA, BB](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new Rule2[A, B](matcher).asInstanceOf[this.type]
}

/**
 * A rule pushing 3 new values of given types onto the parsers value stack.
 */
class Rule3[+A, +B, +C](matcher: Matcher) extends PushRule(matcher: Matcher) {
  def ~[AA >: A, BB >: B, CC >: C](other: PopRule3[AA, BB, CC]) = new Rule0(append(other))
  def ~[BB >: B, CC >: C](other: PopRule2[BB, CC]) = new Rule1[A](append(other))
  def ~[CC >: C](other: PopRule1[CC]) = new Rule2[A, B](append(other))
  def ~(other: PopRuleN3) = new Rule0(append(other))
  def ~(other: PopRuleN2) = new Rule1[A](append(other))
  def ~(other: PopRuleN1) = new Rule2[A, B](append(other))
  def ~[AA >: A, BB >: B, CC >: C, R](other: ReductionRule3[AA, BB, CC, R]) = new Rule1[R](append(other))
  def ~[BB >: B, CC >: C, R](other: ReductionRule2[BB, CC, R]) = new Rule2[A, R](append(other))
  def ~[CC >: C, R](other: ReductionRule1[CC, R]) = new Rule3[A, B, R](append(other))
  def ~(other: Rule0): Rule3[A, B, C] = new Rule3[A, B, C](append(other))
  def ~[D](other: Rule1[D]): Rule4[A, B, C, D] @uncheckedVariance = new Rule4[A, B, C, D](append(other))
  def ~[D, E](other: Rule2[D, E]): Rule5[A, B, C, D, E] @uncheckedVariance = new Rule5[A, B, C, D, E](append(other))
  def ~[D, E, F](other: Rule3[D, E, F]): Rule6[A, B, C, D, E, F] @uncheckedVariance = new Rule6[A, B, C, D, E, F](append(other))
  def ~[D, E, F, G](other: Rule4[D, E, F, G]): Rule7[A, B, C, D, E, F, G] @uncheckedVariance = new Rule7[A, B, C, D, E, F, G](append(other))
  def ~>[R](f: String => R): Rule4[A, B, C, R] @uncheckedVariance = new Rule4[A, B, C, R](appendSeqS(f))
  def ~~>[R](f: C => R) = new Rule3[A, B, R](appendSeq1(f))
  def ~~>[R](f: (B, C) => R) = new Rule2[A, R](appendSeq2(f))
  def ~~>[R](f: (A, B, C) => R) = new Rule1[R](appendSeq3(f))
  def ~~>[Z, R](f: (Z, A, B, C) => R) = new ReductionRule1[Z, R](appendSeq4(f))
  def ~~>[Y, Z, R](f: (Y, Z, A, B, C) => R) = new ReductionRule2[Y, Z, R](appendSeq5(f))
  def ~~>[X, Y, Z, R](f: (X, Y, Z, A, B, C) => R) = new ReductionRule3[X, Y, Z, R](appendSeq6(f))
  def |[AA >: A, BB >: B, CC >: C](other: Rule3[AA, BB, CC]) = new Rule3[AA, BB, CC](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new Rule3[A, B, C](matcher).asInstanceOf[this.type]
}

/**
 * A rule pushing 4 new values of given types onto the parsers value stack.
 */
class Rule4[+A, +B, +C, +D](matcher: Matcher) extends PushRule(matcher: Matcher) {
  def ~[BB >: B, CC >: C, DD >: D](other: PopRule3[BB, CC, DD]) = new Rule1[A](append(other))
  def ~[CC >: C, DD >: D](other: PopRule2[CC, DD]) = new Rule2[A, B](append(other))
  def ~[DD >: D](other: PopRule1[DD]) = new Rule3[A, B, C](append(other))
  def ~(other: PopRuleN3) = new Rule1[A](append(other))
  def ~(other: PopRuleN2) = new Rule2[A, B](append(other))
  def ~(other: PopRuleN1) = new Rule3[A, B, C](append(other))
  def ~[BB >: B, CC >: C, DD >: D, R](other: ReductionRule3[BB, CC, DD, R]) = new Rule2[A, R](append(other))
  def ~[CC >: C, DD >: D, R](other: ReductionRule2[CC, DD, R]) = new Rule3[A, B, R](append(other))
  def ~[DD >: D, R](other: ReductionRule1[DD, R]) = new Rule4[A, B, C, R](append(other))
  def ~(other: Rule0) = new Rule4[A, B, C, D](append(other))
  def ~[E](other: Rule1[E]): Rule5[A, B, C, D, E] @uncheckedVariance = new Rule5[A, B, C, D, E](append(other))
  def ~[E, F](other: Rule2[E, F]): Rule6[A, B, C, D, E, F] @uncheckedVariance = new Rule6[A, B, C, D, E, F](append(other))
  def ~[E, F, G](other: Rule3[E, F, G]): Rule7[A, B, C, D, E, F, G] @uncheckedVariance = new Rule7[A, B, C, D, E, F, G](append(other))
  def ~>[R](f: String => R): Rule5[A, B, C, D, R] @uncheckedVariance = new Rule5[A, B, C, D, R](appendSeqS(f))
  def ~~>[R](f: D => R) = new Rule4[A, B, C, R](appendSeq1(f))
  def ~~>[R](f: (C, D) => R) = new Rule3[A, B, R](appendSeq2(f))
  def ~~>[R](f: (B, C, D) => R) = new Rule2[A, R](appendSeq3(f))
  def ~~>[R](f: (A, B, C, D) => R) = new Rule1[R](appendSeq4(f))
  def ~~>[Z, R](f: (Z, A, B, C, D) => R) = new ReductionRule1[Z, R](appendSeq5(f))
  def ~~>[Y, Z, R](f: (Y, Z, A, B, C, D) => R) = new ReductionRule2[Y, Z, R](appendSeq6(f))
  def ~~>[X, Y, Z, R](f: (X, Y, Z, A, B, C, D) => R) = new ReductionRule3[X, Y, Z, R](appendSeq7(f))
  def |[AA >: A, BB >: B, CC >: C, DD >: D](other: Rule4[AA, BB, CC, DD]) = new Rule4[AA, BB, CC, DD](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new Rule4[A, B, C, D](matcher).asInstanceOf[this.type]
}

/**
 * A rule pushing 5 new values of given types onto the parsers value stack.
 */
class Rule5[+A, +B, +C, +D, +E](matcher: Matcher) extends PushRule(matcher: Matcher) {
  def ~[CC >: C, DD >: D, EE >: E](other: PopRule3[CC, DD, EE]) = new Rule2[A, B](append(other))
  def ~[DD >: D, EE >: E](other: PopRule2[DD, EE]) = new Rule3[A, B, C](append(other))
  def ~[EE >: E](other: PopRule1[EE]) = new Rule4[A, B, C, D](append(other))
  def ~(other: PopRuleN3) = new Rule2[A, B](append(other))
  def ~(other: PopRuleN2) = new Rule3[A, B, C](append(other))
  def ~(other: PopRuleN1) = new Rule4[A, B, C, D](append(other))
  def ~[CC >: C, DD >: D, EE >: E, R](other: ReductionRule3[CC, DD, EE, R]) = new Rule3[A, B, R](append(other))
  def ~[DD >: D, EE >: E, R](other: ReductionRule2[DD, EE, R]) = new Rule4[A, B, C, R](append(other))
  def ~[EE >: E, R](other: ReductionRule1[EE, R]) = new Rule5[A, B, C, D, R](append(other))
  def ~(other: Rule0) = new Rule5[A, B, C, D, E](append(other))
  def ~[F](other: Rule1[F]): Rule6[A, B, C, D, E, F] @uncheckedVariance = new Rule6[A, B, C, D, E, F](append(other))
  def ~[F, G](other: Rule2[F, G]): Rule7[A, B, C, D, E, F, G] @uncheckedVariance = new Rule7[A, B, C, D, E, F, G](append(other))
  def ~>[R](f: String => R): Rule6[A, B, C, D, E, R] @uncheckedVariance = new Rule6[A, B, C, D, E, R](appendSeqS(f))
  def ~~>[R](f: E => R) = new Rule5[A, B, C, D, R](appendSeq1(f))
  def ~~>[R](f: (D, E) => R) = new Rule4[A, B, C, R](appendSeq2(f))
  def ~~>[R](f: (C, D, E) => R) = new Rule3[A, B, R](appendSeq3(f))
  def ~~>[R](f: (B, C, D, E) => R) = new Rule2[A, R](appendSeq4(f))
  def ~~>[R](f: (A, B, C, D, E) => R) = new Rule1[R](appendSeq5(f))
  def ~~>[Z, R](f: (Z, A, B, C, D, E) => R) = new ReductionRule1[Z, R](appendSeq6(f))
  def ~~>[Y, Z, R](f: (Y, Z, A, B, C, D, E) => R) = new ReductionRule2[Y, Z, R](appendSeq7(f))
  def |[AA >: A, BB >: B, CC >: C, DD >: D, EE >: E](other: Rule5[AA, BB, CC, DD, EE]) = new Rule5[AA, BB, CC, DD, EE](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new Rule5[A, B, C, D, E](matcher).asInstanceOf[this.type]
}

/**
 * A rule pushing 6 new values of given types onto the parsers value stack.
 */
class Rule6[+A, +B, +C, +D, +E, +F](matcher: Matcher) extends PushRule(matcher: Matcher) {
  def ~[DD >: D, EE >: E, FF >: F](other: PopRule3[DD, EE, FF]) = new Rule3[A, B, C](append(other))
  def ~[EE >: E, FF >: F](other: PopRule2[EE, FF]) = new Rule4[A, B, C, D](append(other))
  def ~[FF >: F](other: PopRule1[FF]) = new Rule5[A, B, C, D, E](append(other))
  def ~(other: PopRuleN3) = new Rule3[A, B, C](append(other))
  def ~(other: PopRuleN2) = new Rule4[A, B, C, D](append(other))
  def ~(other: PopRuleN1) = new Rule5[A, B, C, D, E](append(other))
  def ~[DD >: D, EE >: E, FF >: F, R](other: ReductionRule3[DD, EE, FF, R]) = new Rule4[A, B, C, R](append(other))
  def ~[EE >: E, FF >: F, R](other: ReductionRule2[EE, FF, R]) = new Rule5[A, B, C, D, R](append(other))
  def ~[FF >: F, R](other: ReductionRule1[FF, R]) = new Rule6[A, B, C, D, E, R](append(other))
  def ~(other: Rule0) = new Rule6[A, B, C, D, E, F](append(other))
  def ~[G](other: Rule1[G]): Rule7[A, B, C, D, E, F, G] @uncheckedVariance = new Rule7[A, B, C, D, E, F, G](append(other))
  def ~>[R](f: String => R): Rule7[A, B, C, D, E, F, R] @uncheckedVariance = new Rule7[A, B, C, D, E, F, R](appendSeqS(f))
  def ~~>[R](f: E => R) = new Rule6[A, B, C, D, E, R](appendSeq1(f))
  def ~~>[R](f: (E, F) => R) = new Rule5[A, B, C, D, R](appendSeq2(f))
  def ~~>[R](f: (D, E, F) => R) = new Rule4[A, B, C, R](appendSeq3(f))
  def ~~>[R](f: (C, D, E, F) => R) = new Rule3[A, B, R](appendSeq4(f))
  def ~~>[R](f: (B, C, D, E, F) => R) = new Rule2[A, R](appendSeq5(f))
  def ~~>[R](f: (A, B, C, D, E, F) => R) = new Rule1[R](appendSeq6(f))
  def ~~>[Z, R](f: (Z, A, B, C, D, E, F) => R) = new ReductionRule1[Z, R](appendSeq7(f))
  def |[AA >: A, BB >: B, CC >: C, DD >: D, EE >: E, FF >: F](other: Rule6[AA, BB, CC, DD, EE, FF]) = new Rule6[AA, BB, CC, DD, EE, FF](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new Rule6[A, B, C, D, E, F](matcher).asInstanceOf[this.type]
}

/**
 * A rule pushing 7 new values of given types onto the parsers value stack.
 */
class Rule7[+A, +B, +C, +D, +E, +F, +G](matcher: Matcher) extends PushRule(matcher: Matcher) {
  def ~[EE >: E, FF >: F, GG >: G](other: PopRule3[EE, FF, GG]) = new Rule4[A, B, C, F](append(other))
  def ~[FF >: F, GG >: G](other: PopRule2[FF, GG]) = new Rule5[A, B, C, D, F](append(other))
  def ~[GG >: G](other: PopRule1[GG]) = new Rule6[A, B, C, D, E, F](append(other))
  def ~(other: PopRuleN3) = new Rule4[A, B, C, D](append(other))
  def ~(other: PopRuleN2) = new Rule5[A, B, C, D, E](append(other))
  def ~(other: PopRuleN1) = new Rule6[A, B, C, D, E, F](append(other))
  def ~[EE >: E, FF >: F, GG >: G, R](other: ReductionRule3[EE, FF, GG, R]) = new Rule5[A, B, C, D, R](append(other))
  def ~[FF >: F, GG >: G, R](other: ReductionRule2[FF, GG, R]) = new Rule6[A, B, C, D, E, R](append(other))
  def ~[GG >: G, R](other: ReductionRule1[GG, R]) = new Rule7[A, B, C, D, E, F, R](append(other))
  def ~(other: Rule0) = new Rule7[A, B, C, D, E, F, G](append(other))
  def ~~>[R](f: G => R) = new Rule7[A, B, C, D, E, F, R](appendSeq1(f))
  def ~~>[R](f: (F, G) => R) = new Rule6[A, B, C, D, E, R](appendSeq2(f))
  def ~~>[R](f: (E, F, G) => R) = new Rule5[A, B, C, D, R](appendSeq3(f))
  def ~~>[R](f: (D, E, F, G) => R) = new Rule4[A, B, C, R](appendSeq4(f))
  def ~~>[R](f: (C, D, E, F, G) => R) = new Rule3[A, B, R](appendSeq5(f))
  def ~~>[R](f: (B, C, D, E, F, G) => R) = new Rule2[A, R](appendSeq6(f))
  def ~~>[R](f: (A, B, C, D, E, F, G) => R) = new Rule1[R](appendSeq7(f))
  def |[AA >: A, BB >: B, CC >: C, DD >: D, EE >: E, FF >: F, GG >: G](other: Rule7[AA, BB, CC, DD, EE, FF, GG]) = new Rule7[AA, BB, CC, DD, EE, FF, GG](appendChoice(other))
  protected def withMatcher(matcher: Matcher) = new Rule7[A, B, C, D, E, F, G](matcher).asInstanceOf[this.type]
}

/**
 * A rule matching one single character.
 */
class CharRule(val c: Char) extends Rule0(new CharMatcher(c).label('\'' + escape(c) + '\'')) {

  /**
   * Creates a rule matching the range of characters between the character of this rule and the given character
   * (inclusively).
   */
  override def -(upperBound: String) =
    if (upperBound == null || upperBound.length != 1)
      super.-(upperBound)
    else
      new Rule0(new CharRangeMatcher(c, upperBound.charAt(0)).label(c + ".." + upperBound))
}

object Rule0 {
  implicit def toRule(rule: Rule0): org.parboiled.Rule = rule.matcher
}

object Rule1 {
  implicit def toRule(rule: Rule1[_]): org.parboiled.Rule = rule.matcher
}