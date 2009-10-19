package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.support.ParserConstructionException;

class OptionalMatcher extends AbstractMatcher {

    public OptionalMatcher(@NotNull Rule subRule) {
        super(subRule);
    }

    @Override
    public Rule enforce() {
        throw new ParserConstructionException("Optional rules cannot be enforced");
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        Checks.ensure(!isEnforced(), "Optional rule '%s' must not be enforced", context.getPath());

        Matcher matcher = getChildren().get(0);
        Checks.ensure(!matcher.isEnforced(), "The inner rule of Optional rule '%s' must not be enforced",
                context.getPath());
        context.runMatcher(matcher, false);
        context.createNode();
        return true;
    }

}
