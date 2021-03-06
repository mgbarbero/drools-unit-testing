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


import Operator.EQUAL;
import Operator.NOT_EQUAL;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.test.model.Cheese;
import org.junit.Test;


public class TripleBetaConstraintsTest extends BaseBetaConstraintsTest {
    @Test
    public void testNoneIndxed() {
        BetaNodeFieldConstraint constraint0 = getConstraint("cheeseType0", NOT_EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint constraint1 = getConstraint("cheeseType1", NOT_EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint constraint3 = getConstraint("cheeseType2", NOT_EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[]{ constraint0, constraint1, constraint3 };
        checkBetaConstraints(constraints, TripleBetaConstraints.class);
    }

    @Test
    public void testOneIndxed() {
        BetaNodeFieldConstraint constraint0 = getConstraint("cheeseType0", EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint constraint1 = getConstraint("cheeseType1", NOT_EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint constraint3 = getConstraint("cheeseType2", NOT_EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[]{ constraint0, constraint1, constraint3 };
        checkBetaConstraints(constraints, TripleBetaConstraints.class);
        constraint0 = getConstraint("cheeseType0", NOT_EQUAL, "type", Cheese.class);
        constraint1 = getConstraint("cheeseType1", EQUAL, "type", Cheese.class);
        constraint3 = getConstraint("cheeseType2", NOT_EQUAL, "type", Cheese.class);
        constraints = new BetaNodeFieldConstraint[]{ constraint0, constraint1, constraint3 };
        checkBetaConstraints(constraints, TripleBetaConstraints.class);
        constraint0 = getConstraint("cheeseType0", NOT_EQUAL, "type", Cheese.class);
        constraint1 = getConstraint("cheeseType1", NOT_EQUAL, "type", Cheese.class);
        constraint3 = getConstraint("cheeseType2", EQUAL, "type", Cheese.class);
        constraints = new BetaNodeFieldConstraint[]{ constraint0, constraint1, constraint3 };
        checkBetaConstraints(constraints, TripleBetaConstraints.class);
    }

    @Test
    public void testTwoIndxed() {
        BetaNodeFieldConstraint constraint0 = getConstraint("cheeseType0", EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint constraint1 = getConstraint("cheeseType1", EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint constraint3 = getConstraint("cheeseType2", NOT_EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[]{ constraint0, constraint1, constraint3 };
        checkBetaConstraints(constraints, TripleBetaConstraints.class);
        constraint0 = getConstraint("cheeseType0", EQUAL, "type", Cheese.class);
        constraint1 = getConstraint("cheeseType1", NOT_EQUAL, "type", Cheese.class);
        constraint3 = getConstraint("cheeseType2", EQUAL, "type", Cheese.class);
        constraints = new BetaNodeFieldConstraint[]{ constraint0, constraint1, constraint3 };
        checkBetaConstraints(constraints, TripleBetaConstraints.class);
        constraint0 = getConstraint("cheeseType0", NOT_EQUAL, "type", Cheese.class);
        constraint1 = getConstraint("cheeseType1", EQUAL, "type", Cheese.class);
        constraint3 = getConstraint("cheeseType2", EQUAL, "type", Cheese.class);
        constraints = new BetaNodeFieldConstraint[]{ constraint0, constraint1, constraint3 };
        checkBetaConstraints(constraints, TripleBetaConstraints.class);
    }

    @Test
    public void testThreeIndxed() {
        BetaNodeFieldConstraint constraint0 = getConstraint("cheeseType0", EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint constraint1 = getConstraint("cheeseType1", EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint constraint3 = getConstraint("cheeseType2", EQUAL, "type", Cheese.class);
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[]{ constraint0, constraint1, constraint3 };
        checkBetaConstraints(constraints, TripleBetaConstraints.class);
    }
}

