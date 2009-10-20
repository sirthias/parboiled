package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.utils.StringUtils2;
import org.parboiled.utils.Utils;

import java.util.Set;

class FirstOfMatcher extends AbstractMatcher {

    public FirstOfMatcher(@NotNull Rule[] subRules) {
        super(subRules);
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        for (int i = 0; i < getChildren().size(); i++) {
            Matcher matcher = getChildren().get(i);
            boolean matched = context.runMatcher(matcher, false);
            if (matched) {
                context.createNode();
                return true;
            }
        }
        return false;
    }

    public boolean collectFirstCharSet(@NotNull Set<Character> firstCharSet) {
        for (Matcher matcher : getChildren()) {
            Checks.ensure(matcher.collectFirstCharSet(firstCharSet),
                    "Rule '{}' allows empty matches, unlikely to be correct as a sub rule of an OneOf-Rule");
        }
        return true;
    }

    public String getExpectedString() {
        int count = getChildren().size();
        if (count == 0) return "";
        if (count == 1) return getChildren().get(0).toString();
        return StringUtils2.join(Utils.subarray(getChildren().toArray(), 0, count - 1), ", ") +
                " or " + getChildren().get(count - 1);
    }

}