/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.scala

import org.parboiled.matchers._
import _root_.scala.collection.mutable
import org.parboiled.Context
import rules.Rule._
import org.parboiled.support.{Chars, ValueStack, Characters}

/**
 * The main Parser trait for scala parboiled parsers. Defines the basic rule building methods as well as the
 * caching and proxying logic.
 */
trait Parser {

  private val cache = mutable.Map.empty[RuleMethod, Rule]
  private val lock = new AnyRef()

  /**
   * Indicates whether parboiled will create a parse tree during a parsing run of this parser.
   * Override and return true (you can also do this with a "override val buildParseTree = true") to enable
   * parse tree building.
   */
  def buildParseTree = false
  
  /**
   * Defines a parser rule wrapping the given rule construction block with caching and recursion protection.
   */
  def rule[T <: Rule](block: => T)(implicit creator: Matcher => T): T = {
    val ruleMethod = getCurrentRuleMethod
    rule(ruleMethod.getMethodName, ruleMethod, Seq.empty, block, creator)
  }

  /**
   * Defines a parser rule wrapping the given rule construction block with caching and recursion protection
   * using the given rule option(s).
   */
  def rule[T <: Rule](firstOption: RuleOption, more: RuleOption*)(block: => T)(implicit creator: Matcher => T): T = {
    val ruleMethod = getCurrentRuleMethod
    rule(ruleMethod.getMethodName, ruleMethod, firstOption +: more, block, creator)
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
          var rule = withCurrentRuleLabel(label) { block.label(label) } // evaluate rule definition block
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
  def optional(sub: Rule0): Rule0 = new Rule0(new OptionalMatcher(sub.matcher))

  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional[Z](sub: ReductionRule1[Z, Z]): ReductionRule1[Z, Z] =
    new ReductionRule1[Z, Z](new OptionalMatcher(sub.matcher))

  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional[A](sub: Rule1[A]): Rule1[Option[A]] = make(sub ~~> (Some(_)) | push(None)) {
    _.matcher.asInstanceOf[FirstOfMatcher].defaultLabel("Optional")
  }
  
  /**
   * Creates a rule that tries the given sub rule and always matches, even if the sub rule did not match.
   */
  def optional[A, B](sub: Rule2[A, B]): Rule1[Option[(A, B)]] = optional(sub ~~> ((_, _)))
  
  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   */
  def zeroOrMore(sub: Rule0): Rule0 = new Rule0(new ZeroOrMoreMatcher(sub.matcher))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   */
  def zeroOrMore[Z](sub: ReductionRule1[Z, Z]): ReductionRule1[Z, Z] =
    new ReductionRule1[Z, Z](new ZeroOrMoreMatcher(sub.matcher))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   * This overload automatically builds a list from the return values of its sub rule and pushes it onto the value stack.
   */
  def zeroOrMore[A](sub: Rule1[A]): Rule1[List[A]] = make(
    push(Nil) ~ zeroOrMore(sub ~~> ((list: List[A], subRet) => subRet :: list)) ~~> ((list: List[A]) => list.reverse)
  ) { _.matcher.asInstanceOf[SequenceMatcher].defaultLabel("ZeroOrMore") }
  
  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches even if the sub rule did not match once.
   * This overload automatically builds a list from the return values of its sub rule and pushes it onto the value stack.
   */
  def zeroOrMore[A, B](sub: Rule2[A, B]): Rule1[List[(A, B)]] = zeroOrMore(sub ~~> ((_, _)))

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
  def zeroOrMore(sub: Rule0, separator: Rule0): Rule0 = make(optional(oneOrMore(sub, separator))) {
    _.matcher.asInstanceOf[OptionalMatcher].defaultLabel("ZeroOrMore")
  }

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
  def zeroOrMore[A](sub: Rule1[A], separator: Rule0): Rule1[List[A]] = make(oneOrMore(sub, separator) | push(Nil)) {
    _.matcher.asInstanceOf[FirstOfMatcher].defaultLabel("ZeroOrMore")
  }
  
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
  def zeroOrMore[A, B](sub: Rule2[A, B], separator: Rule0): Rule1[List[(A, B)]] = zeroOrMore(sub ~~> ((_, _)), separator)

  /**
   *  Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   */
  def oneOrMore(sub: Rule0): Rule0 = new Rule0(new OneOrMoreMatcher(sub.matcher))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   */
  def oneOrMore[Z](sub: ReductionRule1[Z, Z]): ReductionRule1[Z, Z] =
    new ReductionRule1[Z, Z](new OneOrMoreMatcher(sub.matcher))

  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   * This overload automatically builds a list from the return values of its sub rule and pushes it onto the value stack.
   * If the sub rule did not match at all the pushed list will be empty.
   */
  def oneOrMore[A](sub: Rule1[A]): Rule1[List[A]] = make(
    sub ~~> (List(_)) ~ zeroOrMore(sub ~~> ((list: List[A], subRet) => subRet :: list)) ~~> (_.reverse)
  ) { _.matcher.asInstanceOf[SequenceMatcher].defaultLabel("OneOrMore") }
  
  /**
   * Creates a rule that tries the given sub rule repeatedly until it fails. Matches if the sub rule matched at least once.
   * This overload automatically builds a list from the return values of its sub rule and pushes it onto the value stack.
   * If the sub rule did not match at all the pushed list will be empty.
   */
  def oneOrMore[A, B](sub: Rule2[A, B]): Rule1[List[(A, B)]] = oneOrMore(sub ~~> ((_, _)))

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
  def oneOrMore(sub: Rule0, separator: Rule0): Rule0 = make(sub ~ zeroOrMore(separator ~ sub)) {
    _.matcher.asInstanceOf[SequenceMatcher].defaultLabel("OneOrMore")
  }

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
  def oneOrMore[A](sub: Rule1[A], separator: Rule0): Rule1[List[A]] = make(
    sub ~~> (List(_)) ~ zeroOrMore(separator ~ sub ~~> ((list: List[A], subRet) => subRet :: list)) ~~> (_.reverse)
  ) { _.matcher.asInstanceOf[SequenceMatcher].defaultLabel("OneOrMore") }
  
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
  def oneOrMore[A, B](sub: Rule2[A, B], separator: Rule0): Rule1[List[(A, B)]] = oneOrMore(sub ~~> ((_, _)), separator)

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
    case n if n > 0 => {
      val join = if (separator != null) ((_:Rule0) ~ separator ~ (_:Rule0)) else ((_:Rule0) ~ (_:Rule0))
      def multiply(n: Int): Rule0 = if (n > 1) join(multiply(n-1), sub) else sub
      nameNTimes(multiply(times), times)
    }
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
    case n if n > 0 => {
      type R = ReductionRule1[Z, Z]
      val join = if (separator != null) ((_:R) ~ separator ~ (_:R)) else ((_:R) ~ (_:R))
      def multiply(n: Int): R = if (n > 1) join(multiply(n-1), sub) else sub
      nameNTimes(multiply(times), times)
    }
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
    case n if n > 0 => {
      def join(a: Rule1[List[A]], b: Rule1[A]) =
        a ~ (if (separator != null) separator ~ b else b) ~~> ((list, x) => x :: list)
      def multiply(n: Int): Rule1[List[A]] = if (n > 1) join(multiply(n-1), sub) else sub ~~> (_ :: Nil)
      nameNTimes(multiply(times) ~~> (_.reverse), times)
    }
    case _ => throw new IllegalArgumentException("Illegal number of repetitions: " + times)
  }

