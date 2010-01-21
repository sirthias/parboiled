/*
 * Copyright (C) 2009 Mathias Doenitz
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
import static org.parboiled.common.Utils.arrayOf;
import org.parboiled.exceptions.ParserRuntimeException;
import org.parboiled.matchers.*;
import org.parboiled.support.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for custom parsers. Defines basic methods for rule and action parameter creation.
 *
 * @param <V> The type of the value field of the parse tree nodes created by this parser.
 */
public abstract class BaseParser<V> extends BaseActions<V> {

    /**
     * Cache of frequently used, bottom level rules. Per default used for character and string matching rules.
     */
    private final Map<Object, Rule> ruleCache = new HashMap<Object, Rule>();

    /**
     * Runs the given parser rule against the given input string. Note that the rule must have been created by
     * a rule creation method of this parser object, which must have been created with
     * {@link Parboiled#createParser(Class, Object[])}.
     *
     * @param rule  the rule
     * @param input the input string
     * @return the ParsingResult for the run
     */
    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> parse(Rule rule, @NotNull String input) {
        Checks.ensure(getClass().getSimpleName().endsWith("$$parboiled"),
                "Illegal parser instance, please use Parboiled.createParser(...) for creating this parser");

        // prepare
        InputBuffer inputBuffer = new InputBuffer(input);
        InputLocation startLocation = new InputLocation(inputBuffer);
        List<ParseError> parseErrors = new ArrayList<ParseError>();
        Matcher<V> matcher = (Matcher<V>) toRule(rule);
        MatcherContext<V> context = new MatcherContext<V>(inputBuffer, startLocation, matcher, parseErrors);

        // run the actual matcher tree
        context.runMatcher(null, true);

        return new ParsingResult<V>(context.getNode(), parseErrors, inputBuffer);
    }

    ////////////////////////////////// RULE CREATION ///////////////////////////////////

    /**
     * Explicitly creates a rule matching the given character. Normally you can just specify the character literal
     * directly in you rule description. However, if you want to not go through {@link #fromCharLiteral(char)},
     * e.g. because you redefined it, you can also use this wrapper.
     *
     * @param c the char to match
     * @return a new rule
     */
    public Rule ch(char c) {
        return new CharMatcher(c);
    }

    /**
     * Explicitly creates a rule matching the given character ignoring the case.
     *
     * @param c the char to match independently of its case
     * @return a new rule
     */
    public Rule charIgnoreCase(char c) {
        return Character.isLetter(c) ? new CharIgnoreCaseMatcher(c) : ch(c);
    }

    /**
     * Creates a rule matching a range of characters from cLow to cHigh (both inclusively).
     *
     * @param cLow  the start char of the range (inclusively)
     * @param cHigh the end char of the range (inclusively)
     * @return a new rule
     */
    public Rule charRange(char cLow, char cHigh) {
        return cLow == cHigh ? ch(cLow) : new CharRangeMatcher(cLow, cHigh);
    }

