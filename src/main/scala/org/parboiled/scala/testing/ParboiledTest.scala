package org.parboiled.scala.testing

import util.DynamicVariable
import org.parboiled.scala._
import org.parboiled.errors.ErrorUtils
import org.parboiled.support.ParseTreeUtils

trait ParboiledTest {
  this: {def fail(msg: String): Nothing} =>

  type Result

  protected val pResult = new DynamicVariable[ParsingResult[_ <: Result]](null)
  protected val pRunner = new DynamicVariable[ParseRunner[_ <: Result]](null)

  def parse(runner: ParseRunner[_ <: Result], input: Input)(f: => Unit) {
    pRunner.withValue(runner) {
      val res = runner.run(input)
      if (!res.matched) {
        fail(ErrorUtils.printParseErrors(res))
      }
      pResult.withValue(res)(f)
    }
  }

  def failParse(runner: ParseRunner[_ <: Result], input: Input)(f: => Unit) {
    pRunner.withValue(runner) {
      val res = runner.run(input)
      if (res.matched) {
        fail("Test unexpectedly succeeded")
      }
      pResult.withValue(res)(f)
    }
  }

  def parsingResult = pResult.value

  def matched = parsingResult.matched

  def result = parsingResult.result 

  def resultOption = parsingResult.resultOption

  def parseErrors = parsingResult.parseErrors

  def parseTreeRoot = parsingResult.parseTreeRoot

  def traceLog = pRunner.value match {
    case t: TracingParseRunner[_] => t.traceLog
    case _ => ""
  }

  def errors = ErrorUtils.printParseErrors(parsingResult)

  def parseTree = ParseTreeUtils.printNodeTree(parsingResult)
}