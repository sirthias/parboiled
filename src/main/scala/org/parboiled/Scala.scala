package org.parboiled

import matchers._
import support.Characters
import _root_.scala.collection.mutable
import org.parboiled.{Action => PAction}

object Scala extends Support {

  trait Parser[V] extends Rules[V] {

    sealed class RuleOption
    case object SuppressNode extends RuleOption
    case object SuppressSubnodes extends RuleOption
    case object SkipNode extends RuleOption

    private val cache = mutable.Map.empty[RuleMethod, Rule]
    protected var context:Context[V] = _
    var buildParseTree = false

    def withParseTreeBuilding(): this.type = { buildParseTree = true; this }

    def rule(block: => Rule): Rule = {
      val ruleMethod = getCurrentRuleMethod
      rule(ruleMethod.getMethodName, ruleMethod, Seq.empty, block)
    }

    def rule(label: String, options: RuleOption*)(block: => Rule): Rule = {
      rule(label, getCurrentRuleMethod, options, block)
    }

    private def rule(label: String, key: RuleMethod, options: Seq[RuleOption], block: => Rule): Rule =
      cache.get(key) match {
        case Some(rule) => rule
        case None => {
          val proxy = new ProxyRule
          cache += key -> proxy // protect block from infinite recursion by immediately caching the proxy
          var rule = block.withLabel(label) // evaluate rule definition block
          if (!buildParseTree || options.contains(SuppressNode)) rule = rule.withNodeSuppressed
          if (options.contains(SuppressSubnodes)) rule = rule.withSubnodesSuppressed
          if (options.contains(SkipNode)) rule = rule.withNodeSkipped
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

    def ^?(f: => Boolean) = toTestAction(c => f)
    def ^?(f: String => Boolean) = toTestAction(c => f(c.getMatch))
    def ^(f: => Unit) = toAction(c => f)
    def ^(f: String => Unit) = toAction(c => f(c.getMatch))
    def push(f: => V) = toAction(c => stack.push(f))
    def push(down:Int, f: => V) = toAction(c => stack.push(down, f))
    def poke(f: => V) = toAction(c => stack.poke(f) )
    def poke(down:Int, f: => V) = toAction(c => stack.poke(down, f))
    def push(f: String => V) = toAction(c => stack.push(f(c.getMatch)))
    def push(down:Int, f: String => V) = toAction(c => stack.push(down, f(c.getMatch)))
    def poke(f: String => V) = toAction(c => stack.poke(f(c.getMatch)))
    def poke(down:Int, f: String => V) = toAction(c => stack.poke(down, f(c.getMatch)))

    private def toAction(f: Context[V] => Unit): Rule = new SimpleRule(new ActionMatcher(
      new PAction[V] {
        def run(c: Context[V]): Boolean = {
          context = c
          f(c)
          true
        }
      }), "Action")

    private def toTestAction(f: Context[V] => Boolean): Rule = new SimpleRule(new ActionMatcher(
      new PAction[V] {
        def run(c: Context[V]): Boolean = {
          context = c
          f(c)
        }
      }), "TestAction")

    def stack = context.getValueStack
    def spush(value:V) = stack.push(value)
    def spush(down:Int, value:V) = stack.push(down, value)
    def pushAll(firstValue:V, values:V*) = stack.pushAll(firstValue, values:_*)
    def pushAll(values:Traversable[V]) = values.foreach(v => stack.push(v))
    def pop() = stack.pop()
    def pop(down:Int) = stack.pop(down)
    def dup() = stack.dup()
    def peek = stack.peek()
    def peek(down:Int) = stack.peek(down)
    def spoke(value:V) = stack.poke(value)
    def spoke(down:Int, value:V) = stack.poke(down, value)
    def swap() = stack.swap()
    def swap3() = stack.swap3()
    def swap4() = stack.swap4()
    def swap5() = stack.swap5()
    def swap6() = stack.swap6()

    implicit def toRule(c: Char) = new CharRule(c)
    implicit def toRule(s: String): Rule = toRule(s.toCharArray)
    implicit def toRule(rule: Rule) = rule.toMatcher
    implicit def toRule(chars: Array[Char]): Rule = chars.length match {
      case 0 => EMPTY
      case 1 => toRule(chars(0))
      case _ => new SimpleRule(new StringMatcher(chars.map(toRule).map(_.toMatcher), chars), "\"" + chars + '"')
    }

  }

}