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

package org.parboiled;

import org.parboiled.annotations.*;
import org.parboiled.common.Utils;
import org.parboiled.errors.GrammarException;
import org.parboiled.matchers.*;
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;
import org.parboiled.support.Checks;

import static org.parboiled.common.Preconditions.*;

/**
 * Base class of all parboiled parsers. Defines the basic rule creation methods.
 *
 * @param <V> the type of the parser values
 */
@SuppressWarnings( {"UnusedDeclaration"})
public abstract class BaseParser<V> extends BaseActions<V> {

    /**
     * Matches the {@link Chars#EOI} (end of input) character.
     */
    public static final Rule EOI = new CharMatcher(Chars.EOI);

    /**
     * Matches the special {@link Chars#INDENT} character produces by the org.parboiled.buffers.IndentDedentInputBuffer
     */
    public static final Rule INDENT = new CharMatcher(Chars.INDENT);

    /**
     * Matches the special {@link Chars#DEDENT} character produces by the org.parboiled.buffers.IndentDedentInputBuffer
     */
    public static final Rule DEDENT = new CharMatcher(Chars.DEDENT);

    /**
     * Matches any character except {@link Chars#EOI}.
     */
    public static final Rule ANY = new AnyMatcher();

    /**
     * Matches nothing and always succeeds.
     */
    public static final Rule EMPTY = new EmptyMatcher();

    /**
     * Matches nothing and always fails.
     */
    public static final Rule NOTHING = new NothingMatcher();

    /**
     * Creates a new instance of this parsers class using the no-arg constructor. If no no-arg constructor
     * exists this method will fail with a java.lang.NoSuchMethodError.
     * Using this method is faster than using {@link Parboiled#createParser(Class, Object...)} for creating
     * new parser instances since this method does not use reflection.
     *
     * @param <P> the parser class
     * @return a new parser instance
     */
    public <P extends BaseParser<V>> P newInstance() {
        throw new UnsupportedOperationException(
                "Illegal parser instance, you have to use Parboiled.createParser(...) to create your parser instance!");
    }

    /**
     * Explicitly creates a rule matching the given character. Normally you can just specify the character literal
     * directly in you rule description. However, if you don't want to go through {@link #fromCharLiteral(char)},
     * e.g. because you redefined it, you can also use this wrapper.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param c the char to match
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule Ch(char c) {
        return new CharMatcher(c);
    }

    /**
     * Explicitly creates a rule matching the given character case-independently.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param c the char to match independently of its case
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule IgnoreCase(char c) {
        if (Character.isLowerCase(c) == Character.isUpperCase(c)) {
            return Ch(c);
        }
        return new CharIgnoreCaseMatcher(c);
    }

    /**
     * Creates a rule matching a range of characters from cLow to cHigh (both inclusively).
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param cLow  the start char of the range (inclusively)
     * @param cHigh the end char of the range (inclusively)
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule CharRange(char cLow, char cHigh) {
        return cLow == cHigh ? Ch(cLow) : new CharRangeMatcher(cLow, cHigh);
    }

    /**
     * Creates a new rule that matches any of the characters in the given string.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters
     * @return a new rule
     */
    @DontLabel
    public Rule AnyOf(String characters) {
        checkArgNotNull(characters, "characters");
        return AnyOf(characters.toCharArray());
    }

    /**
     * Creates a new rule that matches any of the characters in the given char array.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters
     * @return a new rule
     */
    @DontLabel
    public Rule AnyOf(char[] characters) {
        checkArgNotNull(characters, "characters");
        checkArgument(characters.length > 0);
        return characters.length == 1 ? Ch(characters[0]) : AnyOf(Characters.of(characters));
    }

    /**
     * Creates a new rule that matches any of the given characters.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule AnyOf(Characters characters) {
        checkArgNotNull(characters, "characters");
        if (!characters.isSubtractive() && characters.getChars().length == 1) {
            return Ch(characters.getChars()[0]);
        }
        if (characters.equals(Characters.NONE)) return NOTHING;
        return new AnyOfMatcher(characters);
    }

    /**
     * Explicitly creates a rule matching the given string. Normally you can just specify the string literal
     * directly in you rule description. However, if you want to not go through {@link #fromStringLiteral(String)},
     * e.g. because you redefined it, you can also use this wrapper.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param string the String to match
     * @return a new rule
     */
    @DontLabel
    public Rule String(String string) {
        checkArgNotNull(string, "string");
        return String(string.toCharArray());
    }

