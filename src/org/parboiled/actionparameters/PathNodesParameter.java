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
import org.parboiled.Node;
import org.parboiled.support.ParseTreeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An ActionParameter that returns all parse tree nodes found under the given path in the current Context scope
 * as a list.
 */
public class PathNodesParameter extends ActionParameterWithArgument<String> {

    public PathNodesParameter(Object path) {
        super(List.class, path, String.class);
    }

    @SuppressWarnings({"unchecked"})
    public Object resolve(@NotNull MatcherContext<?> context) {
        List<? extends Node<?>> subNodes = context.getSubNodes();
        String path = resolveArgument(context);
        return ParseTreeUtils.collectNodesByPath((List<Node<Object>>) subNodes, path, new ArrayList());
    }

    @Override
    public String toString() {
        return "NODES(" + argument + ')';
    }

}
