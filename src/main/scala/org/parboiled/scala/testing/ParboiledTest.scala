package org.parboiled.scala.testing

import util.DynamicVariable
import org.parboiled.scala._
import org.parboiled.errors.ErrorUtils
import org.parboiled.support.ParseTreeUtils

trait ParboiledTest {
  this: {def fail(msg: String): Nothing} =>

  type Result

  protected val pResult = new DynamicVariable[ParsingResult[_ <: Result]](null)
  protected val runner = new DynamicVariable[ParseRunner[_ <: Result]](null)

  def parse(rule: Rule0, input: Input)(f: => Unit) {
    runner.withValue(ReportingParseRunner(rule))(parse(input, f))
  }

  def parse(rule: Rule1[Result], input: Input)(f: => Unit) {
    runner.withValue(ReportingParseRunner(rule))(parse(input, f))
  }

  def traceParse(rule: Rule0, input: Input)(f: => Unit) {
    runner.withValue(TracingParseRunner(rule))(parse(input, f))
  }

  def traceParse(rule: Rule1[Result], input: Input)(f: => Unit) {
    runner.withValue(TracingParseRunner(rule))(parse(input, f))
  }

  protected def parse(input: Input, f: => Unit) {
    val res = runner.value.run(input)
    verify(res)
    pResult.withValue(res)(f)
  }

  def failParse(rule: Rule0, input: Input)(f: => Unit) {
    runner.withValue(ReportingParseRunner(rule))(failParse(input, f))
  }

  def failParse(rule: Rule1[Result], input: Input)(f: => Unit) {
    runner.withValue(ReportingParseRunner(rule))(failParse(input, f))
  }

  def traceFailParse(rule: Rule0, input: Input)(f: => Unit) {
    runner.withValue(TracingParseRunner(rule))(failParse(input, f))
  }

  def traceFailParse(rule: Rule1[Result], input: Input)(f: => Unit) {
    runner.withValue(TracingParseRunner(rule))(failParse(input, f))
  }

  protected def failParse(input: Input, f: => Unit) {
    val res = runner.value.run(input)
    if (res.matched) {
      fail("Test unexpectedly succeeded")
    }
    pResult.withValue(res)(f)
  }

  def parseWithRecovery(rule: Rule0, input: Input)(f: => Unit) {
    runner.withValue(RecoveringParseRunner(rule))(parseWithRecovery(input, f))
  }

  def parseWithRecovery(rule: Rule1[Result], input: Input)(f: => Unit) {
    runner.withValue(RecoveringParseRunner(rule))(parseWithRecovery(input, f))
  }

  protected def parseWithRecovery(input: Input, f: => Unit) {
    val res = runner.value.run(input)
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

  def resultOption = parsingResult.resultOption

  def parseErrors = parsingResult.parseErrors

  def parseTreeRoot = parsingResult.parseTreeRoot

  def traceLog = runner.value match {
    case t: TracingParseRunner[_] => t.traceLog
    case _ => ""
  }

  def errors = ErrorUtils.printParseErrors(parsingResult)

  def parseTree = ParseTreeUtils.printNodeTree(parsingResult)
}