package org.parboiled.scala.test

import org.parboiled.scala._

trait ParboiledTest extends TypedParboiledTest[Any] {
  this: {def fail(msg: String): Nothing} =>

  def parse(rule: Rule0, input: Input)(f: => Unit) = {
    val res: ParsingResult[Any] = ReportingParseRunner(rule).run(input)
    verify(res)
    pResult.withValue(res)(f)
  }

  def failParse(rule: Rule0, input: Input)(f: => Unit) = {
    val res = ReportingParseRunner(rule).run(input)
    if (res.matched) {
      fail("Test unexpectedly succeeded")
    }
    pResult.withValue(res)(f)
  }

  def parseWithRecovery(rule: Rule0, input: Input)(f: => Unit) = {
    val res = RecoveringParseRunner(rule).run(input)
    pResult.withValue(res)(f)
  }

}