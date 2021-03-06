/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.drools.testcoverage.functional;


import KieServices.Factory;
import java.io.File;
import org.assertj.core.api.Assertions;
import org.drools.testcoverage.common.util.TestConstants;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;


/**
 * Tests correct behavior of KieContainer in specific cases, not covered by other tests.
 */
public class KieContainerTest {
    private static final String DRL = "package defaultKBase;\n rule testRule when then end\n";

    private static final String SESSION_NAME = "defaultKSession";

    private static final ReleaseId RELEASE_ID = Factory.get().newReleaseId(TestConstants.PACKAGE_TESTCOVERAGE, "kie-container-test", "1.0.0");

    private KieServices kieServices;

    /**
     * Tests not disposing a KieSession created from the same KieContainer with the same name.
     */
    @Test
    public void testNotDisposingAnotherKieSession() {
        this.createKieModule(KieContainerTest.RELEASE_ID);
        final KieContainer kieContainer = kieServices.newKieContainer(KieContainerTest.RELEASE_ID);
        // get a new KieSession with specified name
        final KieSession firstKSession = kieContainer.newKieSession(KieContainerTest.SESSION_NAME);
        // get another KieSession with the same name - it should not dispose the former
        final KieSession secondKSession = kieContainer.newKieSession(KieContainerTest.SESSION_NAME);
        try {
            // session should not already be disposed
            firstKSession.fireAllRules();
        } catch (IllegalStateException e) {
            Assertions.fail("KieSession should not have been already disposed.", e);
        } finally {
            firstKSession.dispose();
            secondKSession.dispose();
        }
    }

    @Test
    public void testFileSystemResourceBuilding() {
        // DROOLS-2339
        KieServices kieServices = Factory.get();
        KieResources kieResources = kieServices.getResources();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        File drlFile = new File("src/test/resources/org/drools/testcoverage/functional/parser/drl/asterisk-imports.drl");
        kieFileSystem.write(kieResources.newFileSystemResource(drlFile, "UTF-8"));
        KieModuleModel kmodel = kieServices.newKieModuleModel();
        kieFileSystem.writeKModuleXML(kmodel.toXML());
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieContainer kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        KieBaseConfiguration kieBaseConfiguration = kieServices.newKieBaseConfiguration();
        KieBase kieBase = kieContainer.newKieBase(kieBaseConfiguration);
        Assertions.assertThat(kieBase.getKiePackages()).isNotEmpty();
    }
}

