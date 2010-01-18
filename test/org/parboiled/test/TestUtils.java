/*
 * Copyright (C) 2009 Mathias Doenitz
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

package org.parboiled.test;

import org.testng.Assert;

import java.io.*;
import java.util.zip.CRC32;

public class TestUtils {

    private TestUtils() {}

    public static void assertEqualsMultiline(String actual, String expected) {
        Assert.assertEquals(
                actual.replace("\r\n", "\n"),
                expected.replace("\r\n", "\n")
        );
    }

    public static long computeCRC(String text) throws Exception {
        CRC32 crc32 = new CRC32();
        byte[] buf = text.getBytes("UTF8");
        crc32.update(buf);
        return crc32.getValue();
    }

    public static void writeToFile(String string, File file) {
        try {
            FileWriter out = new FileWriter(file);
            out.write(string);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyAll(InputStream in, OutputStream out) {
        try {
            byte[] data = new byte[1024]; // copy in chunks of 1K
            int count;
            while ((count = in.read(data)) >= 0) {
                out.write(data, 0, count);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

