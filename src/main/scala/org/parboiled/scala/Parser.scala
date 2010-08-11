package org.parboiled.scala

import org.parboiled.matchers._
import _root_.scala.collection.mutable
import org.parboiled.common.StringUtils.escape
import Support._
import org.parboiled.{Action, Context}
import org.parboiled.support.{ValueStack, Characters}

/**
 * The main Parser trait for scala parboiled parsers. Defines the basic rule building methods as well as the
 * caching and proxying logic.
 */
trait Parser {

  /**
   * Rule building expressions can take a number of options which are implemented as case objects derived from this
   * class.
   */
  sealed abstract class RuleOption

  /**
   * This rule option advises parboiled to not create a parse tree node for this rule and all sub rules
   * (in case that parse tree building is enabled on the parser).
   */
  case object SuppressNode extends RuleOption

  /**
   * This rule option advises parboiled to not create a parse tree node for the sub rules of this rule
   * (in case that parse tree building is enabled on the parser).
   */
  case object SuppressSubnodes extends RuleOption

  /**
   * This rule option advises parboiled to not create a parse tree node for this rule
   * (in case that parse tree building is enabled on the parser).
   */
  case object SkipNode extends RuleOption

  private val cache = mutable.Map.empty[RuleMethod, Rule]

  /**
   * Flag indicating whether parboiled will create a parse tree during a parsing run of this parser.
   * This flag has to be set before the root is being built in order to have any effect.
   */
  var buildParseTree = false

  /**
   * Marks this parser as parse-tree-building (sets the  { @link # buildParseTree } flag)
   */
  def withParseTreeBuilding(): this.type = {buildParseTree = true; this}

  /**
   * Defines a parser rule wrapping the given rule construction block with caching and recursion protection.
   */
  def rule[T <: Rule](block: => T)(implicit manifest: Manifest[T]): T = {
    val ruleMethod = getCurrentRuleMethod
    rule(ruleMethod.getMethodName, ruleMethod, Seq.empty, block, manifest)
  }

  /**
   * Defines a parser rule wrapping the given rule construction block with caching and recursion protection.
   * Labels the constructed rule with the given labell and optionally marks it according to the given rule options.
   */
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

  /**
   * Creates an "AND" syntactic predicate according to the PEG formalism.
   */
  def &(sub: Rule) = new Rule0(new TestMatcher(sub.matcher).label("Test"))

  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional(sub: Rule0) = new Rule0(new OptionalMatcher(sub.matcher).label("Optional"))

  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional[Z](sub: ReductionRule1[Z, Z]) =
    new ReductionRule1[Z, Z](new OptionalMatcher(sub.matcher).label("Optional"))

  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional[A](sub: Rule1[A]): Rule1[Option[A]] = sub ~~> (Some(_)) | push(None)

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   */
  def zeroOrMore(sub: Rule0) = new Rule0(new ZeroOrMoreMatcher(sub.matcher).label("ZeroOrMore"))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   */
  def zeroOrMore[Z](sub: ReductionRule1[Z, Z]) =
    new ReductionRule1[Z, Z](new ZeroOrMoreMatcher(sub.matcher).label("ZeroOrMore"))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   * This overload automatically builds a list from the return values of its sub rule and pushes it onto the value stack.
   */
  def zeroOrMore[A](sub: Rule1[A]): Rule1[List[A]] =
    push(Nil) ~ zeroOrMore(sub ~~> ((list: List[A], subRet) => subRet :: list)) ~~> ((list: List[A]) => list.reverse)

  /**
   * <p>Creates a rule that zero or more times tries to match a given sub rule. Between two sub rule matches the given
   * separator rule has to match. So this rule matches following sequences:</p>
   * <ul>
   * <li>{nothing}</li>
   * <li>{sub}</li>
   * <li>{sub} {separator} {sub}</li>
   * <li>{sub} {separator} {sub} {separator} {sub}</li>
   * <li>...</li>
   * </ul>
   */
  def zeroOrMore(sub: Rule0, separator: Rule0): Rule0 = optional(oneOrMore(sub, separator))

  /**
   * <p>Creates a rule that zero or more times tries to match a given sub rule. Between two sub rule matches the given
   * separator rule has to match. So this rule matches following sequences:</p>
   * <ul>
   * <li>{nothing}</li>
   * <li>{sub}</li>
   * <li>{sub} {separator} {sub}</li>
   * <li>{sub} {separator} {sub} {separator} {sub}</li>
   * <li>...</li>
   * </ul>
   * This overload automatically builds a list from the return values of the sub rule and pushes it onto the value stack.
   */
  def zeroOrMore[A](sub: Rule1[A], separator: Rule0): Rule1[List[A]] = oneOrMore(sub, separator) | push(Nil)

