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

package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.actionparameters.ActionParameterUtils;

public class SetValueMatcher<V> extends SpecialMatcher<V> {
    private final V value;

    public SetValueMatcher(V value) {
        this.value = value;
    }

    public String getLabel() {
        return "set value";
    }

    @SuppressWarnings({"unchecked"})
    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        context = context.getParent(); // actions want to operate in the parent scope
        Object value = ActionParameterUtils.resolve(this.value, context);
        context.setNodeValue((V) value);
        return true;
    }

}
