/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

package org.parboiled.errorhandling;

import org.parboiled.MatcherContext;

/**
 * A handler for parse errors that can be passed to
 * {@link org.parboiled.BaseParser#parse(org.parboiled.Rule, String, ParseErrorHandler)} in order to run custom logic
 * in the event of parse errors.
 *
 * @param <V>
 */
public interface ParseErrorHandler<V> {

    /**
     * <p>This method is being called by the parboiled parser every time a rule match was attempted (and either
     * succeeded or failed). Since this is the case many times during a parsing run this method should be fast
     * if parsing performance is of the essence.</p>
     * <p>If the context is enforced and the match failed the mismatch constitutes a parse error that the
     * handler can handle.</p>
     *
     * @param context the context of the rule attempt that was just performed
     * @return true for rematch
     */
    boolean handleMatchAttempt(MatcherContext<V> context);

}
