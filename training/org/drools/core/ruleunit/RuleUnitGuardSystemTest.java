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
package org.drools.core.ruleunit;


import org.drools.core.impl.RuleUnitExecutorSession;
import org.drools.core.spi.Activation;
import org.junit.Test;
import org.mockito.Mockito;


public class RuleUnitGuardSystemTest {
    private RuleUnitExecutorSession executorSession;

    private RuleUnitGuardSystem guardSystem;

    @Test
    public void registerGuard() {
        assertThat(guardSystem.fireActiveUnits()).isEqualTo(0);
        // Simulating that first fire sets new guard and current rule unit.
        final TestRuleUnit testRuleUnit = new TestRuleUnit();
        guardSystem.registerGuard(testRuleUnit, prepareActivation());
        Mockito.when(executorSession.getCurrentRuleUnit()).thenReturn(testRuleUnit);
        assertThat(guardSystem.fireActiveUnits()).isEqualTo(1);
    }

    @Test
    public void removeActivation() {
        assertThat(guardSystem.fireActiveUnits()).isEqualTo(0);
        final Activation activation = prepareActivation();
        final TestRuleUnit testRuleUnit = new TestRuleUnit();
        guardSystem.registerGuard(testRuleUnit, activation);
        guardSystem.removeActivation(activation);
        assertThat(guardSystem.fireActiveUnits()).isEqualTo(0);
    }

    @Test
    public void fireActiveUnitsFromRuleUnit() {
        final TestRuleUnit testRuleUnit = new TestRuleUnit();
        Mockito.when(executorSession.getCurrentRuleUnit()).thenReturn(testRuleUnit);
        guardSystem.registerGuard(testRuleUnit, prepareActivation());
        assertThat(guardSystem.fireActiveUnits(new TestRuleUnit2())).isEqualTo(0);
        assertThat(guardSystem.fireActiveUnits(testRuleUnit)).isEqualTo(1);
    }
}

