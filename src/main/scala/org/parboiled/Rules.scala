package org.parboiled

import org.parboiled.matchers._
import org.parboiled.support.Characters
import org.parboiled.common.StringUtils.escape

trait Rules {

  trait Rule[+V] {
    def toMatcher: Matcher[_]

    def ~(other: LeafRule): Rule[V] = new SequenceRule[V, Nothing, V](this, other)

    def ~[T](other: Rule[T]): Rule[T] = new SequenceRule[V, T, T](this, other)

    def |[T >: V](other: Rule[T]): Rule[T] = new FirstOfRule[T](this, other)

    override def toString = getClass.getSimpleName // + " @" + System.identityHashCode(this)
  }

  class LeafRule(val matcher: Matcher[_]) extends Rule[Nothing] {
    def withLabel(label: String) = new LeafRule(matcher.label(label).asInstanceOf[Matcher[_]])

    def toMatcher = matcher
  }

  trait NonLeafRule[+V] extends Rule[V] {
    private var proxies: Option[collection.mutable.ListBuffer[ProxyMatcher[_]]] = None

    def withLabel(label: String): NonLeafRule[V] = new LabelRule(this, label)

    def registerProxy(matcher: ProxyMatcher[_]) {
      proxies match {
        case Some(list) => list += matcher
        case None => proxies = Some(collection.mutable.ListBuffer(matcher))
      }
    }

    def updateProxies[T](matcher: Matcher[T]): Matcher[T] = proxies match {
      case Some(list) => {for (p <- list) p.asInstanceOf[ProxyMatcher[T]].arm(matcher); matcher}
      case None => matcher
    }
  }

  trait UnaryRule[+V] extends NonLeafRule[V] {
    val sub: Rule[V]
  }

  trait BinaryRule[+L, +R, +V] extends NonLeafRule[V] {
    val left: Rule[L]
    val right: Rule[R]
    }

  class OneOrMoreRule[+V](val sub: Rule[V]) extends UnaryRule[V] {
    lazy val toMatcher = updateProxies(new OneOrMoreMatcher(sub.toMatcher).label("OneOrMore"))
  }

  class ZeroOrMoreRule[+V](val sub: Rule[V]) extends UnaryRule[V] {
    lazy val toMatcher = updateProxies(new ZeroOrMoreMatcher(sub.toMatcher).label("ZeroOrMore"))
  }

  class OptionalRule[+V](val sub: Rule[V]) extends UnaryRule[V] {
    lazy val toMatcher = updateProxies(new OptionalMatcher(sub.toMatcher).label("Optional"))
  }

  class TestRule(val sub: Rule[_]) extends NonLeafRule[Nothing] {
    lazy val toMatcher = updateProxies(new TestMatcher(sub.toMatcher).label("Test"))
  }

  class TestNotRule(val sub: Rule[_]) extends NonLeafRule[Nothing] {
    lazy val toMatcher = updateProxies(new TestNotMatcher(sub.toMatcher).label("TestNot"))
  }

  class SequenceRule[+L, +R, +V](val left: Rule[L], val right: Rule[R]) extends BinaryRule[L, R, V] {
    lazy val toMatcher = updateProxies(new SequenceMatcher(groupLeft.reverse.toArray).label("Sequence"))
    def groupLeft: List[Matcher[_]] = left match {
      case left: SequenceRule[_,_,_] => right.toMatcher :: left.groupLeft
      case _ => List(right.toMatcher, left.toMatcher)
    }
  }

  class FirstOfRule[+V](val left: Rule[V], val right: Rule[V]) extends BinaryRule[V, V, V] {
    lazy val toMatcher = updateProxies(new FirstOfMatcher(groupLeft.reverse.toArray).label("FirstOf"))
    def groupLeft: List[Matcher[_]] = left match {
      case left: FirstOfRule[_] => right.toMatcher :: left.groupLeft
      case _ => List(right.toMatcher, left.toMatcher)
    }
  }

  class LabelRule[+V](val sub: Rule[V], val label: String) extends UnaryRule[V] {
    lazy val toMatcher = updateProxies(sub.toMatcher.label(label).asInstanceOf[Matcher[_]])
  }

  class CharRule(val c: Char) extends LeafRule(new CharMatcher(c).label('\'' + escape(c) + '\'')) {
    def --(upperBound: Char) = new LeafRule(new CharRangeMatcher(c, upperBound).label(c + ".." + upperBound))
  }

  class ProxyRule[V] extends NonLeafRule[V] {
    private var inner: NonLeafRule[V] = _

    lazy val toMatcher = {
      require(inner != null)
      val proxy = new ProxyMatcher[V]()
      inner.registerProxy(proxy)
      proxy
    }

    def arm(inner: NonLeafRule[V]): NonLeafRule[V] = {this.inner = inner; inner}
  }

  lazy val EMPTY = new LeafRule(new EmptyMatcher().label("EMPTY"))
  lazy val ANY = new LeafRule(new AnyMatcher().label("ANY"))
  lazy val EOI = new LeafRule(new CharMatcher(Characters.EOI).label("EOI"))
}