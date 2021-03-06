/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.compiler.simulation;


import KieServices.Factory;
import Scope.APPLICATION;
import Scope.CONVERSATION;
import Scope.REQUEST;
import org.drools.compiler.CommonTestMethodBase;
import org.drools.compiler.Message;
import org.drools.core.fluent.impl.ExecutableBuilderImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutableRunner;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.RequestContext;
import org.kie.internal.builder.fluent.ExecutableBuilder;
import org.kie.internal.builder.fluent.KieSessionFluent;


public class BatchRunFluentTest extends CommonTestMethodBase {
    String header = (("package org.drools.compiler\n" + "import ") + (Message.class.getCanonicalName())) + "\n";

    String drl1 = "global String outS;\n" + (((((("global Long timeNow;\n" + "rule R1 when\n") + "   s : String()\n") + "then\n") + "    kcontext.getKnowledgeRuntime().setGlobal(\"outS\", s);\n") + "    kcontext.getKnowledgeRuntime().setGlobal(\"timeNow\", kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n") + "end\n");

    ReleaseId releaseId = SimulateTestBase.createKJarWithMultipleResources("org.kie", new String[]{ (header) + (drl1) }, new ResourceType[]{ ResourceType.DRL });

    @Test
    public void testOutName() {
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").out("outS").dispose();
        RequestContext requestContext = ExecutableRunner.create().execute(f.getExecutable());
        Assert.assertEquals("h1", requestContext.getOutputs().get("outS"));
    }

    @Test
    public void testOutWithPriorSetAndNoName() {
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").set("outS").out().dispose();
        RequestContext requestContext = ExecutableRunner.create().execute(f.getExecutable());
        Assert.assertEquals("h1", requestContext.getOutputs().get("outS"));
        Assert.assertEquals("h1", requestContext.get("outS"));
    }

    @Test
    public void testSetAndOutBehaviour() {
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").set("outS").getGlobal("outS").set("outS1").out().dispose();
        RequestContext requestContext = ExecutableRunner.create().execute(f.getExecutable());
        Assert.assertNull(requestContext.getOutputs().get("outS"));
        Assert.assertEquals("h1", requestContext.get("outS"));
        Assert.assertNotNull(requestContext.getOutputs().get("outS1"));
        Assert.assertEquals(requestContext.get("outS1"), requestContext.getOutputs().get("outS1"));
        Assert.assertEquals(requestContext.get("outS"), requestContext.get("outS1"));
    }

    @Test
    public void testOutWithoutPriorSetAndNoName() {
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").out().dispose();
        try {
            RequestContext requestContext = ExecutableRunner.create().execute(f.getExecutable());
            Assert.assertEquals("h1", requestContext.get("out1"));
            Assert.fail("Must throw Exception, as no prior set was called and no name given to out");
        } catch (Exception e) {
        }
    }

    @Test
    public void testSetAndGetWithCommandRegisterWithEnds() {
        ExecutableBuilder f = ExecutableBuilder.create();
        // assign s1 to out
        // initialise s2 with data
        // initialise s1 with data
        // create two sessions, and assign names
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().set("s1").end().getKieContainer(releaseId).newSession().set("s2").end().get("s1", KieSessionFluent.class).insert("h1").fireAllRules().end().get("s2", KieSessionFluent.class).insert("h2").fireAllRules().end().get("s1", KieSessionFluent.class).getGlobal("outS").out("outS1").dispose().get("s2", KieSessionFluent.class).getGlobal("outS").out("outS2").dispose();
        RequestContext requestContext = ExecutableRunner.create().execute(f.getExecutable());
        // Check that nothing went to the 'out'
        Assert.assertEquals("h1", requestContext.getOutputs().get("outS1"));
        Assert.assertEquals("h2", requestContext.getOutputs().get("outS2"));
    }

