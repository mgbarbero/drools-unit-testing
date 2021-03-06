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
package org.drools.compiler.integrationtests;


import KieServices.Factory;
import Level.ERROR;
import ResourceType.JAVA;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.drools.compiler.CommonTestMethodBase;
import org.drools.compiler.compiler.io.Folder;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.core.impl.InternalKieContainer;
import org.drools.core.util.DroolsAssert;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;


public class KieContainerTest extends CommonTestMethodBase {
    @Test
    public void testMainKieModule() {
        KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
        CommonTestMethodBase.createAndDeployJar(ks, releaseId, createDRL("ruleA"));
        KieContainer kieContainer = ks.newKieContainer(releaseId);
        KieModule kmodule = getMainKieModule();
        Assert.assertEquals(releaseId, kmodule.getReleaseId());
    }

    @Test
    public void testUpdateToNonExistingRelease() {
        // DROOLS-1562
        KieServices ks = Factory.get();
        ReleaseId releaseId = ks.newReleaseId("org.kie", "test-release", "1.0.0");
        CommonTestMethodBase.createAndDeployJar(ks, releaseId, createDRL("ruleA"));
        KieContainer kieContainer = ks.newKieContainer(releaseId);
        Results results = kieContainer.updateToVersion(ks.newReleaseId("org.kie", "test-release", "1.0.1"));
        Assert.assertEquals(1, results.getMessages(ERROR).size());
        Assert.assertEquals("1.0.0", getContainerReleaseId().getVersion());
    }

    @Test
    public void testReleaseIdGetters() {
        KieServices ks = Factory.get();
        ReleaseId releaseId = ks.newReleaseId("org.kie", "test-delete-v", "1.0.1");
        CommonTestMethodBase.createAndDeployJar(ks, releaseId, createDRL("ruleA"));
        ReleaseId configuredReleaseId = ks.newReleaseId("org.kie", "test-delete-v", "RELEASE");
        KieContainer kieContainer = ks.newKieContainer(configuredReleaseId);
        InternalKieContainer iKieContainer = ((InternalKieContainer) (kieContainer));
        Assert.assertEquals(configuredReleaseId, iKieContainer.getConfiguredReleaseId());
        Assert.assertEquals(releaseId, iKieContainer.getResolvedReleaseId());
        Assert.assertEquals(releaseId, iKieContainer.getReleaseId());
        // demonstrate internal API behavior, in the future shall this be enforced?
        Assert.assertEquals(configuredReleaseId, iKieContainer.getContainerReleaseId());
        ReleaseId newReleaseId = ks.newReleaseId("org.kie", "test-delete-v", "1.0.2");
        CommonTestMethodBase.createAndDeployJar(ks, newReleaseId, createDRL("ruleA"));
        iKieContainer.updateToVersion(newReleaseId);
        Assert.assertEquals(configuredReleaseId, iKieContainer.getConfiguredReleaseId());
        Assert.assertEquals(newReleaseId, iKieContainer.getResolvedReleaseId());
        Assert.assertEquals(newReleaseId, iKieContainer.getReleaseId());
        // demonstrate internal API behavior, in the future shall this be enforced?
        Assert.assertEquals(newReleaseId, iKieContainer.getContainerReleaseId());
    }