    /**
     * Explicitly creates a rule matching the given string. Normally you can just specify the string literal
     * directly in you rule description. However, if you want to not go through {@link #fromStringLiteral(String)},
     * e.g. because you redefined it, you can also use this wrapper.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters of the string to match
     * @return a new rule
     */
    @Cached
    @SuppressSubnodes
    @DontLabel
    public Rule String(char... characters) {
        if (characters.length == 1) return Ch(characters[0]); // optimize one-char strings
        Rule[] matchers = new Rule[characters.length];
        for (int i = 0; i < characters.length; i++) {
            matchers[i] = Ch(characters[i]);
        }
        return new StringMatcher(matchers, characters);
    }

    /**
     * Explicitly creates a rule matching the given string in a case-independent fashion.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param string the string to match
     * @return a new rule
     */
    @DontLabel
    public Rule IgnoreCase(String string) {
        checkArgNotNull(string, "string");
        return IgnoreCase(string.toCharArray());
    }

    /**
     * Explicitly creates a rule matching the given string in a case-independent fashion.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param characters the characters of the string to match
     * @return a new rule
     */
    @Cached
    @SuppressSubnodes
    @DontLabel
    public Rule IgnoreCase(char... characters) {
        if (characters.length == 1) return IgnoreCase(characters[0]); // optimize one-char strings
        Rule[] matchers = new Rule[characters.length];
        for (int i = 0; i < characters.length; i++) {
            matchers[i] = IgnoreCase(characters[i]);
        }
        return ((SequenceMatcher) Sequence(matchers)).label('"' + String.valueOf(characters) + '"');
    }

