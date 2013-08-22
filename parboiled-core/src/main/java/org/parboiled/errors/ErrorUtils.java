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

package org.parboiled.errors;

import static org.parboiled.common.Preconditions.*;

import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.Formatter;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.TestNotMatcher;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Position;

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
     * @param path       the path to the failed matcher
     * @param errorIndex the start index of the respective parse error
     * @return the matcher whose label is best for presentation in "expected" strings
     */
    static Matcher findProperLabelMatcher(MatcherPath path, int errorIndex) {
        try { return findProperLabelMatcher0(path, errorIndex); }
        catch(RuntimeException e) {
            if (e == UnderneathTestNot) return null; else throw e;
        }
    }

    private static RuntimeException UnderneathTestNot = new RuntimeException() {
        @Override public synchronized Throwable fillInStackTrace() { return this; }
    };
    private static Matcher findProperLabelMatcher0(MatcherPath path, int errorIndex) {
        checkArgNotNull(path, "path");
        Matcher found = path.parent != null ? findProperLabelMatcher0(path.parent, errorIndex) : null;
        if (found != null) return found;
        Matcher m = path.element.matcher;
        if (m instanceof TestNotMatcher) throw UnderneathTestNot;
        if (path.element.startIndex == errorIndex && m.hasCustomLabel()) return m;
        return null;
    }

    /**
     * Pretty prints the parse errors of the given ParsingResult showing their location in the given input buffer.
     *
     * @param parsingResult the parsing result
     * @return the pretty print text
     */
    public static String printParseErrors(ParsingResult<?> parsingResult) {
        checkArgNotNull(parsingResult, "parsingResult");
        return printParseErrors(parsingResult.parseErrors);
    }

    /**
     * Pretty prints the given parse errors showing their location in the given input buffer.
     *
     * @param errors      the parse errors
     * @return the pretty print text
     */
    public static String printParseErrors(List<ParseError> errors) {
        checkArgNotNull(errors, "errors");
        StringBuilder sb = new StringBuilder();
        for (ParseError error : errors) {
            if (sb.length() > 0) sb.append("---\n");
            sb.append(printParseError(error));
        }
        return sb.toString();
    }

    /**
     * Pretty prints the given parse error showing its location in the given input buffer.
     *
     * @param error       the parse error
     * @return the pretty print text
     */
    public static String printParseError(ParseError error) {
        checkArgNotNull(error, "error");
        return printParseError(error, new DefaultInvalidInputErrorFormatter());
    }

    /**
     * Pretty prints the given parse error showing its location in the given input buffer.
     *
     * @param error       the parse error
     * @param formatter   the formatter for InvalidInputErrors
     * @return the pretty print text
     */
    public static String printParseError(ParseError error, Formatter<InvalidInputError> formatter) {
        checkArgNotNull(error, "error");
        checkArgNotNull(formatter, "formatter");
        String message = error.getErrorMessage() != null ? error.getErrorMessage() :
                error instanceof InvalidInputError ?
                        formatter.format((InvalidInputError) error) : error.getClass().getSimpleName();
        return printErrorMessage("%s (line %s, pos %s):", message,
                error.getStartIndex(), error.getEndIndex(), error.getInputBuffer());
    }

    /**
     * Prints an error message showing a location in the given InputBuffer.
     *
     * @param format       the format string, must include three placeholders for a string
     *                     (the error message) and two integers (the error line / column respectively)
     * @param errorMessage the error message
     * @param errorIndex   the error location as an index into the inputBuffer
     * @param inputBuffer  the underlying InputBuffer
     * @return the error message including the relevant line from the underlying input plus location indicator
     */
    public static String printErrorMessage(String format, String errorMessage, int errorIndex,
                                           InputBuffer inputBuffer) {
        checkArgNotNull(inputBuffer, "inputBuffer");
        return printErrorMessage(format, errorMessage, errorIndex, errorIndex + 1, inputBuffer);
    }

    /**
     * Prints an error message showing a location in the given InputBuffer.
     *
     * @param format       the format string, must include three placeholders for a string
     *                     (the error message) and two integers (the error line / column respectively)
     * @param errorMessage the error message
     * @param startIndex   the start location of the error as an index into the inputBuffer
     * @param endIndex     the end location of the error as an index into the inputBuffer
     * @param inputBuffer  the underlying InputBuffer
     * @return the error message including the relevant line from the underlying input plus location indicators
     */
    public static String printErrorMessage(String format, String errorMessage, int startIndex, int endIndex,
                                           InputBuffer inputBuffer) {
        checkArgNotNull(inputBuffer, "inputBuffer");
        checkArgument(startIndex <= endIndex);
        Position pos = inputBuffer.getPosition(startIndex);
        StringBuilder sb = new StringBuilder(String.format(format, errorMessage, pos.line, pos.column));
        sb.append('\n');

        String line = inputBuffer.extractLine(pos.line);
        sb.append(line);
        sb.append('\n');

        int charCount = Math.max(Math.min(endIndex - startIndex, StringUtils.length(line) - pos.column + 2), 1);
        for (int i = 0; i < pos.column - 1; i++) sb.append(' ');
        for (int i = 0; i < charCount; i++) sb.append('^');
        sb.append("\n");

        return sb.toString();
    }
}
