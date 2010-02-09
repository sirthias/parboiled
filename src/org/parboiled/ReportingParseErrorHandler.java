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

import org.parboiled.support.Chars;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParseError;

public class ReportingParseErrorHandler<V> implements ParseErrorHandler<V> {

    public boolean handleParseError(MatcherContext<V> context) {
        InputLocation location = context.getCurrentLocation();

        // don't add a new ParseError if we already have one at the same location or deeper in the input
        for (ParseError<V> error : context.getParseErrors()) {
            if (error.getLocation().index >= location.index) return false;
        }

        MatcherPath<V> path = context.getPath();
        ParseError<V> error = new ParseError<V>(
                location,
                path,
                String.format("Invalid input '%s', expected %s", Chars.toString(location.currentChar),
                        path.getHead().getExpectedString())
        );
        context.addParseError(error);
        return false;
    }

}
