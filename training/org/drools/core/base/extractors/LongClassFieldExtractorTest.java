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


import org.drools.core.base.TestBean;
import org.drools.core.spi.InternalReadAccessor;
import org.junit.Assert;
import org.junit.Test;


public class LongClassFieldExtractorTest extends BaseClassFieldExtractorsTest {
    private static final long VALUE = 5;

    InternalReadAccessor extractor;

    TestBean bean = new TestBean();

    @Test
    public void testGetBooleanValue() {
        try {
            this.extractor.getBooleanValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetByteValue() {
        try {
            Assert.assertEquals(LongClassFieldExtractorTest.VALUE, this.extractor.getByteValue(null, this.bean));
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testGetCharValue() {
        try {
            this.extractor.getCharValue(null, this.bean);
            Assert.fail("Should have throw an exception");
        } catch (final Exception e) {
            // success
        }
    }

    @Test
    public void testGetDoubleValue() {
        try {
            Assert.assertEquals(LongClassFieldExtractorTest.VALUE, this.extractor.getDoubleValue(null, this.bean), 0.01);
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testGetFloatValue() {
        try {
            Assert.assertEquals(LongClassFieldExtractorTest.VALUE, this.extractor.getFloatValue(null, this.bean), 0.01);
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testGetIntValue() {
        try {
            Assert.assertEquals(LongClassFieldExtractorTest.VALUE, this.extractor.getIntValue(null, this.bean));
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testGetLongValue() {
        try {
            Assert.assertEquals(LongClassFieldExtractorTest.VALUE, this.extractor.getLongValue(null, this.bean));
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testGetShortValue() {
        try {
            Assert.assertEquals(LongClassFieldExtractorTest.VALUE, this.extractor.getShortValue(null, this.bean));
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testGetValue() {
        try {
            Assert.assertEquals(new Long(((short) (LongClassFieldExtractorTest.VALUE))), this.extractor.getValue(null, this.bean));
            Assert.assertTrue(((this.extractor.getValue(null, this.bean)) instanceof Long));
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }

    @Test
    public void testIsNullValue() {
        try {
            Assert.assertFalse(this.extractor.isNullValue(null, this.bean));
        } catch (final Exception e) {
            Assert.fail("Should not throw an exception");
        }
    }
}

