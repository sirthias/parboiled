package org.parboiled.scala

import org.parboiled.{Action => PAction, Context => PContext}
import org.parboiled.matchers._
import org.parboiled.support.Characters
import org.parboiled.common.StringUtils.escape

object Parboiled {

  // ******************* RULES **********************

  trait Rule[+V] {
    def toMatcher: Matcher[_]

    def &(other: LeafRule): Rule[V] = new SequenceRule[V, Nothing, V](this, other)

    def &[T](other: Rule[T]): Rule[T] = new SequenceRule[V, T, T](this, other)

    def |[T >: V](other: Rule[T]): Rule[T] = new FirstOfRule[T](this, other)

    override def toString = getClass.getSimpleName // + " @" + System.identityHashCode(this)
  }

  class LeafRule(val matcher: Matcher[_]) extends Rule[Nothing] {
    def withLabel(label: String) = new LeafRule(matcher.label(label).asInstanceOf[Matcher[_]])

    def toMatcher = matcher
  }

  trait NonLeafRule[+V] extends Rule[V] {
    lazy val proxies = collection.mutable.ListBuffer.empty[ProxyMatcher[_]]
    def withLabel(label: String): NonLeafRule[V] = new LabelRule(this, label)
    def registerProxy(proxy:ProxyMatcher[_]) { proxies += proxy }
    def updateProxies(matcher:Matcher[_]):Matcher[_] = { for (p <- proxies) p.arm(matcher); matcher}
  }

  trait BinaryRule[+L, +R, +V] extends NonLeafRule[V] {
    val left: Rule[L]
    val right: Rule[R]
    def groupLeft: List[Matcher[_]] = left match {
      case left: this.type => right.toMatcher :: left.groupLeft
      case _ => List(right.toMatcher, left.toMatcher)
    }
  }

  trait UnaryRule[+V] extends NonLeafRule[V] {
    val sub: Rule[V]
  }

  class OneOrMoreRule[+V](val sub: Rule[V]) extends UnaryRule[V] {
    def toMatcher = new OneOrMoreMatcher(sub.toMatcher)
  }

  class ZeroOrMoreRule[+V](val sub: Rule[V]) extends UnaryRule[V] {
    def toMatcher = new ZeroOrMoreMatcher(sub.toMatcher)
  }

  class OptionalRule[+V](val sub: Rule[V]) extends UnaryRule[V] {
    def toMatcher = new OptionalMatcher(sub.toMatcher)
  }

  class SequenceRule[+L, +R, +V](val left: Rule[L], val right: Rule[R]) extends BinaryRule[L, R, V] {
    def toMatcher = new SequenceMatcher(groupLeft.reverse.toArray)
  }

  class FirstOfRule[+V](val left: Rule[V], val right: Rule[V]) extends BinaryRule[V, V, V] {
    def toMatcher = new FirstOfMatcher(groupLeft.reverse.toArray)
  }

  class LabelRule[+V](val sub: Rule[V], val label: String) extends UnaryRule[V] {
    def toMatcher = sub.toMatcher.label(label)
  }

  class CharRule(val c: Char) extends LeafRule(new CharMatcher(c).label('\'' + escape(c) + '\'')) {
    def --(upperBound: Char) = new LeafRule(new CharRangeMatcher(c, upperBound).label(c + ".." + upperBound))
  }

  class ProxyRule extends NonLeafRule[Any] {
    var inner: NonLeafRule[Any] = _

    def toMatcher = {
      require(inner != null)
      val proxy = new ProxyMatcher()
      inner.registerProxy(proxy)
      proxy
    }
  }

  class ActionRule[V](val action: PAction[V]) extends Rule[V] {
    def toMatcher = new ActionMatcher(action)
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
        cache += key -> rule
        rule.asInstanceOf[T]
      }
    }
  }

  // ******************* BASICS **********************

  lazy val EMPTY = new LeafRule(new EmptyMatcher().label("EMPTY"))
  lazy val ANY = new LeafRule(new AnyMatcher().label("ANY"))
  lazy val EOI = new LeafRule(new CharMatcher(Characters.EOI).label("EOI"))

  def ??[V](sub:Rule[V]) : NonLeafRule[V] = new OptionalRule(sub)

  def **[V](sub:Rule[V]) : NonLeafRule[V] = new ZeroOrMoreRule(sub)
  
  def ++[V](sub:Rule[V]) : NonLeafRule[V] = new OneOrMoreRule(sub)

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

  type RuleMethod = StackTraceElement

  private def getCurrentRuleMethod: StackTraceElement = {
    try {
      throw new Throwable
    } catch {
      case t: Throwable => t.getStackTrace()(3)
    }
  }

  case class Val[T](value: T)

  // ******************* IMPLICITS **********************

  implicit def toRule(c: Char) = new CharRule(c)

  implicit def toRule(s: String): LeafRule = toRule(s.toCharArray)

  implicit def toRule[T](rule: Rule[T]) = rule.toMatcher

  implicit def toRule(chars: Array[Char]): LeafRule = chars.length match {
    case 0 => EMPTY
    case 1 => toRule(chars(0))
    case _ => new LeafRule(new StringMatcher(chars.map(toRule).map(_.toMatcher), chars).label("\"" + chars + '"'))
  }

  implicit def toAction[V, T](f: PContext[V] => Boolean): Rule[T] = {
    val action = new PAction[V] {
      def run(c: PContext[V]): Boolean = f(c.asInstanceOf[PContext[V]])
    }
    new ActionRule[T](action.asInstanceOf[PAction[T]])
  }

  implicit def toAction2[V, T](f: PContext[V] => Unit): Rule[T] =
    toAction((c: PContext[V]) => {f(c); true})

  implicit def toAction3[V, T](f: PContext[V] => Val[T]): Rule[T] =
    toAction((c: PContext[V]) => {c.asInstanceOf[PContext[T]].setNodeValue(f(c).value); true})

  implicit def toAction4[T](f: Unit => Boolean): Rule[T] =
    toAction((c: PContext[_]) => f())

  implicit def toAction5[T](f: Unit => Unit): Rule[T] =
    toAction((c: PContext[_]) => {f(); true})

  implicit def toAction6[T](f: Unit => Val[T]): Rule[T] =
    toAction3((c: PContext[_]) => f())

  implicit def toAction7[T](f: String => Boolean): Rule[T] =
    toAction((c: PContext[_]) => f(c.getPrevText))

  implicit def toAction8[T](f: String => Unit): Rule[T] =
    toAction((c: PContext[_]) => {f(c.getPrevText); true})

  implicit def toAction9[T](f: String => Val[T]): Rule[T] =
    toAction3((c: PContext[_]) => f(c.getPrevText))

  implicit def toAction10[V, T](f: Val[V] => Boolean): Rule[T] =
    toAction((c: PContext[V]) => f(Val(c.getPrevValue)))

  implicit def toAction11[V, T](f: Val[V] => Unit): Rule[T] =
    toAction((c: PContext[V]) => {f(Val(c.getPrevValue)); true})

  implicit def toAction12[V, T](f: Val[V] => Val[T]): Rule[T] =
    toAction3((c: PContext[V]) => f(Val(c.getPrevValue)))

}