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
package org.drools.compiler.integrationtests.incrementalcompilation;


import KieServices.Factory;
import KieSessionTestConfiguration.STATELESS_REALTIME;
import ResourceType.DRL;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.drools.core.ClassObjectFilter;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.RuleTerminalNode;
import org.drools.testcoverage.common.model.Address;
import org.drools.testcoverage.common.model.Message;
import org.drools.testcoverage.common.model.Person;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieUtil;
import org.drools.testcoverage.common.util.TestConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.Service;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message.Level.ERROR;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.Resource;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.builder.IncrementalResults;
import org.kie.internal.command.CommandFactory;


@RunWith(Parameterized.class)
public class IncrementalCompilationTest {
    private final KieBaseTestConfiguration kieBaseTestConfiguration;

    public IncrementalCompilationTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        this.kieBaseTestConfiguration = kieBaseTestConfiguration;
    }

    @Test
    public void testLoadOrderAfterRuleRemoval() {
        final String header = (("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n";
        final String drl1 = "rule R1 when\n" + (("   $m : Message( message == \"Hello World1\" )\n" + "then\n") + "end\n");
        final String drl2 = "rule R2 when\n" + (("   $m : Message( message == \"Hello World2\" )\n" + "then\n") + "end\n");
        final String drl3 = "rule R3 when\n" + (("   $m : Message( message == \"Hello World3\" )\n" + "then\n") + "end\n");
        final String drl4 = "rule R4 when\n" + (("   $m : Message( message == \"Hello World4\" )\n" + "then\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, header);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        createAndDeployAndTest(kc, "2", header, ((drl1 + drl2) + drl3), "R1", "R2", "R3");
        createAndDeployAndTest(kc, "3", header, (drl1 + drl3), "R1", "R3");
        createAndDeployAndTest(kc, "4", header, ((drl2 + drl1) + drl4), "R2", "R1", "R4");
        createAndDeployAndTest(kc, "5", header, (drl2 + drl1), "R2", "R1");
        createAndDeployAndTest(kc, "6", header, "");
        createAndDeployAndTest(kc, "7", header, drl3, "R3");
    }

    @Test
    public void testKJarUpgrade() {
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( message == \"Hi Universe\" )\n") + "then\n") + "end\n";
        final String drl2_2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1, drl2_1);
        // Create a session and fire rules
        final KieContainer kc = ks.newKieContainer(releaseId1);
        KieSession ksession = kc.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
        ksession.dispose();
        // Create a new jar for version 1.1.0
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1, drl2_2);
        // try to update the container to version 1.1.0
        kc.updateToVersion(releaseId2);
        // create and use a new session
        ksession = kc.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(2, ksession.fireAllRules());
    }

    @Test
    public void testKJarUpgradeSameSession() {
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( message == \"Hi Universe\" )\n") + "then\n") + "end\n";
        final String drl2_2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1, drl2_1);
        // Create a session and fire rules
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
        // Create a new jar for version 1.1.0
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1, drl2_2);
        // try to update the container to version 1.1.0
        kc.updateToVersion(releaseId2);
        // continue working with the session
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(3, ksession.fireAllRules());
    }

    @Test
    public void testDeletedFile() {
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1, drl2);
        final KieContainer kieContainer = ks.newKieContainer(releaseId1);
        final KieContainer kieContainer2 = ks.newKieContainer(releaseId1);
        KieSession ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(2, ksession.fireAllRules());
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-delete", "1.0.1");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, ((String) (null)), drl2);
        kieContainer.updateToVersion(releaseId2);
        // test with the old ksession ...
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
        // ... and with a brand new one
        ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
        // check that the second kieContainer hasn't been affected by the update of the first one
        final KieSession ksession2 = kieContainer2.newKieSession();
        ksession2.insert(new Message("Hello World"));
        Assert.assertEquals(2, ksession2.fireAllRules());
    }

    @Test
    public void testIncrementalCompilationWithAddedError() {
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( message == \"Hi Universe\" )\n") + "then\n") + "end\n";
        final String drl2_2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( nonExistentField == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1).write("src/main/resources/r2.drl", drl2_1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        final KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        KieSession ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
        kfs.write("src/main/resources/r2.drl", drl2_2);
        final IncrementalResults results = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(1, results.getAddedMessages().size());
        Assert.assertEquals(0, results.getRemovedMessages().size());
        kieContainer.updateToVersion(ks.getRepository().getDefaultReleaseId());
        ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testIncrementalCompilationWithRemovedError() {
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( nonExistentField == \"Hello World\" )\n") + "then\n") + "end\n";
        final String drl2_2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1).write("src/main/resources/r2.drl", drl2_1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(1, kieBuilder.getResults().getMessages(ERROR).size());
        kfs.write("src/main/resources/r2.drl", drl2_2);
        final IncrementalResults results = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(0, results.getAddedMessages().size());
        Assert.assertEquals(1, results.getRemovedMessages().size());
        final KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        final KieSession ksession = kieContainer.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(2, ksession.fireAllRules());
    }

    @Test
    public void testIncrementalCompilationAddErrorThenRemoveError() {
        // Valid
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        // Field is unknown ("nonExistentField" not "message")
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( nonExistentField == \"Hello World\" )\n") + "then\n") + "end\n";
        // Valid
        final String drl2_2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(0, kieBuilder.getResults().getMessages(ERROR).size());
        // Add file with error - expect 1 "added" error message
        kfs.write("src/main/resources/r2.drl", drl2_1);
        final IncrementalResults addResults = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(1, addResults.getAddedMessages().size());
        Assert.assertEquals(0, addResults.getRemovedMessages().size());
        // Update flawed file with correct version - expect 0 "added" error messages and removal of 1 previous error
        kfs.write("src/main/resources/r2.drl", drl2_2);
        final IncrementalResults removeResults = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(0, removeResults.getAddedMessages().size());
        Assert.assertEquals(1, removeResults.getRemovedMessages().size());
    }

    @Test
    public void testIncrementalCompilationAddErrorThenRemoveIt() {
        // Fact Type is unknown ("NonExistentClass" not "Message")
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : NonExistentClass()\n") + "then\n") + "end\n";
        // Field is unknown ("nonExistentField" not "message")
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( nonExistentField == \"Hello World\" )\n") + "then\n") + "end\n";
        // Valid
        final String drl2_2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1);
        // Initial file contains errors
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(1, kieBuilder.getResults().getMessages(ERROR).size());
        // Add file with error - expect 1 "added" error message
        kfs.write("src/main/resources/r2.drl", drl2_1);
        final IncrementalResults addResults = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(1, addResults.getAddedMessages().size());
        Assert.assertEquals(0, addResults.getRemovedMessages().size());
        // Update flawed file with correct version - expect 0 "added" error messages and removal of 1 previous error relating to updated file
        kfs.write("src/main/resources/r2.drl", drl2_2);
        final IncrementalResults removeResults = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(0, removeResults.getAddedMessages().size());
        Assert.assertEquals(1, removeResults.getRemovedMessages().size());
    }

    @Test
    public void testIncrementalCompilationWithDuplicatedRule() {
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(0, kieBuilder.getResults().getMessages(ERROR).size());
        kfs.write("src/main/resources/r2_1.drl", drl2);
        final IncrementalResults addResults = createFileSet("src/main/resources/r2_1.drl").build();
        Assert.assertEquals(0, addResults.getAddedMessages().size());
        Assert.assertEquals(0, addResults.getRemovedMessages().size());
        kfs.write("src/main/resources/r2_2.drl", drl2);
        final IncrementalResults removeResults = createFileSet("src/main/resources/r2_2.drl").build();
        Assert.assertEquals(1, removeResults.getAddedMessages().size());
        Assert.assertEquals(0, removeResults.getRemovedMessages().size());
    }

    @Test
    public void testIncrementalCompilationWithDuplicatedRuleInSameDRL() {
        final String drl1 = (((((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n") + "rule R1 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertFalse(kieBuilder.getResults().getMessages(ERROR).isEmpty());
    }

    @Test
    public void testIncrementalCompilationAddErrorBuildAllMessages() {
        // Valid
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        // Field is unknown ("nonExistentField" not "message")
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( nonExistentField == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(0, kieBuilder.getResults().getMessages(ERROR).size());
        // Add file with error - expect 1 "added" error message
        kfs.write("src/main/resources/r2.drl", drl2_1);
        final IncrementalResults addResults = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(1, addResults.getAddedMessages().size());
        Assert.assertEquals(0, addResults.getRemovedMessages().size());
        // Check errors on a full build
        Assert.assertEquals(1, ks.newKieBuilder(kfs).buildAll().getResults().getMessages().size());
    }

    @Test
    public void testIncrementalCompilationAddErrorThenEmptyWithoutError() {
        // BZ-1009369
        // Invalid. Type "Smurf" is unknown
        final String drl1 = "Smurf";
        // Valid
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        // Add file with error - expect 2 build messages
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(2, kieBuilder.getResults().getMessages(ERROR).size());
        // Add empty file - expect no "added" messages and no "removed" messages
        kfs.write("src/main/resources/r2.drl", "");
        final IncrementalResults addResults1 = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(0, addResults1.getAddedMessages().size());
        Assert.assertEquals(0, addResults1.getRemovedMessages().size());
        // Update file with no errors - expect no "added" messages and no "removed" messages
        kfs.write("src/main/resources/r2.drl", drl2);
        final IncrementalResults addResults2 = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(0, addResults2.getAddedMessages().size());
        Assert.assertEquals(0, addResults2.getRemovedMessages().size());
    }

    @Test
    public void testRuleRemoval() {
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2 = "rule R2 when\n" + (("   $m : Message( message == \"Hi Universe\" )\n" + "then\n") + "end\n");
        final String drl3 = "rule R3 when\n" + (("   $m : Message( message == \"Hello World\" )\n" + "then\n") + "end\n");
        final KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, ((drl1 + drl2) + drl3));
        // Create a session and fire rules
        final KieContainer kc = ks.newKieContainer(releaseId1);
        KiePackage kpkg = kc.getKieBase().getKiePackage("org.drools.compiler");
        Assert.assertEquals(3, kpkg.getRules().size());
        Map<String, Rule> rules = rulestoMap(kpkg.getRules());
        Assert.assertNotNull(rules.get("R1"));
        Assert.assertNotNull(rules.get("R2"));
        Assert.assertNotNull(rules.get("R3"));
        final RuleTerminalNode rtn1_1 = ((RuleTerminalNode) (getReteooBuilder().getTerminalNodes("org.drools.compiler.R1")[0]));
        final RuleTerminalNode rtn3_1 = ((RuleTerminalNode) (getReteooBuilder().getTerminalNodes("org.drools.compiler.R3")[0]));
        // Create a new jar for version 1.1.0
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, (drl1 + drl3));
        // try to update the container to version 1.1.0
        kc.updateToVersion(releaseId2);
        final KnowledgeBaseImpl rb_2 = ((KnowledgeBaseImpl) (kc.getKieBase()));
        final RuleTerminalNode rtn1_2 = ((RuleTerminalNode) (rb_2.getReteooBuilder().getTerminalNodes("org.drools.compiler.R1")[0]));
        final RuleTerminalNode rtn3_2 = ((RuleTerminalNode) (rb_2.getReteooBuilder().getTerminalNodes("org.drools.compiler.R3")[0]));
        Assert.assertNull(rb_2.getReteooBuilder().getTerminalNodes("org.drools.compiler.R2"));
        Assert.assertSame(rtn3_1, rtn3_2);
        Assert.assertSame(rtn1_1, rtn1_2);
        kpkg = kc.getKieBase().getKiePackage("org.drools.compiler");
        Assert.assertEquals(2, kpkg.getRules().size());
        rules = rulestoMap(kpkg.getRules());
        Assert.assertNotNull(rules.get("R1"));
        Assert.assertNull(rules.get("R2"));
        Assert.assertNotNull(rules.get("R3"));
    }

    @Test
    public void testIncrementalCompilationWithSnapshots() {
        // DROOLS-358
        final ReleaseId releaseId = Factory.get().newReleaseId("org.test", "test", "1.0.0");
        testIncrementalCompilation(releaseId, releaseId, false);
    }

    @Test
    public void testIncrementalCompilationWithFixedVersions() {
        // DROOLS-358
        final ReleaseId releaseId1 = Factory.get().newReleaseId("org.test", "test", "1.0.1");
        final ReleaseId releaseId2 = Factory.get().newReleaseId("org.test", "test", "1.0.2");
        testIncrementalCompilation(releaseId1, releaseId2, false);
    }

    @Test
    public void testIncrementalCompilationWithDeclaredType() {
        // DROOLS-358
        final ReleaseId releaseId1 = Factory.get().newReleaseId("org.test", "test", "1.0.1");
        final ReleaseId releaseId2 = Factory.get().newReleaseId("org.test", "test", "1.0.2");
        testIncrementalCompilation(releaseId1, releaseId2, true);
    }

    @Test
    public void testIncrementalCompilationWithRedeclares() {
        // DROOLS-363
        final String drl1 = "package org.drools.compiler\n" + (((((("global java.util.List list\n" + "") + "declare Fooz id : int end \n") + "") + "rule R0 when then insert( new Fooz( 1 ) ); end \n") + "") + "");
        final String drl2 = "package org.drools.compiler\n" + (((((((((((((("global java.util.List list\n" + "") + "declare Fooz id : int end \n") + "") + "declare Barz end \n") + "") + "rule R2 when then insert( new Fooz( 2 ) ); end \n") + "") + "rule R1 when\n") + " $f : Fooz() ") + "then\n") + " list.add( $f.getId() );") + " System.out.println( \"Foo in \" + $f + \" >> \" + System.identityHashCode( $f.getClass() ) ); \n") + "end\n") + "");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        final List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.fireAllRules();
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1, drl2);
        final Results updateResults = kc.updateToVersion(releaseId2);
        Assert.assertEquals(0, updateResults.getMessages().size());
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testIncrementalCompilationWithAmbiguousRedeclares() {
        final String drl1 = "package domestic; " + ((("import foreign.*; " + "declare foreign.Score ") + "    id       : String ") + "end ");
        final String drl2 = "\n" + (((((((("package domestic; " + "import foreign.*; ") + "declare foreign.Score ") + "    id       : String ") + "end\n") + "declare Score ") + "    value : double ") + "end ") + "");
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        final ReleaseId id = ks.newReleaseId("org.test", "foo", "1.0-SNAPSHOT");
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kfs.generateAndWritePomXML(id);
        kfs.write(ks.getResources().newReaderResource(new StringReader(drl1)).setResourceType(DRL).setSourcePath("drl1.drl"));
        kieBuilder.buildAll();
        final KieContainer kc = ks.newKieContainer(id);
        final KieSession ksession = kc.newKieSession();
        ksession.fireAllRules();
        kfs.write(ks.getResources().newReaderResource(new StringReader(drl2)).setResourceType(DRL).setSourcePath("drl2.drl"));
        final IncrementalResults results = incrementalBuild();
        System.out.println(results.getAddedMessages());
        Assert.assertEquals(0, results.getAddedMessages().size());
        final Results updateResults = kc.updateToVersion(id);
        Assert.assertEquals(0, updateResults.getMessages().size());
    }

    @Test
    public void testIncrementalCompilationWithModuleOverride() {
        final String drl1 = "package org.test.compiler; " + (("global java.util.List list; " + "rule A when $s : String() then System.out.println( 'AAA' + $s ); list.add( 'A' + $s ); end ") + "");
        final String drl2 = "package totally.unrelated.pack; " + (("global java.util.List list; " + "rule B when $s : String() then System.out.println( 'BBB' + $s ); list.add( 'B' + $s ); end ") + "");
        final String drl3 = "package totally.unrelated.pack; " + (("global java.util.List list; " + "rule C when $s : String() then System.out.println( 'CCC' + $s ); list.add( 'C' + $s ); end ") + "");
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        final ReleaseId id = ks.newReleaseId("org.test", "foo", "1.0-SNAPSHOT");
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kfs.generateAndWritePomXML(id);
        kfs.write(ks.getResources().newReaderResource(new StringReader(drl1)).setResourceType(DRL).setSourcePath("drl1.drl"));
        kieBuilder.buildAll();
        final KieContainer kc = ks.newKieContainer(id);
        final KieSession ksession = kc.newKieSession();
        final List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert("X");
        ksession.fireAllRules();
        Assert.assertTrue(list.contains("AX"));
        final KieFileSystem kfs2 = ks.newKieFileSystem();
        final KieBuilder kieBuilder2 = ks.newKieBuilder(kfs2);
        kfs2.generateAndWritePomXML(id);
        kfs2.write(ks.getResources().newReaderResource(new StringReader(drl2)).setResourceType(DRL).setSourcePath("drla.drl"));
        kieBuilder2.buildAll();
        final KieContainer kc2 = ks.newKieContainer(id);
        final KieSession ksession2 = kc2.newKieSession();
        ksession2.setGlobal("list", list);
        ksession2.insert("X");
        ksession2.fireAllRules();
        kfs2.write(ks.getResources().newReaderResource(new StringReader(drl3)).setResourceType(DRL).setSourcePath("drlb.drl"));
        final IncrementalResults results = incrementalBuild();
        Assert.assertEquals(0, results.getAddedMessages().size());
        kc2.updateToVersion(id);
        ksession2.fireAllRules();
        Assert.assertEquals(Arrays.asList("AX", "BX", "CX"), list);
    }

    @Test
    public void testIncrementalCompilationWithMissingKSession() {
        // https://bugzilla.redhat.com/show_bug.cgi?id=1066059
        final String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + (((((((("<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n") + "  <modelVersion>4.0.0</modelVersion>\n") + "  <groupId>org.kie</groupId>\n") + "  <artifactId>test</artifactId>\n") + "  <version>1.0</version>\n") + "  <packaging>jar</packaging>\n") + "  <name>test</name>\n") + "</project>");
        final String kmodule = "<kmodule xmlns=\"http://www.drools.org/xsd/kmodule\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + ("<kbase name=\"kbase\" includes=\"nonExistent\"/>\n" + "</kmodule>");
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( message == \"Hi Universe\" )\n") + "then\n") + "end\n";
        final String drl2_2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_2 when\n") + "   $m : Message( nonExistentField == \"Hello World\" )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("pom.xml", pom).write("src/main/resources/META-INF/kmodule.xml", kmodule).write("src/main/resources/r2.drl", drl2_1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        kfs.write("src/main/resources/r2.drl", drl2_2);
        final IncrementalResults results = createFileSet("src/main/resources/r2.drl").build();
        // since there's a missing include tha kiebase is not built at all
        Assert.assertEquals(0, results.getAddedMessages().size());
        Assert.assertEquals(0, results.getRemovedMessages().size());
    }

    @Test
    public void testIncrementalCompilationWithIncludes() {
        // DROOLS-462
        final String drl1 = "global java.util.List list\n" + (((("rule R1 when\n" + " $s : String() ") + "then\n") + " list.add( \"a\" + $s );") + "end\n");
        final String drl2 = "global java.util.List list\n" + (((("rule R1 when\n" + " $s : String() ") + "then\n") + " list.add( \"b\" + $s );") + "end\n");
        final ReleaseId releaseId = Factory.get().newReleaseId("org.test", "test", "1.0.0");
        final KieServices ks = Factory.get();
        final KieModuleModel kproj = ks.newKieModuleModel();
        final KieBaseModel kieBaseModel1 = kproj.newKieBaseModel("KBase1").addPackage("org.pkg1");
        kieBaseModel1.newKieSessionModel("KSession1");
        final KieBaseModel kieBaseModel2 = kproj.newKieBaseModel("KBase2").addPackage("org.pkg2").addInclude("KBase1");
        kieBaseModel2.newKieSessionModel("KSession2");
        final KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(releaseId).write("src/main/resources/KBase1/org/pkg1/r1.drl", drl1).writeKModuleXML(kproj.toXML());
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kieBuilder.buildAll();
        Assert.assertEquals(0, kieBuilder.getResults().getMessages().size());
        final KieContainer kc = ks.newKieContainer(releaseId);
        final KieSession ksession = kc.newKieSession("KSession2");
        final List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert("Foo");
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("aFoo", list.get(0));
        list.clear();
        kfs.delete("src/main/resources/KBase1/org/pkg1/r1.drl");
        kfs.write("src/main/resources/KBase1/org/pkg1/r2.drl", drl2);
        final IncrementalResults results = incrementalBuild();
        Assert.assertEquals(0, results.getAddedMessages().size());
        final Results updateResults = kc.updateToVersion(releaseId);
        Assert.assertEquals(0, updateResults.getMessages().size());
        ksession.insert("Bar");
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.containsAll(Arrays.asList("bBar", "bFoo")));
    }

    @Test
    public void testIncrementalCompilationWithInvalidDRL() {
        final String drl1 = "Smurf";
        final String drl2_1 = "package org.drools.compiler\n" + (((("rule R2\n" + "when\n") + "   $m : NonExistentClass()\n") + "then\n") + "end\n");
        final String drl2_2 = ((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2\n") + "when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        // First file contains errors
        kfs.write("src/main/resources/r1.drl", drl1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        final Results results1 = kieBuilder.getResults();
        Assert.assertEquals(2, results1.getMessages().size());
        // Second file also contains errors.. expect some added messages
        kfs.write("src/main/resources/r2.drl", drl2_1);
        final IncrementalResults results2 = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(1, results2.getAddedMessages().size());
        Assert.assertEquals(0, results2.getRemovedMessages().size());
        // Correct second file... expect original errors relating to the file to be removed
        kfs.write("src/main/resources/r2.drl", drl2_2);
        final IncrementalResults results3 = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(0, results3.getAddedMessages().size());
        Assert.assertEquals(1, results3.getRemovedMessages().size());
        // Remove first file... expect related errors to be removed
        kfs.delete("src/main/resources/r1.drl");
        final IncrementalResults results4 = createFileSet("src/main/resources/r1.drl").build();
        Assert.assertEquals(0, results4.getAddedMessages().size());
        Assert.assertEquals(2, results4.getRemovedMessages().size());
    }

    @Test
    public void testKJarUpgradeSameSessionAddingGlobal() {
        // DROOLS-523
        final String drl1 = ((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "global java.lang.String foo\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2_1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2_1 when\n") + "   $m : Message( message == \"Hi Universe\" )\n") + "then\n") + "end\n";
        final String drl2_2 = ((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "global java.lang.String foo\n") + "rule R2_2 when\n") + "   $m : Message( message == foo )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1, drl2_1);
        // Create a session and fire rules
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
        // Create a new jar for version 1.1.0
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1, drl2_2);
        // try to update the container to version 1.1.0
        kc.updateToVersion(releaseId2);
        ksession.setGlobal("foo", "Hello World");
        // continue working with the session
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(2, ksession.fireAllRules());
    }

    @Test
    public void testKJarUpgradeWithDSL() {
        // DROOLS-718
        final String dsl = "[when][]There is a Message=Message()\n" + (("[when][]-with message \"{factId}\"=message==\"{factId}\"\n" + "\n") + "[then][]Print \"{message}\"=System.out.println(\"{message}\");\n");
        final String drl2_1 = ((((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule \"bla\"\n") + "when\n") + "\tThere is a Message\t   \n") + "\t-with message \"Hi Universe\"\n") + "then\n") + "\tPrint \"Found a Message Hi Universe.\"\n") + "end\n";
        final String drl2_2 = ((((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule \"bla\"\n") + "when\n") + "\tThere is a Message\t   \n") + "\t-with message \"Hello World\"\n") + "then\n") + "\tPrint \"Found a Message Hello World.\"\n") + "end\n";
        final KieServices ks = Factory.get();
        final Resource dslResource = KieUtil.getResource(dsl, ((TestConstants.TEST_RESOURCES_FOLDER) + "rulesDsl.dsl"));
        final Resource drlResource2_1 = KieUtil.getResource(drl2_1, ((TestConstants.TEST_RESOURCES_FOLDER) + "rules1.rdslr"));
        final Resource drlResource2_2 = KieUtil.getResource(drl2_2, ((TestConstants.TEST_RESOURCES_FOLDER) + "rules2.rdslr"));
        // Create an in-memory jar for version 1.0.0
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromResources(releaseId1, kieBaseTestConfiguration, dslResource, drlResource2_1);
        // Create a session and fire rules
        final KieContainer kc = ks.newKieContainer(releaseId1);
        KieSession ksession = kc.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(0, ksession.fireAllRules());
        ksession.dispose();
        // Create a new jar for version 1.1.0
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromResources(releaseId2, kieBaseTestConfiguration, dslResource, drlResource2_2);
        // try to update the container to version 1.1.0
        kc.updateToVersion(releaseId2);
        // create and use a new session
        ksession = kc.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testKJarUpgradeSameSessionRemovingGlobal() {
        // DROOLS-752
        final String drl1 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "global java.lang.String foo\n") + "global java.lang.String bar\n") + "rule R1 when\n") + "   $m : Message()\n") + "then\n") + "end\n";
        final String drl2 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "global java.lang.String foo\n") + "global java.lang.String baz\n") + "rule R2 when\n") + "   $m : Message( )\n") + "then\n") + "end\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.setGlobal("foo", "foo");
        ksession.setGlobal("bar", "bar");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
        ksession.setGlobal("baz", "baz");
        final Globals globals = ksession.getGlobals();
        Assert.assertEquals(2, globals.getGlobalKeys().size());
        Assert.assertEquals("foo", ksession.getGlobal("foo"));
        Assert.assertNull(ksession.getGlobal("bar"));
        Assert.assertEquals("baz", ksession.getGlobal("baz"));
    }

    @Test
    public void testUpdateVersionWithKSessionLogger() {
        // DROOLS-790
        final String drl1 = "import java.util.List\n" + (((((((("import java.util.ArrayList\n" + "\n") + "rule \"Test1\"\n") + "\n") + "when\n") + "   $a : Integer()\n") + "then\n") + "   insert(new ArrayList());\n") + "end\n");
        final String drl2 = "rule \"Test2\"\n" + (((("when\n" + "   $b : List()\n") + " then\n") + "   $b.isEmpty();\n") + "end");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, STATELESS_REALTIME, new HashMap(), drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final StatelessKieSession statelessKieSession = kc.newStatelessKieSession();
        final KieRuntimeLogger kieRuntimeLogger = ks.getLoggers().newConsoleLogger(statelessKieSession);
        final List<Command> cmds = new ArrayList<>();
        cmds.add(CommandFactory.newInsertElements(new ArrayList()));
        final FireAllRulesCommand fireAllRulesCommand = ((FireAllRulesCommand) (CommandFactory.newFireAllRules()));
        cmds.add(fireAllRulesCommand);
        cmds.add(CommandFactory.newGetObjects("returnedObjects"));
        final BatchExecutionCommand batchExecutionCommand = CommandFactory.newBatchExecution(cmds);
        statelessKieSession.execute(batchExecutionCommand);
        kieRuntimeLogger.close();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, STATELESS_REALTIME, new HashMap(), (drl1 + drl2));
        kc.updateToVersion(releaseId2);
    }

    @Test
    public void testChangeParentRule() {
        final String drl1 = "global java.util.List list;" + ((((((((("rule B extends A when\n" + "    $s : String()\n") + "then\n") + "    list.add( $s );\n") + "end\n") + "\n") + "rule A when\n") + "    $i : Integer( this > 3 )\n") + "then\n") + "end");
        final String drl2 = "global java.util.List list;" + ((((((((("rule B extends A when\n" + "    $s : String()\n") + "then\n") + "    list.add( $s );\n") + "end\n") + "\n") + "rule A when\n") + "    $i : Integer( this > 2 )\n") + "then\n") + "end");
        final String drl3 = "global java.util.List list;" + ((((((((("rule B extends A when\n" + "    $s : String()\n") + "then\n") + "    list.add( $s );\n") + "end\n") + "\n") + "rule A when\n") + "    $i : Integer( this > 5 )\n") + "then\n") + "end");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        final List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert(4);
        ksession.insert("test");
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        list.clear();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        list.clear();
        final ReleaseId releaseId3 = ks.newReleaseId("org.kie", "test-upgrade", "1.2.0");
        KieUtil.getKieModuleFromDrls(releaseId3, kieBaseTestConfiguration, drl3);
        kc.updateToVersion(releaseId3);
        ksession.fireAllRules();
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testRuleRemovalAfterUpdate() {
        // DROOLS-801
        final String drl = "rule Rule1\n" + ((((((((((((("  when\n" + "    Integer()\n") + "    String()\n") + "    Long()\n") + "    not (Double())\n") + "  then \n") + "end\n") + "\n") + "rule Rule2\n") + "  when\n") + "    Integer()\n") + "    String()\n") + "  then \n") + "end");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.insert("test");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl);
        kc.updateToVersion(releaseId2);
        final FactHandle handle = ksession.insert(1);
        ksession.fireAllRules();
        ksession.update(handle, 1);
        ksession.fireAllRules();
        final ReleaseId releaseId3 = ks.newReleaseId("org.kie", "test-upgrade", "1.2.0");
        KieUtil.getKieModuleFromDrls(releaseId3, kieBaseTestConfiguration);
        kc.updateToVersion(releaseId3);
    }

    @Test
    public void testIncrementalTypeDeclarationOnInterface() {
        // DROOLS-861
        final String drl1 = ((((("import " + (Service.class.getCanonicalName())) + "\n") + "rule A when\n") + "    Service( )\n") + "then\n") + "end";
        final String drl2 = (((((("import " + (Service.class.getCanonicalName())) + "\n") + "declare Service @role( event ) end\n") + "rule A when\n") + "    Service( )\n") + "then\n") + "end";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        kc.newKieSession();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
    }

    @Test
    public void testNonHashablePropertyWithIncrementalCompilation() {
        // DROOLS-870
        final String drl1 = "rule \"HelloGreetingService\"\n" + (((((((((("    when\n" + "        $name : String(this == \"first\")\n") + "    then\n") + "        System.out.println(String.format(\"Hello %s!\", $name));\n") + "end\n") + "rule \"CiaoGreetingService\"\n") + "    when\n") + "        $name : String(this == \"second\")\n") + "    then\n") + "        System.out.println(String.format(\"Ciao %s!\", $name));\n") + "end\n");
        final String drl2 = "rule \"HelloGreetingService\"\n" + (((((((((("    when\n" + "        $name : String(this == \"first\")\n") + "    then\n") + "        System.out.println(String.format(\"Modified Hello %s!\", $name));\n") + "end\n") + "rule \"CiaoGreetingService\"\n") + "    when\n") + "        $name : String(this == \"second\")\n") + "    then\n") + "        System.out.println(String.format(\"Modified Ciao %s!\", $name));\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        kc.newKieSession();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
    }

    @Test
    public void testConcurrentKJarDeployment() throws Exception {
        // DROOLS-923
        final int parallelThreads = 10;
        final ExecutorService executor = Executors.newFixedThreadPool(parallelThreads);
        try {
            final CompletionService<Boolean> ecs = new ExecutorCompletionService<>(executor);
            for (final Callable<Boolean> s : IncrementalCompilationTest.Deployer.getDeployer(parallelThreads, kieBaseTestConfiguration)) {
                ecs.submit(s);
            }
            for (int i = 0; i < parallelThreads; ++i) {
                Assert.assertTrue(ecs.take().get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    public static class Deployer implements Callable<Boolean> {
        private static final KieServices ks = Factory.get();

        private final int i;

        private final KieBaseTestConfiguration kieBaseTestConfiguration;

        public Deployer(final int i, final KieBaseTestConfiguration kieBaseTestConfiguration) {
            this.i = i;
            this.kieBaseTestConfiguration = kieBaseTestConfiguration;
        }

        public Boolean call() {
            final String drl = (((("rule R when\n" + "   Integer( this == ") + (i)) + " )\n") + "then\n") + "end\n";
            final ReleaseId releaseId1 = IncrementalCompilationTest.Deployer.ks.newReleaseId("org.kie", ("test-" + (i)), "1.0.0");
            try {
                for (int i = 0; i < 10; i++) {
                    KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl);
                    IncrementalCompilationTest.Deployer.ks.getRepository().removeKieModule(releaseId1);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public static Collection<Callable<Boolean>> getDeployer(final int nr, final KieBaseTestConfiguration kieBaseTestConfiguration) {
            final Collection<Callable<Boolean>> solvers = new ArrayList<>();
            for (int i = 0; i < nr; ++i) {
                solvers.add(new IncrementalCompilationTest.Deployer(i, kieBaseTestConfiguration));
            }
            return solvers;
        }
    }

    @Test
    public void testSegmentSplitOnIncrementalCompilation() {
        // DROOLS-930
        final String drl = (((((((((((((((((((("import " + (Person.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "rule R1 when\n") + "  $s : String()") + "  Person( name == $s ) \n") + "then\n") + "  list.add(\"R1\");\n") + "end\n") + "rule R2 when\n") + "  $s : String()") + "  Person( name == $s ) \n") + "then\n") + "  list.add(\"R2\");\n") + "end\n") + "rule R3 when\n") + "  $s : String()") + "  Person( name != $s ) \n") + "then\n") + "  list.add(\"R3\");\n") + "end\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        kc.updateToVersion(releaseId1);
        ksession.insert(new Person("John", 26));
        ksession.insert("John");
        ksession.fireAllRules();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl);
        kc.updateToVersion(releaseId2);
        final List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.containsAll(Arrays.asList("R1", "R2")));
    }

    @Test
    public void testSegmentMergeOnRuleRemovalWithNotExistingSegment() {
        // DROOLS-950
        final String drl1 = "rule R1 when\n" + (((((((("    $i : Integer()\n" + "    $s : String( length == $i )\n") + "then\n") + "end\n") + "rule R2 when\n") + "    $i : Integer()\n") + "    $l : Long( this > $i )\n") + "then\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        kc.updateToVersion(releaseId1);
        ksession.insert("Test");
        ksession.insert(4L);
        ksession.fireAllRules();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration);
        kc.updateToVersion(releaseId2);
    }

    @Test
    public void testRemoveRuleWithRia() {
        // DROOLS-954
        final String drl1 = ((((((("import " + (List.class.getCanonicalName())) + "\n") + "rule R when\n") + "    $list : List()\n") + "    exists Integer( this == 1 ) from $list\n") + "    exists Integer( this == 2 ) from $list\n") + "then \n") + "end\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration);
        kc.updateToVersion(releaseId2);
        final Rete rete = getRete();
        final EntryPointNode entryPointNode = rete.getEntryPointNodes().values().iterator().next();
        for (final ObjectTypeNode otns : entryPointNode.getObjectTypeNodes().values()) {
            Assert.assertEquals(0, otns.getObjectSinkPropagator().getSinks().length);
        }
    }

    @Test
    public void testRetractLogicalAssertedObjectOnRuleRemoval() {
        // DROOLS-951
        final String drl1 = "rule R1 when\n" + ((("    exists( Integer() )\n" + "then\n") + "    insertLogical( \"found1\" );") + "end\n");
        final String drl2 = "package org.drools.compiler\n" + (((("rule R2 when\n" + "    exists( Integer() )\n") + "then\n") + "    insertLogical( \"found2\" );") + "end\n");
        final String drl3 = "package org.drools.compiler\n" + (((("rule R3 when\n" + "    exists( Integer() )\n") + "then\n") + "    insertLogical( \"found3\");") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1, drl2, drl3);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.insert(4);
        ksession.fireAllRules();
        Assert.assertEquals(3, ksession.getObjects(new ClassObjectFilter(String.class)).size());
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1, drl2);
        kc.updateToVersion(releaseId2);
        ksession.fireAllRules();
        Assert.assertEquals(2, ksession.getObjects(new ClassObjectFilter(String.class)).size());
        final ReleaseId releaseId3 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.3");
        KieUtil.getKieModuleFromDrls(releaseId3, kieBaseTestConfiguration, drl1);
        kc.updateToVersion(releaseId3);
        ksession.fireAllRules();
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(String.class)).size());
        final ReleaseId releaseId4 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.4");
        KieUtil.getKieModuleFromDrls(releaseId4, kieBaseTestConfiguration);
        kc.updateToVersion(releaseId4);
        ksession.fireAllRules();
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(String.class)).size());
    }

    @Test
    public void testRetractLogicalAssertedObjectOnRuleRemovalWithSameObject() {
        // DROOLS-951
        final String drl1 = "rule R1 when\n" + ((("    exists( Integer() )\n" + "then\n") + "    insertLogical( \"found\" );") + "end\n");
        final String drl2 = "package org.drools.compiler\n" + (((("rule R2 when\n" + "    exists( Integer() )\n") + "then\n") + "    insertLogical( \"found\" );") + "end\n");
        final String drl3 = "package org.drools.compiler\n" + (((("rule R3 when\n" + "    exists( Integer() )\n") + "then\n") + "    insertLogical( \"found\");") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1, drl2, drl3);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.insert(4);
        ksession.fireAllRules();
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(String.class)).size());
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1, drl2);
        kc.updateToVersion(releaseId2);
        ksession.fireAllRules();
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(String.class)).size());
        final ReleaseId releaseId3 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.3");
        KieUtil.getKieModuleFromDrls(releaseId3, kieBaseTestConfiguration, drl1);
        kc.updateToVersion(releaseId3);
        ksession.fireAllRules();
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(String.class)).size());
        final ReleaseId releaseId4 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.4");
        KieUtil.getKieModuleFromDrls(releaseId4, kieBaseTestConfiguration);
        kc.updateToVersion(releaseId4);
        ksession.fireAllRules();
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(String.class)).size());
    }

    @Test
    public void testUpdateWithNewDrlAndChangeInOldOne() {
        // BZ-1275378
        String drl1 = "package org.kie.test\n" + ((((("global java.util.List list\n" + "rule rule1\n") + "when\n") + "then\n") + "list.add( drools.getRule().getName() );\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains("rule1"));
        drl1 = "package org.kie.test\n" + (((((("global java.util.List list\n" + "rule rule1\n") + "when\n") + "Object()\n") + "then\n") + "list.add( drools.getRule().getName() );\n") + "end\n");
        final String drl2 = "package org.kie.test\n" + ((((("global java.util.List list\n" + "rule rule2\n") + "when\n") + "then\n") + "list.add( drools.getRule().getName() );\n") + "end\n");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1, drl2);
        kc.updateToVersion(releaseId2);
        list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains("rule2"));
    }

    @Test
    public void testIncrementalCompilationWithEagerRules() {
        // DROOLS-978
        final String drl1 = "rule R1 when\n" + ((((((((("then\n" + "  insert(\"test\");\n") + "end\n") + "\n") + "rule R2\n") + "no-loop true\n") + "when\n") + "then\n") + "  insert(1);\n") + "end\n");
        final String drl2 = "rule R3\n" + (((("no-loop true\n" + "when\n") + "then\n") + "  insert(1);\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.fireAllRules();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
        ksession.fireAllRules();
    }

    @Test
    public void testMultipleIncrementalCompilationWithExistentialRules() {
        // DROOLS-988
        final List<String> drls = new ArrayList<>();
        drls.add(getExistenzialRule("R0", "> 10"));
        // Create a session with the above rule
        final KieServices ks = Factory.get();
        final ReleaseId releaseId = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId, kieBaseTestConfiguration, drls.toArray(new String[]{  }));
        final KieContainer kc = ks.newKieContainer(releaseId);
        final KieSession ksession = kc.newKieSession();
        final AtomicInteger counter = new AtomicInteger(0);
        ksession.setGlobal("counter", counter);
        ksession.insert(3);
        ksession.fireAllRules();
        Assert.assertEquals(0, counter.get());
        for (int i = 1; i < 11; i++) {
            final ReleaseId newReleaseId = ks.newReleaseId("org.kie", "test-upgrade", ("1.1." + i));
            drls.add(getExistenzialRule(("R" + i), "< 10"));
            KieUtil.getKieModuleFromDrls(newReleaseId, kieBaseTestConfiguration, drls.toArray(new String[]{  }));
            kc.updateToVersion(newReleaseId);
            ksession.fireAllRules();
            Assert.assertEquals(i, counter.get());
        }
    }

    @Test
    public void testRuleRemovalWithOR() {
        // DROOLS-1007
        final String drl1 = "rule R1 when\n" + ((("    $s : String()\n" + "\t (Integer(this == $s.length) or Long(this == $s.length))\n") + "then\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.fireAllRules();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration);
        try {
            kc.updateToVersion(releaseId2);
        } catch (final Exception e) {
            Assert.fail(("Incremental update should succeed, but failed with " + (e.getLocalizedMessage())));
        }
        ksession.fireAllRules();
    }

    @Test
    public void testSplitAfterQuery() {
        final String drl1 = "global java.util.List list; " + ((((((((((((((((("query foo( Integer $i ) " + "   $i := Integer( this < 10 ) ") + "end\n") + "\n") + "rule r2 when ") + "   foo( $i; ) ") + "   Integer( this == 20 ) ") + "then ") + "   System.out.println(\"20 \" + $i);") + "   list.add( 20 + $i );\n") + "end\n") + "rule r3 when ") + "   $i : Integer( this == 1 ) ") + "then ") + "   System.out.println($i);") + "   update( kcontext.getKieRuntime().getFactHandle( $i ), $i + 1 );") + "end\n") + "\n");
        final String drl2 = "global java.util.List list; " + (((((((((((((((((((((((("query foo( Integer $i ) " + "   $i := Integer( this < 10 ) ") + "end\n") + "\n") + "rule r1 when ") + "   foo( $i ; ) ") + "   Integer( this == 10 ) ") + "then ") + "   System.out.println(\"10 \" + $i);") + "   list.add( 10 + $i );\n") + "end\n") + "rule r2 when ") + "   foo( $i; ) ") + "   Integer( this == 20 ) ") + "then ") + "   System.out.println(\"20 \" + $i);") + "   list.add( 20 + $i );\n") + "end\n") + "rule r3 when ") + "   $i : Integer( this == 1 ) ") + "then ") + "   System.out.println($i);") + "   update( kcontext.getKieRuntime().getFactHandle( $i ), $i + 1 );") + "end\n") + "\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        final List<Integer> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert(1);
        ksession.insert(20);
        ksession.fireAllRules();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(21, ((int) (list.get(0))));
        Assert.assertEquals(22, ((int) (list.get(1))));
    }

    @Test
    public void testGetFactTypeOnIncrementalUpdate() throws Exception {
        // DROOLS-980 - DROOLS-2195
        final String drl1 = "package org.mytest\n" + ((((("declare Person\n" + "   name : String\n") + "   age : Integer\n") + "end\n") + "\n") + "rule R when Person(age > 30) then end\n");
        final String drl2 = "package org.mytest\n" + (((((("declare Person\n" + "   name : String\n") + "   age : Integer\n") + "   address : String\n") + "end\n") + "\n") + "rule R when Person(age > 40) then end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieBase kbase = kc.getKieBase();
        FactType ftype = kbase.getFactType("org.mytest", "Person");
        Assert.assertNotNull(ftype.getField("name"));
        Assert.assertNotNull(ftype.getField("age"));
        Object fact = ftype.newInstance();
        ftype.set(fact, "name", "me");
        ftype.set(fact, "age", 42);
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
        ftype = kbase.getFactType("org.mytest", "Person");
        Assert.assertNotNull(ftype.getField("name"));
        Assert.assertNotNull(ftype.getField("age"));
        Assert.assertNotNull(ftype.getField("address"));
        fact = ftype.newInstance();
        ftype.set(fact, "name", "me again");
        ftype.set(fact, "age", 43);
        ftype.set(fact, "address", "World");
    }

    @Test
    public void testRuleRemovalWithSubnetworkAndOR() {
        // DROOLS-1025
        final String drl1 = "global java.util.concurrent.atomic.AtomicInteger globalInt\n" + (((((("rule R1 when\n" + "    $s : String()\n") + "\t (or exists Integer(this == 1)\n") + "\t     exists Integer(this == 2) )\n") + "\t exists Integer() from globalInt.get()\n") + "then\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.setGlobal("globalInt", new AtomicInteger(0));
        ksession.insert(1);
        ksession.insert("1");
        ksession.fireAllRules();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration);
        try {
            kc.updateToVersion(releaseId2);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.fail(("Incremental update should succeed, but failed with " + (e.getLocalizedMessage())));
        }
        ksession.fireAllRules();
    }

    @Test
    public void testIncrementalCompilationWithClassFieldReader() {
        // BZ-1318532
        final String personSrc = "package org.test;" + (((((((((((("import java.util.ArrayList;" + "import java.util.List;") + "public class Person {") + "    private String name;") + "    private List<String> addresses = new ArrayList<String>();") + "    public Person() {}") + "    public Person(final String name) { this.name = name; }") + "    public List<String> getAddresses() { return addresses; }") + "    public void setAddresses(List<String> addresses) { this.addresses = addresses; }") + "    public void addAddress(String address) { this.addresses.add(address); }") + "    public String getName() { return this.name; }") + "    public void setName(final String name) { this.name = name; }") + "}");
        final String drl1 = ((((((((((((((((("package org.drools.compiler\n" + (("" + "import org.test.Person;\n") + "import ")) + (Address.class.getCanonicalName())) + ";\n") + "") + "rule R0 salience 100 when\n") + " String()") + "then\n") + " Person person = new Person(\"John\");") + " person.addAddress(\"A street\");") + " insert(person);") + "end\n") + "") + "rule R1 when\n") + " $p : Person($addresses : addresses) ") + " $a : Address() from $addresses ") + "then\n") + "end\n") + "";
        final String drl2 = drl1 + " ";// just a trivial update

        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        final ReleaseId id = ks.newReleaseId("org.test", "myTest", "1.0.0");
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kfs.generateAndWritePomXML(id);
        kfs.write("src/main/java/org/test/Person.java", ks.getResources().newReaderResource(new StringReader(personSrc)));
        kfs.write(ks.getResources().newReaderResource(new StringReader(drl1)).setResourceType(DRL).setSourcePath("drl1.drl"));
        kieBuilder.buildAll();
        final KieContainer kc = ks.newKieContainer(id);
        final KieSession ksession = kc.newKieSession();
        kfs.write(ks.getResources().newReaderResource(new StringReader(drl2)).setResourceType(DRL).setSourcePath("drl1.drl"));// update the drl1.drl file

        ((org.kie.internal.builder.InternalKieBuilder) (kieBuilder)).incrementalBuild();
        // don't call updateToVersion(). This test intends to prove that incrementalBuild doesn't do harm to existing ksession.
        ksession.insert("start");
        ksession.fireAllRules();
    }

    @Test
    public void testIncrementalCompilationRemovingParentRule() {
        // DROOLS-1031
        final String drl1 = "package org.drools.compiler\n" + ((("rule R1 when\n" + "   $s : String()\n") + "then\n") + "end\n");
        final String drl2 = "package org.drools.compiler\n" + ((("rule R2 extends R1 when\n" + "   $i : Integer()\n") + "then\n") + "end\n");
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1).write("src/main/resources/r2.drl", drl2);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        final KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        KieSession ksession = kieContainer.newKieSession();
        ksession.insert("test");
        ksession.insert(1);
        Assert.assertEquals(2, ksession.fireAllRules());
        kfs.delete("src/main/resources/r1.drl");
        final IncrementalResults results = ((org.kie.internal.builder.InternalKieBuilder) (kieBuilder)).createFileSet("src/main/resources/r1.drl", "src/main/resources/r2.drl").build();
        Assert.assertEquals(1, results.getAddedMessages().size());
        Assert.assertEquals(0, results.getRemovedMessages().size());
        kieContainer.updateToVersion(ks.getRepository().getDefaultReleaseId());
        ksession = kieContainer.newKieSession();
        ksession.insert("test");
        ksession.insert(1);
        Assert.assertEquals(2, ksession.fireAllRules());
    }

    @Test
    public void testIncrementalCompilationChangeingParentRule() {
        // DROOLS-1031
        final String drl1_1 = "rule R1 when\n" + (("   $s : String( this == \"s1\" )\n" + "then\n") + "end\n");
        final String drl1_2 = "rule R1 when\n" + (("   $s : String( this == \"s2\" )\n" + "then\n") + "end\n");
        final String drl2 = "rule R2 extends R1 when\n" + (("   $i : Integer()\n" + "then\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-extends", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, (drl1_1 + drl2));
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.insert(1);
        ksession.insert("s2");
        Assert.assertEquals(0, ksession.fireAllRules());
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-extends", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, (drl1_2 + drl2));
        kc.updateToVersion(releaseId2);
        Assert.assertEquals(2, ksession.fireAllRules());
    }

    @Test
    public void testRemovePackageFromKieBaseModel() {
        // DROOLS-1287
        final String drl1 = "global java.util.List list;\n" + (((("rule R1 when\n" + "  String()\n") + "then\n") + "  list.add(\"R1\");") + "end\n");
        final String drl2 = "global java.util.List list;\n" + (((("rule R2 when\n" + "  String()\n") + "then\n") + "  list.add(\"R2\");") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-remove-pkg", "1.0");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-remove-pkg", "1.1");
        final Resource drl1Resource = KieUtil.getResource(drl1, ((TestConstants.TEST_RESOURCES_FOLDER) + "pkg1/r1.drl"));
        final Resource drl2Resource = KieUtil.getResource(drl2, ((TestConstants.TEST_RESOURCES_FOLDER) + "pkg2/r2.drl"));
        KieUtil.getKieModuleFromResources(releaseId1, kieBaseTestConfiguration, drl1Resource, drl2Resource);
        final KieContainer container = ks.newKieContainer(releaseId1);
        KieSession ksession = container.newKieSession();
        List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert("test");
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.containsAll(Arrays.asList("R1", "R2")));
        KieUtil.getKieModuleFromResources(releaseId2, kieBaseTestConfiguration, drl2Resource);
        final Results results = container.updateToVersion(releaseId2);
        Assert.assertEquals(0, results.getMessages().size());
        ksession = container.newKieSession();
        list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert("test");
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains("R2"));
    }

    @Test
    public void testAddPackageToKieBaseModel() {
        // DROOLS-1287
        // DROOLS-1287
        final String drl1 = "global java.util.List list;\n" + (((("rule R1 when\n" + "  String()\n") + "then\n") + "  list.add(\"R1\");") + "end\n");
        final String drl2 = "global java.util.List list;\n" + (((("rule R2 when\n" + "  String()\n") + "then\n") + "  list.add(\"R2\");") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-remove-pkg", "1.0");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-remove-pkg", "1.1");
        final Resource drl1Resource = KieUtil.getResource(drl1, ((TestConstants.TEST_RESOURCES_FOLDER) + "pkg1/r1.drl"));
        final Resource drl2Resource = KieUtil.getResource(drl2, ((TestConstants.TEST_RESOURCES_FOLDER) + "pkg2/r2.drl"));
        KieUtil.getKieModuleFromResources(releaseId1, kieBaseTestConfiguration, drl2Resource);
        final KieContainer container = ks.newKieContainer(releaseId1);
        KieSession ksession = container.newKieSession();
        List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert("test");
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains("R2"));
        KieUtil.getKieModuleFromResources(releaseId2, kieBaseTestConfiguration, drl1Resource, drl2Resource);
        final Results results = container.updateToVersion(releaseId2);
        Assert.assertEquals(0, results.getMessages().size());
        ksession = container.newKieSession();
        list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert("test");
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.containsAll(Arrays.asList("R1", "R2")));
    }

    @Test
    public void testKJarUpgradeWithSpace() {
        // DROOLS-1399
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n "// <<- notice the EXTRA SPACE is the only change in this other version.
         + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        testKJarUpgradeDRLWithSpace(drl1, drl2, "Hello World", 1, 0);
    }

    @Test
    public void testKJarUpgradeDRLWithSpace2() {
        // DROOLS-1399 bis
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello World\"  )\n")// <<- notice the EXTRA SPACE is the only change in this other version.
         + "then\n") + "end\n";
        testKJarUpgradeDRLWithSpace(drl1, drl2, "Hello World", 1, 0);
    }

    @Test
    public void testKJarUpgradeDRLWithSpace3() {
        // DROOLS-1399 ter
        final String drl1 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rs when $s : String() then System.out.println($s); end\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "  System.out.println($m); \n") + "end\n";
        final String drl2 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rs when $s : String( this == \"x\") then System.out.println($s); end\n")// <<- notice rule changed
         + "rule Rx when\n") + "   $m : Message( message == \"Hello World\"  )\n")// <<- notice the EXTRA SPACE is the an ADDITIONAL change in this other version.
         + "then\n") + "  System.out.println($m); \n") + "end\n";
        testKJarUpgradeWithSpaceVariant2(drl1, drl2);
    }

    @Test
    public void testKJarUpgradeDRLWithSpace4() {
        // DROOLS-1399 quater
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello  World\" )\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello World\" )\n")// <<- notice the EXTRA SPACE typo was removed
         + "then\n") + "end\n";
        testKJarUpgradeDRLWithSpace(drl1, drl2, "Hello World", 0, 1);
    }

    @Test
    public void testKJarUpgradeDRLWithSpace5() {
        // DROOLS-1399 quinquies
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello\'  World\" )\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \"Hello\' World\" )\n")// <<- notice the EXTRA SPACE typo was removed
         + "then\n") + "end\n";
        testKJarUpgradeDRLWithSpace(drl1, drl2, "Hello' World", 0, 1);// <<- notice the ' character

    }

    @Test
    public void testKJarUpgradeWithSpace_usingSingleQuote() {
        // DROOLS-1399 (using single quote)
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello World\' )\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n "// <<- notice the EXTRA SPACE is the only change in this other version.
         + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello World\' )\n") + "then\n") + "end\n";
        testKJarUpgradeDRLWithSpace(drl1, drl2, "Hello World", 1, 0);
    }

    @Test
    public void testKJarUpgradeDRLWithSpace2_usingSingleQuote() {
        // DROOLS-1399 bis (using single quote)
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello World\' )\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello World\'  )\n")// <<- notice the EXTRA SPACE is the only change in this other version.
         + "then\n") + "end\n";
        testKJarUpgradeDRLWithSpace(drl1, drl2, "Hello World", 1, 0);
    }

    @Test
    public void testKJarUpgradeDRLWithSpace3_usingSingleQuote() {
        // DROOLS-1399 ter (using single quote)
        final String drl1 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rs when $s : String() then System.out.println($s); end\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello World\' )\n") + "then\n") + "  System.out.println($m); \n") + "end\n";
        final String drl2 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rs when $s : String( this == \'x\') then System.out.println($s); end\n")// <<- notice rule changed
         + "rule Rx when\n") + "   $m : Message( message == \'Hello World\'  )\n")// <<- notice the EXTRA SPACE is the an ADDITIONAL change in this other version.
         + "then\n") + "  System.out.println($m); \n") + "end\n";
        testKJarUpgradeWithSpaceVariant2(drl1, drl2);
    }

    @Test
    public void testKJarUpgradeDRLWithSpace4_usingSingleQuote() {
        // DROOLS-1399 quater (using single quote)
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello  World\' )\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello World\' )\n")// <<- notice the EXTRA SPACE typo was removed
         + "then\n") + "end\n";
        testKJarUpgradeDRLWithSpace(drl1, drl2, "Hello World", 0, 1);
    }

    @Test
    public void testKJarUpgradeDRLWithSpace5_usingSingleQuote() {
        // DROOLS-1399 quinquies (using single quote)
        final String drl1 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello\\\'  World\' )\n") + "then\n") + "end\n";
        final String drl2 = (((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule Rx when\n") + "   $m : Message( message == \'Hello\\\' World\' )\n")// <<- notice the EXTRA SPACE typo was removed
         + "then\n") + "end\n";
        testKJarUpgradeDRLWithSpace(drl1, drl2, "Hello' World", 0, 1);
    }

    @Test
    public void testJavaClassRedefinition() {
        // DROOLS-1402
        final String JAVA1 = "package org.test;" + (((((("    public class MyBean {\n" + "        private String firstName;\n") + "        public MyBean() { /* empty constructor */ }\n") + "        public MyBean(String firstName) { this.firstName = firstName; }\n") + "        public String getFirstName() { return firstName; }\n") + "        public void setFirstName(String firstName) { this.firstName = firstName; }\n") + "    }");
        final String DRL1 = "package org.test;\n" + ((((((("\n" + "//from row number: 1\n") + "rule \"Row 1 HelloRules\"\n") + "    when\n") + "        $b : MyBean( firstName == null )\n") + "    then\n") + "        System.out.println($b);") + "end");
        final String INIT_DRL = "package org.test; rule RINIT when eval(true) then insert(new MyBean()); end";
        final String INIT_DRL_2 = "package org.test; rule RINIT when eval(1==1) then insert(new MyBean()); end";
        final String JAVA2 = "package org.test;" + (((((((((("    public class MyBean {\n" + "        private String firstName;\n") + "        private String lastName;\n") + "        public MyBean() { /* empty constructor */ }\n") + "        public MyBean(String firstName) { this.firstName = firstName; }\n") + "        public MyBean(String firstName, String lastName) { this.firstName = firstName; this.lastName = lastName; }\n") + "        public String getFirstName() { return firstName; }\n") + "        public void setFirstName(String firstName) { this.firstName = firstName; }\n") + "        public String getLastName() { return lastName; }\n") + "        public void setLastName(String lastName) { this.lastName = lastName; }\n") + "    }");
        final String DRL2 = "package org.test;\n" + ((((((("\n" + "//from row number: 1\n") + "rule \"Row 1 HelloRules\"\n") + "    when\n") + "        $b : MyBean( firstName == null , lastName == null )\n") + "    then\n") + "        System.out.println($b);") + "end");
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        final ReleaseId id = ks.newReleaseId("org.test", "myTest", "1.0.0");
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kfs.generateAndWritePomXML(id);
        kfs.write("src/main/java/org/test/MyBean.java", ks.getResources().newReaderResource(new StringReader(JAVA1)));
        kfs.write(ks.getResources().newReaderResource(new StringReader(DRL1)).setResourceType(DRL).setSourcePath("rules.drl"));
        kfs.write(ks.getResources().newReaderResource(new StringReader(INIT_DRL)).setResourceType(DRL).setSourcePath("INIT_DRL.drl"));
        kieBuilder.buildAll();
        final KieContainer kc = ks.newKieContainer(id);
        final KieSession ksession = kc.newKieSession();
        int fired = ksession.fireAllRules();
        Assert.assertEquals(2, fired);
        final ReleaseId id2 = ks.newReleaseId("org.test", "myTest", "2.0.0");
        final KieFileSystem kfs2 = ks.newKieFileSystem();
        final KieBuilder kieBuilder2 = ks.newKieBuilder(kfs2);
        kfs2.generateAndWritePomXML(id2);
        kfs2.write("src/main/java/org/test/MyBean.java", ks.getResources().newReaderResource(new StringReader(JAVA2)));
        kfs2.write(ks.getResources().newReaderResource(new StringReader(DRL2)).setResourceType(DRL).setSourcePath("rules.drl"));
        kfs2.write(ks.getResources().newReaderResource(new StringReader(INIT_DRL_2)).setResourceType(DRL).setSourcePath("INIT_DRL.drl"));
        kieBuilder2.buildAll();
        final Results updateResults = kc.updateToVersion(id2);
        Assert.assertFalse(updateResults.hasMessages(Level.ERROR));
        fired = ksession.fireAllRules();
        Assert.assertEquals(2, fired);
    }

    @Test
    public void testJavaClassRedefinitionJoined() {
        // DROOLS-1402
        final String JAVA1 = "package org.test;" + (((((("    public class MyBean {\n" + "        private String firstName;\n") + "        public MyBean() { /* empty constructor */ }\n") + "        public MyBean(String firstName) { this.firstName = firstName; }\n") + "        public String getFirstName() { return firstName; }\n") + "        public void setFirstName(String firstName) { this.firstName = firstName; }\n") + "    }");
        final String DRL1 = "package org.test;\n" + (((((((("\n" + "//from row number: 1\n") + "rule \"Row 1 HelloRules\"\n") + "    when\n") + "        $b : MyBean( firstName == null )\n") + "        $s : String()\n") + "    then\n") + "        System.out.println($s + \" \" + $b);") + "end");
        final String INIT_DRL = "package org.test; rule RINIT when eval(true) then insert(new MyBean()); end";
        final String INIT_DRL_2 = "package org.test; rule RINIT when eval(1==1) then insert(new MyBean()); end";
        final String JAVA2 = "package org.test;" + (((((((((("    public class MyBean {\n" + "        private String firstName;\n") + "        private String lastName;\n") + "        public MyBean() { /* empty constructor */ }\n") + "        public MyBean(String firstName) { this.firstName = firstName; }\n") + "        public MyBean(String firstName, String lastName) { this.firstName = firstName; this.lastName = lastName; }\n") + "        public String getFirstName() { return firstName; }\n") + "        public void setFirstName(String firstName) { this.firstName = firstName; }\n") + "        public String getLastName() { return lastName; }\n") + "        public void setLastName(String lastName) { this.lastName = lastName; }\n") + "    }");
        final String DRL2 = "package org.test;\n" + (((((((("\n" + "//from row number: 1\n") + "rule \"Row 1 HelloRules\"\n") + "    when\n") + "        $b : MyBean( firstName == null , lastName == null )\n") + "        $s : String()\n") + "    then\n") + "        System.out.println($s + \" \" + $b);") + "end");
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        final ReleaseId id = ks.newReleaseId("org.test", "myTest", "1.0.0");
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kfs.generateAndWritePomXML(id);
        kfs.write("src/main/java/org/test/MyBean.java", ks.getResources().newReaderResource(new StringReader(JAVA1)));
        kfs.write(ks.getResources().newReaderResource(new StringReader(DRL1)).setResourceType(DRL).setSourcePath("rules.drl"));
        kfs.write(ks.getResources().newReaderResource(new StringReader(INIT_DRL)).setResourceType(DRL).setSourcePath("INIT_DRL.drl"));
        kieBuilder.buildAll();
        final KieContainer kc = ks.newKieContainer(id);
        final KieSession ksession = kc.newKieSession();
        ksession.insert("This string joins with");
        int fired = ksession.fireAllRules();
        Assert.assertEquals(2, fired);
        final ReleaseId id2 = ks.newReleaseId("org.test", "myTest", "2.0.0");
        final KieFileSystem kfs2 = ks.newKieFileSystem();
        final KieBuilder kieBuilder2 = ks.newKieBuilder(kfs2);
        kfs2.generateAndWritePomXML(id2);
        kfs2.write("src/main/java/org/test/MyBean.java", ks.getResources().newReaderResource(new StringReader(JAVA2)));
        kfs2.write(ks.getResources().newReaderResource(new StringReader(DRL2)).setResourceType(DRL).setSourcePath("rules.drl"));
        kfs2.write(ks.getResources().newReaderResource(new StringReader(INIT_DRL_2)).setResourceType(DRL).setSourcePath("INIT_DRL.drl"));
        kieBuilder2.buildAll();
        final Results updateResults = kc.updateToVersion(id2);
        Assert.assertFalse(updateResults.hasMessages(Level.ERROR));
        fired = ksession.fireAllRules();
        Assert.assertEquals(2, fired);
    }

    @Test(timeout = 20000L)
    public void testMultipleIncrementalCompilationsWithFireUntilHalt() throws Exception {
        // DROOLS-1406
        final KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-fireUntilHalt", "1.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, getTestRuleForFireUntilHalt(0));
        // Create a session and fire rules
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession kieSession = kc.newKieSession();
        final IncrementalCompilationTest.DebugList<String> list = new IncrementalCompilationTest.DebugList<>();
        kieSession.setGlobal("list", list);
        kieSession.insert(new Message("X"));
        CountDownLatch done = new CountDownLatch(1);
        list.done = done;
        try {
            new Thread(kieSession::fireUntilHalt).start();
            done.await();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals("0 - X", list.get(0));
            list.clear();
            for (int i = 1; i < 10; i++) {
                done = new CountDownLatch(1);
                list.done = done;
                final ReleaseId releaseIdI = ks.newReleaseId("org.kie", "test-fireUntilHalt", ("1." + i));
                KieUtil.getKieModuleFromDrls(releaseIdI, kieBaseTestConfiguration, getTestRuleForFireUntilHalt(i));
                kc.updateToVersion(releaseIdI);
                done.await();
                Assert.assertEquals(1, list.size());
                Assert.assertEquals((i + " - X"), list.get(0));
                list.clear();
            }
        } finally {
            kieSession.halt();
        }
    }

    public static class DebugList<T> extends ArrayList<T> {
        CountDownLatch done;

        @Override
        public synchronized boolean add(final T t) {
            final boolean result = super.add(t);
            done.countDown();
            return result;
        }
    }

    @Test
    public void testIncrementalCompilationWithExtendsRule() {
        // DROOLS-1405
        final String drl1 = "rule \"test1\" when then end\n";
        final String drl2 = "rule \"test2\" extends \"test1\" when then end\n" + "rule \"test3\" extends \"test1\" when then end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem().write("src/main/resources/r1.drl", drl1);
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        Assert.assertEquals(0, kieBuilder.getResults().getMessages(ERROR).size());
        kfs.write("src/main/resources/r2.drl", drl2);
        final IncrementalResults addResults = createFileSet("src/main/resources/r2.drl").build();
        Assert.assertEquals(0, addResults.getAddedMessages().size());
    }

    public static class BaseClass {
        String baseField;

        public String getBaseField() {
            return baseField;
        }

        public void setBaseField(final String baseField) {
            this.baseField = baseField;
        }
    }

    @Test
    public void testUpdateWithPojoExtensionDifferentPackages() throws Exception {
        // DROOLS-1491
        final String drlDeclare = (((("package org.drools.compiler.integrationtests\n" + "declare DroolsApplications extends ") + (IncrementalCompilationTest.BaseClass.class.getCanonicalName())) + "\n") + "    droolsAppName: String\n") + "end";
        final String drlRule = "package org.drools.compiler.test\n" + ((("rule R1 when\n" + "   $fact : org.drools.compiler.integrationtests.DroolsApplications( droolsAppName == \"appName\" )\n") + "then\n") + "end");
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        kfs.generateAndWritePomXML(releaseId1);
        kfs.write(ks.getResources().newReaderResource(new StringReader(drlDeclare)).setResourceType(DRL).setSourcePath("drlDeclare.drl"));
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kieBuilder.buildAll();
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        final FactType factType = kc.getKieBase().getFactType("org.drools.compiler.integrationtests", "DroolsApplications");
        final Object fact = factType.newInstance();
        factType.set(fact, "droolsAppName", "appName");
        ksession.insert(fact);
        Assert.assertEquals(0, ksession.fireAllRules());
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        final KieFileSystem kfs2 = ks.newKieFileSystem();
        kfs2.generateAndWritePomXML(releaseId2);
        kfs2.write(ks.getResources().newReaderResource(new StringReader(drlDeclare)).setResourceType(DRL).setSourcePath("drlDeclare.drl"));
        kfs2.write(ks.getResources().newReaderResource(new StringReader(drlRule)).setResourceType(DRL).setSourcePath("drlRule.drl"));
        final KieBuilder kieBuilder2 = ks.newKieBuilder(kfs2);
        kieBuilder2.buildAll();
        final Results updateResults = kc.updateToVersion(releaseId2);
        Assert.assertEquals(0, updateResults.getMessages().size());
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testPropertyReactivityOfAKnownClass() {
        final String drl1 = (((((((((("import " + (IncrementalCompilationTest.TypeA.class.getCanonicalName())) + "\n") + "import ") + (IncrementalCompilationTest.TypeB.class.getCanonicalName())) + "\n") + "rule \"RULE_1\"\n") + "    when\n") + "        TypeA()") + "        TypeB()") + "    then\n") + "end\n";
        final String drl2 = ((((((("import " + (IncrementalCompilationTest.TypeB.class.getCanonicalName())) + "\n") + "rule \"RULE_2\"\n") + "    when\n") + "        $b : TypeB() @watch(!*)") + "    then\n") + "        modify($b) { setValue(0) } \n") + "end\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
        ksession.insert(new IncrementalCompilationTest.TypeB(1));
        final int fired = ksession.fireAllRules(10);
        Assert.assertEquals(1, fired);
    }

    @Test
    public void testPropertyReactivityOfAnOriginallyUnknownClass() {
        // DROOLS-1684
        final String drl1 = (((((("import " + (IncrementalCompilationTest.TypeA.class.getCanonicalName())) + "\n") + "rule \"RULE_1\"\n") + "    when\n") + "        TypeA()") + "    then\n") + "end\n";
        final String drl2 = ((((((("import " + (IncrementalCompilationTest.TypeB.class.getCanonicalName())) + "\n") + "rule \"RULE_2\"\n") + "    when\n") + "        $b : TypeB() @watch(!*)") + "    then\n") + "        modify($b) { setValue(0) } \n") + "end\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
        ksession.insert(new IncrementalCompilationTest.TypeB(1));
        final int fired = ksession.fireAllRules(10);
        Assert.assertEquals(1, fired);
    }

    public static class TypeA {}

    public static class TypeB {
        int value;

        public TypeB(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(final int value) {
            this.value = value;
        }
    }

    private static final String DECLARES_DRL = "package org.drools.example.api.kiedeclare;\n" + ((("declare Message\n" + "name : String\n") + "text : String\n") + "end \n");

    private static final String RULES1_DRL = "package org.drools.example.api.kiemodulemodel;\n" + (((((("import org.drools.example.api.kiedeclare.*;\n" + "rule rule6 when \n") + "    $m : Message(text == \"What\'s the problem?\") \n") + "then\n") + "    delete( $m );\n") + "    insert( new Message(\"HAL\", \"reply 1\" ) ); \n") + "end \n");

    private static final String RULES2_DRL = "package org.drools.example.api.kiemodulemodel;\n" + (((((("import org.drools.example.api.kiedeclare.*;\n" + "rule rule6 when \n") + "    $m : Message(text == \"What\'s the problem?\") \n") + "then\n") + "    delete( $m );\n") + "    insert( new Message(\"HAL\", \"reply 2\" ) ); \n") + "end \n");

    @Test
    public void testDeclaredTypeInDifferentPackage() throws IllegalAccessException, InstantiationException {
        // DROOLS-1707
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, IncrementalCompilationTest.DECLARES_DRL, IncrementalCompilationTest.RULES1_DRL);
        final KieContainer kContainer = ks.newKieContainer(releaseId1);
        doFire(kContainer.getKieBase(), "reply 1");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, IncrementalCompilationTest.DECLARES_DRL, IncrementalCompilationTest.RULES2_DRL);
        kContainer.updateToVersion(releaseId2);
        doFire(kContainer.getKieBase(), "reply 2");
    }

    @Test
    public void testDeclaredTypeInIncludedKieBase() throws IllegalAccessException, InstantiationException {
        // DROOLS-1707
        final KieServices ks = Factory.get();
        final KieModule kModule = buildKieModule("0.0.1", IncrementalCompilationTest.RULES1_DRL);
        final KieContainer kContainer = ks.newKieContainer(kModule.getReleaseId());
        doFire(kContainer.getKieBase("kiemodulemodel"), "reply 1");
        final KieModule kModule2 = buildKieModule("0.0.2", IncrementalCompilationTest.RULES2_DRL);
        kContainer.updateToVersion(kModule2.getReleaseId());
        doFire(kContainer.getKieBase("kiemodulemodel"), "reply 2");
    }

    @Test
    public void testRemoveAndReaddJavaClass() {
        // DROOLS-1704
        final String javaSource = "package org.drools.test;\n" + "public class Person { }\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        final ReleaseId releaseId3 = ks.newReleaseId("org.kie", "test-upgrade", "1.2.0");
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.generateAndWritePomXML(releaseId1);
        KieModuleModel kModuleModel = ks.newKieModuleModel();
        kfs.writeKModuleXML(kModuleModel.toXML());
        kfs.write("src/main/java/org/drools/test/Person.java", javaSource);
        ks.newKieBuilder(kfs).buildAll();
        final KieContainer kContainer = ks.newKieContainer(releaseId1);
        try {
            Class.forName("org.drools.test.Person", true, kContainer.getClassLoader());
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        kContainer.getKieBase();
        kfs = ks.newKieFileSystem();
        kfs.generateAndWritePomXML(releaseId2);
        kModuleModel = ks.newKieModuleModel();
        kfs.writeKModuleXML(kModuleModel.toXML());
        ks.newKieBuilder(kfs).buildAll();
        kContainer.updateToVersion(releaseId2);
        kContainer.getKieBase();
        kfs = ks.newKieFileSystem();
        kfs.generateAndWritePomXML(releaseId3);
        kModuleModel = ks.newKieModuleModel();
        kfs.writeKModuleXML(kModuleModel.toXML());
        kfs.write("src/main/java/org/drools/test/Person.java", javaSource);
        ks.newKieBuilder(kfs).buildAll();
        kContainer.updateToVersion(releaseId3);
    }

    @Test
    public void testChangedPackage() {
        // DROOLS-1742
        final String drl1 = "package org.a\n" + (((((("rule \"RG_1\"\n" + "    when\n") + "        Boolean()\n") + "        Integer()\n") + "    then\n") + "        System.out.println(\"RG_1\");") + "end\n");
        final String drl2 = "package org.b\n" + (((((("rule \"RG_2\"\n" + "    when\n") + "        Boolean()\n") + "        String()\n") + "    then\n") + "        System.out.println(\"RG_2\");") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl2, drl1);
        final KieContainer kieContainer = ks.newKieContainer(releaseId1);
        final KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert("test");
        Assert.assertEquals(0, kieSession.fireAllRules());
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1);
        kieContainer.updateToVersion(releaseId2);
        Assert.assertEquals(0, kieSession.fireAllRules());
    }

    @Test
    public void testSegmentSplitAfterMerge() {
        final String drl1A = "package org.hightea.a\n" + (((((("rule \"RG_1\"\n" + "    when\n") + "        Boolean()\n") + "        String()\n") + "    then\n") + "end") + "\n");
        final String drl2A = "package org.hightea.b\n" + ((((("rule \"RG_2\"\n" + "    when\n") + "        Boolean()\n") + "        Integer()\n") + "    then\n") + "end\n");
        final String drl2B = "package org.hightea.b\n" + (((((("rule \"RG_2\"\n" + "    when\n") + "        Boolean()\n") + "        Integer()\n") + "    then\n") + "\t\t //Simple comment to mark the rule as modified\n") + "end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1A, drl2A);
        final KieContainer kieContainer = ks.newKieContainer(releaseId1);
        final KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert("A Test String");
        Assert.assertEquals(0, kieSession.fireAllRules());
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1A, drl2B);
        kieContainer.updateToVersion(releaseId2);
        Assert.assertEquals(0, kieSession.fireAllRules());
    }

    @Test
    public void testAddFieldToDeclaredType() {
        // DROOLS-2197
        final String declares1 = "declare Address\n" + (("   streetName : String\n" + "   city : String\n") + "end");
        final String declares2 = "declare Address\n" + ((("   streetName : String\n" + "   city : String\n") + "   flg: String\n") + "end");
        final String rules1 = "rule R when\n" + ((("    a : Address( city == \"Antibes\" )\n" + "then\n") + "    a.setStreetName(\"Av. Jean Medecin\");\n") + "end");
        final String rules2 = "rule R when\n" + ((("    a : Address( city == \" Paris \", flg == \"yes\" )\n" + "then\n") + "    a.setStreetName(\" Champs Elisees \");\n") + "end");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, declares1, rules1);
        final KieContainer kieContainer = ks.newKieContainer(releaseId1);
        final KieSession kieSession = kieContainer.newKieSession();
        Assert.assertEquals(0, kieSession.fireAllRules());
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, declares2, rules2);
        kieContainer.updateToVersion(releaseId2);
        Assert.assertEquals(0, kieSession.fireAllRules());
    }

    @Test
    public void testIncremenatalCompilationAddingFieldToDeclaredType() {
        // DROOLS-2197
        final String declares1 = "declare Address\n" + (("   streetName : String\n" + "   city : String\n") + "end");
        final String declares2 = "declare Address\n" + ((("   streetName : String\n" + "   city : String\n") + "   flg: String\n") + "end");
        final String rules1 = "rule R when\n" + ((("    a : Address( city == \"Antibes\" )\n" + "then\n") + "    a.setStreetName(\"Av. Jean Medecin\");\n") + "end");
        final String rules2 = "rule R when\n" + ((("    a : Address( city == \" Paris \" )\n" + "then\n") + "    a.setStreetName(\" Champs Elisees \");\n") + "end");
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        final ReleaseId id = ks.newReleaseId("org.test", "foo", "1.0-SNAPSHOT");
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kfs.generateAndWritePomXML(id);
        kfs.write(ks.getResources().newReaderResource(new StringReader(declares1)).setResourceType(DRL).setSourcePath("declares.drl"));
        kfs.write(ks.getResources().newReaderResource(new StringReader(rules1)).setResourceType(DRL).setSourcePath("rules.drl"));
        kieBuilder.buildAll();
        final KieContainer kc = ks.newKieContainer(id);
        final KieSession ksession = kc.newKieSession();
        ksession.fireAllRules();
        kfs.write(ks.getResources().newReaderResource(new StringReader(declares2)).setResourceType(DRL).setSourcePath("declares.drl"));
        kfs.write(ks.getResources().newReaderResource(new StringReader(rules2)).setResourceType(DRL).setSourcePath("rules.drl"));
        final IncrementalResults results = incrementalBuild();
        System.out.println(results.getAddedMessages());
        Assert.assertEquals(0, results.getAddedMessages().size());
        final Results updateResults = kc.updateToVersion(id);
        Assert.assertEquals(0, updateResults.getMessages().size());
    }

    @Test
    public void testUnchangedAccumulate() {
        // DROOLS-2194
        final String drl1 = "import java.util.*;\n" + (((((((("rule B\n" + "when\n") + "    $eventCodeDistinctMois : Integer( intValue>0 ) from accumulate ( String( $id : this ),\n") + "                                                                init( Set set = new HashSet(); ),\n") + "                                                                action( set.add($id); ),\n") + "                                                                reverse( set.remove($id); ),\n") + "                                                                result( set.size()) )\n") + "then\n") + "end");
        final String drl2 = "rule C when then end\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1, drl2);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        final FactHandle fh = ksession.insert("1");
        ksession.fireAllRules();
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1);
        kc.updateToVersion(releaseId2);
        final ReleaseId releaseId3 = ks.newReleaseId("org.kie", "test-upgrade", "1.2.0");
        KieUtil.getKieModuleFromDrls(releaseId3, kieBaseTestConfiguration, drl1, drl2);
        kc.updateToVersion(releaseId3);
        ksession.delete(fh);
        ksession.fireAllRules();
        ksession.insert("2");
        ksession.fireAllRules();
    }

    @Test
    public void testGlobalRemovedFromOneDrl() {
        // RHDM-311
        final String drlAWithGlobal = "package org.x.a\nglobal Boolean globalBool\n";
        final String drlANoGlobal = "package org.x.a\n";
        final String drlBWithGlobal = "package org.x.b\nglobal Boolean globalBool\n";
        final String drlBNoGlobal = "package org.x.b\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drlAWithGlobal, drlBWithGlobal);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.setGlobal("globalBool", Boolean.FALSE);
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drlANoGlobal, drlBWithGlobal);
        kc.updateToVersion(releaseId2);
        ksession.setGlobal("globalBool", Boolean.TRUE);
        final ReleaseId releaseId3 = ks.newReleaseId("org.kie", "test-upgrade", "1.2.0");
        KieUtil.getKieModuleFromDrls(releaseId3, kieBaseTestConfiguration, drlANoGlobal, drlBNoGlobal);
        kc.updateToVersion(releaseId3);
        try {
            ksession.setGlobal("globalBool", Boolean.TRUE);
            Assert.fail("the global should be no longer present");
        } catch (final Exception expected) {
            // expected
        }
    }

    @Test
    public void testGlobalRemovedAndAdded() {
        // RHDM-311
        final String drlAWithGlobal = "package org.x.a\nglobal Boolean globalBool\n";
        final String drlANoGlobal = "package org.x.a\n";
        final String drlBWithGlobal = "package org.x.b\nglobal Boolean globalBool\n";
        final String drlBNoGlobal = "package org.x.b\n";
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drlAWithGlobal, drlBNoGlobal);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.setGlobal("globalBool", Boolean.FALSE);
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drlANoGlobal, drlBWithGlobal);
        kc.updateToVersion(releaseId2);
        ksession.setGlobal("globalBool", Boolean.TRUE);
    }

    @Test
    public void testRuleRemovalAndEval() {
        // DROOLS-2276
        final String drl1 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R1 when\n") + "   $m : Message()\n") + "   eval($m != null)\n") + "then\n") + "   System.out.println( \"Hello R1\" );\n") + "end\n";
        final String drl2 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R2 when\n") + "   $m : Message()\n") + "   eval($m != null)\n") + "then\n") + "   System.out.println( \"Hello R2\" );\n") + "end\n";
        final String drl3 = (((((((("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + ";\n") + "rule R3 when\n") + "   $m : Message()\n") + "   eval($m != null)\n") + "then\n") + "   System.out.println( \"Hello R3\" );\n") + "end\n";
        final KieServices ks = Factory.get();
        final KieFileSystem kfs1 = ks.newKieFileSystem();
        kfs1.write("src/main/resources/rules/Sample1.drl", drl1);
        final ReleaseId releaseId1 = ks.newReleaseId("com.sample", "my-sample-a", "1.0.0");
        kfs1.generateAndWritePomXML(releaseId1);
        ks.newKieBuilder(kfs1).buildAll();
        final KieFileSystem kfs2 = ks.newKieFileSystem();
        kfs2.write("src/main/resources/rules/Sample2.drl", drl2);
        final ReleaseId releaseId2 = ks.newReleaseId("com.sample", "my-sample-a", "2.0.0");
        kfs2.generateAndWritePomXML(releaseId2);
        ks.newKieBuilder(kfs2).buildAll();
        final KieFileSystem kfs3 = ks.newKieFileSystem();
        kfs3.write("src/main/resources/rules/Sample3.drl", drl3);
        final ReleaseId releaseId3 = ks.newReleaseId("com.sample", "my-sample-a", "3.0.0");
        kfs3.generateAndWritePomXML(releaseId3);
        ks.newKieBuilder(kfs3).buildAll();
        // Create a session and fire rules
        final KieContainer kc = ks.newKieContainer(releaseId1);
        final KieSession ksession = kc.newKieSession();
        ksession.insert(new Message("Hello World"));
        Assert.assertEquals(1, ksession.fireAllRules());
        kc.updateToVersion(releaseId2);
        kc.updateToVersion(releaseId3);
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testGetFactTypeOnIncrementalUpdateWithNestedFacts() throws Exception {
        // DROOLS-2385
        final String drl1 = "package org.drools.example.api.kiemodulemodel\n" + (((((((("declare MyNestedFact\n" + "   x : String\n") + "end\n") + "declare Message\n") + "   text : String\n") + "   nested : MyNestedFact\n") + "end\n") + "\n") + "rule R when Message(text == \"What\'s the problem?\") then end\n");
        final String drl2 = "package org.drools.example.api.kiemodulemodel\n" + ((((((((("declare MyNestedFact\n" + "   x : String\n") + "   y : int\n") + "end\n") + "declare Message\n") + "   text : String\n") + "   nested : MyNestedFact\n") + "end\n") + "\n") + "rule R when Message(text == \"What\'s the problem?\", nested.y == 42) then end\n");
        final KieServices ks = Factory.get();
        final ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.1");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1);
        final KieContainer kc = ks.newKieContainer(releaseId1);
        KieBase kbase = kc.getKieBase();
        FactType ftype = kbase.getFactType("org.drools.example.api.kiemodulemodel", "Message");
        Assert.assertNotNull(ftype.getField("text"));
        Object fact = ftype.newInstance();
        ftype.set(fact, "text", "What's the problem?");
        FactType nestedftype = kbase.getFactType("org.drools.example.api.kiemodulemodel", "MyNestedFact");
        Assert.assertNotNull(nestedftype.getField("x"));
        Object nestedfact = nestedftype.newInstance();
        ftype.set(fact, "nested", nestedfact);
        final ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.2");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl2);
        kc.updateToVersion(releaseId2);
        kbase = kc.getKieBase();
        ftype = kbase.getFactType("org.drools.example.api.kiemodulemodel", "Message");
        Assert.assertNotNull(ftype.getField("text"));
        fact = ftype.newInstance();
        ftype.set(fact, "text", "What's the problem?");
        nestedftype = kbase.getFactType("org.drools.example.api.kiemodulemodel", "MyNestedFact");
        Assert.assertNotNull(nestedftype.getField("x"));
        Assert.assertNotNull(nestedftype.getField("y"));
        nestedfact = nestedftype.newInstance();
        nestedftype.set(nestedfact, "y", 42);
        ftype.set(fact, "nested", nestedfact);
    }

    @Test
    public void testKJarUpgradeWithNewRule() throws Exception {
        // DROOLS-2596
        String drl1a = "package org.drools.incremental\n" + ((((("global java.util.List list\n" + "rule R1 when\n") + "   $s : String()\n") + "then\n") + "   list.add(\"xxx: \" + $s);") + "end\n");
        String drl1b = "package org.drools.incremental\n" + (((((((((("global java.util.List list\n" + "rule R1 when\n") + "   $s : String()\n") + "then\n") + "   list.add(\"yyy: \" + $s);") + "end\n") + "rule R2 when\n") + "   $i : Integer()\n") + "then\n") + "   list.add(\"\" + $i);") + "end\n");
        KieServices ks = KieServices.get();
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1a);
        KieContainer kc = ks.newKieContainer(releaseId1);
        List<String> list = new ArrayList<>();
        KieSession session = kc.getKieBase().newKieSession();
        session.setGlobal("list", list);
        session.insert("test");
        session.insert(1);
        Assert.assertEquals(1, session.fireAllRules());
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("xxx: test", list.get(0));
        list.clear();
        ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1b);
        kc.updateToVersion(releaseId2);
        session = kc.getKieBase().newKieSession();
        session.setGlobal("list", list);
        session.insert("test");
        session.insert(1);
        Assert.assertEquals(2, session.fireAllRules());
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("yyy: test", list.get(0));
        Assert.assertEquals("1", list.get(1));
    }

    @Test
    public void testKJarUpgradeWithNewRuleAndStatelessSession() throws Exception {
        // DROOLS-2596
        String drl1a = "package org.drools.incremental\n" + ((((("global java.util.List list\n" + "rule R1 when\n") + "   $s : String()\n") + "then\n") + "   list.add(\"xxx: \" + $s);") + "end\n");
        String drl1b = "package org.drools.incremental\n" + (((((((((("global java.util.List list\n" + "rule R1 when\n") + "   $s : String()\n") + "then\n") + "   list.add(\"yyy: \" + $s);") + "end\n") + "rule R2 when\n") + "   $i : Integer()\n") + "then\n") + "   list.add(\"\" + $i);") + "end\n");
        KieServices ks = KieServices.get();
        KieCommands commands = ks.getCommands();
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-upgrade", "1.0.0");
        KieUtil.getKieModuleFromDrls(releaseId1, kieBaseTestConfiguration, drl1a);
        KieContainer kc = ks.newKieContainer(releaseId1);
        StatelessKieSession session = kc.getKieBase().newStatelessKieSession();
        List<String> list = new ArrayList<>();
        session.execute(commands.newBatchExecution(Arrays.asList(commands.newSetGlobal("list", list), commands.newInsert("test"), commands.newInsert(1), commands.newFireAllRules())));
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("xxx: test", list.get(0));
        list.clear();
        ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-upgrade", "1.1.0");
        KieUtil.getKieModuleFromDrls(releaseId2, kieBaseTestConfiguration, drl1b);
        kc.updateToVersion(releaseId2);
        session = kc.getKieBase().newStatelessKieSession();
        session.execute(commands.newBatchExecution(Arrays.asList(commands.newSetGlobal("list", list), commands.newInsert("test"), commands.newInsert(1), commands.newFireAllRules())));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("yyy: test", list.get(0));
        Assert.assertEquals("1", list.get(1));
    }

    @Test
    public void testArgumentRedefinitionInStaticInvocation() {
        // RHDM-709
        final String ARG1 = "package org.test;" + (("    public class MyArg {\n" + "        private String value;\n") + "    }");
        final String ARG2 = "package org.test;" + (("    public class MyArg {\n" + "        private int value;\n") + "    }");
        final String FUNC = "package org.test;" + ((("    public class MyFunc {\n" + "        private String value;\n") + "        public static void func(MyArg arg) {}\n") + "    }");
        final String FUNC2 = "package org.test;" + ((("    public class MyFunc {\n" + "        private int value;\n") + "        public static void func(MyArg arg) {}\n") + "    }");
        final String DRL1 = "package org.test;\n" + (((("rule R dialect\"mvel\" when\n" + "        $arg : MyArg()\n") + "    then\n") + "        MyFunc.func($arg);") + "end");
        final KieServices ks = Factory.get();
        final KieFileSystem kfs = ks.newKieFileSystem();
        final ReleaseId id = ks.newReleaseId("org.test", "myTest", "1.0.0");
        final KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kfs.generateAndWritePomXML(id);
        kfs.write("src/main/java/org/test/MyArg.java", ks.getResources().newReaderResource(new StringReader(ARG1)));
        kfs.write("src/main/java/org/test/MyFunc.java", ks.getResources().newReaderResource(new StringReader(FUNC)));
        kfs.write(ks.getResources().newReaderResource(new StringReader(DRL1)).setResourceType(DRL).setSourcePath("rules.drl"));
        kieBuilder.buildAll();
        final KieContainer kc = ks.newKieContainer(id);
        final KieSession ksession = kc.newKieSession();
        final ReleaseId id2 = ks.newReleaseId("org.test", "myTest", "2.0.0");
        final KieFileSystem kfs2 = ks.newKieFileSystem();
        final KieBuilder kieBuilder2 = ks.newKieBuilder(kfs2);
        kfs2.generateAndWritePomXML(id2);
        kfs2.write("src/main/java/org/test/MyArg.java", ks.getResources().newReaderResource(new StringReader(ARG2)));
        kfs2.write("src/main/java/org/test/MyFunc.java", ks.getResources().newReaderResource(new StringReader(FUNC2)));
        kfs2.write(ks.getResources().newReaderResource(new StringReader(DRL1)).setResourceType(DRL).setSourcePath("rules.drl"));
        kieBuilder2.buildAll();
        final Results updateResults = kc.updateToVersion(id2);
        Assert.assertFalse(updateResults.hasMessages(Level.ERROR));
    }

    @Test
    public void testRemoveRulesWithLogicalAssertions() {
        // DROOLS-2646
        final String DRL1 = "declare MyInt\n" + (((((((((((((((((((((("    val : Integer @key\n" + "end\n") + "\n") + "rule R1 when\n") + "    String(this == \"I\'m active\")\n") + "    $i : Integer(intValue == 1)\n") + "then\n") + "    insertLogical(new MyInt($i));\n") + "end\n") + "\n") + "rule R2 when\n") + "    String()\n") + "    $i : Integer(intValue == 2)\n") + "then\n") + "    insertLogical(new MyInt($i));\n") + "end\n") + "\n") + "rule R3 when\n") + "    String(this == \"I\'m active\")\n") + "    $i : Integer(intValue == 3)\n") + "then\n") + "    insertLogical(new MyInt($i));\n") + "end");
        KieServices ks = KieServices.get();
        final ReleaseId id = ks.newReleaseId("org.test", "logical", "1.0.0");
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.generateAndWritePomXML(id);
        kfs.write(ks.getResources().newReaderResource(new StringReader(DRL1)).setResourceType(DRL).setSourcePath("rules.drl"));
        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        KieContainer kieContainer = ks.newKieContainer(kieModule.getReleaseId());
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert("I'm active");
        kieSession.insert(1);
        kieSession.insert(2);
        kieSession.insert(3);
        Assert.assertEquals(4, kieSession.getObjects().size());
        kieSession.fireAllRules();
        Assert.assertEquals(7, kieSession.getObjects().size());
        ReleaseId id2 = ks.newReleaseId("org.test", "logical", "2.0.0");
        KieFileSystem kfs2 = ks.newKieFileSystem();
        KieBuilder kieBuilder2 = ks.newKieBuilder(kfs2);
        kfs2.generateAndWritePomXML(id2);
        kieBuilder = ks.newKieBuilder(kfs2);
        kieBuilder.buildAll();
        kieModule = kieBuilder.getKieModule();
        kieContainer.updateToVersion(id2);
        kieSession.fireAllRules();
        Assert.assertEquals(4, kieSession.getObjects().size());
    }

    @Test
    public void testRemoveRulesWithAccumulateAndLogicalAssertions() {
        // DROOLS-3554
        final String DRL1 = "rule R1 when\n" + ((("    accumulate(String($l : length > 1); $s : sum($l))\n" + "then\n") + "    insertLogical($s);\n") + "end");
        KieServices ks = KieServices.get();
        final ReleaseId id = ks.newReleaseId("org.test", "logical", "1.0.0");
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.generateAndWritePomXML(id);
        kfs.write(ks.getResources().newReaderResource(new StringReader(DRL1)).setResourceType(DRL).setSourcePath("rules.drl"));
        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        KieContainer kieContainer = ks.newKieContainer(kieModule.getReleaseId());
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert("test");
        kieSession.insert("test2");
        Assert.assertEquals(2, kieSession.getObjects().size());
        kieSession.fireAllRules();
        Assert.assertEquals(3, kieSession.getObjects().size());
        Assert.assertEquals(9, kieSession.getObjects(new ClassObjectFilter(Integer.class)).iterator().next());
        ReleaseId id2 = ks.newReleaseId("org.test", "logical", "2.0.0");
        KieFileSystem kfs2 = ks.newKieFileSystem();
        KieBuilder kieBuilder2 = ks.newKieBuilder(kfs2);
        kfs2.generateAndWritePomXML(id2);
        kieBuilder = ks.newKieBuilder(kfs2);
        kieBuilder.buildAll();
        kieModule = kieBuilder.getKieModule();
        kieContainer.updateToVersion(id2);
        kieSession.fireAllRules();
        Assert.assertEquals(2, kieSession.getObjects().size());
        Assert.assertTrue(kieSession.getObjects(new ClassObjectFilter(Integer.class)).isEmpty());
    }
}

