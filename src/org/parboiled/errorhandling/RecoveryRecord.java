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

import org.parboiled.common.Formatter;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

public class RecoveryRecord<V> {

    public final List<MatcherPath<V>> failedMatcherPaths = new ArrayList<MatcherPath<V>>();
    public InputLocation errorLocation;
    public MatcherPath<V> lastMatch;
    public InvalidInputError<V> parseError;
    public RecoveryRecord<V> next;

    public RecoveryRecord<V> getNext() {
        if (next == null) {
            next = new RecoveryRecord<V>();
        }
        return next;
    }

    public InvalidInputError<V> createParseError(Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        parseError = new InvalidInputError<V>(errorLocation, lastMatch, failedMatcherPaths, invalidInputErrorFormatter);
        return parseError;
    }

    public boolean matchesLocation(InputLocation location) {
        return errorLocation.index == location.index;
    }

    public void advanceErrorLocation(InputLocation currentLocation) {
        if (errorLocation.index < currentLocation.index) {
            errorLocation = currentLocation;
        }
    }
    
}
