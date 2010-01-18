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

import org.parboiled.AbstractAction;
import org.parboiled.ActionResult;
import org.parboiled.BaseParser;
import org.objectweb.asm.util.ASMifierClassVisitor;

public class ASMifierHelper extends BaseParser<String> {

    private final Integer action = 12345;

    public class ActionWrapper extends AbstractAction<String> {

        public ActionResult run() {
            return SET(action.toString());
        }

    }

    public static void main(String[] args) throws Exception {
        ASMifierClassVisitor.main(new String[] { ActionWrapper.class.getName() });
    }

}
