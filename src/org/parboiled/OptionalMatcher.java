package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;

class OptionalMatcher extends AbstractMatcher {

    public OptionalMatcher(@NotNull Rule subRule) {
        super(subRule);
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        Matcher matcher = getChildren().get(0);
        context.runMatcher(matcher, false);
        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        return getChildren().get(0).getStarterChars().add(Chars.EMPTY);
    }
}
