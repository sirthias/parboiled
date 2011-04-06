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

package org.parboiled.scala

/**
 * Rule building expressions can take a number of options which are implemented as case objects derived from this
 * class.
 */
sealed abstract class RuleOption

/**
 * This rule option advises parboiled to not create a parse tree node for this rule and all sub rules
 * (in case that parse tree building is enabled on the parser).
 */
case object SuppressNode extends RuleOption

/**
 * This rule option advises parboiled to not create a parse tree node for the sub rules of this rule
 * (in case that parse tree building is enabled on the parser).
 */
case object SuppressSubnodes extends RuleOption

/**
 * This rule option advises parboiled to not create a parse tree node for this rule
 * (in case that parse tree building is enabled on the parser).
 */
case object SkipNode extends RuleOption

/**
 * Enables memoization of rule mismatches for consecutive rule applications at the same input location.
 */
case object MemoMismatches extends RuleOption