  /**
   * Matches the given sub rule a specified number of times, whereby two rule matches have to be separated by a match
   * of the given separator rule. If the given number is zero the result is equivalent to the EMPTY match.
   */
  def nTimes[A, B](times: Int, sub: Rule2[A, B]): Rule1[List[(A, B)]] = nTimes(times, sub, null)

  /**
   * Matches the given sub rule a specified number of times, whereby two rule matches have to be separated by a match
   * of the given separator rule. If the given number is zero the result is equivalent to the EMPTY match.
   */
  def nTimes[A, B](times: Int, sub: Rule2[A, B], separator: Rule0): Rule1[List[(A, B)]] = {
    nTimes(times, sub ~~> ((_, _)), separator)
  }

  private def nameNTimes[R <: Rule](rule: R, times: Int) = {
    rule.matcher match {
      case sm: SequenceMatcher => sm.defaultLabel(times + "-times")
      case _ =>
    }
    rule
  }

  /**
   * Creates a rule that matches the given character independently of its case.
   */
  def ignoreCase(c: Char): Rule0 = new Rule0(new CharIgnoreCaseMatcher(c))

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
   * If the string is empty the rule is equivalent to the EMPTY rule.
   */
  def str(s: String): Rule0 = str(s.toCharArray)

  /**
   * Creates a rule that matches the given character array.
   * If the array is empty the rule is equivalent to the EMPTY rule.
   */
  def str(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ch(chars(0))
    case _ => new Rule0(new StringMatcher(wrapCharArray(chars).map(c => ch(c).matcher).toArray, chars))
  }

