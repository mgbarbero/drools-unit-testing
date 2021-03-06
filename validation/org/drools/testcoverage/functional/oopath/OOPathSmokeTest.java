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
package org.drools.testcoverage.functional.oopath;


import KieServices.Factory;
import org.assertj.core.api.Assertions;
import org.drools.testcoverage.common.model.Address;
import org.drools.testcoverage.common.model.Person;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieBaseUtil;
import org.drools.testcoverage.common.util.KieUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;


/**
 * Tests basic usage of OOPath expressions.
 */
@RunWith(Parameterized.class)
public class OOPathSmokeTest {
    private static final KieServices KIE_SERVICES = Factory.get();

    private static final ReleaseId RELEASE_ID = OOPathSmokeTest.KIE_SERVICES.newReleaseId("org.drools.testcoverage.oopath", "marshalling-test", "1.0");

    private KieSession kieSession;

    private final KieBaseTestConfiguration kieBaseTestConfiguration;

    public OOPathSmokeTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        this.kieBaseTestConfiguration = kieBaseTestConfiguration;
    }

    @Test
    public void testBuildKieBase() {
        final KieBase kieBase = KieBaseUtil.getKieBaseFromClasspathResources(this.getClass(), kieBaseTestConfiguration, "oopath.drl");
        Assertions.assertThat(kieBase).isNotNull();
    }

    @Test
    public void testBuildTwoKieBases() {
        final Resource drlResource = OOPathSmokeTest.KIE_SERVICES.getResources().newUrlResource(this.getClass().getResource("oopath.drl"));
        KieUtil.getKieModuleFromResources(OOPathSmokeTest.RELEASE_ID, KieBaseTestConfiguration.CLOUD_IDENTITY, drlResource);
        // creating two KieContainers and KieBases may trigger deep cloning
        for (int i = 0; i < 2; i++) {
            final KieContainer kieContainer = OOPathSmokeTest.KIE_SERVICES.newKieContainer(OOPathSmokeTest.RELEASE_ID);
            final KieBase kieBase = kieContainer.getKieBase();
            Assertions.assertThat(kieBase).isNotNull();
        }
    }

    @Test
    public void testFireRule() {
        final KieBase kieBase = KieBaseUtil.getKieBaseFromClasspathResources(this.getClass(), kieBaseTestConfiguration, "oopath.drl");
        this.kieSession = kieBase.newKieSession();
        final Person person = new Person("Bruno", 21);
        person.setAddress(new Address("Some Street", 10, "Beautiful City"));
        this.kieSession.insert(person);
        Assertions.assertThat(this.kieSession.fireAllRules()).isEqualTo(1);
    }
}

