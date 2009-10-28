package org.parboiled;

/**
 * Represents a parsing rule.
 */
public interface Rule {

    /**
     * Attaches a label to this Rule.
     *
     * @param label the label
     * @return this Rule
     */
    Rule label(String label);

    /**
     * Create a Matcher for this rule.
     *
     * @return the Matcher for this rule
     */
    Matcher toMatcher();

}