  /**
   * Creates a rule that matches any single character in the given string.
   * If the string is empty the rule is equivalent to the NOTHING rule.
   */
  def anyOf(s: String): Rule0 = anyOf(s.toCharArray)

  /**
   * Creates a rule that matches any single character in the given character array.
   * If the array is empty the rule is equivalent to the NOTHING rule.
   */
  def anyOf(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => NOTHING
    case 1 => ch(chars(0))
    case _ => anyOf(Characters.of(chars: _*))
  }

  /**
   * Creates a rule that matches any single character in the given { @link org.parboiled.support.Characters } instance.
   */
  def anyOf(chars: Characters): Rule0 = {
    if (!chars.isSubtractive && chars.getChars.length == 1)
      ch(chars.getChars.apply(0))
    else new Rule0(new AnyOfMatcher(chars))
  }

  /**
   * Creates a rule that matches any single character except the ones in the given string and EOI.
   * If the string is empty the rule is equivalent to the ANY rule.
   *
   */
  def noneOf(s: String): Rule0 = noneOf(s.toCharArray)

  /**
   * Creates a rule that matches any single character except the ones in the given character array and EOI.
   * If the array is empty the rule is equivalent to the ANY rule.
   */
  def noneOf(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => ANY
    case _ => {
      val charsWithEOI = if (chars.contains(Chars.EOI)) chars else (wrapCharArray(chars) :+ Chars.EOI).toArray
      anyOf(Characters.allBut(charsWithEOI: _*))
    }
  }

  /**
   * Creates a rule that matches the given character array case-independently.
   * If the array is empty the rule is equivalent to the EMPTY rule.
   */
  def ignoreCase(chars: Array[Char]): Rule0 = chars.length match {
    case 0 => EMPTY
    case 1 => ignoreCase(chars(0))
    case _ => new Rule0(new SequenceMatcher(chars.map(ignoreCase(_)).map(_.matcher)).defaultLabel("\"" + chars + '"'))
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
  def push[A](f: => A): Rule1[A] =
    new Rule1[A](new ActionMatcher(action(ok(_.getValueStack.push(f)))).label(nameAction("Push1")))

  /**
   * Create a parser action whose two result values are pushed onto the value stack.
   */
  def push[A, B](a: => A, b: => B): Rule2[A, B] = new Rule2[A, B](
    new ActionMatcher(action(ok({ (c: Context[Any]) =>
      val vs: ValueStack[Any] = c.getValueStack
      vs.push(a)
      vs.push(b)
    }))).label(nameAction("Push2")))

  /**
   * Create a parser action whose three result values are pushed onto the value stack.
   */
  def push[A, B, C](a: => A, b: => B, c: => C): Rule3[A, B, C] = new Rule3[A, B, C](
    new ActionMatcher(action(ok({ (c: Context[Any]) =>
      val vs: ValueStack[Any] = c.getValueStack
      vs.push(a)
      vs.push(b)
      vs.push(c)
    }))).label(nameAction("Push3")))

  /**
   * Create a parser action from the given function whose result value is pushed onto the value stack.
   */
  def pushFromContext[A](f: Context[Any] => A): Rule1[A] =
    new Rule1[A](new ActionMatcher(action(rules.Rule.push(f)))).label(nameAction("Push1"))

  def withContext[A, R](f: (A, Context[Any]) => R) = new WithContextAction1[A, R](f)
  def withContext[A, B, R](f: (A, B, Context[Any]) => R) = new WithContextAction2[A, B, R](f)
  def withContext[A, B, C, R](f: (A, B, C, Context[Any]) => R) = new WithContextAction3[A, B, C, R](f)
  def withContext[A, B, C, D, R](f: (A, B, C, D, Context[Any]) => R) = new WithContextAction4[A, B, C, D, R](f)
  def withContext[A, B, C, D, E, R](f: (A, B, C, D, E, Context[Any]) => R) = new WithContextAction5[A, B, C, D, E, R](f)
  def withContext[A, B, C, D, E, F, R](f: (A, B, C, D, E, F, Context[Any]) => R) = new WithContextAction6[A, B, C, D, E, F, R](f)
  def withContext[A, B, C, D, E, F, G, R](f: (A, B, C, D, E, F, G, Context[Any]) => R) = new WithContextAction7[A, B, C, D, E, F, G, R](f)

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