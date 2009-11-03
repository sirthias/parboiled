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

import net.sf.cglib.proxy.Factory;
import org.jetbrains.annotations.NotNull;
import org.parboiled.actionparameters.*;
import static org.parboiled.actionparameters.ActionParameterUtils.mixInParameter;
import org.parboiled.common.Converter;
import org.parboiled.common.Preconditions;
import org.parboiled.common.Utils;
import static org.parboiled.common.Utils.arrayOf;
import org.parboiled.support.*;
import org.parboiled.matchers.*;

import java.util.*;

/**
 * Base class for custom parsers. Defines basic methods for rule and action parameter creation.
 *
 * @param <V> The type of the value field of the parse tree nodes created by this parser.
 * @param <A> The type of the parser Actions you would like to use in your rules. If you don't need any parser
 * actions (e.g. for very simple examples) you can just use the Actions interface directly.
 */
public abstract class BaseParser<V, A extends Actions<V>> {

    private final Map<Character, Rule> charMatchers = new HashMap<Character, Rule>();
    private final Map<String, Rule> stringMatchers = new HashMap<String, Rule>();
    private final Class<V> nodeValueType; // the type of V, i.e. the value field of the parse tree nodes
    final Stack<ActionParameter> actionParameters = new Stack<ActionParameter>();

    /**
     * The immutable reference to your parser actions.
     */
    public final A actions;

    /**
     * Constructs a new parser object using the given actions.
     *
     * @param actions the parser actions (can be null)
     */
    @SuppressWarnings({"unchecked"})
    public BaseParser(A actions) {
        this.actions = actions;
        List<Class<?>> typeArguments = Utils.getTypeArguments(BaseParser.class, getClass());
        Preconditions.checkState(typeArguments.size() == 2);
        nodeValueType = (Class<V>) typeArguments.get(0);
    }

    /**
     * Runs the given parser rule against the given input string. Note that the rule must have been created by
     * a rule creation method of this parser object, which must have been created with Parser.create(...).
     *
     * @param rule  the rule
     * @param input the input string
     * @return the ParsingResult for the run
     */
    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> parse(@NotNull Rule rule, @NotNull String input) {
        Checks.ensure(rule instanceof StagingRule,
                "Illegal rule instance, please use Parboiled.createActions(...) for creating this parser");
        Checks.ensure(((StagingRule) rule).getParser() == this,
                "Illegal rule instance, it was not created by this parser");

        // prepare
        InputBuffer inputBuffer = new InputBuffer(input);
        InputLocation startLocation = new InputLocation(inputBuffer);
        List<ParseError> parseErrors = new ArrayList<ParseError>();
        Matcher<V> matcher = (Matcher<V>) rule.toMatcher();
        MatcherContext<V> context = new MatcherContext<V>(startLocation, matcher, actions, parseErrors);

        // the matcher tree has already been built during the call to Parboiled.parse(...), usually immediately
        // before the invocation of this method, we need to signal to the ActionInterceptor that rule construction
        // is over and all further action calls should not continue to createActions ActionMatchers but actually be
        // "routed through" to the actual action method implementations
        if (actions != null) {
            ActionInterceptor actionInterceptor = (ActionInterceptor) ((Factory) actions).getCallback(1);
            actionInterceptor.setParser(null);
        }

        // run the actual matcher tree
        context.runMatcher(true);

        return new ParsingResult<V>(context.getNode(), parseErrors, inputBuffer);
    }

    ////////////////////////////////// RULE CREATION ///////////////////////////////////

