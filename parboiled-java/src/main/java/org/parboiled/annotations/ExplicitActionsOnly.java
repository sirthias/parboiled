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
 * Instructs parboiled to not perform implicit action expression wrapping, i.e. not treat expressions that form
 * parameters to Boolean.valueOf(boolean) calls as action expressions.
 * Instead only expressions wrapped by explicit calls to {@link org.parboiled.BaseParser#ACTION(boolean)} will be
 * treated as action expressions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExplicitActionsOnly {
}
