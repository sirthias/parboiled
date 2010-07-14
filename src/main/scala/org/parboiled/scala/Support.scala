package org.parboiled.scala

import org.parboiled.matchers.{CharMatcher, AnyMatcher, EmptyMatcher}
import org.parboiled.support.Characters

private[scala] object Support {

  def getCurrentRuleMethod:StackTraceElement = {
     try {
       throw new Throwable
     } catch {
       case t: Throwable => t.getStackTrace()(1)
     }
  }

}