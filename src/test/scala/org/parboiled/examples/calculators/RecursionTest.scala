package org.parboiled.examples.calculators

import org.testng.annotations.Test
import org.parboiled.test.AbstractTest
import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import org.parboiled.scala._
import org.parboiled.matchers.Matcher
import org.parboiled.support.ToStringFormatter
import org.parboiled.trees.{Filters, Filter, GraphUtils}

class RecursionTest extends AbstractTest with TestNGSuite {

  class RecursionParser extends Parser {
    def LotsOfAs: Rule0 = rule { ignoreCase('a') ~ optional(LotsOfAs) }
  }

  val parser = new RecursionParser().withParseTreeBuilding()

  @Test
  def testSimpleParser() = {
    val rule = parser.LotsOfAs
    assertEquals(Support.printRule(rule),
      """LotsOfAs: SequenceCreator
  'a/A': SimpleCreator
  Optional: UnaryCreator
    ProxyCreator
      LotsOfAs: SequenceCreator
""")

    assertEquals(GraphUtils.printTree(rule.toMatcher, new ToStringFormatter[Matcher], Filters.preventLoops()),
      """LotsOfAs
  'a/A'
  Optional
    LotsOfAs
""");

    val res = testWithoutRecovery(rule.toMatcher, "aAAAa",
      """[LotsOfAs] 'aAAAa'
  ['a/A'] 'a'
  [Optional] 'AAAa'
    [LotsOfAs] 'AAAa'
      ['a/A'] 'A'
      [Optional] 'AAa'
        [LotsOfAs] 'AAa'
          ['a/A'] 'A'
          [Optional] 'Aa'
            [LotsOfAs] 'Aa'
              ['a/A'] 'A'
              [Optional] 'a'
                [LotsOfAs] 'a'
                  ['a/A'] 'a'
                  [Optional]
""")
  }

}