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
package org.drools.verifier.core.maps;


import org.junit.Assert;
import org.junit.Test;


public class MultiMapFactoryTest {
    @Test
    public void testDefault() throws Exception {
        Assert.assertFalse(((MultiMapFactory.make()) instanceof ChangeHandledMultiMap));
    }

    @Test
    public void normal() throws Exception {
        Assert.assertFalse(((MultiMapFactory.make(false)) instanceof ChangeHandledMultiMap));
    }

    @Test
    public void changeHandled() throws Exception {
        Assert.assertTrue(((MultiMapFactory.make(true)) instanceof ChangeHandledMultiMap));
    }
}

