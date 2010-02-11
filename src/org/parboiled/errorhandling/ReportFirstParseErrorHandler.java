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

import java.util.List;
import java.util.ArrayList;

public class ReportFirstParseErrorHandler<V> implements ParseErrorHandler<V> {

    private int lastMatchIndex;
    private List<Matcher<V>> lastMatchFollowers;

    public boolean handleMatchAttempt(MatcherContext<V> context) {
        int currentIndex = context.getCurrentLocation().index;
        if (context.hasMatched()) {
            if (lastMatchIndex < currentIndex) {
                lastMatchFollowers = context.getCurrentFollowerMatchers();
                lastMatchIndex = currentIndex;
            }
            return false;
        }

        if (!context.isEnforced()) return false;

        for (ParseError<V> error : context.getParseErrors()) {
            if (error.getLocation().index >= currentIndex) return false;
        }

        if (lastMatchFollowers == null) {
            lastMatchFollowers = new ArrayList<Matcher<V>>();
            lastMatchFollowers.add(context.getMatcher());
        }

        context.addParseError(new ParseError<V>(context.getCurrentLocation(), context.getPath(),
                String.format("Invalid input '%s', expected %s",
                        Utils.toString(context.getCurrentLocation().currentChar),
                        getExpectedString()
                )
        ));
        return false;
    }

    public String getExpectedString() {
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
