package org.parboiled;

import org.apache.commons.lang.StringUtils;
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
                StringUtils.replace(actual, "\r\n", "\n"),
                StringUtils.replace(expected, "\r\n", "\n")
        );
    }

}

