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
import java.util.Collections;
import org.drools.core.datasources.CursoredDataSource;
import org.drools.core.impl.InternalRuleUnitExecutor;
import org.junit.Test;
import org.kie.api.runtime.rule.RuleUnit;
import org.mockito.Mockito;


public class RuleUnitFactoryTest {
    private RuleUnitFactory factory;

    @Test
    public void getOrCreateRuleUnitWithClass() {
        final InternalRuleUnitExecutor ruleUnitExecutor = Mockito.mock(InternalRuleUnitExecutor.class);
        final TestRuleUnit testRuleUnit = factory.getOrCreateRuleUnit(ruleUnitExecutor, TestRuleUnit.class);
        assertThat(testRuleUnit).isNotNull();
        assertThat(factory.getOrCreateRuleUnit(ruleUnitExecutor, TestRuleUnit.class)).isSameAs(testRuleUnit);
        Mockito.verifyZeroInteractions(ruleUnitExecutor);
    }

    @Test
    public void getOrCreateRuleUnitWithClassName() {
        final InternalRuleUnitExecutor ruleUnitExecutor = Mockito.mock(InternalRuleUnitExecutor.class);
        final RuleUnit testRuleUnit = factory.getOrCreateRuleUnit(ruleUnitExecutor, TestRuleUnit.class.getCanonicalName(), this.getClass().getClassLoader());
        assertThat(testRuleUnit).isNotNull().isInstanceOf(TestRuleUnit.class);
        assertThat(factory.getOrCreateRuleUnit(ruleUnitExecutor, TestRuleUnit.class)).isSameAs(testRuleUnit);
        Mockito.verifyZeroInteractions(ruleUnitExecutor);
    }

    @Test
    public void registerUnit() {
        final TestRuleUnit testRuleUnit = new TestRuleUnit(new Integer[]{  }, BigDecimal.ZERO);
        final InternalRuleUnitExecutor ruleUnitExecutor = Mockito.mock(InternalRuleUnitExecutor.class);
        assertThat(factory.registerUnit(ruleUnitExecutor, testRuleUnit)).isSameAs(testRuleUnit);
        Mockito.verifyZeroInteractions(ruleUnitExecutor);
        assertThat(factory.getOrCreateRuleUnit(ruleUnitExecutor, TestRuleUnit.class)).isSameAs(testRuleUnit);
        Mockito.verifyZeroInteractions(ruleUnitExecutor);
    }

    @Test
    public void injectUnitVariablesNoDataSourceInUnit() {
        factory.bindVariable("numberVariable", BigDecimal.ONE);
        factory.bindVariable("stringList", Collections.singletonList("test"));
        final TestRuleUnit testRuleUnit = new TestRuleUnit(new Integer[]{  }, BigDecimal.ZERO);
        testRuleUnit.getStringList().add("bla");
        final InternalRuleUnitExecutor ruleUnitExecutor = Mockito.mock(InternalRuleUnitExecutor.class);
        factory.injectUnitVariables(ruleUnitExecutor, testRuleUnit);
        // Unassigned variables or numbers equal 0 should be reassigned if a variable exists.
        assertThat(testRuleUnit.getNumber()).isEqualTo(BigDecimal.ONE);
        // Others should remain the same.
        assertThat(testRuleUnit.bound).isFalse();
        assertThat(testRuleUnit.getNumbersArray()).isNotNull().isEmpty();
        assertThat(testRuleUnit.getSimpleFactList()).isNotNull().isEmpty();
        assertThat(testRuleUnit.getStringList()).isNotNull().hasSize(1).containsExactly("bla");
        Mockito.verifyZeroInteractions(ruleUnitExecutor);
    }

    @Test
    public void injectUnitVariablesDataSourceInUnit() {
        final CursoredDataSource<Object> dataSource = Mockito.mock(CursoredDataSource.class);
        final RuleUnitWithDataSource testRuleUnit = new RuleUnitWithDataSource(dataSource);
        final InternalRuleUnitExecutor ruleUnitExecutor = Mockito.mock(InternalRuleUnitExecutor.class);
        factory.injectUnitVariables(ruleUnitExecutor, testRuleUnit);
        Mockito.verify(ruleUnitExecutor).bindDataSource(dataSource);
        Mockito.verifyNoMoreInteractions(ruleUnitExecutor);
    }
}

