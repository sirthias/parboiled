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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.support.Characters;
import org.parboiled.support.MatcherVisitor;

/**
 * A {@link Matcher} matching a single character out of a given {@link org.parboiled.support.Characters} set.
 */
public class AnyOfMatcher extends AbstractMatcher {

    public final Characters characters;

    public AnyOfMatcher(@NotNull Characters characters) {
        Preconditions.checkArgument(!characters.equals(Characters.NONE));
        this.characters = characters;
    }

    public boolean match(@NotNull MatcherContext context) {
        if (!characters.contains(context.getCurrentChar())) return false;
        context.advanceIndex(1);
        context.createNode();
        return true;
    }

    public <R> R accept(@NotNull MatcherVisitor<R> visitor) {
        return visitor.visit(this);
    }

}