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
package org.drools.workbench.models.guided.scorecard.backend.test1;


import KieServices.Factory;
import java.util.List;
import org.drools.workbench.models.guided.scorecard.backend.base.Helper;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.internal.builder.IncrementalResults;


public class GuidedScoreCardIntegrationJavaClassesOnClassPathTest {
    @Test
    public void testEmptyScoreCardCompilation() throws Exception {
        String xml1 = Helper.createEmptyGuidedScoreCardXML();
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("pom.xml", Helper.getPom());
        kfs.write("src/main/resources/META-INF/kmodule.xml", Helper.getKModule());
        kfs.write("src/main/resources/sc1.scgd", xml1);
        // Add complete Score Card
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        final List<Message> messages = kieBuilder.getResults().getMessages();
        Helper.dumpMessages(messages);
        Assert.assertEquals(0, messages.size());
    }

    @Test
    public void testCompletedScoreCardCompilation() throws Exception {
        String xml1 = Helper.createGuidedScoreCardXML(false);
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("pom.xml", Helper.getPom());
        kfs.write("src/main/resources/META-INF/kmodule.xml", Helper.getKModule());
        kfs.write("src/main/resources/sc1.scgd", xml1);
        // Add complete Score Card
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        final List<Message> messages = kieBuilder.getResults().getMessages();
        Helper.dumpMessages(messages);
        Assert.assertEquals(0, messages.size());
    }

    @Test
    public void testIncrementalCompilation() throws Exception {
        String xml1_1 = Helper.createEmptyGuidedScoreCardXML();
        String xml1_2 = Helper.createGuidedScoreCardXML(false);
        KieServices ks = Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("pom.xml", Helper.getPom());
        kfs.write("src/main/resources/META-INF/kmodule.xml", Helper.getKModule());
        kfs.write("src/main/resources/sc1.scgd", xml1_1);
        // Add empty Score Card
        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        final List<Message> messages = kieBuilder.getResults().getMessages();
        Helper.dumpMessages(messages);
        Assert.assertEquals(0, messages.size());
        // Update with complete Score Card
        kfs.write("src/main/resources/sc1.scgd", xml1_2);
        IncrementalResults results = incrementalBuild();
        final List<Message> addedMessages = results.getAddedMessages();
        final List<Message> removedMessages = results.getRemovedMessages();
        Helper.dumpMessages(addedMessages);
        Assert.assertEquals(0, addedMessages.size());
        Helper.dumpMessages(removedMessages);
        Assert.assertEquals(0, removedMessages.size());
    }
}

