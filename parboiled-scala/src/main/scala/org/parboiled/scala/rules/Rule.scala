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

package org.parboiled.scala
package rules

import org.parboiled.matchers._
import org.parboiled.Action
import org.parboiled.Context
import java.lang.String
import Rule._
import org.parboiled.support.{IndexRange, ValueStack}

/**
 * The base class of all scala parser rules.
 */
abstract class Rule {
  val matcher: Matcher

  /**
   * Creates a "NOT" syntactic predicate according to the PEG formalism.
   */
  def unary_! : Rule0 = new TestNotMatcher(matcher)

  /**
   * Connects two rules into a rule a sequence.
   */
  def ~ (other: Rule0): this.type = withMatcher(append(other))

  /**
   * Creates a semantic predicate on the first char of the input text matched by the immediately preceding rule.
   */
  def ~:? (f: Char => Boolean): this.type = withMatcher(append(exec(GetMatchedChar, f)))

  /**
   * Creates a semantic predicate on the input text matched by the immediately preceding rule.
   */
  def ~? (f: String => Boolean): this.type = withMatcher(append(exec(GetMatch, f)))

  /**
   * Creates a simple parser action with the first char of the input text matched by the immediately preceding
   * rule as parameter.
   */
  def ~:% (f: Char => Unit): this.type = ~:?(ok(f))
  
  /**
   * Creates a simple parser action with the input text matched by the immediately preceding rule as parameter.
   */
  def ~% (f: String => Unit): this.type = ~?(ok(f))
  
  def label(label: String): this.type = withMatcher(matcher.label(label).asInstanceOf[Matcher])

  def suppressNode: this.type = withMatcher(matcher.suppressNode().asInstanceOf[Matcher])

  def suppressSubnodes: this.type = withMatcher(matcher.suppressSubnodes().asInstanceOf[Matcher])

  def skipNode: this.type = withMatcher(matcher.skipNode().asInstanceOf[Matcher])

  def memoMismatches: this.type = withMatcher(matcher.memoMismatches().asInstanceOf[Matcher])

  override def toString = getClass.getSimpleName + ": " + matcher.toString

  protected def withMatcher(matcher: Matcher): this.type

  protected def append(action: Action[_]): Matcher = append(new ActionMatcher(action).label(nameAction("")))

  protected def append(f: Context[Any] => Boolean): Matcher = append(action(f))

  protected def append(other: Rule): Matcher = append(other.matcher)

  protected def append(other: Matcher): Matcher = matcher match {
    case m: SequenceMatcher if (m.getLabel == "Sequence") => new SequenceMatcher(addSub(m.getChildren, other))
    case _ => new SequenceMatcher(Array(matcher, other))
  }

  protected def appendChoice(other: Rule): Matcher = appendChoice(other.matcher)

  protected def appendChoice(other: Matcher): Matcher = matcher match {
    case m: StringMatcher => other match {
      case o: StringMatcher => new FirstOfStringsMatcher(Array(m, o), Array(m.characters, o.characters))
      case _ => new FirstOfMatcher(Array(matcher, other))
    }
    case m: FirstOfStringsMatcher if (m.getLabel == "FirstOf") => other match {
      case o: StringMatcher => new FirstOfStringsMatcher(addSub(m.getChildren, o), addSub(m.strings, o.characters))
      case _ => new FirstOfMatcher(addSub(m.getChildren, other))
    }
    case m: FirstOfMatcher if (m.getLabel == "FirstOf") => new FirstOfMatcher(addSub(m.getChildren, other))
    case _ => new FirstOfMatcher(Array(matcher, other))
  }
}

object Rule {
  private[parboiled] val GetMatch: (Context[Any] => String) = _.getMatch
  private[parboiled] val GetMatchedChar: (Context[Any] => Char) = _.getFirstMatchChar
  private[parboiled] val GetMatchRange: (Context[Any] => IndexRange) = _.getMatchRange
  private[parboiled] val Pop = (vs:ValueStack[Any], _:Int) => vs.pop
  private[parboiled] val Peek: ((ValueStack[Any], Int) => Any) = _.peek(_)

  private def addSub(subs: java.util.List[Matcher], element: Matcher): Array[org.parboiled.Rule] = {
    val count = subs.size
    val array = new Array[org.parboiled.Rule](count + 1)
    subs.toArray(array)
    array(count) = element
    array
  }

