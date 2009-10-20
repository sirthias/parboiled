package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.ImmutableList;
import org.parboiled.utils.Preconditions;

abstract class AbstractMatcher extends AbstractRule<Matcher> implements Matcher {

    protected AbstractMatcher() {
        super(ImmutableList.<Matcher>of());
    }

    protected AbstractMatcher(@NotNull Rule subRule) {
        super(ImmutableList.of(subRule.toMatcher()));
    }

    protected AbstractMatcher(@NotNull Rule[] subRules) {
        super(ImmutableList.of(toMatchers(subRules)));
    }

    private static Matcher[] toMatchers(@NotNull Rule[] subRules) {
        Preconditions.checkArgument(subRules.length > 0);
        Matcher[] matchers = new Matcher[subRules.length];
        for (int i = 0; i < subRules.length; i++) {
            matchers[i] = subRules[i].toMatcher();
        }
        return matchers;
    }

    // implementation of toMatcher() from the Rule interface
    public Matcher toMatcher() {
        return this;
    }

    public String getExpectedString() {
        return getLabel(); // default implementation
    }

    @Override
    public String toString() {
        String label = getLabel();
        if (label != null) return label;
        label = getClass().getSimpleName();
        return label.substring(0, label.length() - 7); // remove the "Matcher" ending
    }

}

