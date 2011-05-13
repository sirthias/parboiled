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

package org.parboiled.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used on parser methods returning {@link org.parboiled.Rule} objects.
 * Instructs parboiled to not create a parse tree node for this rule. The parse tree nodes of all subrules are
 * directly attached to the parent of this rule (or more correctly: the first ancestor not carrying @SkipNode).
 * Note that, even though a rule carrying @SkipNode does not create a parse tree node of its own and is therefore
 * "invisible" in the parse tree, the rule still exists as a regular rule in the rule tree and is accompanied by
 * a "regular" rule {@link org.parboiled.Context} during rule matching.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SkipNode {
}