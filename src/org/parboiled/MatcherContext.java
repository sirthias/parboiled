package org.parboiled;

import org.jetbrains.annotations.NotNull;

interface MatcherContext extends Context {

    boolean runMatcher(@NotNull Matcher matcher, boolean enforced);

    void addUnexpectedInputError(@NotNull String expected);

    void addActionError(@NotNull String errorMessage);

    void createNode();

}
