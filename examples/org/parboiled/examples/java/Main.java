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

package org.parboiled.examples.java;

import org.jetbrains.annotations.NotNull;
import org.parboiled.Parboiled;
import static org.parboiled.support.ParseTreeUtils.printParseErrors;
import org.parboiled.support.ParsingResult;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("parboiled Java parser, performance test");
        System.out.println("---------------------------------------");

        System.out.print("Creating parser... :");
        long start = System.currentTimeMillis();
        Parboiled.createParser(JavaParser.class);
        time(start);

        System.out.print("Creating parser again... :");
        start = System.currentTimeMillis();
        JavaParser parser = Parboiled.createParser(JavaParser.class);
        time(start);

        start = System.currentTimeMillis();
        System.out.print("Retrieving file list...");
        List<File> sources = recursiveGetAllJavaSources(new File("."), new ArrayList<File>());
        time(start);

        System.out.printf("Parsing all %s parboiled java sources", sources.size());
        start = System.currentTimeMillis();
        int lines = 0, characters = 0;
        for (File sourceFile : sources) {
            long dontCountStart = System.currentTimeMillis();
            String sourceText = readAllText(sourceFile);
            start += System.currentTimeMillis() - dontCountStart; // do not count the time for reading the text file

            ParsingResult<Object> result = parser.parse(parser.compilationUnit(), sourceText);
            if (!result.matched) {
                System.out.printf("\nParse error(s) in file '%s':\n%s",
                        sourceFile,
                        printParseErrors(result.parseErrors, result.inputBuffer));
                System.exit(1);
            } else {
                System.out.print('.');
            }
            lines += result.totalRows;
            characters += result.inputBuffer.getBuffer().length;
        }
        long time = time(start);

        System.out.println("Parsing statistics:");
        System.out.printf("    Files/sec: %.2f\n", sources.size() * 1000.0 / time);
        System.out.printf("    Lines/sec: %s\n", lines * 1000 / time);
        System.out.printf("    Chars/sec: %s\n", characters * 1000 / time);
    }

    private static long time(long start) {
        long end = System.currentTimeMillis();
        System.out.printf(" %s ms\n", end - start);
        return end - start;
    }

    private static final FileFilter fileFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().endsWith(".java");
        }
    };

    private static List<File> recursiveGetAllJavaSources(File file, ArrayList<File> list) {
        File[] files = file.listFiles(fileFilter);
        for (File f : files) {
            if (f.isDirectory()) {
                recursiveGetAllJavaSources(f, list);
            } else {
                list.add(f);
            }
        }
        return list;
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

    public static String readAllText(InputStream stream, @NotNull Charset charset) {
        if (stream == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
        StringWriter writer = new StringWriter();
        copyAll(reader, writer);
        return writer.toString();
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

}