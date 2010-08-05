package org.parboiled.scala

object Support {

  type RuleMethod = StackTraceElement

  private[parboiled] def getCurrentRuleMethod: StackTraceElement = {
    try {
      throw new Throwable
    } catch {
      case t: Throwable => t.getStackTrace()(3)
    }
  }

}