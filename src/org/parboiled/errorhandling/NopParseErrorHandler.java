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
 * The most trivial implementation of the {@link ParseErrorHandler} interface.
 * It does not report any parse errors nor recover from them. Therefore it never causes the parser to perform more
 * than one parsing run and is the faster way to determine whether a given input conforms to the rule grammar.
 *
 * @param <V>
 */
public class NopParseErrorHandler<V> implements ParseErrorHandler<V> {

    private NopParseErrorHandler() {
    }

    public void initialize() {
    }

    public void initializeBeforeParsingRerun(MatcherContext rootContext) {
    }

    public void handleMatch(MatcherContext context) {
    }

    public boolean handleMismatch(MatcherContext context) {
        return false;
    }

    public boolean isRerunRequested(MatcherContext rootContext) {
        return false;
    }
}
