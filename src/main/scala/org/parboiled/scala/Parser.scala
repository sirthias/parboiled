package org.parboiled.scala

import org.parboiled.matchers._
import _root_.scala.collection.mutable
import org.parboiled.common.StringUtils.escape
import Support._
import org.parboiled.{Action, Context}
import org.parboiled.support.{ValueStack, Characters}

trait Parser {

  sealed class RuleOption
  case object SuppressNode extends RuleOption
  case object SuppressSubnodes extends RuleOption
  case object SkipNode extends RuleOption

  private val cache = mutable.Map.empty[RuleMethod, Rule]
  var buildParseTree = false

  def withParseTreeBuilding(): this.type = { buildParseTree = true; this }

  def rule[T <: Rule](block: => T)(implicit manifest: Manifest[T]): T = {
    val ruleMethod = getCurrentRuleMethod
    rule(ruleMethod.getMethodName, ruleMethod, Seq.empty, block, manifest)
  }

  def rule[T <: Rule](label: String, options: RuleOption*)(block: => T)(implicit manifest: Manifest[T]): T = {
    rule(label, getCurrentRuleMethod, options, block, manifest)
  }

  private def rule[T <: Rule](label: String, key: RuleMethod, options: Seq[RuleOption], block: => T, manifest: Manifest[T]): T =
    cache.get(key) match {
      case Some(rule) => rule.asInstanceOf[T]
      case None => {
        val proxy = new ProxyMatcher
        // protect block from infinite recursion by immediately caching a new Rule of type T wrapping the proxy creator
        cache += key -> manifest.erasure.getConstructor(classOf[Matcher]).newInstance(proxy).asInstanceOf[T]
        var rule = block.withLabel(label) // evaluate rule definition block
        if (!buildParseTree || options.contains(SuppressNode)) rule = rule.withNodeSuppressed
        if (options.contains(SuppressSubnodes)) rule = rule.withSubnodesSuppressed
        if (options.contains(SkipNode)) rule = rule.withNodeSkipped
        proxy.arm(rule.matcher) // arm the proxy in case it is in use
        cache += key -> rule // replace the cache value with the actual rule (overwriting the proxy rule)
        rule
      }
    }

  def &(sub: Rule) = new Rule0(new TestMatcher(sub.matcher).label("Test"))
  def optional(sub: Rule0) = new Rule0(new OptionalMatcher(sub.matcher).label("Optional"))
  def optional[Z](sub: ReductionRule1[Z, Z]) = new ReductionRule1[Z, Z](new OptionalMatcher(sub.matcher).label("Optional"))
  def zeroOrMore(sub: Rule0) = new Rule0(new ZeroOrMoreMatcher(sub.matcher).label("ZeroOrMore"))
  def zeroOrMore[Z](sub: ReductionRule1[Z, Z]) = new ReductionRule1[Z, Z](new ZeroOrMoreMatcher(sub.matcher).label("ZeroOrMore"))
  def oneOrMore(sub: Rule0) = new Rule0(new OneOrMoreMatcher(sub.matcher).label("OneOrMore"))
  def oneOrMore[Z](sub: ReductionRule1[Z, Z]) = new ReductionRule1[Z, Z](new OneOrMoreMatcher(sub.matcher).label("OneOrMore"))

  def ignoreCase(c: Char): Rule0 = new Rule0(new CharIgnoreCaseMatcher(c).label("'" + escape(c.toLower) + '/' + escape(c.toUpper) + "'"))
  def ignoreCase(s: String): Rule0 = ignoreCase(s.toCharArray)
  def anyOf(s: String): Rule0 = anyOf(s.toCharArray)
  def ch(c: Char) = new CharRule(c)
  def str(s: String): Rule0 = str(s.toCharArray)
  def str(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ch(chars(0))
    case _ => new Rule0(new StringMatcher(chars.map(ch).map(_.matcher), chars).label("\"" + chars + '"'))
  }

  def anyOf(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ch(chars(0))
    case _ => anyOf(Characters.of(chars: _*))
  }

  def anyOf(chars: Characters): Rule0 = {
    if (!chars.isSubtractive && chars.getChars().length == 1)
      ch(chars.getChars()(0))
    else
      new Rule0(new CharSetMatcher(chars).label(chars.toString))
  }

  def ignoreCase(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ignoreCase(chars(0))
    case _ => new Rule0(new SequenceMatcher(chars.map(ignoreCase(_)).map(_.matcher)).label("\"" + chars + '"'))
  }

  def test(f: => Boolean) = testAction((c:Context[Any]) => f)
  def test(f: String => Boolean) = testAction((c:Context[Any]) => f(c.getMatch))
  private def testAction(f: Context[Any] => Boolean) = new Rule0(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = f(context)
  }).label("TestAction"))

  def run(f: => Unit) = runAction((c:Context[Any]) => f)
  def run(f: String => Unit) = runAction((c:Context[Any]) => f(c.getMatch))
  private def runAction(f: Context[Any] => Unit) = new Rule0(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = { f(context); true }
  })label("RunAction"))

  def push[A](f: => A) = new Rule1[A](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = { context.getValueStack.push(f); true }
  }).label("Push1Action"))

  def push[A, B](a: => A, b: => B) = new Rule2[A, B](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs: ValueStack[Any] = context.getValueStack
      vs.push(a)
      vs.push(b)
      true
    }
  }).label("Push2Action"))

  def push[A, B, C](a: => A, b: => B, c: => C) = new Rule3[A, B, C](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs: ValueStack[Any] = context.getValueStack
      vs.push(a)
      vs.push(b)
      vs.push(c)
      true
    }
  }).label("Push3Action"))

  def pop[Z](f: Z => Unit) = new PopRule1[Z](new ActionMatcher(new Action[Z] {
    def run(context: Context[Z]): Boolean = { f(context.getValueStack.pop()); true }
  }).label("Pop1Action"))

  def pop[Y, Z](f: (Y, Z) => Unit) = new PopRule2[Y, Z](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop().asInstanceOf[Z]
      val y = vs.pop().asInstanceOf[Y]
      f(y, z)
      true
    }
  }).label("Pop2Action"))

  def pop[X, Y, Z](f: (X, Y, Z) => Unit) = new PopRule3[X, Y, Z](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop().asInstanceOf[Z]
      val y = vs.pop().asInstanceOf[Y]
      val x = vs.pop().asInstanceOf[X]
      f(x, y, z)
      true
    }
  }).label("Pop3Action"))

  def pop1() = new PopRuleN1(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      true
    }
  }).label("PopN1Action"))

  def pop2() = new PopRuleN2(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      context.getValueStack.pop()
      true
    }
  }).label("PopN2Action"))

  def pop3() = new PopRuleN3(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      context.getValueStack.pop()
      context.getValueStack.pop()
      true
    }
  }).label("PopN3Action"))

  implicit def toRule(s: String): Rule0 = str(s.toCharArray)
  implicit def toRule(chars: Array[Char]): Rule0 = str(chars)
  implicit def toRule(rule: Rule0) = rule.matcher
  implicit def toRule(rule: Rule1[_]) = rule.matcher

  lazy val EMPTY = new Rule0(new EmptyMatcher().label("EMPTY"))
  lazy val ANY = new Rule0(new AnyMatcher().label("ANY"))
  lazy val EOI = new Rule0(new CharMatcher(Characters.EOI).label("EOI"))

}