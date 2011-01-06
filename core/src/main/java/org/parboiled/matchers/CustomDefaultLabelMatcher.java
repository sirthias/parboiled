package org.parboiled.matchers;

import org.parboiled.Rule;

abstract class CustomDefaultLabelMatcher<T extends CustomDefaultLabelMatcher<T>> extends AbstractMatcher {
    private String defaultLabel;

    protected CustomDefaultLabelMatcher(Rule subRule, String defaultLabel) {
        super(subRule, null);
        this.defaultLabel = defaultLabel;
    }

    protected CustomDefaultLabelMatcher(Rule[] subRules, String defaultLabel) {
        super(subRules, null);
        this.defaultLabel = defaultLabel;
    }

    @Override
    public String getLabel() {
        return hasCustomLabel() ? super.getLabel() : defaultLabel;
    }

    @Override
    public boolean hasCustomLabel() {
        return super.getLabel() != null;
    }

    @SuppressWarnings( {"unchecked"})
    public T defaultLabel(String defaultLabel) {
        this.defaultLabel = defaultLabel;
        return (T)this;
    }
}
