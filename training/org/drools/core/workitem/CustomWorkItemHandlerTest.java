/**
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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
package org.drools.core.workitem;


import java.util.Map;
import java.util.Properties;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;


public class CustomWorkItemHandlerTest {
    @Test
    public void testRegisterHandlerWithKsessionUsingConfiguration() {
        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Properties props = new Properties();
        props.setProperty("drools.workItemHandlers", "CustomWorkItemHandlers.conf");
        KieSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(props);
        KieSession ksession = kbase.newKieSession(config, EnvironmentFactory.newEnvironment());
        Assert.assertNotNull(ksession);
        // this test would fail on creation of the work item manager if injecting session is not supported
        WorkItemManager manager = ksession.getWorkItemManager();
        Assert.assertNotNull(manager);
        Map<String, WorkItemHandler> handlers = getWorkItemHandlers();
        Assert.assertNotNull(handlers);
        Assert.assertEquals(1, handlers.size());
        Assert.assertTrue(handlers.containsKey("Custom"));
    }
}