    /**
     * Creates a new rule that successively tries all of the given subrules and succeeds when the first one of
     * its subrules matches. If all subrules fail this rule fails as well.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    @DontLabel
    public Rule FirstOf(Object rule, Object rule2, Object... moreRules) {
        checkArgNotNull(moreRules, "moreRules");
        return FirstOf(Utils.arrayOf(rule, rule2, moreRules));
    }

    /**
     * Creates a new rule that successively tries all of the given subrules and succeeds when the first one of
     * its subrules matches. If all subrules fail this rule fails as well.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rules the subrules
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule FirstOf(Object[] rules) {
        checkArgNotNull(rules, "rules");
        if (rules.length == 1) {
            return toRule(rules[0]);
        }
        Rule[] convertedRules = toRules(rules);
        char[][] chars = new char[rules.length][];
        for (int i = 0, convertedRulesLength = convertedRules.length; i < convertedRulesLength; i++) {
            Object rule = convertedRules[i];
            if (rule instanceof StringMatcher) {
                chars[i] = ((StringMatcher) rule).characters;
            } else {
                return new FirstOfMatcher(convertedRules);
            }
        }
        return new FirstOfStringsMatcher(convertedRules, chars);
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule and succeeds if the subrule matches at least once.
     * If the subrule does not match at least once this rule fails.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule OneOrMore(Object rule) {
        return new OneOrMoreMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that tries repeated matches of a sequence of the given subrules and succeeds if the sequence
     * matches at least once. If the sequence does not match at least once this rule fails.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    @DontLabel
    public Rule OneOrMore(Object rule, Object rule2, Object... moreRules) {
        checkArgNotNull(moreRules, "moreRules");
        return OneOrMore(Sequence(rule, rule2, moreRules));
    }

    /**
     * Creates a new rule that tries a match on its subrule and always succeeds, independently of the matching
     * success of its sub rule.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule Optional(Object rule) {
        return new OptionalMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that tries a match on the sequence of the given subrules and always succeeds, independently
     * of the matching success of its sub sequence.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    @DontLabel
    public Rule Optional(Object rule, Object rule2, Object... moreRules) {
        checkArgNotNull(moreRules, "moreRules");
        return Optional(Sequence(rule, rule2, moreRules));
    }

    /**
     * Creates a new rule that only succeeds if all of its subrule succeed, one after the other.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    @DontLabel
    public Rule Sequence(Object rule, Object rule2, Object... moreRules) {
        checkArgNotNull(moreRules, "moreRules");
        return Sequence(Utils.arrayOf(rule, rule2, moreRules));
    }

    /**
     * Creates a new rule that only succeeds if all of its subrule succeed, one after the other.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rules the sub rules
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule Sequence(Object[] rules) {
        checkArgNotNull(rules, "rules");
        return rules.length == 1 ? toRule(rules[0]) : new SequenceMatcher(toRules(rules));
    }

    /**
     * <p>Creates a new rule that acts as a syntactic predicate, i.e. tests the given sub rule against the current
     * input position without actually matching any characters. Succeeds if the sub rule succeeds and fails if the
     * sub rule rails. Since this rule does not actually consume any input it will never create a parse tree node.</p>
     * <p>Also it carries a {@link SuppressNode} annotation, which means all sub nodes will also never create a parse
     * tree node. This can be important for actions contained in sub rules of this rule that otherwise expect the
     * presence of certain parse tree structures in their context.
     * Also see {@link org.parboiled.annotations.SkipActionsInPredicates}</p>
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @SuppressNode
    @DontLabel
    public Rule Test(Object rule) {
        Rule subMatcher = toRule(rule);
        return new TestMatcher(subMatcher);
    }

    /**
     * <p>Creates a new rule that acts as a syntactic predicate, i.e. tests the sequence of the given sub rule against
     * the current input position without actually matching any characters. Succeeds if the sub sequence succeeds and
     * fails if the sub sequence rails. Since this rule does not actually consume any input it will never create a
     * parse tree node.</p>
     * <p>Also it carries a {@link SuppressNode} annotation, which means all sub nodes will also never create a parse
     * tree node. This can be important for actions contained in sub rules of this rule that otherwise expect the
     * presence of certain parse tree structures in their context.
     * Also see {@link org.parboiled.annotations.SkipActionsInPredicates}</p>
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    @DontLabel
    public Rule Test(Object rule, Object rule2, Object... moreRules) {
        checkArgNotNull(moreRules, "moreRules");
        return Test(Sequence(rule, rule2, moreRules));
    }

    /**
     * <p>Creates a new rule that acts as an inverse syntactic predicate, i.e. tests the given sub rule against the
     * current input position without actually matching any characters. Succeeds if the sub rule fails and fails if the
     * sub rule succeeds. Since this rule does not actually consume any input it will never create a parse tree node.</p>
     * <p>Also it carries a {@link SuppressNode} annotation, which means all sub nodes will also never create a parse
     * tree node. This can be important for actions contained in sub rules of this rule that otherwise expect the
     * presence of certain parse tree structures in their context.
     * Also see {@link org.parboiled.annotations.SkipActionsInPredicates}</p>
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @SuppressNode
    @DontLabel
    public Rule TestNot(Object rule) {
        Rule subMatcher = toRule(rule);
        return new TestNotMatcher(subMatcher);
    }

    /**
     * <p>Creates a new rule that acts as an inverse syntactic predicate, i.e. tests the sequence of the given sub rules
     * against the current input position without actually matching any characters. Succeeds if the sub sequence fails
     * and fails if the sub sequence succeeds. Since this rule does not actually consume any input it will never create
     * a parse tree node.</p>
     * <p>Also it carries a {@link SuppressNode} annotation, which means all sub nodes will also never create a parse
     * tree node. This can be important for actions contained in sub rules of this rule that otherwise expect the
     * presence of certain parse tree structures in their context.
     * Also see {@link org.parboiled.annotations.SkipActionsInPredicates}</p>
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    @DontLabel
    public Rule TestNot(Object rule, Object rule2, Object... moreRules) {
        checkArgNotNull(moreRules, "moreRules");
        return TestNot(Sequence(rule, rule2, moreRules));
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule.
     * Succeeds always, even if the subrule doesn't match even once.
     * <p>Note: This methods carries a {@link Cached} annotation, which means that multiple invocations with the same
     * argument will yield the same rule instance.</p>
     *
     * @param rule the subrule
     * @return a new rule
     */
    @Cached
    @DontLabel
    public Rule ZeroOrMore(Object rule) {
        return new ZeroOrMoreMatcher(toRule(rule));
    }

