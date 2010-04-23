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

package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.*;

import java.util.*;

public class ParserStatistics<V> implements MatcherVisitor<V, ParserStatistics> {

    private final Matcher<V> root;
    private int totalRules;
    private final Set<AnyMatcher<V>> anyMatchers = new HashSet<AnyMatcher<V>>();
    private final Set<CharIgnoreCaseMatcher<V>> charIgnoreCaseMatchers = new HashSet<CharIgnoreCaseMatcher<V>>();
    private final Set<CharMatcher<V>> charMatchers = new HashSet<CharMatcher<V>>();
    private final Set<CustomMatcher<V>> customMatchers = new HashSet<CustomMatcher<V>>();
    private final Set<CharRangeMatcher<V>> charRangeMatchers = new HashSet<CharRangeMatcher<V>>();
    private final Set<CharSetMatcher<V>> charSetMatchers = new HashSet<CharSetMatcher<V>>();
    private final Set<EmptyMatcher<V>> emptyMatchers = new HashSet<EmptyMatcher<V>>();
    private final Set<FirstOfMatcher<V>> firstOfMatchers = new HashSet<FirstOfMatcher<V>>();
    private final Set<OneOrMoreMatcher<V>> oneOrMoreMatchers = new HashSet<OneOrMoreMatcher<V>>();
    private final Set<OptionalMatcher<V>> optionalMatchers = new HashSet<OptionalMatcher<V>>();
    private final Set<SequenceMatcher<V>> sequenceMatchers = new HashSet<SequenceMatcher<V>>();
    private final Set<TestMatcher<V>> testMatchers = new HashSet<TestMatcher<V>>();
    private final Set<TestNotMatcher<V>> testNotMatchers = new HashSet<TestNotMatcher<V>>();
    private final Set<ZeroOrMoreMatcher<V>> zeroOrMoreMatchers = new HashSet<ZeroOrMoreMatcher<V>>();

    private final Set<Action<V>> actions = new HashSet<Action<V>>();
    private final Set<Class<?>> actionClasses = new HashSet<Class<?>>();
    private final Set<ProxyMatcher<V>> proxyMatchers = new HashSet<ProxyMatcher<V>>();
    private final Set<VarFramingMatcher<V>> varFramingMatchers = new HashSet<VarFramingMatcher<V>>();

    @SuppressWarnings({"unchecked"})
    public static <V> ParserStatistics<V> generateFor(@NotNull Rule rule) {
        Matcher<V> matcher = (Matcher<V>) rule;
        return matcher.accept(new ParserStatistics<V>(matcher));
    }

    private ParserStatistics(Matcher<V> root) {
        this.root = root;
        countSpecials(root);
    }

    public Rule getRootRule() {
        return root;
    }

    public int getTotalRules() {
        return totalRules;
    }

    public Set<AnyMatcher<V>> getAnyMatchers() {
        return anyMatchers;
    }

    public Set<CharIgnoreCaseMatcher<V>> getCharIgnoreCaseMatchers() {
        return charIgnoreCaseMatchers;
    }

    public Set<CharMatcher<V>> getCharMatchers() {
        return charMatchers;
    }

    public Set<CustomMatcher<V>> getCustomMatchers() {
        return customMatchers;
    }

    public Set<CharRangeMatcher<V>> getCharRangeMatchers() {
        return charRangeMatchers;
    }

    public Set<CharSetMatcher<V>> getCharSetMatchers() {
        return charSetMatchers;
    }

    public Set<EmptyMatcher<V>> getEmptyMatchers() {
        return emptyMatchers;
    }

    public Set<FirstOfMatcher<V>> getFirstOfMatchers() {
        return firstOfMatchers;
    }

    public Set<OneOrMoreMatcher<V>> getOneOrMoreMatchers() {
        return oneOrMoreMatchers;
    }

    public Set<OptionalMatcher<V>> getOptionalMatchers() {
        return optionalMatchers;
    }

    public Set<SequenceMatcher<V>> getSequenceMatchers() {
        return sequenceMatchers;
    }

    public Set<TestMatcher<V>> getTestMatchers() {
        return testMatchers;
    }

    public Set<TestNotMatcher<V>> getTestNotMatchers() {
        return testNotMatchers;
    }

    public Set<ZeroOrMoreMatcher<V>> getZeroOrMoreMatchers() {
        return zeroOrMoreMatchers;
    }

    public Set<Action<V>> getActions() {
        return actions;
    }

    public Set<Class<?>> getActionClasses() {
        return actionClasses;
    }

    public Set<ProxyMatcher<V>> getProxyMatchers() {
        return proxyMatchers;
    }

    public Set<VarFramingMatcher<V>> getVarFramingMatchers() {
        return varFramingMatchers;
    }

    // MatcherVisitor interface

    public ParserStatistics<V> visit(ActionMatcher<V> matcher) {
        if (!actions.contains(matcher.action)) {
            totalRules++;
            actions.add(matcher.action);
            actionClasses.add(matcher.action.getClass());
        }
        return this;
    }

    public ParserStatistics<V> visit(AnyMatcher<V> matcher) {
        return visit(matcher, anyMatchers);
    }

