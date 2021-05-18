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
package org.drools.verifier.core.cache.inspectors.condition;


import org.drools.verifier.core.index.model.Field;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;


@RunWith(Parameterized.class)
public class ComparableConditionInspectorConflictResolverTest {
    private final Field field;

    private final Comparable value1;

    private final Comparable value2;

    private final String operator1;

    private final String operator2;

    private final boolean conflictExpected;

    public ComparableConditionInspectorConflictResolverTest(final String operator1, final Comparable value1, final String operator2, final Comparable value2, final boolean conflictExpected) {
        this.field = Mockito.mock(Field.class);
        this.value1 = value1;
        this.value2 = value2;
        this.operator1 = operator1;
        this.operator2 = operator2;
        this.conflictExpected = conflictExpected;
    }

    @Test
    public void parametrizedTest() {
        final ComparableConditionInspector a = getCondition(value1, operator1);
        final ComparableConditionInspector b = getCondition(value2, operator2);
        Assert.assertEquals(getAssertDescriptionConflict(a, b, conflictExpected), conflictExpected, a.conflicts(b));
        Assert.assertEquals(getAssertDescriptionConflict(a, b, conflictExpected), conflictExpected, a.conflicts(b));
        Assert.assertEquals(getAssertDescriptionOverlap(a, b, (!(conflictExpected))), (!(conflictExpected)), a.overlaps(b));
        Assert.assertEquals(getAssertDescriptionOverlap(b, a, (!(conflictExpected))), (!(conflictExpected)), b.overlaps(a));
    }
}

