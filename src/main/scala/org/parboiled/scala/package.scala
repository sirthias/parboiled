package org.parboiled

import matchers._
import support.Characters
import scala._

/**
 * Main parboiled for Scala Module.
 * Use with "import org.parboiled.scala.Parboiled._"
 */
package object scala {

  /**
   * Creates an "AND" syntactic predicate according to the PEG formalism.
   */
  def &(sub: scala.Rule) = new Rule0(new TestMatcher(sub.matcher).label("Test"))

  /**
   * Groups the given sub rule into one entity so that a following ~> operator receives the text matched by the whole
   * group rather than only the immediately preceeding sub rule.
   */
  def group[T <: scala.Rule](rule: T) = rule.label("group")

  /**
   * The EMPTY rule, a rule that always matches and consumes no input.
   */
  lazy val EMPTY = new Rule0(new EmptyMatcher().label("EMPTY"))

  /**
   * The ANY rule, which matches any single character except EOI.
   */
  lazy val ANY = new Rule0(new AnyMatcher().label("ANY"))

  /**
   * The EOI rule, which matches the End-Of-Input "character".
   */
  lazy val EOI = new Rule0(new CharMatcher(Characters.EOI).label("EOI"))
    
  type RuleMethod = StackTraceElement

  private[scala] def getCurrentRuleMethod: StackTraceElement = {
    try {
      throw new Throwable
    } catch {
      case t: Throwable => t.getStackTrace()(3)
    }
  }

  /**
   * Creates a semantic predicate taking the current parsing context as input.
   * Note that the action can read but should not alter the parsers value stack (unless the types of all value stack
   * elements remain compatible with the relevant subset of the other parser actions)!
   */
  implicit def toTestAction(f: Context[Any] => Boolean): Rule0 = new Rule0(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = f(context)
  }).label("TestAction"))

  /**
   * Creates a simple parser action taking the current parsing context as input.
   * Note that the action can read but should not alter the parsers value stack (unless the types of all value stack
   * elements remain compatible with the relevant subset of the other parser actions)!
   */
  implicit def toRunAction(f: Context[Any] => Unit): Rule0 = new Rule0(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {f(context); true}
  }).label("RunAction"))

  implicit def creator4PopRule1[Z](m: Matcher): PopRule1[Z] = new PopRule1[Z](m)
  implicit def creator4PopRule2[Y, Z](m: Matcher): PopRule2[Y, Z] = new PopRule2[Y, Z](m)
  implicit def creator4PopRule3[X, Y, Z](m: Matcher): PopRule3[X, Y, Z] = new PopRule3[X, Y, Z](m)
  implicit def creator4PopRuleN1(m: Matcher): PopRuleN1 = new PopRuleN1(m)
  implicit def creator4PopRuleN2(m: Matcher): PopRuleN2 = new PopRuleN2(m)
  implicit def creator4PopRuleN3(m: Matcher): PopRuleN3 = new PopRuleN3(m)
  implicit def creator4ReductionRule1[Z, R](m: Matcher): ReductionRule1[Z, R] = new ReductionRule1[Z, R](m)
  implicit def creator4ReductionRule2[Y, Z, R](m: Matcher): ReductionRule2[Y, Z, R] = new ReductionRule2[Y, Z, R](m)
  implicit def creator4ReductionRule3[X, Y, Z, R](m: Matcher): ReductionRule3[X, Y, Z, R] = new ReductionRule3[X, Y, Z, R](m)
  implicit def creator4Rule0(m: Matcher): Rule0 = new Rule0(m)
  implicit def creator4Rule1[A](m: Matcher): Rule1[A] = new Rule1[A](m)
  implicit def creator4Rule2[A, B](m: Matcher): Rule2[A, B] = new Rule2[A, B](m)
  implicit def creator4Rule3[A, B, C](m: Matcher): Rule3[A, B, C] = new Rule3[A, B, C](m)
  implicit def creator4Rule4[A, B, C, D](m: Matcher): Rule4[A, B, C, D] = new Rule4[A, B, C, D](m)
  implicit def creator4Rule5[A, B, C, D, E](m: Matcher): Rule5[A, B, C, D, E] = new Rule5[A, B, C, D, E](m)
  implicit def creator4Rule6[A, B, C, D, E, F](m: Matcher): Rule6[A, B, C, D, E, F] = new Rule6[A, B, C, D, E, F](m)
  implicit def creator4Rule7[A, B, C, D, E, F, G](m: Matcher): Rule7[A, B, C, D, E, F, G] = new Rule7[A, B, C, D, E, F, G](m)
}