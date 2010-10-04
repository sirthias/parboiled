package org.parboiled.scala.testing

import util.DynamicVariable
import org.parboiled.scala._
import org.parboiled.support.ParseTreeUtils
import org.parboiled.errors.{ParseError, ErrorUtils}
import org.parboiled.Node

/**
 * A trait simplifying the creation of tests for "parboiled for scala" parsers.
 * It can be mixed into any test class that defines a fail(string) method (e.g. all the scalatest Suites).
 */
trait ParboiledTest {
  this: {def fail(msg: String): Nothing} =>

  /**
   * The type of the root value object created by the parser.
   */
  type Result

  protected val pResult = new DynamicVariable[ParsingResult[_ <: Result]](null)
  protected val pRunner = new DynamicVariable[ParseRunner[_ <: Result]](null)

  /**
   * Runs the given parse runner against the given input and executes the given block upon a successful parsing run.
   * If the parsing run is not successful, i.e. the input did not match the grammar, the test fails will a respective
   * error message.
   */
  def parse(runner: ParseRunner[_ <: Result], input: Input)(f: => Unit) {
    pRunner.withValue(runner) {
      val res = runner.run(input)
      if (!res.matched) {
        fail(ErrorUtils.printParseErrors(res))
      }
      pResult.withValue(res)(f)
    }
  }

  /**
   * Runs the given parse runner against the given input and executes the given block upon an UNSUCCESSFUL parsing run.
   * If the parsing run is successful, i.e. the input did match the grammar, the test fails will a respective
   * error message.
   */
  def failParse(runner: ParseRunner[_ <: Result], input: Input)(f: => Unit) {
    pRunner.withValue(runner) {
      val res = runner.run(input)
      if (res.matched) {
        fail("Test unexpectedly succeeded")
      }
      pResult.withValue(res)(f)
    }
  }

  def parsingResult: ParsingResult[Result] = pResult.value

  def matched: Boolean = parsingResult.matched

  def result: Result = parsingResult.result

  def resultOption: Option[Result] = parsingResult.resultOption

  def parseErrors: List[ParseError] = parsingResult.parseErrors

  def parseTreeRoot: Node[Result] = parsingResult.parseTreeRoot

  def traceLog: String = pRunner.value match {
    case t: TracingParseRunner[_] => t.traceLog
    case _ => ""
  }

  def errors: String = ErrorUtils.printParseErrors(parsingResult)

  def parseTree: String = ParseTreeUtils.printNodeTree(parsingResult)
}