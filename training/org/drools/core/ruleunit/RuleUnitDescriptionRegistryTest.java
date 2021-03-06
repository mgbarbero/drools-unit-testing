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


import java.math.BigDecimal;
import java.util.Optional;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.junit.Test;
import org.mockito.Mockito;


public class RuleUnitDescriptionRegistryTest {
    private RuleUnitDescriptionRegistry registry;

    @Test
    public void getDescriptionForUnit() {
        final TestRuleUnit testRuleUnit = new TestRuleUnit(new Integer[]{  }, BigDecimal.ZERO);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> registry.getDescription(testRuleUnit));
        loadDescriptionIntoRegistry(testRuleUnit.getClass());
        final RuleUnitDescription description = registry.getDescription(testRuleUnit);
        assertThat(description).isNotNull();
        assertThat(description.getRuleUnitClass()).isEqualTo(testRuleUnit.getClass());
    }

    @Test
    public void getDescriptionForUnitClassName() {
        Optional<RuleUnitDescription> description = registry.getDescription(TestRuleUnit.class.getName());
        assertThat(description).isNotPresent();
        loadDescriptionIntoRegistry(TestRuleUnit.class);
        description = registry.getDescription(TestRuleUnit.class.getName());
        assertThat(description).isPresent();
        assertThat(description.get().getRuleUnitClass()).isEqualTo(TestRuleUnit.class);
    }

    @Test
    public void getDescriptionForRuleImpl() {
        final RuleImpl ruleImpl = Mockito.mock(RuleImpl.class);
        Mockito.when(ruleImpl.getRuleUnitClassName()).thenReturn(TestRuleUnit.class.getName());
        Optional<RuleUnitDescription> description = registry.getDescription(ruleImpl);
        assertThat(description).isNotPresent();
        loadDescriptionIntoRegistry(TestRuleUnit.class);
        description = registry.getDescription(ruleImpl);
        assertThat(description).isPresent();
        assertThat(description.get().getRuleUnitClass()).isEqualTo(TestRuleUnit.class);
    }

    @Test
    public void add() {
        loadDescriptionIntoRegistry(TestRuleUnit.class);
        assertDescriptionIsLoaded(TestRuleUnit.class);
        final Optional<RuleUnitDescription> description = registry.getDescription(TestRuleUnit2.class.getName());
        assertThat(description).isNotPresent();
        loadDescriptionIntoRegistry(TestRuleUnit2.class);
        assertDescriptionIsLoaded(TestRuleUnit2.class);
        assertDescriptionIsLoaded(TestRuleUnit.class);
    }

    @Test
    public void hasUnits() {
        assertThat(registry.hasUnits()).isFalse();
        loadDescriptionIntoRegistry(TestRuleUnit.class);
        assertThat(registry.hasUnits()).isTrue();
    }
}

