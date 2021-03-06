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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.drools.compiler.Address;
import org.drools.compiler.Cheese;
import org.drools.compiler.CommonTestMethodBase;
import org.drools.compiler.IndexedNumber;
import org.drools.compiler.OuterClass;
import org.drools.compiler.Person;
import org.drools.compiler.Target;
import org.drools.compiler.integrationtests.SerializationHelper;
import org.drools.compiler.integrationtests.facts.AFact;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.command.Setter;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


public class UpdateTest extends CommonTestMethodBase {
    @Test
    public void testModifyBlock() throws Exception {
        doModifyTest("test_ModifyBlock.drl");
    }

    @Test
    public void testModifyBlockWithPolymorphism() throws Exception {
        doModifyTest("test_ModifyBlockWithPolymorphism.drl");
    }

    @Test
    public void testModifyBlockWithFrom() throws Exception {
        final KieBase kbase = SerializationHelper.serializeObject(loadKnowledgeBase("test_ModifyBlockWithFrom.drl"));
        final KieSession ksession = createKnowledgeSession(kbase);
        final List results = new ArrayList();
        ksession.setGlobal("results", results);
        final Person bob = new Person("Bob");
        final Address addr = new Address("abc");
        bob.addAddress(addr);
        ksession.insert(bob);
        ksession.insert(addr);
        ksession.fireAllRules();
        // modify worked
        Assert.assertEquals("12345", addr.getZipCode());
        // chaining worked
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(addr, results.get(0));
    }

    // this test requires mvel 1.2.19. Leaving it commented until mvel is released.
    @Test
    public void testJavaModifyBlock() throws Exception {
        final KieBase kbase = SerializationHelper.serializeObject(loadKnowledgeBase("test_JavaModifyBlock.drl"));
        final KieSession ksession = createKnowledgeSession(kbase);
        final List list = new ArrayList();
        ksession.setGlobal("results", list);
        final Person bob = new Person("Bob", 30);
        bob.setStatus("hungry");
        ksession.insert(bob);
        ksession.insert(new Cheese());
        ksession.insert(new Cheese());
        ksession.insert(new OuterClass.InnerClass(1));
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("full", bob.getStatus());
        Assert.assertEquals(31, bob.getAge());
        Assert.assertEquals(2, ((OuterClass.InnerClass) (list.get(1))).getIntAttr());
    }

    @Test
    public void testModifyJava() {
        testModifyWithDialect("java");
    }

    @Test
    public void testModifyMVEL() {
        testModifyWithDialect("mvel");
    }

    @Test
    public void testModifySimple() {
        final String str = "package org.drools.compiler;\n" + ((((((((((((("\n" + "rule \"test modify block\"\n") + "when\n") + "    $p: Person( name == \"hungry\" )\n") + "then\n") + "    modify( $p ) { setName(\"fine\") }\n") + "end\n") + "\n") + "rule \"Log\"\n") + "when\n") + "    $o: Object()\n") + "then\n") + "    System.out.println( $o );\n") + "end");
        final KieBase kbase = loadKnowledgeBaseFromString(str);
        final KieSession ksession = kbase.newKieSession();
        final Person p = new Person("hungry");
        ksession.insert(p);
        ksession.fireAllRules();
        ksession.dispose();
    }

    @Test
    public void testModifyWithLockOnActive() throws Exception {
        final KieBase kbase = SerializationHelper.serializeObject(loadKnowledgeBase("test_ModifyWithLockOnActive.drl"));
        final KieSession session = createKnowledgeSession(kbase);
        final List results = new ArrayList();
        session.setGlobal("results", results);
        final Person bob = new Person("Bob", 15);
        final Person mark = new Person("Mark", 16);
        final Person michael = new Person("Michael", 14);
        session.insert(bob);
        session.insert(mark);
        session.insert(michael);
        session.getAgenda().getAgendaGroup("feeding").setFocus();
        session.fireAllRules(5);
        Assert.assertEquals(2, ((List) (session.getGlobal("results"))).size());
    }

