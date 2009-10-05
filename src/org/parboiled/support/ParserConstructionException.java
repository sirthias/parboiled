package org.parboiled.support;

public class ParserConstructionException extends RuntimeException {

    public ParserConstructionException() {
    }

    public ParserConstructionException(String message) {
        super(message);
    }

    public ParserConstructionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParserConstructionException(Throwable cause) {
        super(cause);
    }

}