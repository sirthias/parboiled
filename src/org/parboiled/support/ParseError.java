/*
 * Copyright (C) 2009 Mathias Doenitz
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
import org.parboiled.Context;
import org.parboiled.Node;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.Matcher;

/**
 * Immutable class holding all values describing a certain error encountered during a parsing run.
 */
public class ParseError {

    private final Context context;
    private final InputLocation errorStart;
    private final InputLocation errorEnd;
    private final Matcher failedMatcher;
    private final Node node;
    private final String errorMessage;

    public ParseError(@NotNull Context context, InputLocation errorStart, InputLocation errorEnd,
                      Matcher failedMatcher, Node node, @NotNull String errorMessage) {
        this.context = context;
        this.errorStart = errorStart;
        this.errorEnd = errorEnd;
        this.failedMatcher = failedMatcher;
        this.node = node;
        this.errorMessage = errorMessage;
    }

    @NotNull
    public Context getContext() {
        return context;
    }

    public InputLocation getErrorStart() {
        return errorStart;
    }

    public InputLocation getErrorEnd() {
        return errorEnd;
    }

    public Matcher getFailedMatcher() {
        return failedMatcher;
    }

    public Node getNode() {
        return node;
    }

    @NotNull
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + errorMessage;
    }

    public static String createMessageSuffix(@NotNull InputBuffer inputBuffer, @NotNull InputLocation start,
                                             @NotNull InputLocation end) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" (line %s, pos %s):", start.row + 1, start.column + 1));
        sb.append('\n');

        String line = StringUtils.getLine(inputBuffer.getBuffer(), start.row);
        sb.append(line);
        sb.append('\n');

        int charCount = Math
                .min(start.row == end.row ? end.index - start.index : 1000, StringUtils.length(line) - start.column);
        sb.append(StringUtils.repeat(' ', start.column));
        sb.append(StringUtils.repeat('^', Math.max(charCount, 1)));
        sb.append('\n');

        return sb.toString();
    }

}

