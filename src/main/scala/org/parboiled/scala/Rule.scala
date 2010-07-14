package org.parboiled.scala

import org.parboiled.common.StringUtils.escape
import org.parboiled.{Rule => PRule}
import org.parboiled.matchers._
import java.lang.String

abstract class Rule[+T](private var label:String) { rule =>
  protected def toMatcher:PRule
  def toRule = if (label != null) toMatcher.label(label) else toMatcher
  def withLabel(label:String) = { this.label = label; this }
  def ? = new Rule[T]("Optional") {
    def toMatcher = new OptionalMatcher(rule.toRule)
  }
  def * = new Rule[T]("ZeroOrMore") {
    def toMatcher = new ZeroOrMoreMatcher(rule.toRule)
  }
  def + = new Rule[T]("OneOrMore") {
    def toMatcher = new OneOrMoreMatcher(rule.toRule)
  }
  def &[S](other: Rule[S]):Rule[S] = new CompositeRule[S]("Sequence", List(other, this)) {
    def toMatcher = new SequenceMatcher(subs.reverse.map(_.toRule).toArray)
  }
  def &(other: SimpleRule):Rule[T] = new CompositeRule[T]("Sequence", List(other, this)) {
    def toMatcher = new SequenceMatcher(subs.reverse.map(_.toRule).toArray)
  }
  def |[S >: T](other: Rule[S]):Rule[S] = new CompositeRule[S]("FirstOf", List(other, this)) {
    def toMatcher = new FirstOfMatcher(subs.reverse.map(_.toRule).toArray)
  }
}

abstract class CompositeRule[+T](label:String, var subs:List[Rule[Any]]) extends Rule[T](label) {
  override def &[S](other: Rule[S]):Rule[S] = { subs = other :: subs; this.asInstanceOf[Rule[S]] }
}

class SimpleRule(label:String, val rule:PRule) extends Rule[Nothing](label) {
  protected def toMatcher = rule
}

class CharRule(val c:Char) extends SimpleRule('\'' + escape(c) + '\'', new CharMatcher(c)) {
  def --(upperBound:Char):Rule[Nothing] = {
    new SimpleRule(c + ".." + upperBound, new CharRangeMatcher(c, upperBound))
  }
}

class ProxyRule extends Rule[Any](null) {
  var inner:Rule[Any] = _
  protected def toMatcher = {
    require(inner != null)
    inner.toRule
  }
}




