package org.parboiled.scala

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

/**
 * Enables memoization of rule mismatches for consecutive rule applications at the same input location.
 */
case object MemoMismatches extends RuleOption