package org.parboiled;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;

class FirstOfMatcher extends AbstractMatcher {

    public FirstOfMatcher(@NotNull Rule[] subRules) {
        super(subRules);
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        for (int i = 0; i < getChildren().size(); i++) {
            Matcher matcher = getChildren().get(i);
            Checks.ensure(!matcher.isEnforced(), "Option %s of a FirstOf rule '%s' must not be enforced", i + 1,
                    context.getCurrentPath());
            boolean matched = context.runMatcher(matcher, false);
            if (matched) {
                context.createNode();
                return true;
            }
        }

        if (enforced) {
            context.addUnexpectedInputError(createExpectedString());
            context.createNode();
            return true;
        }
        return false;
    }

    private String createExpectedString() {
        int count = getChildren().size();
        return count == 0 ? "" :
                count == 1 ? getChildren().get(0).toString() :
                        StringUtils.join(ArrayUtils.subarray(getChildren().toArray(), 0, count - 1), ", ") +
                                " or " + getChildren().get(count - 1);
    }

}