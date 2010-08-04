package org.parboiled.scala

import org.parboiled.{Context, Action}
import org.parboiled.matchers._
import collection.mutable.ListBuffer

abstract class MatcherCreator {
  private var proxies: Option[ListBuffer[ProxyMatcher]] = None

  var label:String = _
  var suppressNode = false
  var suppressSubnodes = false
  var skipNode = false

  def createMatcher(): Matcher

  lazy val toMatcher = {
    var matcher = createMatcher
    if (label != null) matcher = matcher.label(label).asInstanceOf[Matcher]
    if (suppressNode) matcher = matcher.suppressNode().asInstanceOf[Matcher]
    if (suppressSubnodes) matcher = matcher.suppressSubnodes().asInstanceOf[Matcher]
    if (skipNode) matcher = matcher.skipNode().asInstanceOf[Matcher]
    for (list <- proxies; p <- list) p.arm(matcher)
    matcher
  }

  def registerProxy(matcher: ProxyMatcher) {
    proxies match {
      case Some(list) => list += matcher
      case None => proxies = Some(ListBuffer(matcher))
    }
  }

  def appendSeq(other: MatcherCreator): MatcherCreator = new SequenceCreator(List(this, other))

  def appendSeq(action: Action[Any]): MatcherCreator = appendSeq(new ActionCreator(action))

  def appendSeqS[R](f: String => R): MatcherCreator = appendSeq(new Action[Any] {
    def run(c: Context[Any]): Boolean = {
      c.getValueStack.push(f(c.getMatch)); true
    }
  })

  def appendSeq1[Z, R](f: Z => R) = appendSeq(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val r = f(z)
      vs.push(r)
      true
    }
  })

  def appendSeq2[Y, Z, R](f: (Y, Z) => R) = appendSeq(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop.asInstanceOf[Z]
      val y = vs.pop.asInstanceOf[Y]
      val r = f(y, z)
      vs.push(r)
      true
    }
  })

  def appendSeq3[X, Y, Z, R](f: (X, Y, Z) => R) = appendSeq(new Action[Any] {
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

  def appendSeq4[W, X, Y, Z, R](f: (W, X, Y, Z) => R) = appendSeq(new Action[Any] {
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

  def appendSeq5[V, W, X, Y, Z, R](f: (V, W, X, Y, Z) => R) = appendSeq(new Action[Any] {
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

  def appendSeq6[U, V, W, X, Y, Z, R](f: (U, V, W, X, Y, Z) => R) = appendSeq(new Action[Any] {
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

  def appendSeq7[T, U, V, W, X, Y, Z, R](f: (T, U, V, W, X, Y, Z) => R) = appendSeq(new Action[Any] {
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

  def appendChoice(other: MatcherCreator): MatcherCreator = new FirstOfCreator(List(this, other))

  override def toString = (if (label != null) label + ": " else "") + getClass.getSimpleName
}

class SimpleCreator(val creator: () => Matcher) extends MatcherCreator {
  def createMatcher() = creator()
}

class UnaryCreator(val sub: MatcherCreator, val creator: Matcher => Matcher) extends MatcherCreator {
  def createMatcher() = creator(sub.toMatcher)
}

abstract class NaryCreator(var subs: List[MatcherCreator]) extends MatcherCreator

class SequenceCreator(sbs: List[MatcherCreator]) extends NaryCreator(sbs) {
  override def appendSeq(other: MatcherCreator) = {subs = other :: subs; this}

  def createMatcher() = new SequenceMatcher(subs.reverse.map(_.toMatcher).toArray).label("Sequence")
}

class FirstOfCreator(sbs: List[MatcherCreator]) extends NaryCreator(sbs) {
  override def appendChoice(other: MatcherCreator) = {subs = other :: subs; this}

  def createMatcher() = new FirstOfMatcher(subs.reverse.map(_.toMatcher).toArray).label("FirstOf")
}

class ActionCreator(val action: Action[_]) extends MatcherCreator {
  def createMatcher() = new ActionMatcher(action).label("Action")
}

class ProxyCreator extends MatcherCreator {
  var creator: () => Matcher = _
  def createMatcher() = creator()
}