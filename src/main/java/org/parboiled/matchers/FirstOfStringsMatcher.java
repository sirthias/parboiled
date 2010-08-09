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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FirstOfStringsMatcher extends FirstOfMatcher {

    static class Record {
        final char[] chars;
        final Record[] subs;

        private Record(char[] chars, Record[] subs) {
            this.chars = chars;
            this.subs = subs;
        }
    }

    private final Record root;

    public FirstOfStringsMatcher(@NotNull Rule[] subRules, char[][] strings) {
        super(subRules);
        root = createRecord(0, strings);
    }

    @Override
    public boolean match(@NotNull MatcherContext context) {
        if (!context.fastStringMatching()) {
            return super.match(context);
        }

        int endIx = test(root, context.getCurrentChar(), context.getInputBuffer(), context.getCurrentIndex());
        if (endIx == -1) {
            return false;
        }

        context.advanceIndex(endIx - context.getCurrentIndex());
        context.createNode();
        return true;
    }

    static Record createRecord(int pos, char[][] strings) {
        Map<Character, Set<char[]>> map = new HashMap<Character, Set<char[]>>();
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

    private static int test(Record rec, char c, InputBuffer buffer, int ix) {
        char[] chars = rec.chars;
        for (int i = 0; i < chars.length; i++) {
            if (c == chars[i]) {
                Record sub = rec.subs[i];
                ix++;
                if (sub == null) {
                    return ix;
                } else {
                    return test(sub, buffer.charAt(ix), buffer, ix);
                }
            }
        }
        return -1;
    }

}