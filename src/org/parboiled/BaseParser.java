package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Chars;
import org.parboiled.support.Checks;
import org.parboiled.support.Converter;
import org.parboiled.support.ParserConstructionException;
import static org.parboiled.utils.Utils.arrayOf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Base class for custom classes defining parser rules. Derivation of BaseParser is not technically required,
 * i.e. any other base class would also work. However, this class defines a lot of very handy method for basic
 * rule creation, which you will probably want to use.
 *
 * @param <T> The type of the parser Actions you would like to use in your rules. If you don't need any parser
 * actions (e.g. for very simple examples) you can just use the Actions interface directly.
 */
public abstract class BaseParser<T extends Actions> {

    /**
     * Special object to be used for null arguments to action methods.
     */
    public static final Object NULL = new Object();

    private final Map<Character, Rule> charMatchers = new HashMap<Character, Rule>();
    private final Map<String, Rule> stringMatchers = new HashMap<String, Rule>();
    private final LinkedList<ActionParameter> actionParameterStack = new LinkedList<ActionParameter>();

    /**
     * The immutable reference to your parser actions.
     */
    public final T actions;

    /**
     * Constructs a new parser rules object using the given actions.
     *
     * @param actions the parser actions (can be null, if not required)
     */
    public BaseParser(T actions) {
        this.actions = actions;
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
     * Creates an action parameter that evaluates to the first Node found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Node node(@NotNull String path) {
        actionParameterStack.add(new ActionParameter.Node(path));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to an array of Nodes found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Node[] nodes(@NotNull String path) {
        actionParameterStack.add(new ActionParameter.Nodes(path));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the first Node found matching the given label prefix.
     * Caution: Since this only identifies nodes by their label and not by their paths nodes from arbitrary tree
     * depths might be returned, which can be unindended, especially in recursive rules.
     *
     * @param labelPrefix the label prefix to be searched for
     * @return the action parameter
     */
    public Node nodeWithLabel(@NotNull String labelPrefix) {
        actionParameterStack.add(new ActionParameter.NodeWithLabel(labelPrefix));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to an array of Nodes matching the given label prefix.
     * Caution: Since this only identifies nodes by their label and not by their paths nodes from arbitrary tree
     * depths might be returned, which can be unindended, especially in recursive rules.
     *
     * @param labelPrefix the label prefix to be searched for
     * @return the action parameter
     */
    public Node[] nodesWithLabel(@NotNull String labelPrefix) {
        actionParameterStack.add(new ActionParameter.NodesWithLabel(labelPrefix));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the value of the first Node found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Object value(String path) {
        return value(path, null);
    }

    /**
     * Creates an action parameter that evaluates to the value of the first Node found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @param type the type of the node value, this parameter is not actually used but rather helps the compiler
     * infer the correct type parameter for the method return type.
     * @return the action parameter
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public <T> T value(String path, Class<T> type) {
        actionParameterStack.add(new ActionParameter.Value(path));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to an array of node value for the Nodes found with the
     * given prefix path. The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Object[] values(String path) {
        return values(path, null);
    }

    /**
     * Creates an action parameter that evaluates to an array of node value for the Nodes found with the given prefix path.
     * The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @param type the type of the node values, this parameter is not actually used but rather helps the compiler
     * infer the correct type parameter for the method return type.
     * @return the action parameter
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public <T> T[] values(String path, Class<T> type) {
        actionParameterStack.add(new ActionParameter.Values(path));
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
        actionParameterStack.add(new ActionParameter.Text(path));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to an array of input texts matched by the Nodes found with the
     * given prefix path. The path is a '/' separated list of node label prefixes, relative to the current rule.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public String[] texts(String path) {
        actionParameterStack.add(new ActionParameter.Texts(path));
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
        actionParameterStack.add(new ActionParameter.Char(path));
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
    public Character[] chars(String path) {
        actionParameterStack.add(new ActionParameter.Chars(path));
        return null;
    }

    /**
     * Creates an action parameter that evaluates to the first non-null action parameter passed as parameters.
     *
     * @param value the first parameter
     * @param value2 the second parameter
     * @param moreValues more parameters
     * @return the first non-null action parameter
     */
    @SuppressWarnings({"unchecked"})
    public <T> T firstNonNull(T value, T value2, T... moreValues) {
        T[] args = arrayOf(value, arrayOf(value2, moreValues));
        for (int i = args.length - 1; i >= 0; i--) {
            T arg = args[i];
            if (arg == null) {
                arg = (T) actionParameterStack.removeLast();
                Checks.ensure(arg != null, "Illegal argument list for firstNonNull(): null values are not allowed!");
            }
            args[i] = arg;
        }
        actionParameterStack.add(new ActionParameter.FirstOfNonNull(args));
        return null;
    }

    /**
     * Creates an action parameter that converts the given text parameter to an object using the given converter.
     * @param text the text (parameter) to convert
     * @param converter the converter to use
     * @return the action parameter
     */
    public <T> T convert(String text, @NotNull Converter<T> converter) {
        Object arg = text;
        if (text == null) {
            arg = actionParameterStack.removeLast();
            Checks.ensure(arg != null, "Illegal argument list for convert(): null values are not allowed!");
        }
        actionParameterStack.add(new ActionParameter.Convert<T>(arg, converter));
        return null;
    }

    /**
     * Creates an action parameter that converts the given text parameter to an Integer.
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

    ///************************* PACKAGE ***************************///

    ActionParameter[] retrieveAndClearActionParameters() {
        ActionParameter[] params = actionParameterStack.toArray(new ActionParameter[actionParameterStack.size()]);
        actionParameterStack.clear();
        return params;
    }

    ///************************* PRIVATE ***************************///

    private Rule cachedChar(char c) {
        Rule matcher = charMatchers.get(c);
        if (matcher == null) {
            matcher = ((AbstractMatcher) ch(c)).lock();
            charMatchers.put(c, matcher);
        }
        return matcher;
    }

    private Rule cachedString(String string) {
        Rule matcher = stringMatchers.get(string);
        if (matcher == null) {
            matcher = ((AbstractMatcher) string(string)).lock();
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
        if (obj instanceof Rule) {
            return (Rule) obj;
        }
        if (obj instanceof Character) {
            return cachedChar((Character) obj);
        }
        if (obj instanceof String) {
            return cachedString((String) obj);
        }
        throw new ParserConstructionException("\'" + obj + "\' is not a valid Rule");
    }

}
