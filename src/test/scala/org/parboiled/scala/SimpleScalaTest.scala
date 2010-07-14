package org.parboiled.scala

import org.testng.annotations.Test
import org.parboiled.test.AbstractTest

class SimpleScalaTest extends AbstractTest {
  val parser = new SimpleParser

  @Test
  def verifyEasy() = {
    val rule = parser.InputLine
    testWithoutRecovery(rule.toRule, "1+5b", "" +
            "[Clause] '1+5b'\n" +
            "    [Digit] '1'\n" +
            "    [Operator] '+'\n" +
            "        ['+'] '+'\n" +
            "    [Digit] '5'\n" +
            "    [[abcd]] 'b'\n" +
            "    [EOI]\n")
  }

}