/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.common;

import org.parboiled.Node;
import static org.parboiled.common.Utils.getTypeArguments;
import org.parboiled.support.NodeFormatter;

import static org.parboiled.common.Utils.humanize;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class UtilsTest {

    @SuppressWarnings({"unchecked", "InstantiatingObjectToGetClassObject"})
    @Test
    public void testGetTypeArguments() {
        assertEquals(getTypeArguments(ArrayList.class, new ArrayList<String>() {
        }.getClass()), Arrays.asList(String.class));

        assertEquals(getTypeArguments(Formatter.class, new Formatter<Integer>() {
            public String format(Integer object) {
                return null;
            }
        }.getClass()), Arrays.asList(Integer.class));

        assertEquals(getTypeArguments(Formatter.class, NodeFormatter.class), Arrays.asList(Node.class));
    }

    @Test
    public void testHumanize() {
        assertEquals(humanize(0), "0 ");
        assertEquals(humanize(1), "1 ");
        assertEquals(humanize(12), "12 ");
        assertEquals(humanize(123), "123 ");
        assertEquals(humanize(1234), "1.234K");
        assertEquals(humanize(1200), "1.2K");
        assertEquals(humanize(12345), "12.35K");
        assertEquals(humanize(123456), "123.5K");
        assertEquals(humanize(1234567), "1.235M");
        assertEquals(humanize(12345678), "12.35M");
        assertEquals(humanize(123456789), "123.5M");
        assertEquals(humanize(1234567890), "1.235G");
        assertEquals(humanize(12345678901L), "12.35G");
        assertEquals(humanize(123456789012L), "123.5G");
        assertEquals(humanize(1234567890123L), "1.235T");
        assertEquals(humanize(12345678901234L), "12.35T");
        assertEquals(humanize(123456789012345L), "123.5T");
        assertEquals(humanize(1234567890123456L), "1.235P");
        assertEquals(humanize(12345678901234567L), "12.35P");
        assertEquals(humanize(123456789012345678L), "123.5P");
        assertEquals(humanize(1234567890123456789L), "1.235E");
    }

}
