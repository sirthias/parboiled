package org.parboiled

import collection.mutable.Set

private[parboiled] trait Support {

  type RuleMethod = StackTraceElement

  private[parboiled] def getCurrentRuleMethod: StackTraceElement = {
    try {
      throw new Throwable
    } catch {
      case t: Throwable => t.getStackTrace()(4)
    }
  }

  def printRule[T](rule: Rules[T]#Rule): String = printRule(rule, "", false, Set.empty[Rules[T]#Rule])

  private def printRule[T](rule: Rules[T]#Rule, indent: String, printIndent: Boolean, printed: Set[Rules[T]#Rule]): String = {
    val printSub = if (printed.contains(rule)) ((r: Rules[T]#Rule) => "")
    else printRule(_: Rules[T]#Rule, indent + "  ", true, printed)
    printed += rule
    (if (printIndent) indent else "") + (if (rule.label != null) rule.label + ": " else "") + rule + '\n' + (rule match {
      case r: Rules[_]#BinaryRule => printSub(r.left) + printSub(r.right)
      case r: Rules[_]#UnaryRule => printSub(r.sub)
      case r: Rules[_]#Rule => ""
    })
  }

}