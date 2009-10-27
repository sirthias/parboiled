package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.support.InputLocation;
import org.parboiled.support.Chars;
import org.parboiled.support.Characters;

import java.util.Set;

class ZeroOrMoreMatcher extends AbstractMatcher implements FollowMatcher {

    public ZeroOrMoreMatcher(@NotNull Rule subRule) {
        super(subRule);
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        Matcher matcher = getChildren().get(0);

        InputLocation lastLocation = context.getCurrentLocation();
        while (context.runMatcher(matcher, false)) {
            InputLocation currentLocation = context.getCurrentLocation();
            Checks.ensure(currentLocation.index > lastLocation.index,
                    "The inner rule of ZeroOrMore rule '%s' must not allow empty matches", context.getPath());
            lastLocation = currentLocation;
        }

        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        Characters chars = getChildren().get(0).getStarterChars();
        Checks.ensure(!chars.contains(Chars.EMPTY), "Sub rule of an ZeroOrMore-rule must not allow empty matches");
        return chars;
    }

    public Characters getFollowerChars(MatcherContext context) {
        return getStarterChars().add(Chars.EMPTY);
    }

}