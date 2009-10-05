package org.parboiled.utils;

public class ToStringFormatter<T> implements Formatter<T> {

    public String format(T obj) {
        return obj != null ? obj.toString() : "null";
    }

}
