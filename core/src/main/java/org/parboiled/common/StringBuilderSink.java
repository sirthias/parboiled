package org.parboiled.common;

public class StringBuilderSink implements Sink<String>{
    public final StringBuilder builder = new StringBuilder();
    
    public void receive(String value) {
        builder.append(value);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
