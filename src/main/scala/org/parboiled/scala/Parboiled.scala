package org.parboiled.scala

import org.parboiled.{Action => PAction, Rule => PRule, Context => PContext}
import org.parboiled.matchers._
import org.parboiled.support.Characters
import org.parboiled.common.StringUtils.escape

object Parboiled {

  // ******************* RULES **********************

  trait Rule[+V] {
    def toRule: PRule

    def ? = new OptionalRule(this)

    def * = new ZeroOrMoreRule(this)

    def + = new OneOrMoreRule(this)

    def &(other: LeafRule): Rule[V] = new SequenceRule[V, Nothing, V](this, other)

    def &[T](other: Rule[T]): Rule[T] = new SequenceRule[V, T, T](this, other)

    def |[T >: V](other: Rule[T]) = new FirstOfRule[T](this, other)
  }

  trait NonLeafRule[+V] extends Rule[V] {
    def withLabel(label: String) = new LabelRule(this, label)
  }

  trait CompositeRule[+L, +R, +V] extends NonLeafRule[V] {
    val left: Rule[L]
    val right: Rule[R]

    def groupLeft: List[PRule] = left match {
      case left: CompositeRule[_, _, _] => right.toRule :: left.groupLeft
      case _ => List(right.toRule, left.toRule)
    }
  }

  class LeafRule(val rule: PRule) extends Rule[Nothing] {
    def withLabel(label: String) = new LeafRule(rule.label(label))

    def toRule = rule
  }

  class OneOrMoreRule[+V](val sub: Rule[V]) extends NonLeafRule[V] {
    def toRule = new OneOrMoreMatcher(sub.toRule)
  }

  class ZeroOrMoreRule[+V](val sub: Rule[V]) extends NonLeafRule[V] {
    def toRule = new ZeroOrMoreMatcher(sub.toRule)
  }

  class OptionalRule[+V](val sub: Rule[V]) extends NonLeafRule[V] {
    def toRule = new OptionalMatcher(sub.toRule)
  }

  class SequenceRule[+L, +R, +V](val left: Rule[L], val right: Rule[R]) extends CompositeRule[L, R, V] {
    def toRule = new SequenceMatcher(groupLeft.reverse.toArray)
  }

  class FirstOfRule[+V](val left: Rule[V], val right: Rule[V]) extends CompositeRule[V, V, V] {
    def toRule = new FirstOfMatcher(groupLeft.reverse.toArray)
  }

  class LabelRule[+V](val sub: Rule[V], val label: String) extends NonLeafRule[V] {
    def toRule = sub.toRule.label(label)
  }

  class CharRule(val c: Char) extends LeafRule(new CharMatcher(c).label('\'' + escape(c) + '\'')) {
    def --(upperBound: Char) = new LeafRule(new CharRangeMatcher(c, upperBound).label(c + ".." + upperBound))
  }

  class ProxyRule extends NonLeafRule[Any] {
    var inner: NonLeafRule[Any] = _

    def toRule = {require(inner != null); inner.toRule}
  }

  class ActionRule[V](val action: PAction[V]) extends Rule[V] {
    def this(action: PContext[V] => Boolean) = {
      this (new PAction[V] {
        def run(c: PContext[V]): Boolean = action(c.asInstanceOf[PContext[V]])
      })
    }

    def toRule = new ActionMatcher(action)
  }

  // ******************* PARSER **********************

  trait Parser {
    private val cache = scala.collection.mutable.Map.empty[RuleMethod, Rule[Any]]

    def rule[T <: Rule[_]](block: => T): T = {
      val ruleMethod = getCurrentRuleMethod
      rule(ruleMethod.getMethodName, ruleMethod, block)
    }

    def rule[T <: Rule[_]](label: String)(block: => T): T = {
      rule(label, getCurrentRuleMethod, block)
    }

    private def rule[T <: Rule[_]](label: String, key: RuleMethod, block: => T): T = cache.get(key) match {
      case Some(rule) => rule.asInstanceOf[T]
      case None => {
        val proxy = new ProxyRule
        cache += key -> proxy // protect block from infinite recursion with a proxy
        val rule = block match { // evaluate block
          case r: NonLeafRule[_] => {
            proxy.inner = r.withLabel(label) // arm proxy (in case it has been returned from the cache in a recursion)
            proxy.inner 
          }
          case r: LeafRule => r.withLabel(label)
          case _ => throw new IllegalStateException
        }
        cache += key -> proxy.inner
        rule.asInstanceOf[T]
      }
    }
  }

  // ******************* BASICS **********************

  type RuleMethod = StackTraceElement

  lazy val EMPTY = new LeafRule(new EmptyMatcher().label("EMPTY"))
  lazy val ANY = new LeafRule(new AnyMatcher().label("ANY"))
  lazy val EOI = new LeafRule(new CharMatcher(Characters.EOI).label("EOI"))

  private def getCurrentRuleMethod: StackTraceElement = {
    try {
      throw new Throwable
    } catch {
      case t: Throwable => t.getStackTrace()(1)
    }
  }

  def anyOf(s: String): LeafRule = anyOf(s.toCharArray)

  def anyOf(chars: Array[Char]): LeafRule = chars.length match {
    case 0 => EMPTY
    case 1 => toRule(chars(0))
    case _ => anyOf(Characters.of(chars: _*))
  }

  def anyOf(chars: Characters): LeafRule = {
    if (!chars.isSubtractive && chars.getChars().length == 1)
      toRule(chars.getChars()(0))
    else
      new LeafRule(new CharSetMatcher(chars).label(chars.toString))
  }

  // ******************* IMPLICITS **********************

  implicit def toRule(c: Char) = new CharRule(c)

  implicit def toRule(s: String):LeafRule = toRule(s.toCharArray)

  implicit def toRule[T](rule: Rule[T]) = rule.toRule

  implicit def toRule(chars: Array[Char]):LeafRule = chars.length match {
    case 0 => EMPTY
    case 1 => toRule(chars(0))
    case _ => new LeafRule(new StringMatcher(chars.map(toRule).map(_.toRule), chars).label("\"" + chars + '"'))
  }

}