    /**
     * Creates a new rule that tries repeated matches of the sequence of the given sub rules.
     * Succeeds always, even if the sub sequence doesn't match even once.
     * <p>Note: This methods provides caching, which means that multiple invocations with the same
     * arguments will yield the same rule instance.</p>
     *
     * @param rule      the first subrule
     * @param rule2     the second subrule
     * @param moreRules the other subrules
     * @return a new rule
     */
    @DontLabel
    public Rule ZeroOrMore(Object rule, Object rule2, Object... moreRules) {
        checkArgNotNull(moreRules, "moreRules");
        return ZeroOrMore(Sequence(rule, rule2, moreRules));
    }

    ///************************* "MAGIC" METHODS ***************************///

    /**
     * Explicitly marks the wrapped expression as an action expression.
     * parboiled transforms the wrapped expression into an {@link Action} instance during parser construction.
     *
     * @param expression the expression to turn into an Action
     * @return the Action wrapping the given expression
     */
    public static Action ACTION(boolean expression) {
        throw new UnsupportedOperationException("ACTION(...) calls can only be used in Rule creating parser methods");
    }

    ///************************* HELPER METHODS ***************************///

    /**
     * Used internally to convert the given character literal to a parser rule.
     * You can override this method, e.g. for specifying a Sequence that automatically matches all trailing
     * whitespace after the character.
     *
     * @param c the character
     * @return the rule
     */
    @DontExtend
    protected Rule fromCharLiteral(char c) {
        return Ch(c);
    }

    /**
     * Used internally to convert the given string literal to a parser rule.
     * You can override this method, e.g. for specifying a Sequence that automatically matches all trailing
     * whitespace after the string.
     *
     * @param string the string
     * @return the rule
     */
    @DontExtend
    protected Rule fromStringLiteral(String string) {
        checkArgNotNull(string, "string");
        return fromCharArray(string.toCharArray());
    }

    /**
     * Used internally to convert the given char array to a parser rule.
     * You can override this method, e.g. for specifying a Sequence that automatically matches all trailing
     * whitespace after the characters.
     *
     * @param array the char array
     * @return the rule
     */
    @DontExtend
    protected Rule fromCharArray(char[] array) {
        checkArgNotNull(array, "array");
        return String(array);
    }

    /**
     * Converts the given object array to an array of rules.
     *
     * @param objects the objects to convert
     * @return the rules corresponding to the given objects
     */
    @DontExtend
    public Rule[] toRules(Object... objects) {
        checkArgNotNull(objects, "objects");
        Rule[] rules = new Rule[objects.length];
        for (int i = 0; i < objects.length; i++) {
            rules[i] = toRule(objects[i]);
        }
        return rules;
    }

    /**
     * Converts the given object to a rule.
     * This method can be overriden to enable the use of custom objects directly in rule specifications.
     *
     * @param obj the object to convert
     * @return the rule corresponding to the given object
     */
    @DontExtend
    public Rule toRule(Object obj) {
        if (obj instanceof Rule) return (Rule) obj;
        if (obj instanceof Character) return fromCharLiteral((Character) obj);
        if (obj instanceof String) return fromStringLiteral((String) obj);
        if (obj instanceof char[]) return fromCharArray((char[]) obj);
        if (obj instanceof Action) {
            Action action = (Action) obj;
            return new ActionMatcher(action);
        }
        Checks.ensure(!(obj instanceof Boolean), "Rule specification contains an unwrapped Boolean value, " +
                "if you were trying to specify a parser action wrap the expression with ACTION(...)");

        throw new GrammarException("'" + obj + "' cannot be automatically converted to a parser Rule");
    }

}
