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

import org.parboiled.MatcherContext;
import org.parboiled.common.ImmutableList;
import org.parboiled.common.Preconditions;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

public class ReportFirstParseErrorHandler<V> implements ParseErrorHandler<V> {

    private MatcherContext<V> rootContext;
    private InputLocation errorLocation;
    private boolean seekingToParseError;
    private boolean addedFirstError;

    public void beforeParsingRun(MatcherContext<V> rootContext) {
        this.rootContext = rootContext;
    }

    public void handleMatch(MatcherContext<V> context) {
        if (seekingToParseError) {
            if (!addedFirstError && context.getCurrentLocation().index == errorLocation.index) {
                // we are back at the location we previously marked as the error location
                createParseError(context.getPath(), context.getCurrentFollowerMatchers());
                addedFirstError = true;
            }
        } else if (errorLocation == null || errorLocation.index < context.getCurrentLocation().index) {
            // record the last successful match, the current location might be a parse error
            errorLocation = context.getCurrentLocation();
        }
    }

    public boolean handleMismatch(MatcherContext<V> context) {
        if (context == rootContext) {
            if (seekingToParseError) {
                // we completed the second run and already added the error, so don't rerun
                seekingToParseError = false;
                Preconditions.checkState(addedFirstError);
            } else {
                if (errorLocation == null) {
                    // if we have never seen a successful match
                    // create a parse error for the first input char and don't rerun
                    errorLocation = context.getCurrentLocation();
                    createParseError(null, ImmutableList.of(context.getMatcher()));
                } else {
                    seekingToParseError = true;
                }
            }
        }
        return false; // never "overrule" a mismatch, since we don't recover
    }

    public boolean isRerunRequested(MatcherContext<V> context) {
        return seekingToParseError;
    }

    private void createParseError(MatcherPath<V> lastMatchPath, List<Matcher<V>> expectedMatchers) {
        rootContext.addParseError(new ParseError<V>(errorLocation, lastMatchPath,
                String.format("Invalid input '%s', expected %s",
                        StringUtils.escape(String.valueOf(errorLocation.currentChar)),
                        getExpectedString(expectedMatchers)
                )
        ));
    }

    private String getExpectedString(List<Matcher<V>> expectedMatchers) {
        List<String> labelList = new ArrayList<String>();
        ToFirstLabelVisitor<V> toFirstLabelVisitor = new ToFirstLabelVisitor<V>(labelList);
        for (Matcher<V> follower : expectedMatchers) {
            follower.accept(toFirstLabelVisitor);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labelList.size(); i++) {
            if (i > 0) sb.append(i < labelList.size() - 1 ? ", " : " or ");
            sb.append(labelList.get(i));
        }
        return StringUtils.escape(sb.toString());
    }

}
