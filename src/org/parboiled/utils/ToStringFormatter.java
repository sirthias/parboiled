package org.parboiled.utils;

/**
 * A simple Formatter falling back to the objects toString() method.
 * @param <T>
 */
public class ToStringFormatter<T> implements Formatter<T> {

    public String format(T obj) {
        return obj != null ? obj.toString() : "null";
    }

}
