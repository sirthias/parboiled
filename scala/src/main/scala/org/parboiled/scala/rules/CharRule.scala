package org.parboiled.scala.rules

import org.parboiled.matchers._
import org.parboiled.common.StringUtils.escape
import java.lang.String

/**
 * A rule matching one single character.
 */
class CharRule(val c: Char) extends Rule0(new CharMatcher(c).label('\'' + escape(c) + '\'')) {

  /**
   * Creates a rule matching the range of characters between the character of this rule and the given character
   * (inclusively).
   */
  override def -(upperBound: String) = {
    require(upperBound != null && upperBound.length == 1)
    new Rule0(new CharRangeMatcher(c, upperBound.charAt(0)).label(c + ".." + upperBound))
  }

}


