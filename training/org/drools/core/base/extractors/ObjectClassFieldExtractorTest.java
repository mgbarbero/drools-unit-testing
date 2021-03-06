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
package org.drools.core.base.extractors;


import java.util.Collections;
import java.util.List;
import org.drools.core.base.ClassFieldAccessorCache;
import org.drools.core.base.ClassFieldAccessorStore;
import org.drools.core.base.TestBean;
import org.drools.core.spi.InternalReadAccessor;
import org.junit.Assert;
import org.junit.Test;


public class ObjectClassFieldExtractorTest extends BaseClassFieldExtractorsTest {
    InternalReadAccessor reader;

    TestBean bean = new TestBean();

    @Test
    public void testGetBooleanValue() {
        try {
            this.reader.getBooleanValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetByteValue() {
        try {
            this.reader.getByteValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetCharValue() {
        try {
            this.reader.getCharValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetDoubleValue() {
        try {
            this.reader.getDoubleValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetFloatValue() {
        try {
            this.reader.getFloatValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetIntValue() {
        try {
            this.reader.getIntValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetLongValue() {
        try {
            this.reader.getLongValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetShortValue() {
        try {
            this.reader.getShortValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetValue() {
        Assert.assertEquals(Collections.EMPTY_LIST, this.reader.getValue(null, this.bean));
        Assert.assertTrue(((this.reader.getValue(null, this.bean)) instanceof List));
    }

    @Test
    public void testIsNullValue() {
        Assert.assertFalse(this.reader.isNullValue(null, this.bean));
        ClassFieldAccessorStore store = new ClassFieldAccessorStore();
        store.setClassFieldAccessorCache(new ClassFieldAccessorCache(Thread.currentThread().getContextClassLoader()));
        store.setEagerWire(true);
        InternalReadAccessor nullExtractor = store.getReader(TestBean.class, "nullAttr");
        Assert.assertTrue(nullExtractor.isNullValue(null, this.bean));
    }
}

