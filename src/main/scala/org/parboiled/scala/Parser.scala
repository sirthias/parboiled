package org.parboiled.scala

import org.parboiled.matchers._
import _root_.scala.collection.mutable
import org.parboiled.Context
import org.parboiled.support.{ValueStack, Characters}
import org.parboiled.common.StringUtils
import rules.Rule._

/**
 * The main Parser trait for scala parboiled parsers. Defines the basic rule building methods as well as the
 * caching and proxying logic.
 */
trait Parser {

  private val cache = mutable.Map.empty[RuleMethod, Rule]

  /**
   *  Flag indicating whether parboiled will create a parse tree during a parsing run of this parser.
   * This flag has to be set before the root is being built in order to have any effect.
   */
  var buildParseTree = false

  private val lock = new AnyRef()

  /**
   * Marks this parser as parse-tree-building (sets the  { @link # buildParseTree } flag)
   */
  def withParseTreeBuilding(): this.type = {buildParseTree = true; this}

  /**
   * Defines a parser rule wrapping the given rule construction block with caching and recursion protection.
   */
  def rule[T <: Rule](block: => T)(implicit creator: Matcher => T): T = {
    val ruleMethod = getCurrentRuleMethod
    rule(ruleMethod.getMethodName, ruleMethod, Seq.empty, block, creator)
  }

  /**
   * Defines a parser rule wrapping the given rule construction block with caching and recursion protection.
   * Labels the constructed rule with the given label and optionally marks it according to the given rule options.
   */
  def rule[T <: Rule](label: String, options: RuleOption*)(block: => T)(implicit creator: Matcher => T): T = {
    rule(label, getCurrentRuleMethod, options, block, creator)
  }

  private def rule[T <: Rule](label: String, key: RuleMethod, options: Seq[RuleOption], block: => T, creator: Matcher => T): T =
    lock.synchronized {
      cache.get(key) match {
        case Some(rule) => rule.asInstanceOf[T]
        case None => {
          val proxy = new ProxyMatcher
          // protect block from infinite recursion by immediately caching a new Rule of type T wrapping the proxy creator
          cache += key -> creator(proxy)
          var rule = block.label(label) // evaluate rule definition block
          if (!buildParseTree || options.contains(SuppressNode)) rule = rule.suppressNode
          if (options.contains(SuppressSubnodes)) rule = rule.suppressSubnodes
          if (options.contains(SkipNode)) rule = rule.skipNode
          if (options.contains(MemoMismatches)) rule = rule.memoMismatches
          proxy.arm(rule.matcher) // arm the proxy in case it is in use
          cache += key -> rule // replace the cache value with the actual rule (overwriting the proxy rule)
          rule
        }
      }
    }

  // the following rule creators should be moved to the package object to avoid bytecode duplication across different
  // parsers, so far they cannot be moved due to the "package objects do not support overloaded methods" bug of the
  // scala compiler (http://lampsvn.epfl.ch/trac/scala/ticket/1987)

  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional(sub: Rule0): Rule0 = new Rule0(new OptionalMatcher(sub.matcher).label("Optional"))

  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional[Z](sub: ReductionRule1[Z, Z]): ReductionRule1[Z, Z] =
    new ReductionRule1[Z, Z](new OptionalMatcher(sub.matcher).label("Optional"))

  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional[A](sub: Rule1[A]): Rule1[Option[A]] = sub ~~> (Some(_)) | push(None)

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   */
  def zeroOrMore(sub: Rule0): Rule0 = new Rule0(new ZeroOrMoreMatcher(sub.matcher).label("ZeroOrMore"))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   */
  def zeroOrMore[Z](sub: ReductionRule1[Z, Z]): ReductionRule1[Z, Z] =
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
  def oneOrMore(sub: Rule0): Rule0 = new Rule0(new OneOrMoreMatcher(sub.matcher).label("OneOrMore"))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   */
  def oneOrMore[Z](sub: ReductionRule1[Z, Z]): ReductionRule1[Z, Z] =
    new ReductionRule1[Z, Z](new OneOrMoreMatcher(sub.matcher).label("OneOrMore"))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   * This overload automatically builds a list from the return values of its sub rule and pushes it onto the value stack.
   * If the sub rule did not match at all the pushed list will be empty.
   */
  def oneOrMore[A](sub: Rule1[A]): Rule1[List[A]] =
    sub ~~> (List(_)) ~ zeroOrMore(sub ~~> ((list: List[A], subRet) => subRet :: list)) ~~> (_.reverse)

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
   * Matches the given sub rule a specified number of times.
   * If the given number is zero the result is equivalent to the EMPTY match.
   */
  def nTimes(times: Int, sub: Rule0): Rule0 = nTimes(times, sub, null)

  /**
   * Matches the given sub rule a specified number of times, whereby two rule matches have to be separated by a match
   * of the given separator rule. If the given number is zero the result is equivalent to the EMPTY match.
   */
  def nTimes(times: Int, sub: Rule0, separator: Rule0): Rule0 = times match {
    case 0 => EMPTY
    case 1 => sub
    case n if n > 1 =>
      (if (separator != null) nTimes(times - 1, sub, separator) ~ separator else nTimes(times - 1, sub)) ~ sub
    case _ => throw new IllegalArgumentException("Illegal number of repetitions: " + times)
  }

  /**
   * Matches the given sub rule a specified number of times.
   * If the given number is zero the result is equivalent to the EMPTY match.
   */
  def nTimes[Z](times: Int, sub: ReductionRule1[Z, Z]): ReductionRule1[Z, Z] = nTimes(times, sub, null)

