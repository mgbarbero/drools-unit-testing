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
package org.drools.core.reteoo;


import java.lang.reflect.Field;
import org.drools.core.test.model.DroolsTestCase;
import org.junit.Assert;
import org.junit.Test;


public class ObjectSourceTest extends DroolsTestCase {
    @Test
    public void testObjectSourceConstructor() {
        final MockObjectSource source = new MockObjectSource(15);
        Assert.assertEquals(15, getId());
        Assert.assertEquals(0, source.getAttached());
        source.attach();
        Assert.assertEquals(1, source.getAttached());
    }

    @Test
    public void testAddObjectSink() throws Exception {
        final MockObjectSource source = new MockObjectSource(15);
        // We need to re-assign this var each time the sink changes references
        final Field field = ObjectSource.class.getDeclaredField("sink");
        field.setAccessible(true);
        ObjectSinkPropagator sink = ((ObjectSinkPropagator) (field.get(source)));
        Assert.assertSame(EmptyObjectSinkAdapter.getInstance(), sink);
        final MockObjectSink sink1 = new MockObjectSink();
        addObjectSink(sink1);
        sink = ((ObjectSinkPropagator) (field.get(source)));
        Assert.assertSame(SingleObjectSinkAdapter.class, sink.getClass());
        Assert.assertEquals(1, sink.getSinks().length);
        final MockObjectSink sink2 = new MockObjectSink();
        addObjectSink(sink2);
        sink = ((ObjectSinkPropagator) (field.get(source)));
        Assert.assertSame(CompositeObjectSinkAdapter.class, sink.getClass());
        Assert.assertEquals(2, sink.getSinks().length);
        final MockObjectSink sink3 = new MockObjectSink();
        addObjectSink(sink3);
        Assert.assertSame(CompositeObjectSinkAdapter.class, sink.getClass());
        Assert.assertEquals(3, sink.getSinks().length);
        removeObjectSink(sink2);
        Assert.assertSame(CompositeObjectSinkAdapter.class, sink.getClass());
        Assert.assertEquals(2, sink.getSinks().length);
        removeObjectSink(sink1);
        sink = ((ObjectSinkPropagator) (field.get(source)));
        Assert.assertSame(SingleObjectSinkAdapter.class, sink.getClass());
        Assert.assertEquals(1, sink.getSinks().length);
        removeObjectSink(sink3);
        sink = ((ObjectSinkPropagator) (field.get(source)));
        Assert.assertSame(EmptyObjectSinkAdapter.getInstance(), sink);
        Assert.assertEquals(0, sink.getSinks().length);
    }
}

