package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;

import java.util.List;

class SequenceMatcher extends AbstractMatcher implements FollowMatcher {

    private final boolean enforced;

    public SequenceMatcher(@NotNull Rule[] subRules, boolean enforced) {
        super(subRules);
        this.enforced = enforced;
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        List<Matcher> children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Matcher matcher = children.get(i);

            // remember the current index in the context, so we can access it for building the current follower set
            context.setTag(i);

            boolean matched = context.runMatcher(matcher, enforced || (this.enforced && i > 0));
            if (!matched) return false;
        }
        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        return getStarterChars(0);
    }

    private Characters getStarterChars(int startIndex) {
        Characters chars = Characters.ONLY_EMPTY;
        for (int i = startIndex; i < getChildren().size(); i++) {
            Characters matcherStarters = getChildren().get(i).getStarterChars();
            chars = chars.add(matcherStarters);
            if (!matcherStarters.contains(Chars.EMPTY)) return chars.remove(Chars.EMPTY);
        }
        return chars;
    }

    public Characters getFollowerChars(MatcherContext context) {
        int currentIndex = (Integer) context.getTag();
        return getStarterChars(currentIndex + 1);
    }

}