  /**
   * Matches the given sub rule a specified number of times, whereby two rule matches have to be separated by a match
   * of the given separator rule. If the given number is zero the result is equivalent to the EMPTY match.
   */
  def nTimes[Z](times: Int, sub: ReductionRule1[Z, Z], separator: Rule0): ReductionRule1[Z, Z] = times match {
    case 0 => new ReductionRule1[Z, Z](EMPTY.matcher)
    case 1 => sub
    case n if n > 1 =>
      (if (separator != null) nTimes(times - 1, sub, separator) ~ separator else nTimes(times - 1, sub)) ~ sub
    case _ => throw new IllegalArgumentException("Illegal number of repetitions: " + times)
  }

  /**
   * Matches the given sub rule a specified number of times.
   * If the given number is zero the result is equivalent to the EMPTY match.
   */
  def nTimes[A](times: Int, sub: Rule1[A]): Rule1[List[A]] = nTimes(times, sub, null)

  /**
   * Matches the given sub rule a specified number of times, whereby two rule matches have to be separated by a match
   * of the given separator rule. If the given number is zero the result is equivalent to the EMPTY match.
   */
  def nTimes[A](times: Int, sub: Rule1[A], separator: Rule0): Rule1[List[A]] = times match {
    case 0 => push(Nil)
    case 1 => sub ~~> (List(_))
    case n if n > 1 => nTimesInternal(times, sub, separator) ~~> (_.reverse)
    case _ => throw new IllegalArgumentException("Illegal number of repetitions: " + times)
  }

  private def nTimesInternal[A](times: Int, sub: Rule1[A], separator: Rule0): Rule1[List[A]] = times match {
    case 1 => sub ~~> (List(_))
    case n if n > 1 =>
      (if (separator != null)
        nTimesInternal(times - 1, sub, separator) ~ separator
      else
        nTimesInternal(times - 1, sub, null)) ~ sub ~~> ((list: List[A], subRet) => subRet :: list)
    case _ => throw new IllegalStateException
  }

  /**
   * Creates a rule that matches the given character independently of its case.
   */
  def ignoreCase(c: Char): Rule0 =
    new Rule0(new CharIgnoreCaseMatcher(c).label(
      "'" + StringUtils.escape(Character.toLowerCase(c)) + '/' + StringUtils.escape(Character.toUpperCase(c)) + "'"
    ))

  /**
   * Creates a rule that matches the given string case-independently.
   */
  def ignoreCase(s: String): Rule0 = ignoreCase(s.toCharArray)

  /**
   * Creates a rule that matches the given character.
   */
  def ch(c: Char): CharRule = new CharRule(c)

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
   * Creates a rule that matches any single character in the given string.
   */
  def anyOf(s: String): Rule0 = anyOf(s.toCharArray)

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
      new Rule0(new AnyOfMatcher(chars).label(chars.toString))
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
   * Creates a simple semantic predicate.
   */
  def test(f: => Boolean): Rule0 = toTestAction((c: Context[Any]) => f)

  /**
   * Creates a simple parser action.
   */
  def run(f: => Unit): Rule0 = toRunAction((c: Context[Any]) => f)

  /**
   * Create a parser action whose result value is pushed onto the value stack.
   */
  def push[A](f: => A): Rule1[A] = new Rule1[A](new ActionMatcher(action(ok(_.getValueStack.push(f)))).label("Push1Action"))

  /**
   * Create a parser action whose two result values are pushed onto the value stack.
   */
  def push[A, B](a: => A, b: => B): Rule2[A, B] = new Rule2[A, B](
    new ActionMatcher(action(ok({ (c: Context[Any]) =>
      val vs: ValueStack[Any] = c.getValueStack
      vs.push(a)
      vs.push(b)
    }))).label("Push2Action"))

  /**
   * Create a parser action whose three result values are pushed onto the value stack.
   */
  def push[A, B, C](a: => A, b: => B, c: => C): Rule3[A, B, C] = new Rule3[A, B, C](
    new ActionMatcher(action(ok({ (c: Context[Any]) =>
      val vs: ValueStack[Any] = c.getValueStack
      vs.push(a)
      vs.push(b)
      vs.push(c)
    }))).label("Push3Action"))

  def withContext[R](f: Context[_] => R) = new WithContextAction[R](f)
  def withContext[A, R](f: (A, Context[_]) => R) = new WithContextAction1[A, R](f)
  def withContext[A, B, R](f: (A, B, Context[_]) => R) = new WithContextAction2[A, B, R](f)
  def withContext[A, B, C, R](f: (A, B, C, Context[_]) => R) = new WithContextAction3[A, B, C, R](f)
  def withContext[A, B, C, D, R](f: (A, B, C, D, Context[_]) => R) = new WithContextAction4[A, B, C, D, R](f)
  def withContext[A, B, C, D, E, R](f: (A, B, C, D, E, Context[_]) => R) = new WithContextAction5[A, B, C, D, E, R](f)
  def withContext[A, B, C, D, E, F, R](f: (A, B, C, D, E, F, Context[_]) => R) = new WithContextAction6[A, B, C, D, E, F, R](f)
  def withContext[A, B, C, D, E, F, G, R](f: (A, B, C, D, E, F, G, Context[_]) => R) = new WithContextAction7[A, B, C, D, E, F, G, R](f)

  // the following implicits are defined here in the parser and not in the package object so there are available
  // for overriding

  /**
   *   Converts the given string into a corresponding parser rule.
   */
  implicit def toRule(string: String): Rule0 = str(string)

  /**
   * Converts the given character array into a corresponding parser rule.
   */
  implicit def toRule(chars: Array[Char]): Rule0 = str(chars)

  /**
   * Converts the given symbol into a corresponding parser rule.
   */
  implicit def toRule(symbol: Symbol): Rule0 = str(symbol.name)

}