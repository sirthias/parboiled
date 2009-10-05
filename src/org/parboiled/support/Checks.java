package org.parboiled.support;

public class Checks {

    private Checks() {}
    
    public static void ensure(boolean condition, String errorMessageFormat, Object... errorMessageArgs) {
        if (!condition) {
            throw new ParserConstructionException(String.format(errorMessageFormat, errorMessageArgs));
        }
    }

    public static void ensure(boolean condition, String errorMessage) {
        if (!condition) {
            throw new ParserConstructionException(errorMessage);
        }
    }

}
