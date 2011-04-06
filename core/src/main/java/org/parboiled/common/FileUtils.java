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
import static org.parboiled.common.Preconditions.*;

import java.io.*;
import java.nio.charset.Charset;

public final class FileUtils {

    private FileUtils() {}

    public static String readAllTextFromResource(String resource) {
        checkArgNotNull(resource, "resource");
        return readAllText(FileUtils.class.getClassLoader().getResourceAsStream(resource));
    }

    public static String readAllTextFromResource(String resource, Charset charset) {
        checkArgNotNull(resource, "resource");
        checkArgNotNull(charset, "charset");
        return readAllText(FileUtils.class.getClassLoader().getResourceAsStream(resource), charset);
    }

    public static String readAllText(String filename) {
        checkArgNotNull(filename, "filename");
        return readAllText(new File(filename));
    }

    public static String readAllText(String filename, Charset charset) {
        checkArgNotNull(filename, "filename");
        checkArgNotNull(charset, "charset");
        return readAllText(new File(filename), charset);
    }

    public static String readAllText(File file) {
        checkArgNotNull(file, "file");
        return readAllText(file, Charset.forName("UTF8"));
    }

    public static String readAllText(File file, Charset charset) {
        checkArgNotNull(file, "file");
        checkArgNotNull(charset, "charset");
        try {
            return readAllText(new FileInputStream(file), charset);
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String readAllText(InputStream stream) {
        return readAllText(stream, Charset.forName("UTF8"));
    }

    public static String readAllText(InputStream stream, Charset charset) {
        checkArgNotNull(charset, "charset");
        if (stream == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
        StringWriter writer = new StringWriter();
        copyAll(reader, writer);
        return writer.toString();
    }
    
    public static char[] readAllCharsFromResource(String resource) {
        checkArgNotNull(resource, "resource");
        return readAllChars(FileUtils.class.getClassLoader().getResourceAsStream(resource));
    }

    public static char[] readAllCharsFromResource(String resource, Charset charset) {
        checkArgNotNull(resource, "resource");
        checkArgNotNull(charset, "charset");
        return readAllChars(FileUtils.class.getClassLoader().getResourceAsStream(resource), charset);
    }

    public static char[] readAllChars(String filename) {
        checkArgNotNull(filename, "filename");
        return readAllChars(new File(filename));
    }

    public static char[] readAllChars(String filename, Charset charset) {
        checkArgNotNull(filename, "filename");
        checkArgNotNull(charset, "charset");
        return readAllChars(new File(filename), charset);
    }

    public static char[] readAllChars(File file) {
        checkArgNotNull(file, "file");
        return readAllChars(file, Charset.forName("UTF8"));
    }

    public static char[] readAllChars(File file, Charset charset) {
        checkArgNotNull(file, "file");
        checkArgNotNull(charset, "charset");
        try {
            return readAllChars(new FileInputStream(file), charset);
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }

    public static char[] readAllChars(InputStream stream) {
        return readAllChars(stream, Charset.forName("UTF8"));
    }

    public static char[] readAllChars(InputStream stream, Charset charset) {
        checkArgNotNull(charset, "charset");
        if (stream == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
        CharArrayWriter writer = new CharArrayWriter();
        copyAll(reader, writer);
        return writer.toCharArray();
    }

    public static byte[] readAllBytesFromResource(String resource) {
        checkArgNotNull(resource, "resource");
        return readAllBytes(FileUtils.class.getClassLoader().getResourceAsStream(resource));
    }

    public static byte[] readAllBytes(String filename) {
        checkArgNotNull(filename, "filename");
        return readAllBytes(new File(filename));
    }

    public static byte[] readAllBytes(File file) {
        checkArgNotNull(file, "file");
        try {
            return readAllBytes(new FileInputStream(file));
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }

    public static byte[] readAllBytes(InputStream stream) {
        if (stream == null) return null;
        BufferedInputStream in = new BufferedInputStream(stream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyAll(in, out);
        return out.toByteArray();
    }

    public static void writeAllText(String text, String filename) {
        checkArgNotNull(filename, "filename");
        writeAllText(text, new File(filename));
    }

    public static void writeAllText(String text, String filename, Charset charset) {
        checkArgNotNull(filename, "filename");
        checkArgNotNull(charset, "charset");
        writeAllText(text, new File(filename), charset);
    }

    public static void writeAllText(String text, File file) {
        checkArgNotNull(file, "file");
        writeAllText(text, file, Charset.forName("UTF8"));
    }

    public static void writeAllText(String text, File file, Charset charset) {
        checkArgNotNull(file, "file");
        checkArgNotNull(charset, "charset");
        try {
            ensureParentDir(file);
            writeAllText(text, new FileOutputStream(file), charset);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeAllText(String text, OutputStream stream) {
        checkArgNotNull(stream, "stream");
        writeAllText(text, stream, Charset.forName("UTF8"));
    }

    public static void writeAllText(String text, OutputStream stream, Charset charset) {
        checkArgNotNull(stream, "stream");
        checkArgNotNull(charset, "charset");
        StringReader reader = new StringReader(text != null ? text : "");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, charset));
        copyAll(reader, writer);
    }
    
    public static void writeAllChars(char[] chars, String filename) {
        checkArgNotNull(filename, "filename");
        writeAllChars(chars, new File(filename));
    }

    public static void writeAllChars(char[] chars, String filename, Charset charset) {
        checkArgNotNull(filename, "filename");
        checkArgNotNull(charset, "charset");
        writeAllChars(chars, new File(filename), charset);
    }

    public static void writeAllChars(char[] chars, File file) {
        checkArgNotNull(file, "file");
        writeAllChars(chars, file, Charset.forName("UTF8"));
    }

    public static void writeAllChars(char[] chars, File file, Charset charset) {
        checkArgNotNull(file, "file");
        checkArgNotNull(charset, "charset");
        try {
            ensureParentDir(file);
            writeAllChars(chars, new FileOutputStream(file), charset);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeAllChars(char[] chars, OutputStream stream) {
        checkArgNotNull(stream, "stream");
        writeAllChars(chars, stream, Charset.forName("UTF8"));
    }

    public static void writeAllChars(char[] chars, OutputStream stream, Charset charset) {
        checkArgNotNull(stream, "stream");
        checkArgNotNull(charset, "charset");
        CharArrayReader reader = new CharArrayReader(chars != null ? chars : new char[0]);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, charset));
        copyAll(reader, writer);
    }

    public static void writeAllBytes(byte[] data, String filename) {
        checkArgNotNull(data, "data");
        checkArgNotNull(filename, "filename");
        writeAllBytes(data, new File(filename));
    }

    public static void writeAllBytes(byte[] data, File file) {
        checkArgNotNull(data, "data");
        checkArgNotNull(file, "file");
        try {
            ensureParentDir(file);
            writeAllBytes(data, new FileOutputStream(file));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeAllBytes(byte[] data, OutputStream stream) {
        checkArgNotNull(data, "data");
        checkArgNotNull(stream, "stream");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BufferedOutputStream out = new BufferedOutputStream(stream);
        copyAll(in, out);
    }

    public static void copyAll(Reader reader, Writer writer) {
        checkArgNotNull(reader, "reader");
        checkArgNotNull(writer, "writer");
        try {
            char[] data = new char[4096]; // copy in chunks of 4K
            int count;
            while ((count = reader.read(data)) >= 0) writer.write(data, 0, count);

            reader.close();
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyAll(InputStream in, OutputStream out) {
        checkArgNotNull(in, "in");
        checkArgNotNull(out, "out");
        try {
            byte[] data = new byte[4096]; // copy in chunks of 4K
            int count;
            while ((count = in.read(data)) >= 0) {
                out.write(data, 0, count);
            }

            in.close();
            out.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ensureParentDir(String filename) {
        ensureParentDir(new File(filename));
    }

    public static void ensureParentDir(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            try {
                forceMkdir(parentDir);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Could not create directory %s", parentDir), e);
            }
        }
    }

    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (directory.isFile()) {
                throw new IOException(
                        "File '" + directory + "' exists and is not a directory. Unable to create directory.");
            }
        } else {
            if (!directory.mkdirs()) {
                throw new IOException("Unable to create directory " + directory);
            }
        }
    }

}