    @Test
    public void testSharedTypeDeclarationsUsingClassLoader() throws Exception {
        String type = "package org.drools.test\n" + (("declare Message\n" + "   message : String\n") + "end\n");
        String drl1 = "package org.drools.test\n" + (((((("rule R1 when\n" + "   $o : Object()\n") + "then\n") + "   if ($o.getClass().getName().equals(\"org.drools.test.Message\") && $o.getClass() != new Message(\"Test\").getClass()) {\n") + "       throw new RuntimeException();\n") + "   }\n") + "end\n");
        String drl2 = "package org.drools.test\n" + (((((("rule R2_2 when\n" + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "   if ($m.getClass() != new Message(\"Test\").getClass()) {\n") + "       throw new RuntimeException();\n") + "   }\n") + "end\n");
        KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
        KieModule km = CommonTestMethodBase.createAndDeployJar(ks, releaseId1, type, drl1, drl2);
        KieContainer kieContainer = ks.newKieContainer(releaseId1);
        KieContainer kieContainer2 = ks.newKieContainer(releaseId1);
        KieSession ksession = kieContainer.newKieSession();
        KieSession ksession2 = kieContainer2.newKieSession();
        Class cls1 = kieContainer.getClassLoader().loadClass("org.drools.test.Message");
        Constructor constructor = cls1.getConstructor(String.class);
        ksession.insert(constructor.newInstance("Hello World"));
        Assert.assertEquals(2, ksession.fireAllRules());
        Class cls2 = kieContainer2.getClassLoader().loadClass("org.drools.test.Message");
        Constructor constructor2 = cls2.getConstructor(String.class);
        ksession2.insert(constructor2.newInstance("Hello World"));
        Assert.assertEquals(2, ksession2.fireAllRules());
        Assert.assertNotSame(cls1, cls2);
    }

    @Test
    public void testSharedTypeDeclarationsUsingFactTypes() throws Exception {
        String type = "package org.drools.test\n" + (("declare Message\n" + "   message : String\n") + "end\n");
        String drl1 = "package org.drools.test\n" + (((((("rule R1 when\n" + "   $m : Message()\n") + "then\n") + "   if ($m.getClass() != new Message(\"Test\").getClass()) {\n") + "       throw new RuntimeException();\n") + "   }\n") + "end\n");
        String drl2 = "package org.drools.test\n" + (((((("rule R2_2 when\n" + "   $m : Message( message == \"Hello World\" )\n") + "then\n") + "   if ($m.getClass() != new Message(\"Test\").getClass()) {\n") + "       throw new RuntimeException();\n") + "   }\n") + "end\n");
        KieServices ks = Factory.get();
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId1 = ks.newReleaseId("org.kie", "test-delete", "1.0.0");
        CommonTestMethodBase.createAndDeployJar(ks, releaseId1, type, drl1, drl2);
        KieContainer kieContainer = ks.newKieContainer(releaseId1);
        KieContainer kieContainer2 = ks.newKieContainer(releaseId1);
        KieSession ksession = kieContainer.newKieSession();
        KieSession ksession2 = kieContainer2.newKieSession();
        insertMessageFromTypeDeclaration(ksession);
        Assert.assertEquals(2, ksession.fireAllRules());
        ReleaseId releaseId2 = ks.newReleaseId("org.kie", "test-delete", "1.0.1");
        CommonTestMethodBase.createAndDeployJar(ks, releaseId2, type, null, drl2);
        kieContainer.updateToVersion(releaseId2);
        // test with the old ksession ...
        ksession = kieContainer.newKieSession();
        insertMessageFromTypeDeclaration(ksession);
        Assert.assertEquals(1, ksession.fireAllRules());
        // ... and with a brand new one
        ksession = kieContainer.newKieSession();
        insertMessageFromTypeDeclaration(ksession);
        Assert.assertEquals(1, ksession.fireAllRules());
        // check that the second kieContainer hasn't been affected by the update of the first one
        insertMessageFromTypeDeclaration(ksession2);
        Assert.assertEquals(2, ksession2.fireAllRules());
        ksession2 = kieContainer2.newKieSession();
        insertMessageFromTypeDeclaration(ksession2);
        Assert.assertEquals(2, ksession2.fireAllRules());
    }

    @Test(timeout = 10000)
    public void testIncrementalCompilationSynchronization() throws Exception {
        final KieServices kieServices = Factory.get();
        ReleaseId releaseId = kieServices.newReleaseId("org.kie.test", "sync-scanner-test", "1.0.0");
        CommonTestMethodBase.createAndDeployJar(kieServices, releaseId, createDRL("rule0"));
        final KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieSession kieSession = kieContainer.newKieSession();
        List<String> list = new ArrayList<String>();
        kieSession.setGlobal("list", list);
        kieSession.fireAllRules();
        kieSession.dispose();
        Assert.assertEquals(1, list.size());
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i < 10; i++) {
                    ReleaseId releaseId = kieServices.newReleaseId("org.kie.test", "sync-scanner-test", ("1.0." + i));
                    CommonTestMethodBase.createAndDeployJar(kieServices, releaseId, createDRL(("rule" + i)));
                    kieContainer.updateToVersion(releaseId);
                }
            }
        });
        t.setDaemon(true);
        t.start();
        while (true) {
            kieSession = kieContainer.newKieSession();
            list = new ArrayList<String>();
            kieSession.setGlobal("list", list);
            kieSession.fireAllRules();
            kieSession.dispose();
            // There can be multiple items in the list if an updateToVersion is triggered during a fireAllRules
            // (updateToVersion can be called multiple times during fireAllRules, especially on slower machines)
            // in that case it may fire with the old rule and multiple new ones
            Assertions.assertThat(list).isNotEmpty();
            if (list.get(0).equals("rule9")) {
                break;
            }
        } 
    }

    @Test
    public void testMemoryFileSystemFolderUniqueness() {
        KieServices kieServices = Factory.get();
        String drl = "package org.drools.test\n" + ((("rule R1 when\n" + "   $m : Object()\n") + "then\n") + "end\n");
        Resource resource = kieServices.getResources().newReaderResource(new StringReader(drl), "UTF-8");
        resource.setTargetPath("org/drools/test/rules.drl");
        String kmodule = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + (((("<kmodule xmlns=\"http://www.drools.org/xsd/kmodule\">\n" + "  <kbase name=\"testKbase\" packages=\"org.drools.test\">\n") + "    <ksession name=\"testKsession\"/>\n") + "  </kbase>\n") + "</kmodule>");
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId = kieServices.newReleaseId("org.kie", "test-delete", "1.0.0");
        CommonTestMethodBase.createAndDeployJar(kieServices, kmodule, releaseId, resource);
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieModule kieModule = getMainKieModule();
        MemoryFileSystem memoryFileSystem = getMemoryFileSystem();
        Folder rootFolder = memoryFileSystem.getFolder("");
        Object[] members = rootFolder.getMembers().toArray();
        Assert.assertEquals(2, members.length);
        Folder firstFolder = ((Folder) (members[0]));
        Folder secondFolder = ((Folder) (members[1]));
        Assert.assertEquals(firstFolder.getParent(), secondFolder.getParent());
    }

    @Test
    public void testClassLoaderGetResources() throws IOException {
        KieServices kieServices = Factory.get();
        String drl1 = "package org.drools.testdrl;\n" + ((("rule R1 when\n" + "   $m : Object()\n") + "then\n") + "end\n");
        Resource resource1 = kieServices.getResources().newReaderResource(new StringReader(drl1), "UTF-8");
        resource1.setTargetPath("org/drools/testdrl/rules1.drl");
        String drl2 = "package org.drools.testdrl;\n" + ((("rule R2 when\n" + "   $m : Object()\n") + "then\n") + "end\n");
        Resource resource2 = kieServices.getResources().newReaderResource(new StringReader(drl2), "UTF-8");
        resource2.setTargetPath("org/drools/testdrl/rules2.drl");
        String java3 = "package org.drools.testjava;\n" + "public class Message {}";
        Resource resource3 = kieServices.getResources().newReaderResource(new StringReader(java3), "UTF-8");
        resource3.setTargetPath("org/drools/testjava/Message.java");
        resource3.setResourceType(JAVA);
        String kmodule = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + (((("<kmodule xmlns=\"http://www.drools.org/xsd/kmodule\">\n" + "  <kbase name=\"testKbase\" packages=\"org.drools.testdrl\">\n") + "    <ksession name=\"testKsession\"/>\n") + "  </kbase>\n") + "</kmodule>");
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId = kieServices.newReleaseId("org.kie", "test-delete", "1.0.0");
        CommonTestMethodBase.createAndDeployJar(kieServices, kmodule, releaseId, resource1, resource2, resource3);
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        ClassLoader classLoader = kieContainer.getClassLoader();
        DroolsAssert.assertEnumerationSize(1, classLoader.getResources("org/drools/testjava"));// no trailing "/"

        DroolsAssert.assertEnumerationSize(1, classLoader.getResources("org/drools/testdrl/"));// trailing "/" to test both variants

        // make sure the package resource correctly lists all its child resources (files in this case)
        URL url = classLoader.getResources("org/drools/testdrl").nextElement();
        List<String> lines = IOUtils.readLines(url.openStream());
        Assertions.assertThat(lines).contains("rules1.drl", "rules1.drl.properties", "rules2.drl", "rules2.drl.properties");
        DroolsAssert.assertUrlEnumerationContainsMatch("^mfs\\:/$", classLoader.getResources(""));
    }

    @Test
    public void testGetDefaultKieSessionModel() {
        KieServices kieServices = Factory.get();
        String drl = "package org.drools.test\n" + ((("rule R1 when\n" + "   $m : Object()\n") + "then\n") + "end\n");
        Resource resource = kieServices.getResources().newReaderResource(new StringReader(drl), "UTF-8");
        resource.setTargetPath("org/drools/test/rules.drl");
        String kmodule = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + (((("<kmodule xmlns=\"http://www.drools.org/xsd/kmodule\">\n" + "  <kbase name=\"testKbase\" packages=\"org.drools.test\">\n") + "    <ksession name=\"testKsession\" default=\"true\"/>\n") + "  </kbase>\n") + "</kmodule>");
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId = kieServices.newReleaseId("org.kie", "test-testGetDefaultKieSessionModel", "1.0.0");
        CommonTestMethodBase.createAndDeployJar(kieServices, kmodule, releaseId, resource);
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieSessionModel sessionModel = kieContainer.getKieSessionModel(null);
        Assert.assertNotNull(sessionModel);
        Assert.assertEquals("testKsession", sessionModel.getName());
    }

    @Test
    public void testGetDefaultKieSessionModelEmptyKmodule() {
        KieServices kieServices = Factory.get();
        String drl = "package org.drools.test\n" + ((("rule R1 when\n" + "   $m : Object()\n") + "then\n") + "end\n");
        Resource resource = kieServices.getResources().newReaderResource(new StringReader(drl), "UTF-8");
        resource.setTargetPath("org/drools/test/rules.drl");
        String kmodule = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + ("<kmodule xmlns=\"http://www.drools.org/xsd/kmodule\">\n" + "</kmodule>");
        // Create an in-memory jar for version 1.0.0
        ReleaseId releaseId = kieServices.newReleaseId("org.kie", "test-testGetDefaultKieSessionModelEmptyKmodule", "1.0.0");
        CommonTestMethodBase.createAndDeployJar(kieServices, kmodule, releaseId, resource);
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieSessionModel sessionModel = kieContainer.getKieSessionModel(null);
        Assert.assertNotNull(sessionModel);
    }
}

