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

package org.parboiled.asm;

import org.objectweb.asm.Type;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.ContextAware;
import org.parboiled.Rule;
import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;
import org.parboiled.support.Cached;
import org.parboiled.support.KeepAsIs;
import org.parboiled.support.Label;
import org.parboiled.support.Leaf;

interface Types {

    static final Type ABSTRACT_MATCHER_TYPE = Type.getType(AbstractMatcher.class);
    static final Type ACTION_WRAPPER_BASE_TYPE = Type.getType(ActionWrapperBase.class);
    static final Type BASE_PARSER_TYPE = Type.getType(BaseParser.class);
    static final Type BOOLEAN_TYPE = Type.getType(Boolean.class);
    static final Type CACHED_TYPE = Type.getType(Cached.class);
    static final Type CONTEXT_AWARE_TYPE = Type.getType(ContextAware.class);
    static final Type CONTEXT_TYPE = Type.getType(Context.class);
    static final Type KEEP_AS_IS_TYPE = Type.getType(KeepAsIs.class);
    static final Type LABEL_TYPE = Type.getType(Label.class);
    static final Type LEAF_TYPE = Type.getType(Leaf.class);
    static final Type MATCHER_TYPE = Type.getType(Matcher.class);
    static final Type PROXY_MATCHER_TYPE = Type.getType(ProxyMatcher.class);
    static final Type RULE_TYPE = Type.getType(Rule.class);

}
