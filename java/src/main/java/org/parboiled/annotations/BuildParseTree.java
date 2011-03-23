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
 * <p>Annotation that can be used on the parser class itself.
 * Instructs parboiled to build a parse tree during a parsing run. When this annotation is present on the parser class
 * the parse tree building can be further tweaked by decorating certain rules with {@link SuppressNode},
 * {@link SuppressSubnodes} and/or {@link SkipNode}. When this annotation is not present on the parser class the listed
 * rule annotations do not have any effect because no parse tree is build at all.</p>
 * <p>Note: If the input contains parse errors and you use the {@link org.parboiled.parserunners.RecoveringParseRunner} parboiled
 * will create parse tree nodes for all rules that have recorded parse errors (note that this always includes the root
 * rule)</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BuildParseTree {
}