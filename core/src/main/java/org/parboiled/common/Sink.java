package org.parboiled.common;

public interface Sink<T> {
    void receive(T value);
}