    public ParserStatistics<V> visit(CharIgnoreCaseMatcher<V> matcher) {
        return visit(matcher, charIgnoreCaseMatchers);
    }

    public ParserStatistics<V> visit(CharMatcher<V> matcher) {
        return visit(matcher, charMatchers);
    }

    public ParserStatistics<V> visit(CustomMatcher<V> matcher) {
        return visit(matcher, customMatchers);
    }

    public ParserStatistics<V> visit(CharRangeMatcher<V> matcher) {
        return visit(matcher, charRangeMatchers);
    }

    public ParserStatistics<V> visit(CharSetMatcher<V> matcher) {
        return visit(matcher, charSetMatchers);
    }

    public ParserStatistics<V> visit(EmptyMatcher<V> matcher) {
        return visit(matcher, emptyMatchers);
    }

    public ParserStatistics<V> visit(FirstOfMatcher<V> matcher) {
        return visit(matcher, firstOfMatchers);
    }

    public ParserStatistics<V> visit(OneOrMoreMatcher<V> matcher) {
        return visit(matcher, oneOrMoreMatchers);
    }

    public ParserStatistics<V> visit(OptionalMatcher<V> matcher) {
        return visit(matcher, optionalMatchers);
    }

    public ParserStatistics<V> visit(SequenceMatcher<V> matcher) {
        return visit(matcher, sequenceMatchers);
    }

    public ParserStatistics<V> visit(TestMatcher<V> matcher) {
        return visit(matcher, testMatchers);
    }

    public ParserStatistics<V> visit(TestNotMatcher<V> matcher) {
        return visit(matcher, testNotMatchers);
    }

    public ParserStatistics<V> visit(ZeroOrMoreMatcher<V> matcher) {
        return visit(matcher, zeroOrMoreMatchers);
    }

    private <M extends Matcher<V>> ParserStatistics<V> visit(M matcher, Set<M> set) {
        if (!set.contains(matcher)) {
            totalRules++;
            set.add(matcher);
            for (Matcher<V> child : matcher.getChildren()) {
                countSpecials(child);
                child.accept(this);
            }
        }
        return this;
    }

    private void countSpecials(Matcher<V> matcher) {
        if (matcher instanceof ProxyMatcher) {
            proxyMatchers.add((ProxyMatcher<V>) matcher);
        } else if (matcher instanceof VarFramingMatcher) {
            varFramingMatchers.add((VarFramingMatcher<V>) matcher);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("Parser statistics for rule '").append(root).append("':\n")
                .append("    Total rules       : ").append(totalRules).append('\n')
                .append("        Actions       : ").append(actions.size()).append('\n')
                .append("        Any           : ").append(anyMatchers.size()).append('\n')
                .append("        CharIgnoreCase: ").append(charIgnoreCaseMatchers.size()).append('\n')
                .append("        Char          : ").append(charMatchers.size()).append('\n')
                .append("        Custom        : ").append(customMatchers.size()).append('\n')
                .append("        CharRange     : ").append(charRangeMatchers.size()).append('\n')
                .append("        CharSet       : ").append(charSetMatchers.size()).append('\n')
                .append("        Empty         : ").append(emptyMatchers.size()).append('\n')
                .append("        FirstOf       : ").append(firstOfMatchers.size()).append('\n')
                .append("        OneOrMore     : ").append(oneOrMoreMatchers.size()).append('\n')
                .append("        Optional      : ").append(optionalMatchers.size()).append('\n')
                .append("        Sequence      : ").append(sequenceMatchers.size()).append('\n')
                .append("        Test          : ").append(testMatchers.size()).append('\n')
                .append("        TestNot       : ").append(testNotMatchers.size()).append('\n')
                .append("        ZeroOrMore    : ").append(zeroOrMoreMatchers.size()).append('\n')
                .append('\n')
                .append("    Action Classes    : ").append(actionClasses.size()).append('\n')
                .append("    ProxyMatchers     : ").append(proxyMatchers.size()).append('\n')
                .append("    VarFramingMatchers: ").append(varFramingMatchers.size()).append('\n')
                .toString();
    }

    public String printActionClassInstances() {
        StringBuilder sb = new StringBuilder("Action classes and their instances for rule '")
                .append(root).append("':\n");

        for (String line : printActionClassLines()) {
            sb.append("    ").append(line).append('\n');
        }
        return sb.toString();
    }

    private List<String> printActionClassLines() {
        List<String> lines = new ArrayList<String>();
        int anonymous = 0;
        for (Class<?> actionClass : actionClasses) {
            String name = actionClass.getSimpleName();
            if (StringUtils.isEmpty(name)) {
                anonymous++;
            } else {
                lines.add(name + " : " + StringUtils.join(printActionClassInstances(actionClass), ", "));
            }
        }
        Collections.sort(lines);
        if (anonymous > 0) lines.add("and " + anonymous + " anonymous instance(s)");
        return lines;
    }

    private List<String> printActionClassInstances(Class<?> actionClass) {
        List<String> actionNames = new ArrayList<String>();
        for (Action<V> action : actions) {
            if (action.getClass().equals(actionClass)) {
                actionNames.add(action.toString());
            }
        }
        Collections.sort(actionNames);
        return actionNames;
    }

}
