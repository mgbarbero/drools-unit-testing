/**
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.core.common;


import org.drools.core.reteoo.ReteooFactHandleFactory;
import org.drools.core.test.model.Cheese;
import org.junit.Assert;
import org.junit.Test;


public class EqualityKeyTest {
    @Test
    public void test1() {
        ReteooFactHandleFactory factory = new ReteooFactHandleFactory();
        InternalFactHandle ch1 = factory.newFactHandle(new Cheese("c", 10), null, null, null);
        EqualityKey key = new EqualityKey(ch1);
        Assert.assertSame(ch1, key.getFactHandle());
        Assert.assertEquals(1, key.size());
        InternalFactHandle ch2 = factory.newFactHandle(new Cheese("c", 10), null, null, null);
        key.addFactHandle(ch2);
        Assert.assertEquals(2, key.size());
        Assert.assertEquals(ch2, key.get(1));
        key.removeFactHandle(ch1);
        Assert.assertSame(ch2, key.getFactHandle());
        Assert.assertEquals(1, key.size());
        key.removeFactHandle(ch2);
        Assert.assertNull(key.getFactHandle());
        Assert.assertEquals(0, key.size());
        key = new EqualityKey(ch2);
        key.addFactHandle(ch1);
        Assert.assertSame(ch2, key.getFactHandle());
        Assert.assertEquals(2, key.size());
        Assert.assertEquals(ch1, key.get(1));
        key.removeFactHandle(ch1);
        Assert.assertSame(ch2, key.getFactHandle());
        Assert.assertEquals(1, key.size());
        key.removeFactHandle(ch2);
        Assert.assertNull(key.getFactHandle());
        Assert.assertEquals(0, key.size());
    }
}

