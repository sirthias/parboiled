package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;

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

        // we run the test matcher in a detached context as it is not to affect the parse tree being built
        MatcherContext tempContext = context.createCopy(null, matcher);
        boolean matched = tempContext.runMatcher(matcher, enforced && !inverted);

        return inverted ? !matched : matched;
    }

    public Characters getStarterChars() {
        Characters characters = getChildren().get(0).getStarterChars();
        return inverted ? Characters.ALL.remove(characters) : characters;
    }

    @Override
    public String getExpectedString() {
        return (inverted ? "not " : "") + getChildren().get(0).getExpectedString();
    }

}