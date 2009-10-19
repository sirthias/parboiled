package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.ImmutableList;

class WrapMatcher extends AbstractRule<Matcher> implements Matcher {

    WrapMatcher(@NotNull Rule innerRule) {
        // we cast directly instead of calling toMatcher(), for not triggering the LazyLoader proxy too early
        super(ImmutableList.of((Matcher) innerRule));
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        return label != null ? label : getChildren().get(0).getLabel();
    }

    @Override
    public boolean isEnforced() {
        return super.isEnforced() || getChildren().get(0).isEnforced();
    }

    public Matcher toMatcher() {
        return this;
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        return getChildren().get(0).match(context, enforced || isEnforced());
    }

    @Override
    public String toString() {
        return "wrapper:" + getLabel();
    }

}
