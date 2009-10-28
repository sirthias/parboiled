package org.parboiled.support;

/**
 * A simple From-String-Converter that can parse a simple object from a string.
 *
 * @param <T>
 */
public interface Converter<T> {

    /**
     * Parses the given string into an object of type T.
     *
     * @param string the string to parse
     * @return the parse object
     */
    T parse(String string);

}
