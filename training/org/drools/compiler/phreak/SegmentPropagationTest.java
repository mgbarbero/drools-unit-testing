/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.compiler.phreak;


import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.BetaMemory;
import org.drools.core.reteoo.JoinNode;
import org.drools.core.reteoo.SegmentMemory;
import org.drools.core.reteoo.builder.BuildContext;
import org.junit.Test;


public class SegmentPropagationTest {
    BuildContext buildContext;

    JoinNode joinNode;

    JoinNode sinkNode0;

    JoinNode sinkNode1;

    JoinNode sinkNode2;

    InternalWorkingMemory wm;

    BetaMemory bm;

    SegmentMemory smem;

    BetaMemory bm0;

    BetaMemory bm1;

    BetaMemory bm2;

    SegmentMemory smem0;

    SegmentMemory smem1;

    SegmentMemory smem2;

    A a0 = A.a(0);

    A a1 = A.a(1);

    A a2 = A.a(2);

    A a3 = A.a(3);

    A a4 = A.a(4);

    B b0 = B.b(0);

    B b1 = B.b(1);

    B b2 = B.b(2);

    B b3 = B.b(3);

    B b4 = B.b(4);

    @Test
    public void test1() {
        setupJoinNode();
        JoinNode parentNode = joinNode;
        JoinNode child1Node = new JoinNode();
        JoinNode child2Node = new JoinNode();
        JoinNode child3Node = new JoinNode();
        parentNode.addTupleSink(child1Node);
        parentNode.addTupleSink(child2Node);
        parentNode.addTupleSink(child3Node);
        SegmentMemory smem = new SegmentMemory(parentNode);
        smem.setTipNode(parentNode);
        // @formatter:off
        test().left().insert(a0, a1).right().insert(b0, b1, b2).preStaged(smem0).insert().delete().update().postStaged(smem0).insert(Pair.t(a1, b2), Pair.t(a1, b0), Pair.t(a0, b2), Pair.t(a0, b1)).delete().update().postStaged(smem1).insert(Pair.t(a0, b1), Pair.t(a0, b2), Pair.t(a1, b0), Pair.t(a1, b2)).delete().update().postStaged(smem2).insert(Pair.t(a0, b1), Pair.t(a0, b2), Pair.t(a1, b0), Pair.t(a1, b2)).delete().update().run();
        test().left().update(a0).preStaged(smem0).insert(Pair.t(a1, b2), Pair.t(a1, b0)).delete().update().postStaged(smem0).insert(Pair.t(a0, b2), Pair.t(a0, b1), Pair.t(a1, b2), Pair.t(a1, b0)).delete().update().postStaged(smem1).insert(Pair.t(a1, b0), Pair.t(a1, b2), Pair.t(a0, b1), Pair.t(a0, b2)).delete().update().postStaged(smem2).insert(Pair.t(a1, b0), Pair.t(a1, b2), Pair.t(a0, b1), Pair.t(a0, b2)).delete().update().run();
        test().right().delete(b2).preStaged(smem0).insert(Pair.t(a0, b1), Pair.t(a1, b0)).delete().update().postStaged(smem0).insert(Pair.t(a0, b1), Pair.t(a1, b0)).delete().update().postStaged(smem1).insert(Pair.t(a1, b0), Pair.t(a0, b1)).postStaged(smem2).insert(Pair.t(a1, b0), Pair.t(a0, b1)).run();
        // @formatter:on
    }
}

