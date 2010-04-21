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

/**
 * A {@link Matcher} matching a single character out of a given {@link org.parboiled.support.Characters} set.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class CharSetMatcher<V> extends AbstractMatcher<V> {

    public final Characters characters;

    public CharSetMatcher(@NotNull Characters characters) {
        Preconditions.checkArgument(!characters.equals(Characters.NONE));
        this.characters = characters;
    }

    public boolean match(@NotNull MatcherContext<V> context) {
        if (!characters.contains(context.getCurrentLocation().getChar())) return false;
        context.advanceInputLocation();
        context.createNode();
        return true;
    }

    public <R> R accept(@NotNull MatcherVisitor<V, R> visitor) {
        return visitor.visit(this);
    }

}