package org.parboiled;

import org.testng.Assert;

public class TestUtils {

    private TestUtils() {}

    /**
     * assertEquals(...), newline type ("\r\n" or "\n") agnostic.
     *
     * @param actual   the actual string
     * @param expected the expected string
     */
    public static void assertEqualsMultiline(String actual, String expected) {
        Assert.assertEquals(
                actual.replace("\r\n", "\n"),
                expected.replace("\r\n", "\n")
        );
    }

}

