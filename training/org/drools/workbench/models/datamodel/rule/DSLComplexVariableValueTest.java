/**
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.workbench.models.datamodel.rule;


import org.junit.Assert;
import org.junit.Test;


public class DSLComplexVariableValueTest {
    @Test
    public void testCopy() {
        final DSLComplexVariableValue original = new DSLComplexVariableValue("id", "value");
        final DSLComplexVariableValue copy = ((DSLComplexVariableValue) (original.copy()));
        Assert.assertEquals(original.getId(), copy.getId());
        Assert.assertEquals(original.getValue(), copy.getValue());
        Assert.assertEquals(original, copy);
        Assert.assertNotSame(original, copy);
    }
}

