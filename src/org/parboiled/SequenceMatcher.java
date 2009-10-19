package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.Preconditions;

class SequenceMatcher extends AbstractMatcher {

    public SequenceMatcher(@NotNull Rule[] subRules) {
        super(subRules);
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        for (Matcher matcher : getChildren()) {
            boolean matched = context.runMatcher(matcher, enforced || matcher.isEnforced());
            if (!matched) {
                Preconditions.checkState(!enforced);
                return false;
            }
        }
        context.createNode();
        return true;
    }

}