  /**
   *  Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   */
  def oneOrMore(sub: Rule0) = new Rule0(new OneOrMoreMatcher(sub.matcher).label("OneOrMore"))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   */
  def oneOrMore[Z](sub: ReductionRule1[Z, Z]) =
    new ReductionRule1[Z, Z](new OneOrMoreMatcher(sub.matcher).label("OneOrMore"))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   * This overload automatically builds a list from the return values of its sub rule and pushes it onto the value stack.
   * If the sub rule did not match at all the pushed list will be empty.
   */
  def oneOrMore[A](sub: Rule1[A]): Rule1[List[A]] =
    push(Nil) ~ oneOrMore(sub ~~> ((list: List[A], subRet) => subRet :: list)) ~~> (_.reverse)

  /**
   * <p>Creates a rule that one or more times tries to match a given sub rule. Between two sub rule matches the given
   * separator rule has to match. So this rule matches following sequences:</p>
   * <ul>
   * <li>{sub}</li>
   * <li>{sub} {separator} {sub}</li>
   * <li>{sub} {separator} {sub} {separator} {sub}</li>
   * <li>...</li>
   * </ul>
   */
  def oneOrMore(sub: Rule0, separator: Rule0): Rule0 = sub ~ zeroOrMore(separator ~ sub)

  /**
   * <p>Creates a rule that one or more times tries to match a given sub rule. Between two sub rule matches the given
   * separator rule has to match. So this rule matches following sequences:</p>
   * <ul>
   * <li>{sub}</li>
   * <li>{sub} {separator} {sub}</li>
   * <li>{sub} {separator} {sub} {separator} {sub}</li>
   * <li>...</li>
   * </ul>
   * This overload automatically builds a list from the return values of the sub rule and pushes it onto the value stack.
   */
  def oneOrMore[A](sub: Rule1[A], separator: Rule0): Rule1[List[A]] =
    sub ~~> (List(_)) ~ zeroOrMore(separator ~ sub ~~> ((list: List[A], subRet) => subRet :: list)) ~~> (_.reverse)

  /**
   * Groups the given sub rule into one entity so that a following ~> operator receives the text matched by the whole
   * group rather than only the immediately preceeding sub rule.
   */
  def group[T <: Rule](rule: T) = rule.withLabel("group")

  /**
   * Creates a rule that matches the given character independently of its case.
   */
  def ignoreCase(c: Char): Rule0 =
    new Rule0(new CharIgnoreCaseMatcher(c).label("'" + escape(c.toLower) + '/' + escape(c.toUpper) + "'"))

  /**
   * Creates a rule that matches the given string case-independently.
   */
  def ignoreCase(s: String): Rule0 = ignoreCase(s.toCharArray)

  /**
   * Creates a rule that matches any single character in the given string.
   */
  def anyOf(s: String): Rule0 = anyOf(s.toCharArray)

  /**
   * Creates a rule that matches the given character.
   */
  def ch(c: Char) = new CharRule(c)

  /**
   * Creates a rule that matches the given string.
   */
  def str(s: String): Rule0 = str(s.toCharArray)

  /**
   * Creates a rule that matches the given character array.
   */
  def str(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ch(chars(0))
    case _ => new Rule0(new StringMatcher(chars.map(ch).map(_.matcher), chars).label("\"" + chars + '"'))
  }

  /**
   * Creates a rule that matches any single character in the given character array.
   */
  def anyOf(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ch(chars(0))
    case _ => anyOf(Characters.of(chars: _*))
  }

  /**
   * Creates a rule that matches any single character in the given  { @link org.parboiled.support.Characters } instance.
   */
  def anyOf(chars: Characters): Rule0 = {
    if (!chars.isSubtractive && chars.getChars().length == 1)
      ch(chars.getChars()(0))
    else
      new Rule0(new CharSetMatcher(chars).label(chars.toString))
  }

  /**
   * Creates a rule that matches the given character array case-independently.
   */
  def ignoreCase(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ignoreCase(chars(0))
    case _ => new Rule0(new SequenceMatcher(chars.map(ignoreCase(_)).map(_.matcher)).label("\"" + chars + '"'))
  }

  /**
   * Creates a semantic predicate.
   */
  def test(f: => Boolean): Rule0 = test((c: Context[Any]) => f)