    @Test
    public void testSetAndGetWithCommandRegisterWithoutEnds() {
        ExecutableBuilder f = ExecutableBuilder.create();
        // assign s1 to out
        // initialise s2 with data
        // initialise s1 with data
        // this end is needed, it's the get(String, Class) we are checking to see if it auto ends
        // create two sessions, and assign names
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().set("s1").end().getKieContainer(releaseId).newSession().set("s2").get("s1", KieSessionFluent.class).insert("h1").fireAllRules().get("s2", KieSessionFluent.class).insert("h2").fireAllRules().get("s1", KieSessionFluent.class).getGlobal("outS").out("outS1").dispose().get("s2", KieSessionFluent.class).getGlobal("outS").out("outS2").dispose();
        RequestContext requestContext = ExecutableRunner.create().execute(f.getExecutable());
        // Check that nothing went to the 'out'
        Assert.assertEquals("h1", requestContext.getOutputs().get("outS1"));
        Assert.assertEquals("h2", requestContext.getOutputs().get("outS2"));
    }

    @Test
    public void testDifferentConversationIds() {
        ExecutableRunner<RequestContext> runner = ExecutableRunner.create();
        RequestContext requestContext = runner.createContext();
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").startConversation().getKieContainer(releaseId).newSession().insert("h1").fireAllRules().dispose();
        runner.execute(f.getExecutable(), requestContext);
        String conversationId = requestContext.getConversationContext().getName();
        runner.execute(f.getExecutable(), requestContext);
        Assert.assertNotEquals(conversationId, requestContext.getConversationContext().getName());
    }

    @Test
    public void testRequestScope() {
        ExecutableBuilder f = ExecutableBuilder.create();
        // Request is default
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").set("outS1").dispose();
        RequestContext requestContext = ExecutableRunner.create().execute(f.getExecutable());
        // Check that nothing went to the 'out'
        Assert.assertNull(requestContext.get("outS"));
        Assert.assertNull(requestContext.getOutputs().get("outS1"));
        Assert.assertNull(requestContext.getApplicationContext().get("outS1"));
        Assert.assertNull(requestContext.getConversationContext());
        Assert.assertEquals("h1", requestContext.get("outS1"));
    }

    @Test
    public void testApplicationScope() {
        ExecutableRunner<RequestContext> runner = ExecutableRunner.create();
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").set("outS1", APPLICATION).dispose();
        RequestContext requestContext = runner.execute(f.getExecutable());
        // Check that nothing went to the 'out'
        Assert.assertEquals(null, requestContext.get("outS"));
        Assert.assertEquals("h1", requestContext.getApplicationContext().get("outS1"));
        // Make another request, add to application context, assert old and new values are there.
        f = new ExecutableBuilderImpl();
        f.getApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h2").fireAllRules().getGlobal("outS").set("outS2", APPLICATION).dispose();
        requestContext = ((RequestContext) (runner.execute(f.getExecutable())));
        Assert.assertEquals("h1", requestContext.getApplicationContext().get("outS1"));
        Assert.assertEquals("h2", requestContext.getApplicationContext().get("outS2"));
    }

