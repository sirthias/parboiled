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

package org.parboiled.matchers;

import org.parboiled.MatcherContext;
import org.parboiled.matchervisitors.MatcherVisitor;
import org.parboiled.support.Chars;

import static org.parboiled.common.Preconditions.checkArgNotNull;
import static org.parboiled.common.StringUtils.escape;

/**
 * A {@link Matcher} matching a single given character.
 */
public class CharMatcher extends AbstractMatcher {
    public final char character;

    public CharMatcher(char character) {
        super(getLabel(character));
        this.character = character;
    }

    private static String getLabel(char c) {
        switch (c) {
            case Chars.DEL_ERROR:
            case Chars.INS_ERROR:
            case Chars.RESYNC:
            case Chars.RESYNC_START:
            case Chars.RESYNC_END:
            case Chars.RESYNC_EOI:
            case Chars.INDENT:
            case Chars.DEDENT:
            case Chars.EOI:
                return escape(c);
            default:
                return '\'' + escape(c) + '\'';
        }
    }

    public boolean match(MatcherContext context) {
        if (context.getCurrentChar() != character) return false;
        context.advanceIndex(1);
        context.createNode();
        return true;
    }

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        return visitor.visit(this);
    }

}