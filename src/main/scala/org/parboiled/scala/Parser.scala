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

  /*def rule[T <: Rule](label: String, options: RuleOption*)(block: => T)(implicit manifest: Manifest[T]): T = {
    rule(label, getCurrentRuleMethod, options, block, manifest)
  }*/

  private def rule[T <: Rule](label: String, key: RuleMethod, options: Seq[RuleOption], block: => T, manifest: Manifest[T]): T =
    cache.get(key) match {
      case Some(rule) => rule.asInstanceOf[T]
      case None => {
        val proxy = new ProxyCreator
        // protect block from infinite recursion by immediately caching a new Rule of type T wrapping the proxy creator
        cache += key -> manifest.erasure.getConstructor(classOf[MatcherCreator]).newInstance(proxy).asInstanceOf[T]
        var rule = block.withLabel(label) // evaluate rule definition block
        if (!buildParseTree || options.contains(SuppressNode)) rule = rule.withNodeSuppressed
        if (options.contains(SuppressSubnodes)) rule = rule.withSubnodesSuppressed
        if (options.contains(SkipNode)) rule = rule.withNodeSkipped
        proxy.arm(rule) // arm the proxy in case it is in use
        cache += key -> rule // replace the cache value with the actual rule (overwriting the proxy rule)
        rule
      }
    }

  def &(sub: Rule) = new Rule0(new UnaryCreator(sub.creator, new TestMatcher(_))).withLabel("Test")
  def optional(sub: Rule0) = new Rule0(new UnaryCreator(sub.creator, new OptionalMatcher(_))).withLabel("Optional")
  def optional[Z](sub: ReductionRule1[Z, Z]) = new ReductionRule1[Z, Z](new UnaryCreator(sub.creator, new OptionalMatcher(_))).withLabel("Optional")
  def zeroOrMore(sub: Rule0) = new Rule0(new UnaryCreator(sub.creator, new ZeroOrMoreMatcher(_).label("ZeroOrMore")))
  def zeroOrMore[Z](sub: ReductionRule1[Z, Z]) = new ReductionRule1[Z, Z](new UnaryCreator(sub.creator, new ZeroOrMoreMatcher(_))).withLabel("ZeroOrMore")
  def oneOrMore(sub: Rule0) = new Rule0(new UnaryCreator(sub.creator, new OneOrMoreMatcher(_))).withLabel("OneOrMore")
  def oneOrMore[Z](sub: ReductionRule1[Z, Z]) = new ReductionRule1[Z, Z](new UnaryCreator(sub.creator, new OneOrMoreMatcher(_))).withLabel("OneOrMore")

  def ignoreCase(c: Char): Rule0 = new Rule0(new CharIgnoreCaseMatcher(c)).withLabel("'" + escape(c.toLower) + '/' + escape(c.toUpper) + "'")
  def ignoreCase(s: String): Rule0 = ignoreCase(s.toCharArray)
  def anyOf(s: String): Rule0 = anyOf(s.toCharArray)
  def ch(c: Char) = new CharRule(c)
  def str(s: String): Rule0 = str(s.toCharArray)
  def str(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ch(chars(0))
    case _ => new Rule0(new StringMatcher(chars.map(ch).map(_.toMatcher), chars)).withLabel("\"" + chars + '"')
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
      new Rule0(new CharSetMatcher(chars)).withLabel(chars.toString)
  }

  def ignoreCase(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ignoreCase(chars(0))
    case _ => new Rule0(new SequenceMatcher(chars.map(ignoreCase(_)).map(_.toMatcher))).withLabel("\"" + chars + '"')
  }

  def test(f: => Boolean) = testAction((c:Context[Any]) => f)
  def test(f: String => Boolean) = testAction((c:Context[Any]) => f(c.getMatch))
  private def testAction(f: Context[Any] => Boolean) = new Rule0(new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = f(context)
  }))

  def run(f: => Unit) = runAction((c:Context[Any]) => f)
  def run(f: String => Unit) = runAction((c:Context[Any]) => f(c.getMatch))
  private def runAction(f: Context[Any] => Unit) = new Rule0(new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = { f(context); true }
  }))

  def push[A](f: => A) = new Rule1[A](new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = { context.getValueStack.push(f); true }
  }))

  def push[A, B](a: => A, b: => B) = new Rule2[A, B](new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs: ValueStack[Any] = context.getValueStack
      vs.push(a)
      vs.push(b)
      true
    }
  }))

  def push[A, B, C](a: => A, b: => B, c: => C) = new Rule3[A, B, C](new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs: ValueStack[Any] = context.getValueStack
      vs.push(a)
      vs.push(b)
      vs.push(c)
      true
    }
  }))

  def pop[Z](f: Z => Unit) = new PopRule1[Z](new ActionCreator(new Action[Z] {
    def run(context: Context[Z]): Boolean = { f(context.getValueStack.pop()); true }
  }))

  def pop[Y, Z](f: (Y, Z) => Unit) = new PopRule2[Y, Z](new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop().asInstanceOf[Z]
      val y = vs.pop().asInstanceOf[Y]
      f(y, z)
      true
    }
  }))

  def pop[X, Y, Z](f: (X, Y, Z) => Unit) = new PopRule3[X, Y, Z](new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop().asInstanceOf[Z]
      val y = vs.pop().asInstanceOf[Y]
      val x = vs.pop().asInstanceOf[X]
      f(x, y, z)
      true
    }
  }))

  def pop1() = new PopRuleN1(new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      true
    }
  }))

  def pop2() = new PopRuleN2(new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      context.getValueStack.pop()
      true
    }
  }))

  def pop3() = new PopRuleN3(new ActionCreator(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      context.getValueStack.pop()
      context.getValueStack.pop()
      true
    }
  }))

  implicit def toRule(s: String): Rule0 = str(s.toCharArray)
  implicit def toRule(chars: Array[Char]): Rule0 = str(chars)
  implicit def toRule(rule: Rule0) = rule.toMatcher
  implicit def toRule(rule: Rule1[_]) = rule.toMatcher

  lazy val EMPTY = new Rule0(new EmptyMatcher()).withLabel("EMPTY")
  lazy val ANY = new Rule0(new AnyMatcher()).withLabel("ANY")
  lazy val EOI = new Rule0(new CharMatcher(Characters.EOI)).withLabel("EOI")

}