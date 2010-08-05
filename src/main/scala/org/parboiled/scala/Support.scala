package org.parboiled.scala

import collection.mutable.Set

object Support {

  type RuleMethod = StackTraceElement

  private[parboiled] def getCurrentRuleMethod: StackTraceElement = {
    try {
      throw new Throwable
    } catch {
      case t: Throwable => t.getStackTrace()(3)
    }
  }

  def printRule[T](rule: Rule): String = {
    printCreator(rule.creator, new StringBuilder, "", Set.empty[MatcherCreator]).toString
  }

  private def printCreator[T](creator: MatcherCreator, sb: StringBuilder, indent: String, printed: Set[MatcherCreator]): StringBuilder = {
    val printSub =
      if (printed.contains(creator)) ((c: MatcherCreator) => sb)
      else printCreator(_: MatcherCreator, sb, indent + "  ", printed)
    printed += creator
    sb.append(indent)
    sb.append(creator.toString)
    sb += '\n'
    creator match {
      case c: UnaryCreator => printSub(c.sub)
      case c: NaryCreator => for (sub <- c.subs.reverse) printSub(sub)
      case c: ProxyCreator => printSub(c.inner.creator) 
      case _ =>
    }
    sb
  }

}