    @Test
    public void testConversationScope() {
        ExecutableRunner<RequestContext> runner = ExecutableRunner.create();
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").startConversation().getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").set("outS1", CONVERSATION).dispose();
        RequestContext requestContext = ((RequestContext) (runner.execute(f.getExecutable())));
        // check that nothing went to the 'out'
        Assert.assertEquals(null, requestContext.get("outS"));
        String conversationId = requestContext.getConversationContext().getName();
        Assert.assertEquals("h1", requestContext.getConversationContext().get("outS1"));
        // Make another request, add to conversation context, assert old and new values are there.
        f = new ExecutableBuilderImpl();
        f.getApplicationContext("app1").joinConversation(conversationId).getKieContainer(releaseId).newSession().insert("h2").fireAllRules().getGlobal("outS").set("outS2", CONVERSATION).dispose();
        requestContext = ((RequestContext) (runner.execute(f.getExecutable())));
        Assert.assertEquals("h1", requestContext.getConversationContext().get("outS1"));
        Assert.assertEquals("h2", requestContext.getConversationContext().get("outS2"));
        // End the conversation, check it's now null
        f = new ExecutableBuilderImpl();
        f.endConversation(conversationId);
        requestContext = ((RequestContext) (runner.execute(f.getExecutable())));
        Assert.assertNull(requestContext.getConversationContext());
    }

    @Test
    public void testContextScopeSearching() {
        ExecutableRunner<RequestContext> runner = ExecutableRunner.create();
        ExecutableBuilder f = ExecutableBuilder.create();
        // Check that get() will search up to Application, when no request or conversation values
        f.newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").set("outS1", APPLICATION).get("outS1").out().dispose();
        RequestContext requestContext = runner.execute(f.getExecutable());
        Assert.assertEquals("h1", requestContext.get("outS1"));
        Assert.assertEquals("h1", requestContext.getApplicationContext().get("outS1"));
        Assert.assertEquals("h1", requestContext.get("outS1"));
        // Check that get() will search up to Conversation, thus over-riding Application scope and ignoring Request when it has no value
        f = new ExecutableBuilderImpl();
        f.getApplicationContext("app1").startConversation().getKieContainer(releaseId).newSession().insert("h2").fireAllRules().getGlobal("outS").set("outS1", CONVERSATION).get("outS1").out().dispose();
        requestContext = runner.execute(f.getExecutable());
        Assert.assertEquals("h2", requestContext.get("outS1"));
        Assert.assertEquals("h1", requestContext.getApplicationContext().get("outS1"));
        Assert.assertEquals("h2", requestContext.getConversationContext().get("outS1"));
        Assert.assertEquals("h2", requestContext.get("outS1"));
        // Check that get() will search directly to Request, thus over-riding Application and Conversation scoped values
        f = new ExecutableBuilderImpl();
        f.getApplicationContext("app1").joinConversation(requestContext.getConversationContext().getName()).getKieContainer(releaseId).newSession().insert("h3").fireAllRules().getGlobal("outS").set("outS1", REQUEST).get("outS1").out().dispose();
        requestContext = runner.execute(f.getExecutable());
        Assert.assertEquals("h3", requestContext.get("outS1"));
        Assert.assertEquals("h1", requestContext.getApplicationContext().get("outS1"));
        Assert.assertEquals("h2", requestContext.getConversationContext().get("outS1"));
        Assert.assertEquals("h3", requestContext.get("outS1"));
    }

    @Test
    public void testAfter() {
        ExecutableRunner<RequestContext> runner = ExecutableRunner.create(0L);
        ExecutableBuilder f = ExecutableBuilder.create();
        // Check that get() will search up to Application, when no request or conversation values
        f.after(1000).newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").out("outS1").getGlobal("timeNow").out("timeNow1").dispose().after(2000).newApplicationContext("app1").getKieContainer(releaseId).newSession().insert("h1").fireAllRules().getGlobal("outS").out("outS2").getGlobal("timeNow").out("timeNow2").dispose();
        RequestContext requestContext = runner.execute(f.getExecutable());
        Assert.assertEquals(1000L, requestContext.getOutputs().get("timeNow1"));
        Assert.assertEquals(2000L, requestContext.getOutputs().get("timeNow2"));
    }

    @Test
    public void testSetKieContainerTest() {
        KieServices kieServices = Factory.get();
        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        ExecutableRunner<RequestContext> runner = ExecutableRunner.create(0L);
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").setKieContainer(kieContainer).newSession().insert("h1").fireAllRules().getGlobal("outS").out("outS1").dispose();
        RequestContext requestContext = runner.execute(f.getExecutable());
        Assert.assertEquals("h1", requestContext.getOutputs().get("outS1"));
    }

    @Test
    public void testKieSessionCustomizationTest() {
        ExecutableRunner<RequestContext> runner = ExecutableRunner.create(0L);
        ExecutableBuilder f = ExecutableBuilder.create();
        f.newApplicationContext("app1").getKieContainer(releaseId).newSessionCustomized(null, ( sessionName, kieContainer) -> {
            KieSessionModel kieSessionModel = kieContainer.getKieSessionModel(sessionName);
            kieSessionModel.setConsoleLogger("newConsoleLoggerName");
            return kieContainer;
        }).insert("h1").fireAllRules().getGlobal("outS").out("outS1").dispose();
        RequestContext requestContext = runner.execute(f.getExecutable());
        Assert.assertEquals("h1", requestContext.getOutputs().get("outS1"));
    }
}

