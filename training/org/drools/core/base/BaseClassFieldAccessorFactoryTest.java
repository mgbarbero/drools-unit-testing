/**
 * Copyright 2005 Red Hat, Inc. and/or its affiliates.
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
package org.drools.core.base;


import ClassFieldAccessorCache.CacheEntry;
import org.drools.core.spi.InternalReadAccessor;
import org.drools.core.util.asm.BeanInherit;
import org.drools.core.util.asm.TestAbstract;
import org.drools.core.util.asm.TestAbstractImpl;
import org.drools.core.util.asm.TestInterface;
import org.drools.core.util.asm.TestInterfaceImpl;
import org.junit.Assert;
import org.junit.Test;


public class BaseClassFieldAccessorFactoryTest {
    ClassFieldAccessorStore store = new ClassFieldAccessorStore();

    @Test
    public void testIt() throws Exception {
        ClassFieldAccessorFactory factory = new ClassFieldAccessorFactory();
        ClassFieldAccessorCache.CacheEntry cachEntry = new ClassFieldAccessorCache.CacheEntry(Thread.currentThread().getContextClassLoader());
        InternalReadAccessor ex = factory.getClassFieldReader(TestBean.class, "name", cachEntry);
        Assert.assertEquals("michael", ex.getValue(null, new TestBean()));
        ex = factory.getClassFieldReader(TestBean.class, "age", cachEntry);
        Assert.assertEquals(42, ((Number) (ex.getValue(null, new TestBean()))).intValue());
    }

    @Test
    public void testInterface() throws Exception {
        final InternalReadAccessor ex = store.getReader(TestInterface.class, "something");
        Assert.assertEquals(1, ex.getIndex());
        Assert.assertEquals("foo", ex.getValue(null, new TestInterfaceImpl()));
    }

    @Test
    public void testAbstract() throws Exception {
        final InternalReadAccessor ex = store.getReader(TestAbstract.class, "something");
        Assert.assertEquals(2, ex.getIndex());
        Assert.assertEquals("foo", ex.getValue(null, new TestAbstractImpl()));
    }

    @Test
    public void testInherited() throws Exception {
        final InternalReadAccessor ex = store.getReader(BeanInherit.class, "text");
        Assert.assertEquals("hola", ex.getValue(null, new BeanInherit()));
    }

    @Test
    public void testSelfReference() throws Exception {
        final InternalReadAccessor ex = store.getReader(BeanInherit.class, "this");
        final TestBean bean = new TestBean();
        Assert.assertEquals(bean, ex.getValue(null, bean));
    }
}

