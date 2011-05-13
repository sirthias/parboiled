/*
 * Copyright (c) 2009 Ken Wenzel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.parboiled.examples.sparql;

import org.parboiled.Parboiled;
import org.parboiled.common.FileUtils;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SparqlTest {

    enum Result {
        OK, FAIL
    }

    class TextInfo {
        Result result;
        String text;

        public TextInfo(Result result, String text) {
            super();
            this.result = result;
            this.text = text;
        }
    }

    SparqlParser parser = Parboiled.createParser(SparqlParser.class);

    @Test
    public void test() throws Exception {
        int failures = 0;
        for (TextInfo textInfo : getTextInfos()) {
            ParsingResult result = new RecoveringParseRunner(parser.Query()).run(textInfo.text);

            boolean passed = result.hasErrors() == (textInfo.result == Result.FAIL);
            if (!passed) {
                failures++;
                //System.err.println(textInfo.text + " --> " + textInfo.result + "\n\n");
            }
        }
        Assert.assertEquals(failures, 12); // currently 12 tests require semantic validation and therefore fail
    }

    protected List<TextInfo> getTextInfos() throws Exception {
        List<TextInfo> textInfos = new ArrayList<TextInfo>();

        BufferedReader in = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("SparqlTest.test"),
                Charset.forName("UTF8")));
        while (in.ready()) {
            if (in.read() == '<' && in.ready() && in.read() == '<') {
                in.readLine();
                textInfos.add(parseText(in));
            }
        }
        return textInfos;
    }

    private TextInfo parseText(BufferedReader in) throws Exception {
        Pattern unicodes = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

        StringBuffer text = new StringBuffer();
        while (in.ready()) {
            String line = in.readLine();
            if (line.startsWith(">>")) {
                return new TextInfo(line.toLowerCase().contains("ok") ? Result.OK : Result.FAIL, text.toString());
            } else {
                Matcher matcher = unicodes.matcher(line);
                while (matcher.find()) {
                    matcher.appendReplacement(text, Character.toString((char) Integer.parseInt(matcher.group(1), 16)));
                }
                matcher.appendTail(text);

                text.append('\n');
            }
        }
        throw new NoSuchElementException("Expected \">>\"");
    }

}
