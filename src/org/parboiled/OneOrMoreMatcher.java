package org.parboiled;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.support.InputLocation;

class OneOrMoreMatcher extends AbstractMatcher {

    public OneOrMoreMatcher(@NotNull Rule subRule) {
        super(subRule);
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        Matcher matcher = getChildren().get(0);
        Checks.ensure(!matcher.isEnforced(), "The inner rule of OneOrMore rule '%s' must not be enforced",
                context.getPath());

        boolean matched = context.runMatcher(matcher, enforced);
        if (!matched) {
            Preconditions.checkState(!enforced);
            return false;
        }

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

}