  private def addSub(subs: Array[Array[Char]], characters: Array[Char]): Array[Array[Char]] = {
    val count = subs.length
    val array = new Array[Array[Char]](count + 1)
    Array.copy(subs, 0, array, 0, count)
    array(count) = characters
    array
  }

  private[parboiled] def action(f: Context[Any] => Boolean) = new Action[Any] {
    def run(context: Context[Any]) = f(context)
  }

  private[parboiled] def push(f: Context[Any] => Any) = (context: Context[Any]) => {
    context.getValueStack.push(f(context))
    true
  }

  private[parboiled] def ok[A](f: A => Any): A => Boolean = {
    f match {
      case wca: WithContextAction1[A, Any] => new WithContextAction1({ (a, ctx) => wca.action(a, ctx); true })
      case _ => { a => f(a); true }
    }
  }

  private[parboiled] def stack1[Z](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => {
    get(c.getValueStack, 0).asInstanceOf[Z]
  }

  private[parboiled] def stack2[Z, Y](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y]
          )

  private[parboiled] def stack3[Z, Y, X](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X]
          )

  private[parboiled] def stack4[Z, Y, X, W](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W]
          )

  private[parboiled] def stack5[Z, Y, X, W, V](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V]
          )

  private[parboiled] def stack6[Z, Y, X, W, V, U](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V],
          get(c.getValueStack, 5).asInstanceOf[U]
          )

  private[parboiled] def stack7[Z, Y, X, W, V, U, T](get: (ValueStack[Any], Int) => Any) = (c: Context[Any]) => (
          get(c.getValueStack, 0).asInstanceOf[Z],
          get(c.getValueStack, 1).asInstanceOf[Y],
          get(c.getValueStack, 2).asInstanceOf[X],
          get(c.getValueStack, 3).asInstanceOf[W],
          get(c.getValueStack, 4).asInstanceOf[V],
          get(c.getValueStack, 5).asInstanceOf[U],
          get(c.getValueStack, 6).asInstanceOf[T]
          )

  private[parboiled] def exec[Z, R](extract: Context[Any] => Z, f: Z => R) = (context: Context[Any]) => {
    val z = extract(context)
    f match {
      case a: WithContextAction1[Z, R] => a.action(z, context)
      case _ => f(z)
    }
  }

  private[parboiled] def exec[Y, Z, R](extract: Context[Any] => (Z, Y), f: (Y, Z) => R) = (context: Context[Any]) => {
    val (z, y) = extract(context)
    f match {
      case a: WithContextAction2[Y, Z, R] => a.action(y, z, context)
      case _ => f(y, z)
    }
  }

  private[parboiled] def exec[X, Y, Z, R](extract: Context[Any] => (Z, Y, X), f: (X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x) = extract(context)
    f match {
      case a: WithContextAction3[X, Y, Z, R] => a.action(x, y, z, context)
      case _ => f(x, y, z)
    }
  }

  private[parboiled] def exec[W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W), f: (W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w) = extract(context)
    f match {
      case a: WithContextAction4[W, X, Y, Z, R] => a.action(w, x, y, z, context)
      case _ => f(w, x, y, z)
    }
  }

  private[parboiled] def exec[V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V), f: (V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v) = extract(context)
    f match {
      case a: WithContextAction5[V, W, X, Y, Z, R] => a.action(v, w, x, y, z, context)
      case _ => f(v, w, x, y, z)
    }
  }

  private[parboiled] def exec[U, V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V, U), f: (U, V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v, u) = extract(context)
    f match {
      case a: WithContextAction6[U, V, W, X, Y, Z, R] => a.action(u, v, w, x, y, z, context)
      case _ => f(u, v, w, x, y, z)
    }
  }

  private[parboiled] def exec[T, U, V, W, X, Y, Z, R](extract: Context[Any] => (Z, Y, X, W, V, U, T), f: (T, U, V, W, X, Y, Z) => R) = (context: Context[Any]) => {
    val (z, y, x, w, v, u, t) = extract(context)
    f match {
      case a: WithContextAction7[T, U, V, W, X, Y, Z, R] => a.action(t, u, v, w, x, y, z, context)
      case _ => f(t, u, v, w, x, y, z)
    }
  }
}

