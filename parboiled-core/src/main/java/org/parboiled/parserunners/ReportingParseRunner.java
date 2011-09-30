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

package org.parboiled.parserunners;

import org.parboiled.Rule;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.Preconditions;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.support.ParsingResult;

import static org.parboiled.common.Preconditions.checkArgNotNull;

/**
 * A {@link ParseRunner} implementation that properly reports the first {@link InvalidInputError} if the input
 * does not conform to the rule grammar.
 * It performs exactly as the {@link BasicParseRunner} on valid input, however, on invalid input two more parsing
 * runs are initiated: one for recording the first parse error and one for collecting the error report information.
 */
public class ReportingParseRunner<V> extends AbstractParseRunner<V> {

    /**
     * Create a new ReportingParseRunner instance with the given rule and input text and returns the result of
     * its {@link #run(String)} method invocation.
     *
     * @param rule  the parser rule to run
     * @param input the input text to run on
     * @return the ParsingResult for the parsing run
     * @deprecated  As of 0.11.0 you should use the "regular" constructor and one of the "run" methods rather than
     * this static method. This method will be removed in one of the coming releases.
     */
    @Deprecated
    public static <V> ParsingResult<V> run(Rule rule, String input) {
        checkArgNotNull(rule, "rule");
        checkArgNotNull(input, "input");
        return new ReportingParseRunner<V>(rule).run(input);
    }

    /**
     * Creates a new ReportingParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public ReportingParseRunner(Rule rule) {
        super(rule);
    }

    public ParsingResult<V> run(InputBuffer inputBuffer) {
        checkArgNotNull(inputBuffer, "inputBuffer");
        resetValueStack();

        // first, run a basic match
        ParsingResult<V> result = runBasicMatch(inputBuffer);
        if (result.matched) return result; // all good

        // ok, we have a parse error, so determine the error location
        resetValueStack();
        result = runLocatingMatch(inputBuffer);
        Preconditions.checkState(!result.matched); // we failed before so we should really be failing again
        Preconditions.checkState(result.parseErrors.size() >= 1); // may be more than one in case of custom ActionExceptions
        
        // finally perform a third, reporting run (now that we know the error location)
        resetValueStack();
        result = runReportingMatch(inputBuffer, result.parseErrors.get(0).getStartIndex());
        Preconditions.checkState(!result.matched); // we failed before so we should really be failing again
        return result;
    }

    protected ParsingResult<V> runBasicMatch(InputBuffer inputBuffer) {
        ParseRunner<V> basicRunner = new BasicParseRunner<V>(getRootMatcher())
            .withParseErrors(getParseErrors())
            .withValueStack(getValueStack());
        return basicRunner.run(inputBuffer);
    }

    protected ParsingResult<V> runLocatingMatch(InputBuffer inputBuffer) {
        ParseRunner<V> locatingRunner = new ErrorLocatingParseRunner<V>(getRootMatcher())
                .withValueStack(getValueStack());
        return locatingRunner.run(inputBuffer);
    }

    protected ParsingResult<V> runReportingMatch(InputBuffer inputBuffer, int errorIndex) {
        ParseRunner<V> reportingRunner = new ErrorReportingParseRunner<V>(getRootMatcher(), errorIndex)
                .withParseErrors(getParseErrors())
                .withValueStack(getValueStack());
        return reportingRunner.run(inputBuffer);
    }
}

