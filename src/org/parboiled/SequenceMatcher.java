package org.parboiled;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

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

    public boolean collectFirstCharSet(@NotNull Set<Character> firstCharSet) {
        return collectFirstCharSet(0, firstCharSet);
    }

    private boolean collectFirstCharSet(int startIndex, @NotNull Set<Character> firstCharSet) {
        for (int i = startIndex; i < getChildren().size(); i++) {
            if (getChildren().get(i).collectFirstCharSet(firstCharSet)) return true;
        }
        return false;
    }

    public boolean collectCurrentFollowerSet(MatcherContext context, @NotNull Set<Character> followerSet) {
        int currentIndex = (Integer) context.getTag();
        return collectFirstCharSet(currentIndex + 1, followerSet);
    }

}
