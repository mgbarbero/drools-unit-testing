/**
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.drools.compiler.integrationtests.session;


import ResourceType.DRL;
import SequentialOption.YES;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.drools.compiler.Cheese;
import org.drools.compiler.Cheesery;
import org.drools.compiler.CommonTestMethodBase;
import org.drools.compiler.integrationtests.SerializationHelper;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.command.Command;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;
import org.mockito.Mockito;


public class StatelessSessionTest extends CommonTestMethodBase {
    final List list = new ArrayList();

    final Cheesery cheesery = new Cheesery();

    @Test
    public void testSingleObjectAssert() throws Exception {
        final StatelessKieSession session = getSession2("statelessSessionTest.drl");
        final Cheese stilton = new Cheese("stilton", 5);
        session.execute(stilton);
        Assert.assertEquals("stilton", list.get(0));
    }

    @Test
    public void testArrayObjectAssert() throws Exception {
        final StatelessKieSession session = getSession2("statelessSessionTest.drl");
        final Cheese stilton = new Cheese("stilton", 5);
        session.execute(Arrays.asList(new Object[]{ stilton }));
        Assert.assertEquals("stilton", list.get(0));
    }

    @Test
    public void testCollectionObjectAssert() throws Exception {
        final StatelessKieSession session = getSession2("statelessSessionTest.drl");
        final Cheese stilton = new Cheese("stilton", 5);
        final List collection = new ArrayList();
        collection.add(stilton);
        session.execute(collection);
        Assert.assertEquals("stilton", list.get(0));
    }

    @Test
    public void testInsertObject() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.drools.compiler.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( 30 ); \n";
        str += "end\n";
        Cheese stilton = new Cheese("stilton", 5);
        final StatelessKieSession ksession = getSession2(ResourceFactory.newByteArrayResource(str.getBytes()));
        final ExecutableCommand cmd = ((ExecutableCommand) (CommandFactory.newInsert(stilton, "outStilton")));
        final BatchExecutionCommandImpl batch = new BatchExecutionCommandImpl(Arrays.asList(new ExecutableCommand<?>[]{ cmd }));
        final ExecutionResults result = ((ExecutionResults) (ksession.execute(batch)));
        stilton = ((Cheese) (result.getValue("outStilton")));
        Assert.assertEquals(30, stilton.getPrice());
    }

    @Test
    public void testSetGlobal() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.drools.compiler.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "global java.util.List list2 \n";
        str += "global java.util.List list3 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( 30 ); \n";
        str += "    list1.add( $c ); \n";
        str += "    list2.add( $c ); \n";
        str += "    list3.add( $c ); \n";
        str += "end\n";
        final Cheese stilton = new Cheese("stilton", 5);
        final List list1 = new ArrayList();
        List list2 = new ArrayList();
        List list3 = new ArrayList();
        final StatelessKieSession ksession = getSession2(ResourceFactory.newByteArrayResource(str.getBytes()));
        final Command setGlobal1 = CommandFactory.newSetGlobal("list1", list1);
        final Command setGlobal2 = CommandFactory.newSetGlobal("list2", list2, true);
        final Command setGlobal3 = CommandFactory.newSetGlobal("list3", list3, "outList3");
        final Command insert = CommandFactory.newInsert(stilton);
        final List cmds = new ArrayList();
        cmds.add(setGlobal1);
        cmds.add(setGlobal2);
        cmds.add(setGlobal3);
        cmds.add(insert);
        final ExecutionResults result = ((ExecutionResults) (ksession.execute(CommandFactory.newBatchExecution(cmds))));
        Assert.assertEquals(30, stilton.getPrice());
        Assert.assertNull(result.getValue("list1"));
        list2 = ((List) (result.getValue("list2")));
        Assert.assertEquals(1, list2.size());
        Assert.assertSame(stilton, list2.get(0));
        list3 = ((List) (result.getValue("outList3")));
        Assert.assertEquals(1, list3.size());
        Assert.assertSame(stilton, list3.get(0));
    }

    @Test
    public void testQuery() throws Exception {
        String str = "";
        str += "package org.kie.test  \n";
        str += "import org.drools.compiler.Cheese \n";
        str += "query cheeses \n";
        str += "    stilton : Cheese(type == \'stilton\') \n";
        str += "    cheddar : Cheese(type == \'cheddar\', price == stilton.price) \n";
        str += "end\n";
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), DRL);
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        kbase = SerializationHelper.serializeObject(kbase);
        final StatelessKieSession ksession = kbase.newStatelessKieSession();
        final Cheese stilton1 = new Cheese("stilton", 1);
        final Cheese cheddar1 = new Cheese("cheddar", 1);
        final Cheese stilton2 = new Cheese("stilton", 2);
        final Cheese cheddar2 = new Cheese("cheddar", 2);
        final Cheese stilton3 = new Cheese("stilton", 3);
        final Cheese cheddar3 = new Cheese("cheddar", 3);
        final Set set = new HashSet();
        List list = new ArrayList();
        list.add(stilton1);
        list.add(cheddar1);
        set.add(list);
        list = new ArrayList();
        list.add(stilton2);
        list.add(cheddar2);
        set.add(list);
        list = new ArrayList();
        list.add(stilton3);
        list.add(cheddar3);
        set.add(list);
        final List<Command> cmds = new ArrayList<Command>();
        cmds.add(CommandFactory.newInsert(stilton1));
        cmds.add(CommandFactory.newInsert(stilton2));
        cmds.add(CommandFactory.newInsert(stilton3));
        cmds.add(CommandFactory.newInsert(cheddar1));
        cmds.add(CommandFactory.newInsert(cheddar2));
        cmds.add(CommandFactory.newInsert(cheddar3));
        cmds.add(CommandFactory.newQuery("cheeses", "cheeses"));
        final ExecutionResults batchResult = ((ExecutionResults) (ksession.execute(CommandFactory.newBatchExecution(cmds))));
        final QueryResults results = ((QueryResults) (batchResult.getValue("cheeses")));
        Assert.assertEquals(3, results.size());
        Assert.assertEquals(2, results.getIdentifiers().length);
        final Set newSet = new HashSet();
        for (final QueryResultsRow result : results) {
            list = new ArrayList();
            list.add(result.get("stilton"));
            list.add(result.get("cheddar"));
            newSet.add(list);
        }
        Assert.assertEquals(set, newSet);
    }

    @Test
    public void testNotInStatelessSession() throws Exception {
        final KieBaseConfiguration kbc = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        kbc.setOption(YES);
        final KieBase kbase = SerializationHelper.serializeObject(loadKnowledgeBase(kbc, "test_NotInStatelessSession.drl"));
        final StatelessKieSession session = kbase.newStatelessKieSession();
        final List list = new ArrayList();
        session.setGlobal("list", list);
        session.execute("not integer");
        Assert.assertEquals("not integer", list.get(0));
    }

    @Test
    public void testChannels() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.drools.compiler.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    channels[\"x\"].send( $c ); \n";
        str += "end\n";
        final Cheese stilton = new Cheese("stilton", 5);
        final Channel channel = Mockito.mock(Channel.class);
        final StatelessKieSession ksession = getSession2(ResourceFactory.newByteArrayResource(str.getBytes()));
        ksession.registerChannel("x", channel);
        Assert.assertEquals(1, ksession.getChannels().size());
        Assert.assertEquals(channel, ksession.getChannels().get("x"));
        ksession.execute(stilton);
        Mockito.verify(channel).send(stilton);
        ksession.unregisterChannel("x");
        Assert.assertEquals(0, ksession.getChannels().size());
        Assert.assertNull(ksession.getChannels().get("x"));
    }
}

