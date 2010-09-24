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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.buffers.DefaultInputBuffer;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.Formatter;
import org.parboiled.common.Reference;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.*;
import org.parboiled.support.*;

import java.util.List;

/**
 * General utility methods regarding parse errors.
 */
public final class ErrorUtils {

    private ErrorUtils() {}

    /**
     * Finds the Matcher in the given failedMatcherPath whose label is best for presentation in "expected" strings
     * of parse error messages, given the provided lastMatchPath.
     *
     * @param failedMatcherPath the path to the failed matcher
     * @param lastMatchPath     the path of the last match
     * @return the matcher whose label is best for presentation in "expected" strings
     */
    public static Matcher findProperLabelMatcher(@NotNull final MatcherPath failedMatcherPath,
                                                 MatcherPath lastMatchPath) {
        int commonPrefixLength = failedMatcherPath.getCommonPrefixLength(lastMatchPath);
        if (lastMatchPath != null && commonPrefixLength == lastMatchPath.length()) {
            return failedMatcherPath.getHead();
        }

        final Reference<Integer> ix = new Reference<Integer>();

        DefaultMatcherVisitor<Boolean> hasProperLabelVisitor = new HasCustomLabelVisitor() {
            @Override
            public Boolean visit(SequenceMatcher matcher) {
                // if the sequence only has the default label we never use it
                if (!super.visit(matcher)) return false;

                // a sequence should never be the deepest matcher of a failed matcher path
                Preconditions.checkState(ix.get() + 1 < failedMatcherPath.length());

                // we only accept the sequence name as a good label if the failed matcher is its first "real" child
                Matcher failedSub = failedMatcherPath.get(ix.get() + 1);
                Matcher firstSub = null;
                for (int i = 0; i < matcher.getChildren().size(); i++) {
                    Matcher sub = matcher.getChildren().get(i);
                    // OptionalMatchers and zeroOrMoreMatchers do not count
                    if (sub instanceof OptionalMatcher || sub instanceof ZeroOrMoreMatcher) continue;
                    firstSub = ProxyMatcher.unwrap(sub);
                    break;
                }
                return firstSub == failedSub;
            }
        };

        for (int i = commonPrefixLength; i < failedMatcherPath.length(); i++) {
            ix.set(i);
            Matcher matcher = failedMatcherPath.get(i);
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
    public static String printParseError(@NotNull ParseError error, @NotNull InputBuffer inputBuffer,
                                         @NotNull Formatter<InvalidInputError> formatter) {
        int start = error.getStartIndex();
        String message = error.getErrorMessage() != null ? error.getErrorMessage() :
                error instanceof InvalidInputError ?
                        formatter.format((InvalidInputError) error) : error.getClass().getSimpleName();

        DefaultInputBuffer.Position pos = inputBuffer.getPosition(start);
        StringBuilder sb = new StringBuilder(message);
        sb.append(String.format(" (line %s, pos %s):", pos.line, pos.column));
        sb.append('\n');

        String line = inputBuffer.extractLine(pos.line);
        sb.append(line);
        sb.append('\n');

        int charCount = Math.min(
                error.getEndIndex() - error.getStartIndex(),
                StringUtils.length(line) - pos.column + 2
        );
        for (int i = 0; i < pos.column - 1; i++) sb.append(' ');
        for (int i = 0; i < charCount; i++) sb.append('^');
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Pretty prints the given parse errors showing their location in the given input buffer.
     *
     * @param parsingResult the parsing result
     * @return the pretty print text
     */
    public static String printParseErrors(@NotNull ParsingResult<?> parsingResult) {
        return printParseErrors(parsingResult.parseErrors, parsingResult.inputBuffer);
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
