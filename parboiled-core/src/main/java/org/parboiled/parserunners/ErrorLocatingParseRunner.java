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
import org.parboiled.errors.BasicParseError;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchers.TestNotMatcher;
import org.parboiled.support.ParsingResult;

import static org.parboiled.common.Preconditions.checkArgNotNull;

/**
 * A {@link ParseRunner} implementation that creates a simple {@link BasicParseError} for the first error found in the
 * input and adds it to the list of ParseErrors.
 * It never causes the parser to perform more than one parsing run and is rarely used directly.
 * Instead its functionality is relied upon by the {@link ReportingParseRunner} and {@link RecoveringParseRunner} classes.
 */
public class ErrorLocatingParseRunner<V> extends AbstractParseRunner<V> implements MatchHandler {
    private final MatchHandler inner;
    private int errorIndex;

    /**
     * Creates a new ErrorLocatingParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public ErrorLocatingParseRunner(Rule rule) {
        this(rule, null);
    }
    
    /**
     * Creates a new ErrorLocatingParseRunner instance for the given rule.
     * The given MatchHandler is used as a delegate for the actual match handling.
     *
     * @param rule the parser rule
     * @param inner another MatchHandler to delegate the actual match handling to, can be null
     */
    public ErrorLocatingParseRunner(Rule rule, MatchHandler inner) {
        super(rule);
        this.inner = inner;
    }

    public ParsingResult<V> run(InputBuffer inputBuffer) {
        checkArgNotNull(inputBuffer, "inputBuffer");
        resetValueStack();
        errorIndex = 0;
        
        // run without fast string matching to properly get the error location
        MatcherContext<V> rootContext = createRootContext(inputBuffer, this, false);
        boolean matched = match(rootContext);
        if (!matched) {
            getParseErrors().add(new BasicParseError(inputBuffer, errorIndex, null));
        }
        return createParsingResult(matched, rootContext);
    }

    public boolean match(MatcherContext<?> context) {
        if (inner == null && context.getMatcher().match(context) || inner != null && inner.match(context)) {
            if (errorIndex < context.getCurrentIndex() && notTestNot(context)) {
                errorIndex = context.getCurrentIndex();
            }
            return true;
        }
        return false;
    }

    private boolean notTestNot(MatcherContext context) {
        return !(context.getMatcher() instanceof TestNotMatcher) &&
                (context.getParent() == null || notTestNot(context.getParent()));
    }
}