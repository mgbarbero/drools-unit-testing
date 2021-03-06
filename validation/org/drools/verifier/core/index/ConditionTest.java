/**
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.verifier.core.index;


import org.drools.verifier.core.index.keys.Values;
import org.drools.verifier.core.index.model.Condition;
import org.junit.Assert;
import org.junit.Test;


public class ConditionTest {
    private Condition condition;

    @Test
    public void valueSet() throws Exception {
        Assert.assertEquals(1, condition.getValues().size());
        Assert.assertEquals(1, condition.getValues().iterator().next());
    }

    @Test
    public void changeValue() throws Exception {
        condition.setValue(new Values(2));
        Assert.assertEquals(1, condition.getValues().size());
        Assert.assertEquals(2, condition.getValues().iterator().next());
    }
}

