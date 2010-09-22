package org.parboiled

import matchers._
import support.Characters
import scala._
import rules.Rule._

/**
 * Main parboiled for Scala Module.
 * Use with "import org.parboiled.scala.Parboiled._"
 */
package object scala {

  // type aliases
  type Rule = rules.Rule
  type PopRule1[-Z] = rules.PopRule1[Z]
  type PopRule2[-Y, -Z] = rules.PopRule2[Y, Z]
  type PopRule3[-X, -Y, -Z] = rules.PopRule3[X, Y, Z]
  type ReductionRule1[-Z, +R] = rules.ReductionRule1[Z, R]
  type ReductionRule2[-Y, -Z, +R] = rules.ReductionRule2[Y, Z, R]
  type ReductionRule3[-X, -Y, -Z, +R] = rules.ReductionRule3[X, Y, Z, R]
  type Rule0 = rules.Rule0
  type Rule1[+A] = rules.Rule1[A]
  type Rule2[+A, +B] = rules.Rule2[A, B]
  type Rule3[+A, +B, +C] = rules.Rule3[A, B, C]
  type Rule4[+A, +B, +C, +D] = rules.Rule4[A, B, C, D]
  type Rule5[+A, +B, +C, +D, +E] = rules.Rule5[A, B, C, D, E]
  type Rule6[+A, +B, +C, +D, +E, +F] = rules.Rule6[A, B, C, D, E, F]
  type Rule7[+A, +B, +C, +D, +E, +F, +G] = rules.Rule7[A, B, C, D, E, F, G]
  type CharRule = rules.CharRule
  
  type ParseRunner[V] = scala.parserunners.ParseRunner[V]
  type BasicParseRunner[V] = scala.parserunners.BasicParseRunner[V]
  val BasicParseRunner = scala.parserunners.BasicParseRunner
  type RecoveringParseRunner[V] = scala.parserunners.RecoveringParseRunner[V]
  val RecoveringParseRunner = scala.parserunners.RecoveringParseRunner
  type ReportingParseRunner[V] = scala.parserunners.ReportingParseRunner[V]
  val ReportingParseRunner = scala.parserunners.ReportingParseRunner
  type TracingParseRunner[V] = scala.parserunners.TracingParseRunner[V]
  val TracingParseRunner = scala.parserunners.TracingParseRunner

  /**
   * Creates an "AND" syntactic predicate according to the PEG formalism.
   */
  def &(sub: scala.Rule): Rule0 = new TestMatcher(sub.matcher).label("Test")

  /**
   * Groups the given sub rule into one entity so that a following ~> operator receives the text matched by the whole
   * group rather than only the immediately preceding sub rule.
   */
  def group[T <: scala.Rule](rule: T) = rule.label("<group>")

  /**
   * The Empty rule, a rule that always matches and consumes no input.
   */
  lazy val EMPTY: Rule0 = new EmptyMatcher().label("EMPTY")

  /**
   * The Any rule, which matches any single character except EOI.
   */
  lazy val ANY: Rule0 = new AnyMatcher().label("ANY")

  /**
   * The Eoi rule, which matches the End-Of-Input "character".
   */
  lazy val EOI: Rule0 = new CharMatcher(Characters.EOI).label("EOI")

  /**
   * The Nothing rule, which matches the End-Of-Input "character".
   */
  lazy val NOTHING: Rule0 = new NothingMatcher().label("NOTHING")

  /**
   * A parser action removing the top element from the value stack.
   */
  lazy val POP1: PopRule1[Any] = new ActionMatcher(action(ok(stack1[Any](Pop)))).label("Pop1Action")

  /**
   * A parser action removing the top two elements from the value stack.
   */
  lazy val POP2: PopRule2[Any, Any] = new ActionMatcher(action(ok(stack2[Any, Any](Pop)))).label("Pop2Action")

  /**
   * A parser action removing the top three elements from the value stack.
   */
  lazy val POP3: PopRule3[Any, Any, Any] = new ActionMatcher(action(ok(stack3[Any, Any, Any](Pop)))).label("Pop3Action")

  type RuleMethod = StackTraceElement

  private[scala] def getCurrentRuleMethod: StackTraceElement = {
    try {
      throw new Throwable
    } catch {
      case t: Throwable => t.getStackTrace()(3)
    }
  }

  implicit def toTestAction(f: Context[Any] => Boolean): Rule0 = new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = f(context)
  }).label("TestAction")

  implicit def toRunAction(f: Context[Any] => Unit): Rule0 = new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {f(context); true}
  }).label("RunAction")

  implicit def creator4PopRule1[Z](m: Matcher): PopRule1[Z] = new PopRule1[Z](m)
  implicit def creator4PopRule2[Y, Z](m: Matcher): PopRule2[Y, Z] = new PopRule2[Y, Z](m)
  implicit def creator4PopRule3[X, Y, Z](m: Matcher): PopRule3[X, Y, Z] = new PopRule3[X, Y, Z](m)
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