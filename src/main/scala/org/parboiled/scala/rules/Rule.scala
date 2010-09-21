package org.parboiled.scala.rules

import org.parboiled.matchers._
import org.parboiled.Action
import org.parboiled.Context
import java.lang.String
import org.parboiled.support.ValueStack
import org.parboiled.scala._
import Rule._

/**
 * The base class of all scala parser rules.
 */
abstract class Rule(val matcher: Matcher) {

  /**
   * Creates a "NOT" syntactic predicate according to the PEG formalism.
   */
  def unary_!(): Rule0 = new TestNotMatcher(matcher)

  def ~(other: Rule0): this.type = withMatcher(append(other))

  /**
   * Creates a semantic predicate on the first char of the input text matched by the immediately preceding rule.
   */
  def ~:?[R](f: Char => Boolean): this.type = withMatcher(append(exec(GetMatchedChar, f)))

  /**
   * Creates a semantic predicate on the input text matched by the immediately preceding rule.
   */
  def ~?[R](f: String => Boolean): this.type = withMatcher(append(exec(GetMatch, f)))

  /**
   * Creates a simple parser action with the input text matched by the immediately preceding rule as parameter.
   */
  def ~%(f: String => Unit): this.type = ~?(ok(f))

  def label(label: String): this.type = withMatcher(matcher.label(label).asInstanceOf[Matcher])

  def suppressNode(): this.type = withMatcher(matcher.suppressNode().asInstanceOf[Matcher])

  def suppressSubnodes(): this.type = withMatcher(matcher.suppressSubnodes().asInstanceOf[Matcher])

  def skipNode(): this.type = withMatcher(matcher.skipNode().asInstanceOf[Matcher])

  def memoMismatches(): this.type = withMatcher(matcher.memoMismatches().asInstanceOf[Matcher])

  override def toString = getClass.getSimpleName + ": " + matcher.toString

  protected def withMatcher(matcher: Matcher): this.type

  protected def append(action: Action[_]): Matcher = append(new ActionMatcher(action).label("Action"))

  protected def append(f: Context[Any] => Boolean): Matcher = append(action(f))

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

}

object Rule {

  private[scala] val GetMatchedChar: (Context[Any] => Char) = _.getFirstMatchChar
  private[scala] val GetMatch: (Context[Any] => String) = _.getMatch
  private[scala] val Pop = (vs:ValueStack[Any], _:Int) => vs.pop
  private[scala] val Peek: ((ValueStack[Any], Int) => Any) = _.peek(_)

  private def addSub(subs: java.util.List[Matcher], element: Matcher): Array[org.parboiled.Rule] = {
    val count = subs.size
    val array = new Array[org.parboiled.Rule](count + 1)
    subs.toArray(array)
    array(count) = element
    array
  }

  private[scala] def action(f: Context[Any] => Boolean) = new Action[Any] {
    def run(context: Context[Any]) = f(context)
  }

  private[scala] def push(f: Context[Any] => Any) = (context: Context[Any]) => {
    context.getValueStack.push(f(context))
    true
  }

  private[scala] def ok[A](f: A => Any) = (a: A) => { f(a); true }

  private[scala] def stack1[Z](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => {
    get(c.getValueStack, 0).asInstanceOf[Z]
  }

  private[scala] def stack2[Z, Y](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y]
          )

  private[scala] def stack3[Z, Y, X](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X]
          )

  private[scala] def stack4[Z, Y, X, W](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W]
          )

  private[scala] def stack5[Z, Y, X, W, V](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V]
          )

  private[scala] def stack6[Z, Y, X, W, V, U](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V],
          get(c.getValueStack, 5).asInstanceOf[U]
          )

  private[scala] def stack7[Z, Y, X, W, V, U, T](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V],
          get(c.getValueStack, 5).asInstanceOf[U],
          get(c.getValueStack, 6).asInstanceOf[T]
          )

  private[scala] def exec(f: () => Any) = (context: Context[Any]) => f match {
    case a: WithContextAction[Any] => a.action(context)
    case _ => f
  }

  private[scala] def exec[Z, R](extract: Context[Any] => Z, f: Z => R) = (context: Context[Any]) => {
    val z = extract(context)
    f match {
      case a: WithContextAction1[Z, R] => a.action(z, context)
      case _ => f(z)
    }
  }

  private[scala] def exec[Y, Z, R](extract: Context[Any] => (Z, Y), f: (Y, Z) => R) = (context: Context[Any]) => {
    val (z, y) = extract(context)
    f match {
      case a: WithContextAction2[Y, Z, R] => a.action(y, z, context)
      case _ => f(y, z)
    }
  }

  private[scala] def exec[X, Y, Z, R](extract: Context[Any] => (Z, Y, X), f: (X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x) = extract(context)
    f match {
      case a: WithContextAction3[X, Y, Z, R] => a.action(x, y, z, context)
      case _ => f(x, y, z)
    }
  }

  private[scala] def exec[W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W), f: (W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w) = extract(context)
    f match {
      case a: WithContextAction4[W, X, Y, Z, R] => a.action(w, x, y, z, context)
      case _ => f(w, x, y, z)
    }
  }

  private[scala] def exec[V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V), f: (V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v) = extract(context)
    f match {
      case a: WithContextAction5[V, W, X, Y, Z, R] => a.action(v, w, x, y, z, context)
      case _ => f(v, w, x, y, z)
    }
  }

  private[scala] def exec[U, V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V, U), f: (U, V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v, u) = extract(context)
    f match {
      case a: WithContextAction6[U, V, W, X, Y, Z, R] => a.action(u, v, w, x, y, z, context)
      case _ => f(u, v, w, x, y, z)
    }
  }

  private[scala] def exec[T, U, V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V, U, T), f: (T, U, V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v, u, t) = extract(context)
    f match {
      case a: WithContextAction7[T, U, V, W, X, Y, Z, R] => a.action(t, u, v, w, x, y, z, context)
      case _ => f(t, u, v, w, x, y, z)
    }
  }
}

