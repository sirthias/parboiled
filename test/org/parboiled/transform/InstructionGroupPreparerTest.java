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

package org.parboiled.transform;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

@Test(groups="secondary", dependsOnGroups = "primary")
public class InstructionGroupPreparerTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer()
    );

    public void testRuleWithComplexActionSetup() throws Exception {
        RuleMethod method = processMethod("RuleWithComplexActionSetup", processors);

        assertEquals(method.getGroups().size(), 2);

        InstructionGroup group = method.getGroups().get(0);
        assertEquals(group.getName(), "Action$HCsAlhftW7cYn1dT");
        assertEquals(group.getFields().size(), 3);
        assertEquals(group.getFields().get(0).desc, "I");
        assertEquals(group.getFields().get(1).desc, "I");
        assertEquals(group.getFields().get(2).desc, "I");

        group = method.getGroups().get(1);
        assertEquals(group.getName(), "Action$ZtTW8WICJeWWcGjq");
        assertEquals(group.getFields().size(), 5);
        assertEquals(group.getFields().get(0).desc, "Lorg/parboiled/transform/TestParser$$parboiled;");
        assertEquals(group.getFields().get(1).desc, "I");
        assertEquals(group.getFields().get(2).desc, "Ljava/lang/String;");
        assertEquals(group.getFields().get(3).desc, "I");
        assertEquals(group.getFields().get(4).desc, "I");
    }

}