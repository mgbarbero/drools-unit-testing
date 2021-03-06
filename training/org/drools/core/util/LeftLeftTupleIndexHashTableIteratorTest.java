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
package org.drools.core.util;


import NodeTypeEnums.JoinNode;
import Operator.EQUAL;
import java.util.ArrayList;
import java.util.List;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.base.evaluators.EvaluatorRegistry;
import org.drools.core.common.BetaConstraints;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.reteoo.BetaMemory;
import org.drools.core.reteoo.LeftTupleImpl;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.util.index.TupleIndexHashTable;
import org.drools.core.util.index.TupleIndexHashTable.FieldIndexHashTableFullIterator;
import org.drools.core.util.index.TupleList;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.mockito.Mockito;


public class LeftLeftTupleIndexHashTableIteratorTest {
    public static EvaluatorRegistry registry = new EvaluatorRegistry();

    @Test
    public void test1() {
        BetaNodeFieldConstraint constraint0 = getConstraint("d", EQUAL, "this", LeftLeftTupleIndexHashTableIteratorTest.Foo.class);
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[]{ constraint0 };
        RuleBaseConfiguration config = new RuleBaseConfiguration();
        BetaConstraints betaConstraints = null;
        betaConstraints = new org.drools.core.common.SingleBetaConstraints(constraints, config);
        BetaMemory betaMemory = betaConstraints.createBetaMemory(config, JoinNode);
        InternalKnowledgeBase kBase = ((InternalKnowledgeBase) (KnowledgeBaseFactory.newKnowledgeBase()));
        KieSession ss = kBase.newKieSession();
        InternalFactHandle fh1 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("brie", 1))));
        InternalFactHandle fh2 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("brie", 1))));
        InternalFactHandle fh3 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("soda", 1))));
        InternalFactHandle fh4 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("soda", 1))));
        InternalFactHandle fh5 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("bread", 3))));
        InternalFactHandle fh6 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("bread", 3))));
        InternalFactHandle fh7 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("cream", 3))));
        InternalFactHandle fh8 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("gorda", 15))));
        InternalFactHandle fh9 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("beer", 16))));
        InternalFactHandle fh10 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("mars", 0))));
        InternalFactHandle fh11 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("snicker", 0))));
        InternalFactHandle fh12 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("snicker", 0))));
        InternalFactHandle fh13 = ((InternalFactHandle) (ss.insert(new LeftLeftTupleIndexHashTableIteratorTest.Foo("snicker", 0))));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh1, null, true));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh2, null, true));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh3, null, true));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh4, null, true));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh5, null, true));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh6, null, true));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh7, null, true));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh8, null, true));
        betaMemory.getLeftTupleMemory().add(new LeftTupleImpl(fh9, null, true));
        TupleIndexHashTable hashTable = ((TupleIndexHashTable) (betaMemory.getLeftTupleMemory()));
        // can't create a 0 hashCode, so forcing
        TupleList leftTupleList = new TupleList();
        leftTupleList.add(new LeftTupleImpl(fh10, null, true));
        hashTable.getTable()[0] = leftTupleList;
        leftTupleList = new TupleList();
        leftTupleList.add(new LeftTupleImpl(fh11, null, true));
        leftTupleList.add(new LeftTupleImpl(fh12, null, true));
        leftTupleList.add(new LeftTupleImpl(fh13, null, true));
        ((TupleList) (hashTable.getTable()[0])).setNext(leftTupleList);
        Entry[] table = hashTable.getTable();
        List list = new ArrayList();
        for (int i = 0; i < (table.length); i++) {
            if ((table[i]) != null) {
                List entries = new ArrayList();
                entries.add(i);
                Entry entry = table[i];
                while (entry != null) {
                    entries.add(entry);
                    entry = entry.getNext();
                } 
                list.add(entries.toArray());
            }
        }
        Assert.assertEquals(5, list.size());
        // This tests the hashcode index allocation. If the rehash function (or any other way hashcodes are computed) changes, these numbers will change.
        Object[] entries = ((Object[]) (list.get(0)));
        Assert.assertEquals(0, entries[0]);
        Assert.assertEquals(3, entries.length);
        entries = ((Object[]) (list.get(1)));
        Assert.assertEquals(102, entries[0]);
        Assert.assertEquals(2, entries.length);
        entries = ((Object[]) (list.get(2)));
        Assert.assertEquals(103, entries[0]);
        Assert.assertEquals(2, entries.length);
        entries = ((Object[]) (list.get(3)));
        Assert.assertEquals(115, entries[0]);
        Assert.assertEquals(3, entries.length);
        entries = ((Object[]) (list.get(4)));
        Assert.assertEquals(117, entries[0]);
        Assert.assertEquals(3, entries.length);
        // System.out.println( entries );
        list = new ArrayList<LeftTupleImpl>();
        Iterator it = betaMemory.getLeftTupleMemory().iterator();
        for (LeftTupleImpl leftTuple = ((LeftTupleImpl) (it.next())); leftTuple != null; leftTuple = ((LeftTupleImpl) (it.next()))) {
            list.add(leftTuple);
        }
        Assert.assertEquals(13, list.size());
    }

    @Test
    public void testLastBucketInTheTable() {
        // JBRULES-2574
        // setup the entry array with an element in the first bucket, one
        // in the middle and one in the last bucket
        Entry[] entries = new Entry[10];
        entries[0] = Mockito.mock(TupleList.class);
        entries[5] = Mockito.mock(TupleList.class);
        entries[9] = Mockito.mock(TupleList.class);
        LeftTupleImpl[] tuples = new LeftTupleImpl[]{ Mockito.mock(LeftTupleImpl.class), Mockito.mock(LeftTupleImpl.class), Mockito.mock(LeftTupleImpl.class) };
        // set return values for methods
        Mockito.when(entries[0].getNext()).thenReturn(null);
        Mockito.when(getFirst()).thenReturn(tuples[0]);
        Mockito.when(entries[5].getNext()).thenReturn(null);
        Mockito.when(getFirst()).thenReturn(tuples[1]);
        Mockito.when(entries[9].getNext()).thenReturn(null);
        Mockito.when(getFirst()).thenReturn(tuples[2]);
        // create the mock table for the iterator
        AbstractHashTable table = Mockito.mock(AbstractHashTable.class);
        Mockito.when(table.getTable()).thenReturn(entries);
        // create the iterator
        FieldIndexHashTableFullIterator iterator = new FieldIndexHashTableFullIterator(table);
        // test it
        Assert.assertThat(iterator.next(), CoreMatchers.sameInstance(((Object) (tuples[0]))));
        Assert.assertThat(iterator.next(), CoreMatchers.sameInstance(((Object) (tuples[1]))));
        Assert.assertThat(iterator.next(), CoreMatchers.sameInstance(((Object) (tuples[2]))));
        Assert.assertThat(iterator.next(), CoreMatchers.is(((Object) (null))));
    }

    public static class Foo {
        private String val;

        private int hashCode;

        public Foo(String val, int hashCode) {
            this.val = val;
            this.hashCode = hashCode;
        }

        public String getVal() {
            return val;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if ((this) == obj)
                return true;

            if (obj == null)
                return false;

            if ((getClass()) != (obj.getClass()))
                return false;

            LeftLeftTupleIndexHashTableIteratorTest.Foo other = ((LeftLeftTupleIndexHashTableIteratorTest.Foo) (obj));
            if ((hashCode) != (other.hashCode))
                return false;

            if ((val) == null) {
                if ((other.val) != null)
                    return false;

            } else
                if (!(val.equals(other.val)))
                    return false;


            return true;
        }
    }
}

