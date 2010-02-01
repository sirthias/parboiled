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

package org.parboiled.examples;

import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.examples.java.JavaParser;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;
import org.parboiled.test.AbstractTest;
import static org.parboiled.trees.GraphUtils.collectAllNodes;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;

public class JavaTest extends AbstractTest {

    @Test
    public void test() {
        //String testSource = FileUtils.readAllText("test/org/parboiled/examples/JavaTest.java");
        JavaParser parser = Parboiled.createParser(JavaParser.class);
        Rule compilationUnit = parser.compilationUnit();

        assertFalse(existlIndexCollisions((Matcher) compilationUnit));
    }

    @SuppressWarnings("unchecked")
    private static boolean existlIndexCollisions(Matcher root) {
        boolean collisions = false;
        Set<Matcher> all = collectAllNodes(root, new HashSet<Matcher>());
        Matcher[] seen = new Matcher[all.size()];
        for (Matcher matcher : all) {
            Matcher existing = seen[matcher.getIndex()];
            if (existing != null && ProxyMatcher.unwrap(existing) != ProxyMatcher.unwrap(matcher)) {
                System.err.printf("'%s' and '%s' have the same index %s\n", existing, matcher, matcher.getIndex());
                collisions = true;
            } else {
                seen[matcher.getIndex()] = matcher;
            }
        }
        return collisions;
    }

}