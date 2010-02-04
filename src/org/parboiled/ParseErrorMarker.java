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

package org.parboiled;

import org.parboiled.matchers.Matcher;
import org.parboiled.support.Chars;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParseError;

class ParseErrorMarker<V> {

    public InputLocation location;
    public MatcherPath<V> path;
    public ParseErrorMarker<V> next;

    public void mark(Matcher<V> matcher, MatcherContext<V> context) {
        location = context.getCurrentLocation();
        path = new MatcherPath<V>(matcher, context);
    }

    public Matcher<V> getFailedMatcher() {
        return path.getHead();
    }

    public ParseError<V> createParseError() {
        return new ParseError<V>(location, path, String.format("Invalid input '%s', expected %s",
                location.currentChar != Chars.EOI ? location.currentChar : "EOI",
                getFailedMatcher().getExpectedString()));
    }

    public ParseErrorMarker<V> getNext() {
        if (next == null) {
            next = new ParseErrorMarker<V>();
        }
        return next;
    }

    public boolean isValid() {
        return location != null;
    }

    public boolean matchesState(MatcherContext<V> context, Matcher<V> matcher) {
        return location != null &&
                location.index == context.getCurrentLocation().index &&
                path.matches(matcher, context);
    }

}
