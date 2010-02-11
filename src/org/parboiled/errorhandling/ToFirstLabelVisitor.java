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

import org.parboiled.matchers.*;

import java.util.List;

public class ToFirstLabelVisitor<V> extends DefaultMatcherVisitor<V, Void> {

    private final CanMatchEmptyVisitor<V> canMatchEmptyVisitor = new CanMatchEmptyVisitor<V>();
    private final List<String> stringList;

    public ToFirstLabelVisitor(List<String> stringList) {
        this.stringList = stringList;
    }

    @Override
    public Void visit(ActionMatcher<V> matcher) {
        // don't create a label string for action matchers
        return null;
    }

    @Override
    public Void visit(FirstOfMatcher<V> matcher) {
        String label = matcher.getLabel();
        if (!"firstOf".equals(label) && !label.startsWith("{") && !label.endsWith("}")) return add(matcher);

        for (Matcher<V> child : matcher.getChildren()) {
            child.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(OneOrMoreMatcher<V> matcher) {
        if (!"oneOrMore".equals(matcher.getLabel())) return add(matcher);
        return matcher.subMatcher.accept(this);
    }

    @Override
    public Void visit(OptionalMatcher<V> matcher) {
        if (!"optional".equals(matcher.getLabel())) return add(matcher);
        return matcher.subMatcher.accept(this);
    }

    @Override
    public Void visit(SequenceMatcher<V> matcher) {
        if (!"sequence".equals(matcher.getLabel()) && !"enforcedSequence".equals(matcher.getLabel())) {
            return add(matcher);
        }
        for (Matcher<V> child : matcher.getChildren()) {
            child.accept(this);
            if (!child.accept(canMatchEmptyVisitor)) break;
        }
        return null;
    }

    @Override
    public Void visit(TestMatcher<V> matcher) {
        if (!"test".equals(matcher.getLabel())) return add(matcher);
        return matcher.subMatcher.accept(this);
    }

    @Override
    public Void visit(TestNotMatcher<V> matcher) {
        if (!"testNot".equals(matcher.getLabel())) return add(matcher);        
        int oldLen = stringList.size();
        matcher.subMatcher.accept(this);
        for (int i = oldLen; i < stringList.size(); i++) {
            String string = stringList.get(i);
            if (!string.startsWith("no ")) stringList.set(i, "no " + string);
        }
        return null;
    }

    @Override
    public Void visit(ZeroOrMoreMatcher<V> matcher) {
        if (!"zeroOrMore".equals(matcher.getLabel())) return add(matcher);
        return matcher.subMatcher.accept(this);
    }

    @Override
    public Void defaultValue(AbstractMatcher<V> matcher) {
        return add(matcher);
    }

    private Void add(Matcher<V> matcher) {
        stringList.add(matcher.getLabel());
        return null;
    }

}