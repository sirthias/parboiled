/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

public class FileUtils {

    private FileUtils() {}

    public static String readAllTextFromResource(@NotNull String resource) {
        return readAllText(ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
    }

    public static String readAllTextFromResource(@NotNull String resource, @NotNull Charset charset) {
        return readAllText(ClassLoader.getSystemClassLoader().getResourceAsStream(resource), charset);
    }

    public static String readAllText(@NotNull String filename) {
        return readAllText(new File(filename));
    }

    public static String readAllText(@NotNull String filename, @NotNull Charset charset) {
        return readAllText(new File(filename), charset);
    }

    public static String readAllText(@NotNull File file) {
        return readAllText(file, Charset.forName("UTF8"));
    }

    public static String readAllText(@NotNull File file, @NotNull Charset charset) {
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

    public static String readAllText(InputStream stream, @NotNull Charset charset) {
        if (stream == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
        StringWriter writer = new StringWriter();
        copyAll(reader, writer);
        return writer.toString();
    }
    
    public static char[] readAllCharsFromResource(@NotNull String resource) {
        return readAllChars(ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
    }

    public static char[] readAllCharsFromResource(@NotNull String resource, @NotNull Charset charset) {
        return readAllChars(ClassLoader.getSystemClassLoader().getResourceAsStream(resource), charset);
    }

    public static char[] readAllChars(@NotNull String filename) {
        return readAllChars(new File(filename));
    }

    public static char[] readAllChars(@NotNull String filename, @NotNull Charset charset) {
        return readAllChars(new File(filename), charset);
    }

    public static char[] readAllChars(@NotNull File file) {
        return readAllChars(file, Charset.forName("UTF8"));
    }

    public static char[] readAllChars(@NotNull File file, @NotNull Charset charset) {
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

    public static char[] readAllChars(InputStream stream, @NotNull Charset charset) {
        if (stream == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
        CharArrayWriter writer = new CharArrayWriter();
        copyAll(reader, writer);
        return writer.toCharArray();
    }

    public static byte[] readAllBytesFromResource(@NotNull String resource) {
        return readAllBytes(ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
    }

    public static byte[] readAllBytes(@NotNull String filename) {
        return readAllBytes(new File(filename));
    }

    public static byte[] readAllBytes(@NotNull File file) {
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

    public static void writeAllText(String text, @NotNull String filename) {
        writeAllText(text, new File(filename));
    }

    public static void writeAllText(String text, @NotNull String filename, @NotNull Charset charset) {
        writeAllText(text, new File(filename), charset);
    }

    public static void writeAllText(String text, @NotNull File file) {
        try {
            ensureParentDir(file);
            writeAllText(text, new FileOutputStream(file), Charset.forName("UTF8"));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeAllText(String text, @NotNull File file, @NotNull Charset charset) {
        try {
            writeAllText(text, new FileOutputStream(file), charset);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeAllText(String text, @NotNull OutputStream stream) {
        writeAllText(text, stream, Charset.forName("UTF8"));
    }

    public static void writeAllText(String text, @NotNull OutputStream stream, @NotNull Charset charset) {
        StringReader reader = new StringReader(text != null ? text : "");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, charset));
        copyAll(reader, writer);
    }
    
    public static void writeAllChars(char[] chars, @NotNull String filename) {
        writeAllChars(chars, new File(filename));
    }

    public static void writeAllChars(char[] chars, @NotNull String filename, @NotNull Charset charset) {
        writeAllChars(chars, new File(filename), charset);
    }

    public static void writeAllChars(char[] chars, @NotNull File file) {
        try {
            ensureParentDir(file);
            writeAllChars(chars, new FileOutputStream(file), Charset.forName("UTF8"));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeAllChars(char[] chars, @NotNull File file, @NotNull Charset charset) {
        try {
            writeAllChars(chars, new FileOutputStream(file), charset);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeAllChars(char[] chars, @NotNull OutputStream stream) {
        writeAllChars(chars, stream, Charset.forName("UTF8"));
    }

    public static void writeAllChars(char[] chars, @NotNull OutputStream stream, @NotNull Charset charset) {
        CharArrayReader reader = new CharArrayReader(chars != null ? chars : new char[0]);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, charset));
        copyAll(reader, writer);
    }

    public static void writeAllBytes(@NotNull byte[] data, @NotNull String filename) {
        writeAllBytes(data, new File(filename));
    }

    public static void writeAllBytes(@NotNull byte[] data, @NotNull File file) {
        try {
            ensureParentDir(file);
            writeAllBytes(data, new FileOutputStream(file));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeAllBytes(@NotNull byte[] data, @NotNull OutputStream stream) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        BufferedOutputStream out = new BufferedOutputStream(stream);
        copyAll(in, out);
    }

    public static void copyAll(@NotNull Reader reader, @NotNull Writer writer) {
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

    public static void copyAll(@NotNull InputStream in, @NotNull OutputStream out) {
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
