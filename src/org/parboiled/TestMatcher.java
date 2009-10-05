package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParserConstructionException;

class TestMatcher extends AbstractMatcher {

    private final boolean inverted;

    public TestMatcher(@NotNull Rule subRule) {
        this(subRule, false);
    }

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

    @Override
    public Rule enforce() {
        throw new ParserConstructionException("Test rules cannot be explicitly enforced (they implicitly are)");
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        Checks.ensure(!isEnforced(), "Test rule '%s' must not be explictly enforced (it implicitly is)",
                context.getCurrentPath());

        Matcher matcher = getChildren().get(0);
        Checks.ensure(!matcher.isEnforced(), "The inner rule of test rule '%s' must not be enforced",
                context.getCurrentPath());

        InputBuffer input = context.getInputBuffer();
        InputLocation preTestInputLocation = input.getCurrentLocation();

        boolean matched = context.runMatcher(matcher, enforced && !inverted);
        
        input.rewind(preTestInputLocation);
        if (matched && enforced && inverted) {
            context.addUnexpectedInputError("no match of '" + matcher + '\'');
        }
        return inverted ? !matched : matched;
    }

}