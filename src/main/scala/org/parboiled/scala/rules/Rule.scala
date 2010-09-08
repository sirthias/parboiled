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

  def ~?[R](f: String => Boolean): this.type = withMatcher(append(exec[String, Boolean](getMatch, f)))

  def label(label: String): this.type = withMatcher(matcher.label(label).asInstanceOf[Matcher])

  def suppressNode(): this.type = withMatcher(matcher.suppressNode().asInstanceOf[Matcher])

  def suppressSubnodes(): this.type = withMatcher(matcher.suppressSubnodes().asInstanceOf[Matcher])

  def skipNode(): this.type = withMatcher(matcher.skipNode().asInstanceOf[Matcher])

  def memoMismatches(): this.type = withMatcher(matcher.memoMismatches().asInstanceOf[Matcher])

  override def toString = getClass.getSimpleName + ": " + matcher.toString

  protected def withMatcher(matcher: Matcher): this.type

  protected def append(action: Action[_]): Matcher = append(new ActionMatcher(action).label("Action"))

  protected def append(f: Context[Any] => Boolean): Matcher = append(new Action[Any] {
    def run(context: Context[Any]) = f(context)
  })

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

  private def addSub(subs: java.util.List[Matcher], element: Matcher): Array[org.parboiled.Rule] = {
    val count = subs.size
    val array = new Array[org.parboiled.Rule](count + 1)
    subs.toArray(array)
    array(count) = element
    array
  }

  def push(f: Context[Any] => Any) = (context: Context[Any]) => {
    val res = f(context)
    context.getValueStack.push(res)
    true
  }

  def getMatch(context: Context[Any]): String = context.getMatch

  @inline
  def pop(vs: ValueStack[Any], down: Int): Any = vs.pop

  @inline
  def peek(vs: ValueStack[Any], down: Int): Any = vs.peek(down)

  def stack1[Z](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => {
    get(c.getValueStack, 0).asInstanceOf[Z]
  }

  def stack2[Z, Y](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y]
          )

  def stack3[Z, Y, X](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X]
          )

  def stack4[Z, Y, X, W](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W]
          )

  def stack5[Z, Y, X, W, V](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V]
          )

  def stack6[Z, Y, X, W, V, U](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V],
          get(c.getValueStack, 5).asInstanceOf[U]
          )

  def stack7[Z, Y, X, W, V, U, T](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V],
          get(c.getValueStack, 5).asInstanceOf[U],
          get(c.getValueStack, 6).asInstanceOf[T]
          )

  def exec(f: () => Any) = (context: Context[Any]) => f match {
    case a: WithContextAction[Any] => a.action(context)
    case _ => f
  }

  def exec[Z, R](extract: Context[Any] => Z, f: Z => R) = (context: Context[Any]) => {
    val z = extract(context)
    f match {
      case a: WithContextAction1[Z, R] => a.action(z, context)
      case _ => f(z)
    }
  }

  def exec[Y, Z, R](extract: Context[Any] => (Z, Y), f: (Y, Z) => R) = (context: Context[Any]) => {
    val (z, y) = extract(context)
    f match {
      case a: WithContextAction2[Y, Z, R] => a.action(y, z, context)
      case _ => f(y, z)
    }
  }

  def exec[X, Y, Z, R](extract: Context[Any] => (Z, Y, X), f: (X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x) = extract(context)
    f match {
      case a: WithContextAction3[X, Y, Z, R] => a.action(x, y, z, context)
      case _ => f(x, y, z)
    }
  }

  def exec[W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W), f: (W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w) = extract(context)
    f match {
      case a: WithContextAction4[W, X, Y, Z, R] => a.action(w, x, y, z, context)
      case _ => f(w, x, y, z)
    }
  }

  def exec[V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V), f: (V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v) = extract(context)
    f match {
      case a: WithContextAction5[V, W, X, Y, Z, R] => a.action(v, w, x, y, z, context)
      case _ => f(v, w, x, y, z)
    }
  }

  def exec[U, V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V, U), f: (U, V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v, u) = extract(context)
    f match {
      case a: WithContextAction6[U, V, W, X, Y, Z, R] => a.action(u, v, w, x, y, z, context)
      case _ => f(u, v, w, x, y, z)
    }
  }

  def exec[T, U, V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V, U, T), f: (T, U, V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v, u, t) = extract(context)
    f match {
      case a: WithContextAction7[T, U, V, W, X, Y, Z, R] => a.action(t, u, v, w, x, y, z, context)
      case _ => f(t, u, v, w, x, y, z)
    }
  }
}