    /**
     * Explicitly creates a rule matching the given character.
     * Normally you can just specify the character literal directly in you rule description. However, if you want
     * to specify special rule attributes (like a label) you can also use this wrapper.
     *
     * @param c the char to match
     * @return a new rule
     */
    public Rule ch(char c) {
        return new CharMatcher(c);
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
     * Explicitly creates a rule matching the given string.
     * Normally you can just specify the string literal directly in you rule description. However, if you want
     * to specify special rule attributes (like a label) you can also use this wrapper.
     *
     * @param string the string to match
     * @return a new rule
     */
    public Rule string(@NotNull String string) {
        Rule[] matchers = new Rule[string.length()];
        for (int i = 0; i < string.length(); i++) matchers[i] = cachedChar(string.charAt(i));
        return new SequenceMatcher(matchers, false).label(string);
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
    public Rule firstOf(@NotNull Object rule, Object rule2, Object... moreRules) {
        return new FirstOfMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules)))).label("firstOf");
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule and succeeds if the subrule matches at least once.
     * If the subrule does not match at least once this rule fails.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule oneOrMore(@NotNull Object rule) {
        return new OneOrMoreMatcher(toRule(rule)).label("oneOrMore");
    }

    /**
     * Creates a new rule that tries a match on its subrule and always succeeds, independently of the matching
     * success of its subrule.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule optional(@NotNull Object rule) {
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
    public Rule sequence(@NotNull Object rule, Object rule2, Object... moreRules) {
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
    public Rule enforcedSequence(@NotNull Object rule, Object rule2, Object... moreRules) {
        return new SequenceMatcher(toRules(arrayOf(rule, arrayOf(rule2, moreRules))), true).label("enforcedSequence");
    }

    /**
     * Creates a new rule that acts as a syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule succeeds and fails if the
     * subrule rails.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule test(@NotNull Object rule) {
        return new TestMatcher(toRule(rule), false);
    }

    /**
     * Creates a new rule that acts as an inverse syntactic predicate, i.e. tests the given subrule against the current
     * input position without actually matching any characters. Succeeds if the subrule fails and fails if the
     * subrule succeeds.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule testNot(@NotNull Object rule) {
        return new TestMatcher(toRule(rule), true);
    }

    /**
     * Creates a new rule that tries repeated matches of its subrule.
     * Succeeds always, even if the subrule doesn't match even once.
     *
     * @param rule the subrule
     * @return a new rule
     */
    public Rule zeroOrMore(@NotNull Object rule) {
        return new ZeroOrMoreMatcher(toRule(rule)).label("zeroOrMore");
    }

    /**
     * Matches the EOI (end of input) character.
     *
     * @return a new rule
     */
    public Rule eoi() {
        return ch(Chars.EOI);
    }

    /**
     * Matches any character and therefore always succeeds.
     *
     * @return a new rule
     */
    public Rule any() {
        return ch(Chars.ANY);
    }

    /**
     * Matches nothing and therefore always succeeds.
     *
     * @return a new rule
     */
    public Rule empty() {
        return ch(Chars.EMPTY);
    }

    ////////////////////////////////// ACTION PARAMETERS ///////////////////////////////////

    /**
     * Changes the context scope of all arguments to the current parent scope.
     *
     * @param argument the arguments to change to context for
     * @return the result of the arguments
     */
    public <T> T up(T argument) {
        Object arg = mixInParameter(actionParameters, argument);
        actionParameters.add(new UpParameter(arg));
        return null;
    }

    /**
     * Changes the context scope of all arguments to the current sub scope.
     *
     * @param argument the arguments to change to context for
     * @return the result of the arguments
     */
    public <T> T down(T argument) {
        Object arg = mixInParameter(actionParameters, argument);
        actionParameters.add(new DownParameter(arg));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the first Node found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Node<V> node(@NotNull String path) {
        Object arg = mixInParameter(actionParameters, path);
        actionParameters.add(new PathNodeParameter(arg));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to an array of Nodes found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<Node<V>> nodes(@NotNull String path) {
        Object pathArg = mixInParameter(actionParameters, path);
        actionParameters.add(new PathNodesParameter(pathArg));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the last node created during this parsing run.
     *
     * @return the action parameter
     */
    public Node<V> lastNode() {
        actionParameters.add(new LastNodeParameter());
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the tree value of the current rule level, i.e.,
     * If there is an explicitly set value it is returned. Otherwise the last non-null child value, or, if there
     * is no such value, null.
     *
     * @return the action parameter
     */
    public V value() {
        actionParameters.add(new TreeValueParameter<V>(nodeValueType));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the value of the first Node found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public V value(String path) {
        node(path);
        actionParameters.add(new ValueParameter<V>(nodeValueType, actionParameters.pop()));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to an array of node value for the Nodes found with the
     * given prefix path. The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<V> values(String path) {
        nodes(path);
        actionParameters.add(new ValuesParameter<V>(actionParameters.pop()));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the value of the last node created during this parsing run.
     *
     * @return the action parameter
     */
    public V lastValue() {
        lastNode();
        actionParameters.add(new ValueParameter<V>(nodeValueType, actionParameters.pop()));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the matched input text of the first Node found with the
     * given prefix path. The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public String text(String path) {
        node(path);
        actionParameters.add(new TextParameter<V>(actionParameters.pop()));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to an array of input texts matched by the Nodes found with the
     * given prefix path. The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<String> texts(String path) {
        nodes(path);
        actionParameters.add(new TextsParameter<V>(actionParameters.pop()));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the matched input text of the
     * last node created during this parsing run.
     *
     * @return the action parameter
     */
    public String lastText() {
        lastNode();
        actionParameters.add(new TextParameter<V>(actionParameters.pop()));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the first character of the matched input text of the first Node
     * found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Character ch(String path) {
        node(path);
        actionParameters.add(new CharParameter<V>(actionParameters.pop()));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to an array of the first characters of the input texts matched
     * by the Nodes found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<Character> chars(String path) {
        nodes(path);
        actionParameters.add(new CharsParameter<V>(actionParameters.pop()));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the first character of the matched input text of the
     * last node created during this parsing run.
     *
     * @return the action parameter
     */
    public Character lastChar() {
        lastNode();
        actionParameters.add(new CharParameter<V>(actionParameters.pop()));
        return null;
    }

    /**
     * Creates a special action rule that sets the value of the parse tree node to be created for the current rule
     * to the value of the last node created during the current parsing run.
     *
     * @return a new rule
     */
    public ActionResult set() {
        lastValue();
        actionParameters.add(new SetValueParameter<V>(actionParameters.pop(), nodeValueType));
        return null;
    }

    /**
     * Creates a special action rule that sets the value of the parse tree node to be created for the current rule
     * to the given value.
     *
     * @param value the value to set
     * @return a new rule
     */
    public ActionResult set(V value) {
        Object valueArg = mixInParameter(actionParameters, value);
        actionParameters.add(new SetValueParameter<V>(valueArg, nodeValueType));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to null.
     *
     * @return the action parameter
     */
    public Object NULL() {
        actionParameters.add(new NullParameter());
        return null;
    }

    /**
     * Creates an action parameter that converts the given text parameter to an object using the given converter.
     *
     * @param text      the text (parameter) to convert
     * @param converter the converter to use
     * @return the action parameter
     */
    public <T> T convert(String text, @NotNull Converter<T> converter) {
        Object textArg = mixInParameter(actionParameters, text);
        List<Class<?>> convertedTypes = Utils.getTypeArguments(Converter.class, converter.getClass());
        Preconditions.checkArgument(convertedTypes.size() == 1, "Illegal converter");
        actionParameters.add(new ConvertParameter<T>(convertedTypes.get(0), textArg, converter));
        return null;
    }

    /**
     * Creates an action parameter that converts the given text parameter to an Integer.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Integer convertToInteger(String text) {
        return convert(text, new Converter<Integer>() {
            public Integer parse(String string) {
                return Integer.parseInt(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to a Long.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Long convertToLong(String text) {
        return convert(text, new Converter<Long>() {
            public Long parse(String string) {
                return Long.parseLong(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to a Float.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Float convertToFloat(String text) {
        return convert(text, new Converter<Float>() {
            public Float parse(String string) {
                return Float.parseFloat(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to an Double.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Double convertToDouble(String text) {
        return convert(text, new Converter<Double>() {
            public Double parse(String string) {
                return Double.parseDouble(string);
            }
        });
    }

    ///************************* PRIVATE ***************************///

    private Rule cachedChar(char c) {
        Rule matcher = charMatchers.get(c);
        if (matcher == null) {
            matcher = ((AbstractRule) ch(c)).lock();
            charMatchers.put(c, matcher);
        }
        return matcher;
    }

    private Rule cachedString(String string) {
        Rule matcher = stringMatchers.get(string);
        if (matcher == null) {
            matcher = ((AbstractRule) string(string)).lock();
            stringMatchers.put(string, matcher);
        }
        return matcher;
    }

    private Rule[] toRules(@NotNull Object[] objects) {
        Rule[] rules = new Rule[objects.length];
        for (int i = 0; i < objects.length; i++) {
            rules[i] = toRule(objects[i]);
        }
        return rules;
    }

    private Rule toRule(Object obj) {
        obj = mixInParameter(actionParameters, obj);
        if (obj instanceof Rule) {
            return (Rule) obj;
        }
        if (obj instanceof Character) {
            return cachedChar((Character) obj);
        }
        if (obj instanceof String) {
            return cachedString((String) obj);
        }
        if (obj instanceof ActionParameter) {
            return new ActionMatcher((ActionParameter) obj);
        }
        throw new ParserConstructionException("\'" + obj + "\' is not a valid Rule or parser action");
    }

}
