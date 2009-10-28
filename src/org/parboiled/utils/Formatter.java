package org.parboiled.utils;

/**
 * The capability to transform an object of type T into a string representation.
 * @param <T>
 */
public interface Formatter<T> {

    /**
     * Create a string representation for the given obejct.
     * @param object the object to format
     * @return a string describing the object
     */
    String format(T object);

}
