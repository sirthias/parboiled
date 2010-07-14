package org.parboiled.scala

import org.parboiled.support.Characters
import org.parboiled.matchers._

trait Parser {
  type RuleMethod = StackTraceElement

  private val cache = scala.collection.mutable.Map.empty[RuleMethod, Rule[Any]]

  lazy val EMPTY = new LeafRule("EMPTY", new EmptyMatcher())
  lazy val ANY = new LeafRule("ANY", new AnyMatcher())
  lazy val EOI = new LeafRule("EOI", new CharMatcher(Characters.EOI))

  def rule[T <: Rule[_]](block: => T): T = {
    val ruleMethod = Support.getCurrentRuleMethod
    rule(ruleMethod.getMethodName, ruleMethod, block)
  }

  def rule[T <: Rule[_]](label:String)(block: => T): T = {
    rule(label, Support.getCurrentRuleMethod, block)
  }

  private def rule[T <: Rule[_]](label:String, key:RuleMethod, block: => T): T = cache.get(key) match {
    case Some(rule) => rule.asInstanceOf[T]
    case None => {
      val proxy = new ProxyRule
      cache += key -> proxy // protect block from infinite recursion with a proxy
      val rule = block.withLabel(label) // evaluate block
      cache += key -> proxy.inner
      proxy.inner = rule // arm proxy (in case it has been returned from the cache in the case of recursions)
      rule.asInstanceOf[T]
    }
  }

  def anyOf(s: String):Rule[Nothing] = {
    anyOf(s.toCharArray)
  }

  def anyOf(chars: Array[Char]):Rule[Nothing] = chars.length match {
    case 0 => EMPTY
    case 1 => charToRule(chars(0))
    case _ => anyOf(Characters.of(chars: _*))
  }

  def anyOf(chars: Characters):Rule[Nothing] = {
    if (!chars.isSubtractive && chars.getChars().length == 1)
      charToRule(chars.getChars()(0))
    else
      new LeafRule(chars.toString, new CharSetMatcher(chars))
  }

  implicit def charToRule(c: Char) = new CharRule(c)

  def stringToRule(s: String) = charArrayToRule(s.toCharArray)

  implicit def toRule[T](rule: Rule[T]) = rule.toRule

  implicit def charArrayToRule(chars: Array[Char]) = chars.length match {
    case 0 => EMPTY
    case 1 => charToRule(chars(0))
    case _ => new LeafRule("\"" + chars + '"', new StringMatcher(chars.map(charToRule).map(_.toRule), chars))
  }

}