package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.ImmutableList;

import java.util.Set;

class WrapMatcher extends AbstractRule<Matcher> implements Matcher {

    WrapMatcher(@NotNull Rule innerRule) {
        // we cast directly instead of calling toMatcher(), for not triggering the LazyLoader proxy too early
        super(ImmutableList.of((Matcher) innerRule));
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        return label != null ? label : getInner().getLabel();
    }

    public Matcher toMatcher() {
        return this;
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        return getInner().match(context, enforced);
    }

    @Override
    public String toString() {
        return "wrapper:" + getLabel();
    }

    public boolean collectFirstCharSet(@NotNull Set<Character> firstCharSet) {
        return getInner().collectFirstCharSet(firstCharSet);
    }

    public String getExpectedString() {
        return getInner().getExpectedString();
    }

    private Matcher getInner() {
        return getChildren().get(0);
    }

}
