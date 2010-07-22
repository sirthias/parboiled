package org.parboiled

import matchers._
import org.parboiled.{Action => PAction, Context => PContext}

trait Implicits[V] {
  this: Rules[V] =>

  implicit def toRule(c: Char) = new CharRule(c)

  implicit def toRule(s: String): Rule = toRule(s.toCharArray)

  implicit def toRule(rule: Rule) = rule.toMatcher

  implicit def toRule(chars: Array[Char]): Rule = chars.length match {
    case 0 => EMPTY
    case 1 => toRule(chars(0))
    case _ => new SimpleRule(new StringMatcher(chars.map(toRule).map(_.toMatcher), chars), "\"" + chars + '"')
  }

  /*implicit def toAction(f: PContext[V] => Boolean): Rule =
    new SimpleRule(new ActionMatcher(new PAction {def run(c: PContext[V]): Boolean = f(c)}), "Action")

  implicit def toAction2(f: PContext[V] => Unit) = toAction((c: PContext[V]) => {f(c); true})

  implicit def toAction3(f: PContext[V] => V) = toAction((c: PContext[V]) => {c.push(f(c)); true})

  implicit def toAction4(f: () => Boolean) = toAction((c: PContext[V]) => f())

  implicit def toAction5(f: () => Unit) = toAction((c: PContext[V]) => {f(); true})

  implicit def toAction6(f: () => V) = toAction3((c: PContext[V]) => f())

  implicit def toAction7(f: String => Boolean) = toAction((c: PContext[V]) => f(c.getMatch))

  implicit def toAction8(f: String => Unit) = toAction((c: PContext[V]) => {f(c.getMatch); true})

  implicit def toAction9(f: String => V) = toAction3((c: PContext[V]) => f(c.getMatch))

  implicit def toAction10(f: V => Boolean) = toAction((c: PContext[V]) => f(c.pop().asInstanceOf[V]))

  implicit def toAction11(f: V => Unit) = toAction((c: PContext[V]) => {f(c.pop().asInstanceOf[V]); true})

  implicit def toAction12(f: V => V) = toAction3((c: PContext[V]) => f(c.pop().asInstanceOf[V]))

  def run(f: => Unit) = () => f

  def test(f: => Boolean) = () => f

  def set(f: => V) = () => f
  
  def withMatch[T](f: String => T) = f

  def withValue[T](f: V => T) = f*/
}
