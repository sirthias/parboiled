package org.parboiled;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

class TestMatcher extends AbstractMatcher {

    private final boolean inverted;

    public TestMatcher(@NotNull Rule subRule, boolean inverted) {
        super(subRule);
        this.inverted = inverted;
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        if (label != null) return label;

        return (inverted ? "!(" : "&(") + getChildren().get(0) + ")";
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        Matcher matcher = getChildren().get(0);

        MatcherContext tempContext = new MatcherContextImpl(null, context.getCurrentLocation(), matcher,
                context.getActions(), context.getParseErrors());
        boolean matched = tempContext.runMatcher(matcher, false);

        return inverted ? !matched : matched;
    }

    public boolean collectFirstCharSet(@NotNull Set<Character> firstCharSet) {
        return false;
    }

    @Override
    public String getExpectedString() {
        return (inverted ? "not " : "") + getChildren().get(0).getExpectedString();
    }

}