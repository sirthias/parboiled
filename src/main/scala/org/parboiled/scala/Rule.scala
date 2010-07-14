package org.parboiled.scala

import org.parboiled.common.StringUtils.escape
import org.parboiled.{Rule => PRule}
import org.parboiled.matchers._
import java.lang.String

abstract class Rule[+T](var label:String) {
  def toRule:PRule
  def withLabel(label:String) = { this.label = label; this }
  def ? = new StandardRule[T]("Optional", new OptionalMatcher(toRule))
  def * = new StandardRule[T]("ZeroOrMore", new ZeroOrMoreMatcher(toRule))
  def + = new StandardRule[T]("OneOrMore", new OneOrMoreMatcher(toRule))
  def &(other: LeafRule):Rule[T] = new SequenceRule[T](List(other.toRule, toRule)) 
  def &[S](other: Rule[S]):Rule[S] = new SequenceRule[S](List(other.toRule, toRule))
  def |[S >: T](other: Rule[S]) = new FirstOfRule[S](List(other.toRule, toRule))
}

class LeafRule(label:String, val rule:PRule) extends Rule[Nothing](label) {
  def toRule = rule.label(label)
}

class StandardRule[+T](label:String, val rule:PRule) extends Rule[T](label) {
  def toRule = rule.label(label)
}

class SequenceRule[+T](val subs:List[PRule]) extends Rule[T]("Sequence") {
  def toRule = new SequenceMatcher(subs.reverse.toArray).label(label)
  override def &(other: LeafRule):Rule[T] = new SequenceRule[T](other.toRule :: subs) 
  override def &[S](other: Rule[S]):Rule[S] = new SequenceRule[S](other.toRule :: subs)
}

class FirstOfRule[+T](val subs:List[PRule]) extends Rule[T]("FirstOf") {
  def toRule = new FirstOfMatcher(subs.reverse.toArray).label(label)
  override def |[S >: T](other: Rule[S]) = new FirstOfRule[S](other.toRule :: subs)
}

class CharRule(val c:Char) extends LeafRule('\'' + escape(c) + '\'', new CharMatcher(c)) {
  def --(upperBound:Char) = new LeafRule(c + ".." + upperBound, new CharRangeMatcher(c, upperBound))
}

class ProxyRule extends Rule[Any](null) {
  var inner:Rule[Any] = _
  def toRule = {
    require(inner != null)
    if (label != null) inner.toRule.label(label) else inner.toRule
  }
}




