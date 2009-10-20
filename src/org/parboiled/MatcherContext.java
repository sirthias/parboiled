package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputLocation;

import java.util.List;

interface MatcherContext extends Context {

    boolean runMatcher(boolean enforced);

    boolean runMatcher(@NotNull Matcher matcher, boolean enforced);

    void addUnexpectedInputError(char illegalChar, @NotNull String expected);

    void addActionError(@NotNull String errorMessage);

    void createNode();

    void setCurrentLocation(InputLocation currentLocation);

    List<Node> getSubNodes();

    Node getNode();

    void advanceInputLocation();

    Object getTag();

    void setTag(Object tag);

    Actions getActions();
}
