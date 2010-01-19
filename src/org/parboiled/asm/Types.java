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

package org.parboiled.asm;

import org.objectweb.asm.Type;
import org.parboiled.*;

interface Types {

    final Type OBJECT_TYPE = Type.getType(Object.class);
    final Type BOOLEAN_TYPE = Type.getType(Boolean.class);
    final Type RULE_TYPE = Type.getType(Rule.class);
    final Type CONTEXT_AWARE_TYPE = Type.getType(ContextAware.class);
    final Type CONTEXT_TYPE = Type.getType(Context.class);
    final Type BASE_PARSER_TYPE = Type.getType(BaseParser.class);
    final Type ACTION_WRAPPER_BASE_TYPE = Type.getType(ActionWrapperBase.class);

}
