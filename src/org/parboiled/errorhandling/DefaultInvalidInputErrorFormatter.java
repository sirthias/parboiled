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

import org.parboiled.common.Formatter;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.*;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

public class DefaultInvalidInputErrorFormatter<V> extends DefaultMatcherVisitor<V, Boolean>
        implements Formatter<InvalidInputError<V>> {

    public String format(InvalidInputError<V> error) {
        if (error == null) return "";

        return String.format("Invalid input '%s', expected %s",
                StringUtils.escape(String.valueOf(error.getErrorLocation().currentChar)),
                getExpectedString(error)
        );
    }

    private String getExpectedString(InvalidInputError<V> error) {
        List<String> labelList = new ArrayList<String>();
        for (MatcherPath<V> path : error.getFailedMatchers()) {
            String[] labels = chooseLabelFromPath(path, error);
            if (labels == null) continue;
            for (String label : labels) {
                if (label != null && !labelList.contains(label)) {
                    labelList.add(label);
                }
            }
        }
        return join(labelList);
    }

    private String[] chooseLabelFromPath(MatcherPath<V> path, InvalidInputError<V> error) {
        if (path.equals(error.getLastMatch())) {
            return getLabels(path.getHead());
        }
        int commonPrefixLength = path.getCommonPrefixLength(error.getLastMatch());
        return findFirstProperLabel(path, commonPrefixLength);
    }

    private String[] findFirstProperLabel(MatcherPath<V> path, int startIndex) {
        for (int i = startIndex; i < path.length(); i++) {
            Matcher<V> matcher = path.get(i);
            if (matcher.accept(this)) {
                return getLabels(matcher);
            }
        }
        return null;
    }

    private String[] getLabels(Matcher<V> matcher) {
        if (matcher instanceof CharactersMatcher) {
            CharactersMatcher cMatcher = (CharactersMatcher) matcher;
            if (!cMatcher.characters.isSubtractive()) {
                String[] labels = new String[cMatcher.characters.getChars().length];
                for (int i = 0; i < labels.length; i++) {
                    labels[i] = '\'' + String.valueOf(cMatcher.characters.getChars()[i]) + '\'';
                }
                return labels;
            }
        }
        return new String[] {matcher.getLabel()};
    }

    private String join(List<String> labelList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labelList.size(); i++) {
            if (i > 0) sb.append(i < labelList.size() - 1 ? ", " : " or ");
            sb.append(labelList.get(i));
        }
        return StringUtils.escape(sb.toString());
    }

    // the following MatcherVisitor overrides determine whether the given matcher has a label we want to show
    // in the expected string of the InvalidInputError

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

}
