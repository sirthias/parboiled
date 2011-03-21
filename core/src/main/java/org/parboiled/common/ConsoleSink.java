package org.parboiled.common;

public class ConsoleSink implements Sink<String>{
    public void receive(String value) {
        System.out.print(value);
    }
}
