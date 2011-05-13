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

import org.parboiled.MatchHandler;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchervisitors.IsSingleCharMatcherVisitor;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;

import java.util.ArrayList;
import java.util.List;

import static org.parboiled.common.Preconditions.checkArgNotNull;

/**
 * A {@link org.parboiled.parserunners.ParseRunner} implementation that creates an
 * {@link org.parboiled.errors.InvalidInputError} for the error at a known error location.
 * It never causes the parser to perform more than one parsing run and is rarely used directly.
 * Instead its functionality is relied upon by the {@link ReportingParseRunner} and {@link RecoveringParseRunner} classes.
 */
public class ErrorReportingParseRunner<V> extends AbstractParseRunner<V> implements MatchHandler {
    private final IsSingleCharMatcherVisitor isSingleCharMatcherVisitor = new IsSingleCharMatcherVisitor();
    private final int errorIndex;
    private final MatchHandler inner;
    private final List<MatcherPath> failedMatchers = new ArrayList<MatcherPath>();
    private boolean seeking;

    /**
     * Creates a new ErrorReportingParseRunner instance for the given rule and the given errorIndex.
     *
     * @param rule       the parser rule
     * @param errorIndex the index of the error to report
     */
    public ErrorReportingParseRunner(Rule rule, int errorIndex) {
        this(rule, errorIndex, null);
    }

    /**
     * Creates a new ErrorReportingParseRunner instance for the given rule and the given errorIndex.
     * The given MatchHandler is used as a delegate for the actual match handling.
     *
     * @param rule       the parser rule
     * @param errorIndex the index of the error to report
     * @param inner      another MatchHandler to delegate the actual match handling to, can be null
     */
    public ErrorReportingParseRunner(Rule rule, int errorIndex, MatchHandler inner) {
        super(rule);
        this.errorIndex = errorIndex;
        this.inner = inner;
    }

    public ParsingResult<V> run(InputBuffer inputBuffer) {
        checkArgNotNull(inputBuffer, "inputBuffer");
        resetValueStack();        
        failedMatchers.clear();
        seeking = errorIndex > 0;

        // run without fast string matching to properly get to the error location
        MatcherContext<V> rootContext = createRootContext(inputBuffer, this, false);
        boolean matched = match(rootContext);
        if (!matched) {
            getParseErrors().add(new InvalidInputError(inputBuffer, errorIndex, failedMatchers, null));
        }
        return createParsingResult(matched, rootContext);
    }

    public boolean match(MatcherContext<?> context) {
        boolean matched = inner == null && context.getMatcher().match(context) || inner != null && inner.match(context);
        if (context.getCurrentIndex() == errorIndex) {
            if (matched && seeking) {
                seeking = false;
            }
            if (!matched && !seeking && context.getMatcher().accept(isSingleCharMatcherVisitor)) {
                failedMatchers.add(context.getPath());
            }
        }
        return matched;
    }
}

