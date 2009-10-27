package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;
import org.parboiled.support.Checks;
import org.parboiled.support.InputLocation;

class OneOrMoreMatcher extends AbstractMatcher implements FollowMatcher {

    public OneOrMoreMatcher(@NotNull Rule subRule) {
        super(subRule);
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        Matcher matcher = getChildren().get(0);

        boolean matched = context.runMatcher(matcher, enforced);
        if (!matched) return false;

        // collect all further matches as well
        InputLocation lastLocation = context.getCurrentLocation();
        while (context.runMatcher(matcher, false)) {
            InputLocation currentLocation = context.getCurrentLocation();
            Checks.ensure(currentLocation.index > lastLocation.index,
                    "The inner rule of OneOrMore rule '%s' must not allow empty matches", context.getPath());
            lastLocation = currentLocation;
        }

        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        Characters chars = getChildren().get(0).getStarterChars();
        Checks.ensure(!chars.contains(Chars.EMPTY), "Sub rule of an OneOrMore-rule must not allow empty matches");
        return chars;
    }

    public Characters getFollowerChars(MatcherContext context) {
        // since this call is only legal when we are currently within a match of our sub matcher,
        // i.e. the submatcher can either match once more or the repetition can legally terminate which means
        // our follower set addition is incomplete -> add EMPTY
        return getStarterChars().add(Chars.EMPTY);
    }
}
