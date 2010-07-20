package org.parboiled

import collection.mutable.Set

private[parboiled] trait Support { this: Rules =>

  type RuleMethod = StackTraceElement

  private[parboiled] def getCurrentRuleMethod: StackTraceElement = {
    try {
      throw new Throwable
    } catch {
      case t: Throwable => t.getStackTrace()(4)
    }
  }

  def printRule(rule: Rule[_]):String = printRule(rule, "", false, Set.empty[Rule[_]])

  private def printRule(rule: Rule[_], indent: String, printIndent: Boolean, printed: Set[Rule[_]]): String = {
    val printSub =
      if (printed.contains(rule)) ((r:Rule[_]) => "") else printRule(_: Rule[_], indent + "  ", true, printed)
    printed += rule
    val ind = if (printIndent) indent else ""
    rule match {
      case r: LabelRule[_] => ind + r.label + ": " + printRule(r.sub, indent, false, printed)
      case r: BinaryRule[_, _, _] => ind + rule + '\n' + printSub(r.left) + printSub(r.right)
      case r: UnaryRule[_] => ind + rule + '\n' + printSub(r.sub)
      case r: LeafRule => ind + rule + ": " + r.matcher + '\n'
      case r: Rule[_] => ind + r + '\n'
    }
  }

}