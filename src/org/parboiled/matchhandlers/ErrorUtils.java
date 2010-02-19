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

package org.parboiled.matchhandlers;

import org.jetbrains.annotations.NotNull;
import org.parboiled.matchers.*;
import org.parboiled.support.MatcherPath;

public class ErrorUtils {

    private ErrorUtils() {}

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

}
