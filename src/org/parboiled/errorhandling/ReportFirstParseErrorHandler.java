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
import org.parboiled.common.Utils;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.InputLocation;

import java.util.ArrayList;
import java.util.List;

public class ReportFirstParseErrorHandler<V> implements ParseErrorHandler<V> {

    private InputLocation errorLocation;
    private List<Matcher<V>> lastMatchFollowers;

    public boolean handleMatchAttempt(MatcherContext<V> context) {
        if (context.hasMatched()) {
            // for every successful match record the location reached and the current follower matchers
            // so we can create a nice error message should this match be the last successful one we see
            if (errorLocation == null || errorLocation.index < context.getCurrentLocation().index) {
                errorLocation = context.getCurrentLocation();
                lastMatchFollowers = context.getCurrentFollowerMatchers();
            }
        } else if (context.isEnforced()) {
            createParseError(context);
        }
        return false;
    }

    private void createParseError(MatcherContext<V> context) {
        // if we have never seen a successfull match initialize here
        if (errorLocation == null)  {
            errorLocation = context.getCurrentLocation();
            lastMatchFollowers = new ArrayList<Matcher<V>>();
            lastMatchFollowers.add(context.getMatcher());
        }

        // only create errors at input locations we have not already covered
        for (ParseError<V> error : context.getParseErrors()) {
            if (error.getLocation().index >= errorLocation.index) return;
        }

        // add a new parse error
        context.addParseError(new ParseError<V>(errorLocation, context.getPath(),
                String.format("Invalid input '%s', expected %s",
                        Utils.toString(errorLocation.currentChar),
                        getExpectedString()
                )
        ));
    }

    // creates a new expected string from the follow matchers of the last successful match
    private String getExpectedString() {
        List<String> labelList = new ArrayList<String>();
        ToFirstLabelVisitor<V> toFirstLabelVisitor = new ToFirstLabelVisitor<V>(labelList);
        for (Matcher<V> follower : lastMatchFollowers) {
            follower.accept(toFirstLabelVisitor);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labelList.size(); i++) {
            if (i > 0) sb.append(i < labelList.size() - 1 ? ", " : " or ");
            sb.append(labelList.get(i));
        }
        return sb.toString();
    }

}
