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

package org.parboiled.errors;

import org.jetbrains.annotations.NotNull;
import org.parboiled.common.Formatter;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.*;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.List;

public class ErrorUtils {

    public static <V> Matcher<V> findProperLabelMatcher(@NotNull MatcherPath<V> failedMatcherPath,
                                                        MatcherPath<V> lastMatchPath) {
        int commonPrefixLength = failedMatcherPath.getCommonPrefixLength(lastMatchPath);
        if (lastMatchPath != null && commonPrefixLength == lastMatchPath.length()) {
            return failedMatcherPath.getHead();
        }

        DefaultMatcherVisitor<V, Boolean> hasProperLabelVisitor = new DefaultMatcherVisitor<V, Boolean>() {
            @Override
            public Boolean visit(ActionMatcher<V> matcher) {
                return false;
            }

            @Override
            public Boolean visit(EmptyMatcher<V> matcher) {
                return false;
            }

            @Override
            public Boolean visit(FirstOfMatcher<V> matcher) {
                String label = matcher.getLabel();
                return !"firstOf".equals(label);
            }

            @Override
            public Boolean visit(OneOrMoreMatcher<V> matcher) {
                return !"oneOrMore".equals(matcher.getLabel());
            }

            @Override
            public Boolean visit(OptionalMatcher<V> matcher) {
                return !"optional".equals(matcher.getLabel());
            }

            @Override
            public Boolean visit(SequenceMatcher<V> matcher) {
                return !"sequence".equals(matcher.getLabel());
            }

            @Override
            public Boolean visit(ZeroOrMoreMatcher<V> matcher) {
                return !"zeroOrMore".equals(matcher.getLabel());
            }

            @Override
            public Boolean defaultValue(AbstractMatcher<V> matcher) {
                return true;
            }
        };

        for (int i = commonPrefixLength; i < failedMatcherPath.length(); i++) {
            Matcher<V> matcher = failedMatcherPath.get(i);
            if (matcher.accept(hasProperLabelVisitor)) {
                return matcher;
            }
        }
        return null;
    }

    /**
     * Pretty prints the given parse error showing its location in the given input buffer.
     *
     * @param error       the parse error
     * @param inputBuffer the input buffer
     * @return the pretty print text
     */
    @SuppressWarnings({"unchecked"})
    public static String printParseError(@NotNull ParseError error, @NotNull InputBuffer inputBuffer) {
        return printParseError(error, inputBuffer, new DefaultInvalidInputErrorFormatter());
    }

    /**
     * Pretty prints the given parse error showing its location in the given input buffer.
     *
     * @param error       the parse error
     * @param inputBuffer the input buffer
     * @param formatter   the formatter for InvalidInputErrors
     * @return the pretty print text
     */
    @SuppressWarnings({"unchecked"})
    public static <V> String printParseError(@NotNull ParseError error, @NotNull InputBuffer inputBuffer,
                                             @NotNull Formatter<InvalidInputError<V>> formatter) {
        InputLocation start = error.getErrorLocation();
        String message = error.getErrorMessage() != null ? error.getErrorMessage() :
                error instanceof InvalidInputError ?
                        formatter.format((InvalidInputError<V>) error) : error.getClass().getSimpleName();

        StringBuilder sb = new StringBuilder(message);
        sb.append(String.format(" (line %s, pos %s):", start.getRow() + 1, start.getColumn() + 1));
        sb.append('\n');

        String line = StringUtils.getLine(inputBuffer.getBuffer(), start.getRow());
        sb.append(line);
        sb.append('\n');

        int charCount = Math.min(error.getErrorCharCount(), StringUtils.length(line) - start.getColumn() + 1);
        for (int i = 0; i < start.getColumn(); i++) sb.append(' ');
        for (int i = 0; i < charCount; i++) sb.append('^');
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Pretty prints the given parse errors showing their location in the given input buffer.
     *
     * @param errors      the parse errors
     * @param inputBuffer the input buffer
     * @return the pretty print text
     */
    public static String printParseErrors(@NotNull List<ParseError> errors, @NotNull InputBuffer inputBuffer) {
        StringBuilder sb = new StringBuilder();
        for (ParseError error : errors) {
            if (sb.length() > 0) sb.append("---\n");
            sb.append(printParseError(error, inputBuffer));
        }
        return sb.toString();
    }

}