    @Test
    public void testMissingClosingBraceOnModify() throws Exception {
        // JBRULES-3436
        final String str = "package org.drools.compiler.test;\n" + (((((("import org.drools.compiler.*\n" + "rule R1 when\n") + "   $p : Person( )") + "   $c : Cheese( )") + "then\n") + "   modify($p) { setCheese($c) ;\n") + "end\n");
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), DRL);
        Assert.assertTrue(kbuilder.hasErrors());
    }

    @Test
    public void testInvalidModify1() throws Exception {
        String str = "";
        str += "package org.drools.compiler \n";
        str += ("import " + (Cheese.class.getName())) + "\n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    no-loop \n";
        str += "when \n";
        str += "    $i : Cheese() \n";
        str += "then \n";
        str += "    modify( $i ); ";
        str += "    list.add( $i ); \n";
        str += "end \n";
        testInvalidDrl(str);
    }

    @Test
    public void testInvalidModify2() throws Exception {
        String str = "";
        str += "package org.drools.compiler \n";
        str += ("import " + (Cheese.class.getName())) + "\n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    no-loop \n";
        str += "when \n";
        str += "    $i : Cheese() \n";
        str += "then \n";
        str += "    modify( $i ) { setType( \"stilton\" ); setType( \"stilton\" );}; ";
        str += "    list.add( $i ); \n";
        str += "end \n";
        testInvalidDrl(str);
    }

    @Test
    public void testJoinNodeModifyObject() throws IOException, ClassNotFoundException {
        final KieBase kbase = SerializationHelper.serializeObject(loadKnowledgeBase("test_JoinNodeModifyObject.drl"));
        final KieSession ksession = kbase.newKieSession();
        final List orderedFacts = new ArrayList();
        final List errors = new ArrayList();
        ksession.setGlobal("orderedNumbers", orderedFacts);
        ksession.setGlobal("errors", errors);
        final int MAX = 2;
        for (int i = 1; i <= MAX; i++) {
            final IndexedNumber n = new IndexedNumber(i, ((MAX - i) + 1));
            ksession.insert(n);
        }
        ksession.fireAllRules();
        Assert.assertTrue(("Processing generated errors: " + (errors.toString())), errors.isEmpty());
        for (int i = 1; i <= MAX; i++) {
            final IndexedNumber n = ((IndexedNumber) (orderedFacts.get((i - 1))));
            Assert.assertEquals("Fact is out of order", i, n.getIndex());
        }
    }

    @Test
    public void testModifyCommand() {
        final String str = "rule \"sample rule\"\n" + ((("   when\n" + "   then\n") + "       System.out.println(\"\\\"Hello world!\\\"\");\n") + "end");
        final KieBase kbase = loadKnowledgeBaseFromString(str);
        final KieSession ksession = kbase.newKieSession();
        final Person p1 = new Person("John", "nobody", 25);
        ksession.execute(CommandFactory.newInsert(p1));
        final FactHandle fh = ksession.getFactHandle(p1);
        final Person p = new Person("Frank", "nobody", 30);
        final List<Setter> setterList = new ArrayList<Setter>();
        setterList.add(CommandFactory.newSetter("age", String.valueOf(p.getAge())));
        setterList.add(CommandFactory.newSetter("name", p.getName()));
        setterList.add(CommandFactory.newSetter("likes", p.getLikes()));
        ksession.execute(CommandFactory.newModify(fh, setterList));
    }

    @Test
    public void testNotIterativeModifyBug() {
        // JBRULES-2809
        // This bug occurs when a tuple is modified, the remove/add puts it onto the memory end
        // However before this was done it would attempt to find the next tuple, starting from itself
        // This meant it would just re-add itself as the blocker, but then be moved to end of the memory
        // If this tuple was then removed or changed, the blocked was unable to check previous tuples.
        String str = "";
        str += "package org.simple \n";
        str += ("import " + (AFact.class.getCanonicalName())) + "\n";
        str += "global java.util.List list \n";
        str += "rule xxx \n";
        str += "when \n";
        str += "  $f1 : AFact() \n";
        str += "    not AFact(this != $f1,  eval(field2 == $f1.getField2())) \n";
        str += "    eval( !$f1.getField1().equals(\"1\") ) \n";
        str += "then \n";
        str += "  list.add($f1); \n";
        str += "end  \n";
        final KieBase kbase = loadKnowledgeBaseFromString(str);
        final KieSession ksession = createKnowledgeSession(kbase);
        final List list = new ArrayList();
        ksession.setGlobal("list", list);
        final AFact a1 = new AFact("2", "2");
        final AFact a2 = new AFact("1", "2");
        final AFact a3 = new AFact("1", "2");
        final FactHandle fa1 = ksession.insert(a1);
        final FactHandle fa2 = ksession.insert(a2);
        final FactHandle fa3 = ksession.insert(a3);
        ksession.fireAllRules();
        // a1 is blocked by a2
        Assert.assertEquals(0, list.size());
        // modify a2, so that a1 is now blocked by a3
        a2.setField2("1");// Do

        ksession.update(fa2, a2);
        a2.setField2("2");// Undo

        ksession.update(fa2, a2);
        // modify a3 to cycle, so that it goes on the memory end, but in a previous bug still blocked a1
        ksession.update(fa3, a3);
        a3.setField2("1");// Do

        ksession.update(fa3, a3);
        ksession.fireAllRules();
        Assert.assertEquals(0, list.size());// this should still now blocked by a2, but bug from previous update hanging onto blocked

        ksession.dispose();
    }

    @Test
    public void testLLR() throws Exception {
        final KieBase kbase = SerializationHelper.serializeObject(loadKnowledgeBase("test_JoinNodeModifyTuple.drl"));
        KieSession ksession = createKnowledgeSession(kbase);
        ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
        // 1st time
        Target tgt = new Target();
        tgt.setLabel("Santa-Anna");
        tgt.setLat(60.26544F);
        tgt.setLon(28.952137F);
        tgt.setCourse(145.0F);
        tgt.setSpeed(12.0F);
        tgt.setTime(1.8666667F);
        ksession.insert(tgt);
        tgt = new Target();
        tgt.setLabel("Santa-Maria");
        tgt.setLat(60.236874F);
        tgt.setLon(28.992579F);
        tgt.setCourse(325.0F);
        tgt.setSpeed(8.0F);
        tgt.setTime(1.8666667F);
        ksession.insert(tgt);
        ksession.fireAllRules();
        // 2nd time
        tgt = new Target();
        tgt.setLabel("Santa-Anna");
        tgt.setLat(60.265343F);
        tgt.setLon(28.952267F);
        tgt.setCourse(145.0F);
        tgt.setSpeed(12.0F);
        tgt.setTime(1.9F);
        ksession.insert(tgt);
        tgt = new Target();
        tgt.setLabel("Santa-Maria");
        tgt.setLat(60.236935F);
        tgt.setLon(28.992493F);
        tgt.setCourse(325.0F);
        tgt.setSpeed(8.0F);
        tgt.setTime(1.9F);
        ksession.insert(tgt);
        ksession.fireAllRules();
        // 3d time
        tgt = new Target();
        tgt.setLabel("Santa-Anna");
        tgt.setLat(60.26525F);
        tgt.setLon(28.952396F);
        tgt.setCourse(145.0F);
        tgt.setSpeed(12.0F);
        tgt.setTime(1.9333333F);
        ksession.insert(tgt);
        tgt = new Target();
        tgt.setLabel("Santa-Maria");
        tgt.setLat(60.236996F);
        tgt.setLon(28.992405F);
        tgt.setCourse(325.0F);
        tgt.setSpeed(8.0F);
        tgt.setTime(1.9333333F);
        ksession.insert(tgt);
        ksession.fireAllRules();
        // 4th time
        tgt = new Target();
        tgt.setLabel("Santa-Anna");
        tgt.setLat(60.265163F);
        tgt.setLon(28.952526F);
        tgt.setCourse(145.0F);
        tgt.setSpeed(12.0F);
        tgt.setTime(1.9666667F);
        ksession.insert(tgt);
        tgt = new Target();
        tgt.setLabel("Santa-Maria");
        tgt.setLat(60.237057F);
        tgt.setLon(28.99232F);
        tgt.setCourse(325.0F);
        tgt.setSpeed(8.0F);
        tgt.setTime(1.9666667F);
        ksession.insert(tgt);
        ksession.fireAllRules();
    }

    @Test
    public void noDormantCheckOnModifies() throws Exception {
        // Test case for BZ 862325
        final String str = "package org.drools.compiler;\n" + ((((((((((((((" rule R1\n" + "    salience 10\n") + "    when\n") + "        $c : Cheese( price == 10 ) \n") + "        $p : Person( ) \n") + "    then \n") + "        modify($c) { setPrice( 5 ) }\n") + "        modify($p) { setAge( 20 ) }\n") + "end\n") + "rule R2\n") + "    when\n") + "        $p : Person( )") + "    then \n") + "        // noop\n") + "end\n");
        // load up the knowledge base
        final KieBase kbase = loadKnowledgeBaseFromString(str);
        final KieSession ksession = kbase.newKieSession();
        final AgendaEventListener ael = Mockito.mock(AgendaEventListener.class);
        ksession.addEventListener(ael);
        ksession.insert(new Person("Bob", 19));
        ksession.insert(new Cheese("brie", 10));
        ksession.fireAllRules();
        // both rules should fire exactly once
        Mockito.verify(ael, Mockito.times(2)).afterMatchFired(ArgumentMatchers.any(AfterMatchFiredEvent.class));
        // no cancellations should have happened
        Mockito.verify(ael, Mockito.never()).matchCancelled(ArgumentMatchers.any(MatchCancelledEvent.class));
    }
}

