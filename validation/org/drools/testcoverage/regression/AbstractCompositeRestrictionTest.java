/**
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.drools.testcoverage.regression;


import KieServices.Factory;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.Message;


/**
 * Bugfix test for bz#724655 'NPE in AbstractCompositionRestriction when using
 * unbound variables'
 */
@RunWith(Parameterized.class)
public class AbstractCompositeRestrictionTest {
    private final KieBaseTestConfiguration kieBaseTestConfiguration;

    public AbstractCompositeRestrictionTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        this.kieBaseTestConfiguration = kieBaseTestConfiguration;
    }

    @Test
    public void test() {
        final KieBuilder builder = KieUtil.getKieBuilderFromResources(kieBaseTestConfiguration, false, Factory.get().getResources().newClassPathResource("abstractCompositeRestrictionTest.drl", getClass()));
        final List<Message> msgs = builder.getResults().getMessages();
        final String[] lines = msgs.get(0).getText().split("\n");
        final String unable = "Unable to Analyse Expression valueType == Field.INT || valueType == Field.DOUBLE:";
        Assertions.assertThat(lines[0]).isEqualTo(unable);
    }
}

