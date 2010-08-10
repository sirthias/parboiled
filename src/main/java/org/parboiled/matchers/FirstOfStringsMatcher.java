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

package org.parboiled.matchers;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.support.InputBuffer;

import java.util.*;

/**
 * A specialized FirstOfMatcher that handles FirstOf(string, string, ...) rules much faster that the regular
 * FirstOfMatcher. If fast string matching is enabled this matcher uses a prebuilt character tree to efficiently
 * determine whether the next input characters match the rule expression.
 */
public class FirstOfStringsMatcher extends FirstOfMatcher {

    // a node in the character tree
    static class Record {
        final char[] chars; // the sub characters of this node
        final Record[] subs; // the sub records corresponding to the respective character

        private Record(char[] chars, Record[] subs) {
            this.chars = chars;
            this.subs = subs;
        }
    }

    private final Record root; // the root of the character tree

    public FirstOfStringsMatcher(@NotNull Rule[] subRules, char[][] strings) {
        super(subRules);
        root = createRecord(0, strings);
    }

    @Override
    public boolean match(@NotNull MatcherContext context) {
        if (!context.fastStringMatching()) {
            return super.match(context);
        }

        Record rec = root;
        int endIx = context.getCurrentIndex();
        InputBuffer buffer = context.getInputBuffer();
        char c = context.getCurrentChar();

        loop:
        while (true) {
            char[] chars = rec.chars;
            for (int i = 0; i < chars.length; i++) {
                if (c == chars[i]) {
                    endIx++;
                    Record sub = rec.subs[i];
                    if (sub == null) {
                        break loop; // success, we complected a tree path to a leave
                    }
                    rec = sub;
                    c = buffer.charAt(endIx);
                    continue loop;
                }
            }
            // we checked all sub branches of the current node, none matched, therefore fail
            return false;
        }

        context.advanceIndex(endIx - context.getCurrentIndex());
        context.createNode();
        return true;
    }

    static Record createRecord(int pos, char[][] strings) {
        Map<Character, Set<char[]>> map = new TreeMap<Character, Set<char[]>>();
        for (char[] s : strings) {
            if (s == null || s.length <= pos) continue;
            char c = s[pos];
            Set<char[]> charStrings = map.get(c);
            if (charStrings == null) {
                charStrings = new HashSet<char[]>();
                map.put(c, charStrings);
            }
            charStrings.add(s);
        }

        if (map.isEmpty()) return null;

        char[] chars = new char[map.size()];
        Record[] subs = new Record[map.size()];
        int i = 0;
        for (Map.Entry<Character, Set<char[]>> entry : map.entrySet()) {
            chars[i] = entry.getKey();
            subs[i++] = createRecord(pos + 1, entry.getValue().toArray(new char[entry.getValue().size()][]));
        }
        return new Record(chars, subs);
    }

}