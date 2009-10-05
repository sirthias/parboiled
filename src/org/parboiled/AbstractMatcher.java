package org.parboiled;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang.StringUtils;

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

    @Override
    public String toString() {
        String label = getLabel();
        return label != null ? label : StringUtils.removeEnd(getClass().getSimpleName(), "Matcher");
    }

}

