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

package org.parboiled.common;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ArrayBuilderTest {

    @Test
    public void testAdd() throws Exception {
        assertEquals(new ArrayBuilder<Integer>().add(1, 2).add(3).add(4, 5, 6).get(), new Integer[] {1, 2, 3, 4, 5, 6});
        assertEquals(new ArrayBuilder<Integer>().add(1, 2).addNonNulls(3, null, 4, null).get(), new Integer[] {1, 2, 3, 4});
    }

}