  /**
   * Creates a semantic predicate taking the current parsing context as input.
   * Note that the action can read but should not alter the parsers value stack (unless the types of all value stack
   * elements remain compatible with the relevant subset of the other parser actions)!
   */
  def test(f: Context[Any] => Boolean): Rule0 = new Rule0(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = f(context)
  }).label("TestAction"))

  /**
   * Creates a semantic predicate on the input text matched by the immediately preceeding peer rule.
   * Note that this rule is only valid in sequence rules and must not appear in first position!
   */
  def testMatch(f: String => Boolean) = test((c: Context[Any]) => f(c.getMatch))

  /**
   * Creates a simple parser action.
   */
  def run(f: => Unit): Rule0 = run((c: Context[Any]) => f)

  /**
   * Creates a simple parser action taking the current parsing context as input.
   * Note that the action can read but should not alter the parsers value stack (unless the types of all value stack
   * elements remain compatible with the relevant subset of the other parser actions)!
   */
  def run(f: Context[Any] => Unit): Rule0 = new Rule0(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {f(context); true}
  }).label("RunAction"))

  /**
   * Creates a simple parser action taking the input text matched by the immediately preceeding peer rule as input.
   * Note that this rule is only valid in sequence rules and must not appear in first position!
   */
  def runOnMatch(f: String => Unit) = run((c: Context[Any]) => f(c.getMatch))

  /**
   * Create a parser action whose result value is pushed onto the value stack.
   */
  def push[A](f: => A) = new Rule1[A](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {context.getValueStack.push(f); true}
  }).label("Push1Action"))

  /**
   * Create a parser action whose two result values are pushed onto the value stack.
   */
  def push[A, B](a: => A, b: => B) = new Rule2[A, B](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs: ValueStack[Any] = context.getValueStack
      vs.push(a)
      vs.push(b)
      true
    }
  }).label("Push2Action"))

  /**
   * Create a parser action whose three result values are pushed onto the value stack.
   */
  def push[A, B, C](a: => A, b: => B, c: => C) = new Rule3[A, B, C](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs: ValueStack[Any] = context.getValueStack
      vs.push(a)
      vs.push(b)
      vs.push(c)
      true
    }
  }).label("Push3Action"))

  /**
   * Create a parser action taking the top element popped of the value stack as input.
   */
  def pop[Z](f: Z => Unit) = new PopRule1[Z](new ActionMatcher(new Action[Z] {
    def run(context: Context[Z]): Boolean = {f(context.getValueStack.pop()); true}
  }).label("Pop1Action"))

  /**
   * Create a parser action taking the top two elements popped of the value stack as input.
   */
  def pop[Y, Z](f: (Y, Z) => Unit) = new PopRule2[Y, Z](new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      val vs = context.getValueStack
      val z = vs.pop().asInstanceOf[Z]
      val y = vs.pop().asInstanceOf[Y]
      f(y, z)
      true
    }
  }).label("Pop2Action"))

  /**
   * Create a parser action taking the top three elements popped of the value stack as input.
   */
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

  /**
   * Create a parser action removing the top element from the value stack.
   */
  def pop1() = new PopRuleN1(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      true
    }
  }).label("PopN1Action"))

  /**
   * Create a parser action removing the top two elements from the value stack.
   */
  def pop2() = new PopRuleN2(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      context.getValueStack.pop()
      true
    }
  }).label("PopN2Action"))

  /**
   * Create a parser action removing the top three elements from the value stack.
   */
  def pop3() = new PopRuleN3(new ActionMatcher(new Action[Any] {
    def run(context: Context[Any]): Boolean = {
      context.getValueStack.pop()
      context.getValueStack.pop()
      context.getValueStack.pop()
      true
    }
  }).label("PopN3Action"))

  /**
   * The EMPTY rule, a rule that always matches and consumes no input.
   */
  lazy val EMPTY = new Rule0(new EmptyMatcher().label("EMPTY"))

  /**
   * The ANY rule, which matches any single character except EOI.
   */
  lazy val ANY = new Rule0(new AnyMatcher().label("ANY"))

  /**
   * The EOI rule, which matches the End-Of-Input "character".
   */
  lazy val EOI = new Rule0(new CharMatcher(Characters.EOI).label("EOI"))

  /**
   *   Converts the given string into a corresponding parser rule.
   */
  implicit def toRule(string: String): Rule0 = str(string.toCharArray)

  /**
   * Converts the given character array into a corresponding parser rule.
   */
  implicit def toRule(chars: Array[Char]): Rule0 = str(chars)

  /**
   * Converts the given symbol into a corresponding parser rule.
   */
  implicit def toRule(symbol: Symbol): Rule0 = str(symbol.name)
}