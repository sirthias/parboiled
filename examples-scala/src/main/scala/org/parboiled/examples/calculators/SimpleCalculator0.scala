/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.examples.calculators

import org.parboiled.scala._

/**
 * A recognizer for a very simple calculator language supporting the 4 basic calculation types on integers.
 * This grammar does not contain any actions, it only serves for determinining whether a given input conforms to
 * the language. The SimpleCalculator1 adds the actual calculation actions.
 */
class SimpleCalculator0 extends Parser {

  def InputLine = rule { Expression ~ EOI }

  def Expression: Rule0 = rule { Term ~ zeroOrMore(anyOf("+-") ~ Term) }

  def Term = rule { Factor ~ zeroOrMore(anyOf("*/") ~ Factor) }

  def Factor = rule { Digits | Parens }

  def Parens = rule { "(" ~ Expression ~ ")" }

  def Digits = rule { oneOrMore(Digit) }

  def Digit = rule { "0" - "9" }
}