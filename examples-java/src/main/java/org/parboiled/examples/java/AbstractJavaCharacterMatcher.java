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

package org.parboiled.examples.java;

import org.parboiled.MatcherContext;
import org.parboiled.matchers.CustomMatcher;

public abstract class AbstractJavaCharacterMatcher extends CustomMatcher {

    protected AbstractJavaCharacterMatcher(String label) {
        super(label);
    }

    @Override
    public final boolean isSingleCharMatcher() {
        return true;
    }

    @Override
    public final boolean canMatchEmpty() {
        return false;
    }

    @Override
    public boolean isStarterChar(char c) {
        return acceptChar(c);
    }

    @Override
    public final char getStarterChar() {
        return 'a';
    }

    public final <V> boolean match(MatcherContext<V> context) {
        if (!acceptChar(context.getCurrentChar())) {
            return false;
        }
        context.advanceIndex(1);
        context.createNode();
        return true;
    }

    protected abstract boolean acceptChar(char c);
}
