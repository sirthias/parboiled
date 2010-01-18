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

package org.parboiled.actionparameters;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;

/**
 * An ActionParameter that returns the first parse tree node found in the current Context scope with a given label prefix.
 */
public class LabelNodeParameter implements ActionParameter {
    private final Object labelPrefix;

    public LabelNodeParameter(Object labelPrefix) {
        this.labelPrefix = labelPrefix;
    }

    public Object resolve(@NotNull MatcherContext<?> context) throws Throwable {
        return context.getNodeByLabel(ActionParameterUtils.resolve(labelPrefix, context, String.class));
    }

    @Override
    public String toString() {
        return "NODE_BY_LABEL(" + labelPrefix + ')';
    }

}