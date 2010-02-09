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

package org.parboiled.support;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;

public class ParseErrorMarker<V> {

    private InputLocation location;
    private MatcherPath<V> path;
    private ParseErrorMarker<V> next;

    public InputLocation getLocation() {
        return location;
    }

    public void setLocation(InputLocation location) {
        this.location = location;
    }

    public MatcherPath<V> getPath() {
        return path;
    }

    public void mark(MatcherContext<V> context) {
        if (location == null || location.index < context.getCurrentLocation().index) {
            location = context.getCurrentLocation();
            path = new MatcherPath<V>(context);
        }
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

    public boolean matchesState(@NotNull MatcherContext<V> context) {
        return location != null &&
                location.index == context.getCurrentLocation().index &&
                path.matches(context);
    }

    public ParseError<V> createParseError() {
        return new ParseError<V>(location, path, String.format("Invalid input '%s', expected %s",
                location.currentChar != Chars.EOI ? location.currentChar : "EOI",
                path.getHead().getExpectedString()));
    }

}