    /**
     * Explicitly creates a rule matching the given string. Normally you can just specify the string literal
     * directly in you rule description. However, if you want to not go through {@link #fromStringLiteral(String)},
     * e.g. because you redefined it, you can also use this wrapper.
     *
     * @param string the string to match
     * @return a new rule
     */
    public Rule string(@NotNull String string) {
        if (string.length() == 1) return ch(string.charAt(0)); // optimize one-letter strings
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            Rule rule = cached(c);
            matchers[i] = rule != null ? rule : cache(c, ch(c));
        }
        return new SequenceMatcher(matchers, false).label('"' + string + '"');
    }

    /**
     * Explicitly creates a rule matching the given string in a case-independent fashion.
     *
     * @param string the string to match
     * @return a new rule
     */
    public Rule stringIgnoreCase(String string) {
        if (string.length() == 1) return charIgnoreCase(string.charAt(0)); // optimize one-letter strings
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            boolean letter = Character.isLetter(c);
            Object key = letter ? new IgnoreCaseWrapper(c) : c;
            Rule rule = cached(key);
            matchers[i] = rule != null ? rule : cache(key, letter ? charIgnoreCase(c) : ch(c));
        }
        return new SequenceMatcher(matchers, false).label('"' + string + '"');
    }

    /**
     * Creates a new rule that successively tries all of the given subrules and succeeds when the first one of
     * its subrules matches. If all subrules fail this rule fails as well.
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule firstOf(Object rule, Object rule2, Object... moreRules) {
        return new FirstOfMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules)))).label("firstOf");
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule and succeeds if the subrule matches at least once.
     * If the subrule does not match at least once this rule fails.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule oneOrMore(Object rule) {
        return new OneOrMoreMatcher(toRule(rule)).label("oneOrMore");
    }

    /**
     * Creates a new rule that tries a match on its subrule and always succeeds, independently of the matching
     * success of its subrule.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule optional(Object rule) {
        return new OptionalMatcher(toRule(rule)).label("optional");
    }

    /**
     * Creates a new rule that only succeeds if all of its subrule succeed, one after the other.
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule sequence(Object rule, Object rule2, Object... moreRules) {
        return new SequenceMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules))), false).label("sequence");
    }

    /**
     * Creates a new rule that only succeeds if all of its subrules succeed, one after the other.
     * However, after the first subrule has matched all further subrule matches are enforced, i.e. if one of them
     * fails a ParseError will be created (and error recovery will be tried).
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    public Rule enforcedSequence(Object rule, Object rule2, Object... moreRules) {
        return new SequenceMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules))), true).label("enforcedSequence");
    }

    /**
     * Creates a new rule that acts as a syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule succeeds and fails if the
     * subrule rails. Since this rule does not actually consume any input it will never create a parse tree node.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule test(Object rule) {
        return new TestMatcher(toRule(rule), false);
    }

    /**
     * Creates a new rule that acts as an inverse syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule fails and fails if the
     * subrule succeeds. Since this rule does not actually consume any input it will never create a parse tree node.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule testNot(Object rule) {
        return new TestMatcher(toRule(rule), true);
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule.
     * Succeeds always, even if the subrule doesn't match even once.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule zeroOrMore(Object rule) {
        return new ZeroOrMoreMatcher(toRule(rule)).label("zeroOrMore");
    }

    /**
     * Matches the EOI (end of input) character.
     *
     * @return a new rule
     */
    @DontExtend
    public Rule eoi() {
        return toRule(Chars.EOI);
    }

    /**
     * Matches any character except {@link org.parboiled.support.Chars#EOI}.
     *
     * @return a new rule
     */
    @DontExtend
    public Rule any() {
        return toRule(Chars.ANY);
    }

    /**
     * Matches nothing and therefore always succeeds.
     *
     * @return a new rule
     */
    @DontExtend
    public Rule empty() {
        return toRule(Chars.EMPTY);
    }

    ///************************* "MAGIC" METHODS ***************************///

    /**
     * Changes the context scope of all arguments to the current parent scope.
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T UP(T argument) {
        throw new UnsupportedOperationException("UP(...) calls can only be used in rule defining parser methods");
    }

    /**
     * Changes the context scope of all arguments to the current sub scope. This will only work if this call is
     * at some level wrapped with one or more {@link #UP(Object)} calls, since the default scope is always at
     * the bottom of the context chain.
     *
     * @param argument the arguments to change to context for
     * @return the result of the argument
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final <T> T DOWN(T argument) {
        throw new UnsupportedOperationException("UP(...) calls can only be used in rule defining parser methods");
    }

    ///************************* HELPER METHODS ***************************///

    /**
     * Used internally to convert the given character literal to a parser rule.
     * You can override this method, e.g. for specifying a sequence that automatically matches all trailing
     * whitespace after the character.
     *
     * @param c the character
     * @return the rule
     */
    protected Rule fromCharLiteral(char c) {
        return ch(c);
    }

    /**
     * Used internally to convert the given string literal to a parser rule.
     * You can override this method, e.g. for specifying a sequence that automatically matches all trailing
     * whitespace after the string.
     *
     * @param string the string
     * @return the rule
     */
    protected Rule fromStringLiteral(String string) {
        return string(string);
    }

    /**
     * Returns the rule cache content for the given key.
     *
     * @param key the cache key
     * @return the cached rule or null
     */
    protected Rule cached(Object key) {
        return ruleCache.get(key);
    }

    /**
     * Caches the given rule in the rule cache under the given key and return the rule.
     *
     * @param key  the key to store the rule under in the cache
     * @param rule the rule to cache
     * @return the rule
     */
    protected Rule cache(Object key, Rule rule) {
        ruleCache.put(key, rule);
        return rule;
    }

    /**
     * Converts the given object array to an array of rules.
     *
     * @param objects the objects to convert
     * @return the rules corresponding to the given objects
     */
    protected Rule[] toRules(@NotNull Object[] objects) {
        Rule[] rules = new Rule[objects.length];

        // we need to process the sub rule objects in reverse order so as to correctly mix in parameters
        // from the parameter stack
        for (int i = objects.length - 1; i >= 0; i--) {
            rules[i] = toRule(objects[i]);
        }
        return rules;
    }

    /**
     * Converts the given object to a rule.
     *
     * @param obj the object to convert
     * @return the rule corresponding to the given object
     */
    protected Rule toRule(Object obj) {
        if (obj instanceof Rule) return (Rule) obj;

        Rule rule = cached(obj);
        if (rule != null) return rule;

        if (obj instanceof Character) return cache(obj, fromCharLiteral((Character) obj));
        if (obj instanceof String) return cache(obj, fromStringLiteral((String) obj));
        if (obj instanceof Action) return new ActionMatcher((Action) obj);

        rule = fromUserObject(obj);
        if (rule != null) return cache(obj, rule);

        throw new ParserRuntimeException("\'" + obj + "\' is not a valid Rule or parser action");
    }

    /**
     * Attempts to convert the given object into a rule.
     * Override this method in order to be able to use custom objects directly in your rule specification.
     * The method should return null if the given object cannot be converted into a rule (which is all the
     * default implementation does).
     * Note that the results are automatically cached, so the method is never called twice for equal objects.
     *
     * @param obj the object to convert
     * @return the rule for the object or null
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Rule fromUserObject(Object obj) {
        return null;
    }

    /**
     * Wrapper for rule cache keys that are used for explicit "ignore-case matchers"
     */
    protected static class IgnoreCaseWrapper {
        private final Object key;

        public IgnoreCaseWrapper(Object key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof IgnoreCaseWrapper && key.equals(((IgnoreCaseWrapper) o).key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

}
