package org.parboiled;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

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

    public boolean collectFirstCharSet(@NotNull Set<Character> firstCharSet) {
        getChildren().get(0).collectFirstCharSet(firstCharSet);
        return false;
    }
}
