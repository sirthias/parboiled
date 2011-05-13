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
 * Annotation that can be used on parser rule methods (i.e. methods returning a {@link org.parboiled.Rule} or the
 * parser class itself.
 * Instructs parboiled to skip the evaluation of action expressions in the rule method (or all methods if the
 * annotation is used on the parser class itself) if the rule is currently being run inside a Test/TestNot rule
 * (no matter what the nesting depth is).
 * Note that this annotation only affects action expressions (explicit or implicit)! Custom action objects, be them
 * anonymous actions or instances of some other class implementing the {@link org.parboiled.Action} interface still
 * need to take care of their predicate sensitivities themselves.
 * If you use this annotation on the parser class itself you can override it on specific rule methods with the
 * {@link DontSkipActionsInPredicates} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SkipActionsInPredicates {
}