package org.parboiled.scala.testing

import util.DynamicVariable
import org.parboiled.scala._
import org.parboiled.errors.ErrorUtils
import org.parboiled.support.ParseTreeUtils
import rules.{Rule1, Rule0}

trait ParboiledTest {
  this: {def fail(msg: String): Nothing} =>

  type Result

  protected val pResult = new DynamicVariable[ParsingResult[Result]](null)

  def parse(rule: Rule0, input: Input)(f: => Unit) = {
    val res: ParsingResult[Nothing] = ReportingParseRunner(rule).run(input)
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

  def parse(rule: Rule1[Result], input: Input)(f: => Unit) = {
    val res = ReportingParseRunner(rule).run(input)
    verify(res)
    pResult.withValue(res)(f)
  }

  def failParse(rule: Rule1[Result], input: Input)(f: => Unit) = {
    val res = ReportingParseRunner(rule).run(input)
    if (res.matched) {
      fail("Test unexpectedly succeeded")
    }
    pResult.withValue(res)(f)
  }

  def parseWithRecovery(rule: Rule1[Result], input: Input)(f: => Unit) = {
    val res = RecoveringParseRunner(rule).run(input)
    pResult.withValue(res)(f)
  }

  protected def verify[W](res: ParsingResult[W]) {
    if (!res.matched) {
      fail(ErrorUtils.printParseErrors(res))
    }
  }

  def parsingResult = pResult.value

  def matched = parsingResult.matched

  def result = parsingResult.result 

  def resultValue= parsingResult.resultValue

  def parseErrors = parsingResult.parseErrors

  def parseTreeRoot = parsingResult.parseTreeRoot

  def errors = ErrorUtils.printParseErrors(parsingResult)

  def parseTree = ParseTreeUtils.printNodeTree(parsingResult)
}