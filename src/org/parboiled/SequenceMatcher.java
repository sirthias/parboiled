package org.parboiled;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

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
