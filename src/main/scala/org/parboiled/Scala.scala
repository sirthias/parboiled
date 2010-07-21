package org.parboiled

import matchers._
import support.Characters
import _root_.scala.collection.mutable

object Scala extends Support {

  trait Parser[V] extends Rules[V] with Implicits[V] {
    private val cache = mutable.Map.empty[RuleMethod, Rule]

    def rule(block: => Rule): Rule = {
      val ruleMethod = getCurrentRuleMethod
      rule(ruleMethod.getMethodName, ruleMethod, block)
    }

    def rule(label: String)(block: => Rule): Rule = {
      rule(label, getCurrentRuleMethod, block)
    }

    private def rule(label: String, key: RuleMethod, block: => Rule): Rule = cache.get(key) match {
      case Some(rule) => rule
      case None => {
        val proxy = new ProxyRule
        cache += key -> proxy // protect block from infinite recursion by immediately caching the proxy
        val rule = block.withLabel(label) // evaluate rule definition block
        proxy.arm(rule) // arm the proxy in case it is in use
        cache += key -> rule // replace the cache value with the actual rule (overwriting the proxy)
        rule
      }
    }

    def &(sub: Rule) = new UnaryRule(sub, new TestMatcher(_), "Test")

    def !(sub: Rule) = new UnaryRule(sub, new TestNotMatcher(_), "TestNot")

    def optional(sub: Rule) = new UnaryRule(sub, new OptionalMatcher(_), "Optional")

    def zeroOrMore(sub: Rule) = new UnaryRule(sub, new ZeroOrMoreMatcher(_), "ZeroOrMore")

    def oneOrMore[V](sub: Rule) = new UnaryRule(sub, new OneOrMoreMatcher(_), "OneOrMore")

    def anyOf(s: String): Rule = anyOf(s.toCharArray)

    def anyOf(chars: Array[Char]): Rule = chars.length match {
      case 0 => EMPTY
      case 1 => toRule(chars(0))
      case _ => anyOf(Characters.of(chars: _*))
    }

    def anyOf(chars: Characters): Rule = {
      if (!chars.isSubtractive && chars.getChars().length == 1)
        toRule(chars.getChars()(0))
      else
        new SimpleRule(new CharSetMatcher(chars), chars.toString)
    }

  }

}