package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.support.Checks;
import org.parboiled.support.Chars;
import org.parboiled.utils.StringUtils2;
import org.parboiled.utils.Utils;

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

    public Characters getStarterChars() {
        Characters chars = Characters.NONE;
        for (Matcher matcher : getChildren()) {
            chars = chars.add(matcher.getStarterChars());
            Checks.ensure(!chars.contains(Chars.EMPTY),
                    "Rule '{}' allows empty matches, unlikely to be correct as a sub rule of an FirstOf-Rule");
        }
        return chars;
    }

    public String getExpectedString() {
        int count = getChildren().size();
        if (count == 0) return "";
        if (count == 1) return getChildren().get(0).toString();
        return StringUtils2.join(Utils.subarray(getChildren().toArray(), 0, count - 1), ", ") +
                " or " + getChildren().get(count - 1);
    }

}