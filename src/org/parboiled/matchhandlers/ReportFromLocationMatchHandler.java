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

package org.parboiled.matchhandlers;

import com.google.common.base.Supplier;
import org.jetbrains.annotations.NotNull;
import org.parboiled.MatchHandler;
import org.parboiled.MatcherContext;
import org.parboiled.common.Formatter;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

public class ReportFromLocationMatchHandler<V> implements MatchHandler<V> {

    private final InputLocation errorLocation;
    private final List<MatcherPath<V>> failedMatchers = new ArrayList<MatcherPath<V>>();
    private final Formatter<InvalidInputError<V>> invalidInputErrorFormatter;
    private MatcherPath<V> lastMatch;
    private InvalidInputError<V> parseError;
    private boolean seeking;

    public ReportFromLocationMatchHandler(@NotNull InputLocation errorLocation) {
        this(errorLocation, new DefaultInvalidInputErrorFormatter<V>());
    }

    public ReportFromLocationMatchHandler(@NotNull InputLocation errorLocation,
                                          @NotNull Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this.errorLocation = errorLocation;
        this.invalidInputErrorFormatter = invalidInputErrorFormatter;
    }

    public InvalidInputError<V> getParseError() {
        return parseError;
    }

    public boolean matchRoot(@NotNull Supplier<MatcherContext<V>> rootContextSupplier) {
        failedMatchers.clear();
        MatcherContext<V> rootContext = rootContextSupplier.get();
        seeking = true;
        rootContext.runMatcher();

        parseError = new InvalidInputError<V>(errorLocation, lastMatch, failedMatchers, invalidInputErrorFormatter);
        rootContext.getParseErrors().add(parseError);
        return false;
    }

    public boolean match(MatcherContext<V> context) throws Throwable {
        boolean matched = context.getMatcher().match(context);
        if (context.getCurrentLocation() == errorLocation) {
            if (matched && seeking) {
                lastMatch = context.getPath();
                seeking = false;
            }
            if (!matched && !seeking) {
                failedMatchers.add(context.getPath());
            }
        }
        return matched;
    }

}