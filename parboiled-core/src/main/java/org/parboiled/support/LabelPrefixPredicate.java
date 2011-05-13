/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

package org.parboiled.support;

import org.parboiled.Node;
import org.parboiled.common.Predicate;
import org.parboiled.common.StringUtils;

/**
 * A simple Node predicate determining whether a Node matches a given label prefix.
 * Useful for example for various methods of the {@link ParseTreeUtils}.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class LabelPrefixPredicate<V> implements Predicate<Node<V>> {
    private final String labelPrefix;

    public LabelPrefixPredicate(String labelPrefix) {
        this.labelPrefix = labelPrefix;
    }

    public boolean apply(Node<V> input) {
        return input != null && StringUtils.startsWith(input.getLabel(), labelPrefix);
    }
}
