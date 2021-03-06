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
package org.drools.compiler.integrationtests;


import ClockType.PSEUDO_CLOCK;
import EventProcessingOption.STREAM;
import ResourceType.DRL;
import Role.Type;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.drools.compiler.integrationtests.facts.BasicEvent;
import org.drools.core.ClassObjectFilter;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Expires.Policy;
import org.kie.api.definition.type.Role;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.utils.KieHelper;


public class ExpirationTest {
    @Test
    public void testAlpha() {
        String drl = ((((((((("import " + (ExpirationTest.A.class.getCanonicalName())) + "\n") + "declare A @role( event ) @expires(11ms) end\n") + "global java.util.concurrent.atomic.AtomicInteger counter;\n") + "rule R0 when\n") + "  $a: A( $Aid: id > 0 )\n") + "then\n") + "  System.out.println(\"[\" + $a + \"]\");") + "  counter.incrementAndGet();\n") + "end";
        KieSessionConfiguration sessionConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConfig.setOption(ClockTypeOption.get(PSEUDO_CLOCK.getId()));
        KieHelper helper = new KieHelper();
        helper.addContent(drl, DRL);
        KieBase kbase = helper.build(STREAM);
        KieSession ksession = kbase.newKieSession(sessionConfig, null);
        PseudoClockScheduler sessionClock = ksession.getSessionClock();
        AtomicInteger counter = new AtomicInteger(0);
        ksession.setGlobal("counter", counter);
        ksession.insert(new ExpirationTest.A(1));
        sessionClock.advanceTime(10, TimeUnit.MILLISECONDS);
        ksession.insert(new ExpirationTest.A(2));
        sessionClock.advanceTime(10, TimeUnit.MILLISECONDS);
        ksession.fireAllRules();
        Assert.assertEquals(2, counter.get());
    }

    @Test
    public void testBeta() {
        // DROOLS-1329
        String drl = (((((((((((((("import " + (ExpirationTest.A.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.B.class.getCanonicalName())) + "\n") + "declare A @role( event ) @expires(11ms) end\n") + "declare B @role( event ) @expires(11ms) end\n") + "global java.util.concurrent.atomic.AtomicInteger counter;\n") + "rule R0 when\n") + "  $a: A( $Aid: id > 0 )\n") + "  $b: B( ($Bid: id <= $Aid) && (id > ($Aid - 1 )))\n") + "then\n") + "  System.out.println(\"[\" + $a + \",\" + $b + \"]\");") + "  counter.incrementAndGet();\n") + "end";
        KieSessionConfiguration sessionConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConfig.setOption(ClockTypeOption.get(PSEUDO_CLOCK.getId()));
        KieHelper helper = new KieHelper();
        helper.addContent(drl, DRL);
        KieBase kbase = helper.build(STREAM);
        KieSession ksession = kbase.newKieSession(sessionConfig, null);
        PseudoClockScheduler sessionClock = ksession.getSessionClock();
        AtomicInteger counter = new AtomicInteger(0);
        ksession.setGlobal("counter", counter);
        ksession.insert(new ExpirationTest.A(1));
        ksession.insert(new ExpirationTest.B(1));
        sessionClock.advanceTime(10, TimeUnit.MILLISECONDS);
        ksession.insert(new ExpirationTest.A(2));
        ksession.insert(new ExpirationTest.B(2));
        sessionClock.advanceTime(10, TimeUnit.MILLISECONDS);
        ksession.fireAllRules();
        Assert.assertEquals(2, counter.get());
    }

    @Test
    public void testBetaRightExpired() {
        // DROOLS-1329
        String drl = (((((((((((((("import " + (ExpirationTest.A.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.B.class.getCanonicalName())) + "\n") + "declare A @role( event ) @expires(11ms) end\n") + "declare B @role( event ) @expires(11ms) end\n") + "global java.util.concurrent.atomic.AtomicInteger counter;\n") + "rule R0 when\n") + "  $a: A( $Aid: id > 0 )\n") + "  $b: B( id == $Aid )\n") + "then\n") + "  System.out.println(\"[\" + $a + \",\" + $b + \"]\");") + "  counter.incrementAndGet();\n") + "end";
        KieSessionConfiguration sessionConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConfig.setOption(ClockTypeOption.get(PSEUDO_CLOCK.getId()));
        KieHelper helper = new KieHelper();
        helper.addContent(drl, DRL);
        KieBase kbase = helper.build(STREAM);
        KieSession ksession = kbase.newKieSession(sessionConfig, null);
        PseudoClockScheduler sessionClock = ksession.getSessionClock();
        AtomicInteger counter = new AtomicInteger(0);
        ksession.setGlobal("counter", counter);
        ksession.insert(new ExpirationTest.A(1));
        sessionClock.advanceTime(20, TimeUnit.MILLISECONDS);
        ksession.insert(new ExpirationTest.B(1));
        ksession.fireAllRules();
        Assert.assertEquals(0, counter.get());
    }

    @Test
    public void testBetaLeftExpired() {
        // DROOLS-1329
        String drl = (((((((((((((("import " + (ExpirationTest.A.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.B.class.getCanonicalName())) + "\n") + "declare A @role( event ) @expires(11ms) end\n") + "declare B @role( event ) @expires(11ms) end\n") + "global java.util.concurrent.atomic.AtomicInteger counter;\n") + "rule R0 when\n") + "  $a: A( $Aid: id > 0 )\n") + "  $b: B( id == $Aid )\n") + "then\n") + "  System.out.println(\"[\" + $a + \",\" + $b + \"]\");") + "  counter.incrementAndGet();\n") + "end";
        KieSessionConfiguration sessionConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConfig.setOption(ClockTypeOption.get(PSEUDO_CLOCK.getId()));
        KieHelper helper = new KieHelper();
        helper.addContent(drl, DRL);
        KieBase kbase = helper.build(STREAM);
        KieSession ksession = kbase.newKieSession(sessionConfig, null);
        PseudoClockScheduler sessionClock = ksession.getSessionClock();
        AtomicInteger counter = new AtomicInteger(0);
        ksession.setGlobal("counter", counter);
        ksession.insert(new ExpirationTest.B(1));
        sessionClock.advanceTime(20, TimeUnit.MILLISECONDS);
        ksession.insert(new ExpirationTest.A(1));
        ksession.fireAllRules();
        Assert.assertEquals(0, counter.get());
    }

    @Test
    public void testBetaLeftExpired2() {
        // DROOLS-1329
        String drl = ((((((((((((((((((("import " + (ExpirationTest.A.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.B.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.C.class.getCanonicalName())) + "\n") + "declare A @role( event ) @expires(31ms) end\n") + "declare B @role( event ) @expires(11ms) end\n") + "declare C @role( event ) @expires(31ms) end\n") + "global java.util.concurrent.atomic.AtomicInteger counter;\n") + "rule R0 when\n") + "  $a: A( $Aid: id > 0 )\n") + "  $b: B( $Bid: id == $Aid )\n") + "  $c: C( id == $Bid )\n") + "then\n") + "  System.out.println(\"[\" + $a + \",\" + $b + \",\" + $c + \"]\");") + "  counter.incrementAndGet();\n") + "end";
        KieSessionConfiguration sessionConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConfig.setOption(ClockTypeOption.get(PSEUDO_CLOCK.getId()));
        KieHelper helper = new KieHelper();
        helper.addContent(drl, DRL);
        KieBase kbase = helper.build(STREAM);
        KieSession ksession = kbase.newKieSession(sessionConfig, null);
        PseudoClockScheduler sessionClock = ksession.getSessionClock();
        AtomicInteger counter = new AtomicInteger(0);
        ksession.setGlobal("counter", counter);
        ksession.insert(new ExpirationTest.A(1));
        ksession.insert(new ExpirationTest.B(1));
        sessionClock.advanceTime(20, TimeUnit.MILLISECONDS);
        ksession.insert(new ExpirationTest.C(1));
        ksession.fireAllRules();
        Assert.assertEquals(0, counter.get());
    }

    public class A {
        private final int id;

        public A(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return ("A(" + (id)) + ")";
        }
    }

    public class B {
        private final int id;

        public B(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return ("B(" + (id)) + ")";
        }
    }

    public class C {
        private final int id;

        public C(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return ("C(" + (id)) + ")";
        }
    }

    @Role(Type.EVENT)
    @Expires("10s")
    public static class ExpiringEventA {}

    @Role(Type.EVENT)
    @Expires(value = "30s", policy = Policy.TIME_SOFT)
    public static class ExpiringEventB {}

    @Role(Type.EVENT)
    @Expires(value = "30s", policy = Policy.TIME_SOFT)
    public static class ExpiringEventC {}

    @Test
    public void testSoftExpiration() {
        // DROOLS-1483
        String drl = (((((((((((((((("import " + (ExpirationTest.ExpiringEventA.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.ExpiringEventB.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.ExpiringEventC.class.getCanonicalName())) + "\n") + "rule Ra when\n") + "  $e : ExpiringEventA() over window:time(20s)\n") + "then end\n ") + "rule Rb when\n") + "  $e : ExpiringEventB() over window:time(20s)\n") + "then end\n ") + "rule Rc when\n") + "  $e : ExpiringEventC()\n") + "then end\n";
        KieSessionConfiguration sessionConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConfig.setOption(ClockTypeOption.get(PSEUDO_CLOCK.getId()));
        KieHelper helper = new KieHelper();
        helper.addContent(drl, DRL);
        KieBase kbase = helper.build(STREAM);
        KieSession ksession = kbase.newKieSession(sessionConfig, null);
        PseudoClockScheduler clock = ksession.getSessionClock();
        ksession.insert(new ExpirationTest.ExpiringEventA());
        ksession.insert(new ExpirationTest.ExpiringEventB());
        ksession.insert(new ExpirationTest.ExpiringEventC());
        ksession.fireAllRules();
        clock.advanceTime(5, TimeUnit.SECONDS);
        ksession.fireAllRules();
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventA.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventB.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventC.class)).size());
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.fireAllRules();
        // t=15 -> hard expiration of A
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventA.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventB.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventC.class)).size());
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.fireAllRules();
        // t=25 -> implicit expiration of B
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventA.class)).size());
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventB.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventC.class)).size());
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.fireAllRules();
        // t=35 -> soft expiration of C
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventA.class)).size());
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventB.class)).size());
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.ExpiringEventC.class)).size());
    }

    @Test
    public void testSoftExpirationWithDeclaration() {
        // DROOLS-1483
        String drl = ((((((((((((((((((("import " + (ExpirationTest.A.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.B.class.getCanonicalName())) + "\n") + "import ") + (ExpirationTest.C.class.getCanonicalName())) + "\n") + "declare A @role( event ) @expires(10s) end\n") + "declare B @role( event ) @expires(value = 30s, policy = TIME_SOFT) end\n") + "declare C @role( event ) @expires(value = 30s, policy = TIME_SOFT) end\n") + "rule Ra when\n") + "  $e : A() over window:time(20s)\n") + "then end\n ") + "rule Rb when\n") + "  $e : B() over window:time(20s)\n") + "then end\n ") + "rule Rc when\n") + "  $e : C()\n") + "then end\n";
        KieSessionConfiguration sessionConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConfig.setOption(ClockTypeOption.get(PSEUDO_CLOCK.getId()));
        KieHelper helper = new KieHelper();
        helper.addContent(drl, DRL);
        KieBase kbase = helper.build(STREAM);
        KieSession ksession = kbase.newKieSession(sessionConfig, null);
        PseudoClockScheduler clock = ksession.getSessionClock();
        ksession.insert(new ExpirationTest.A(1));
        ksession.insert(new ExpirationTest.B(2));
        ksession.insert(new ExpirationTest.C(3));
        ksession.fireAllRules();
        clock.advanceTime(5, TimeUnit.SECONDS);
        ksession.fireAllRules();
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.A.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.B.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.C.class)).size());
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.fireAllRules();
        // t=15 -> hard expiration of A
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.A.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.B.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.C.class)).size());
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.fireAllRules();
        // t=25 -> implicit expiration of B
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.A.class)).size());
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.B.class)).size());
        Assert.assertEquals(1, ksession.getObjects(new ClassObjectFilter(ExpirationTest.C.class)).size());
        clock.advanceTime(10, TimeUnit.SECONDS);
        ksession.fireAllRules();
        // t=35 -> soft expiration of C
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.A.class)).size());
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.B.class)).size());
        Assert.assertEquals(0, ksession.getObjects(new ClassObjectFilter(ExpirationTest.C.class)).size());
    }

    @Test
    public void testEventsExpiredInThePast() throws InterruptedException {
        final String drl = ((((((((((((((" package org.drools.compiler.integrationtests;\n" + " import ") + (BasicEvent.class.getCanonicalName())) + ";\n") + " declare BasicEvent\n") + "     @role( event )\n") + "     @timestamp( eventTimestamp )\n") + "     @duration( eventDuration )\n") + " end\n") + " \n") + " rule R1\n") + " when\n") + "     $A : BasicEvent()\n") + "     $B : BasicEvent( this starts $A )\n") + " then \n") + " end\n";
        testEventsExpiredInThePast(drl);
    }

    @Test
    public void testEventsExpiredInThePastTemporalConstraint() throws InterruptedException {
        final String drl = ((((((((((((((" package org.drools.compiler.integrationtests;\n" + " import ") + (BasicEvent.class.getCanonicalName())) + ";\n") + " declare BasicEvent\n") + "     @role( event )\n") + "     @timestamp( eventTimestamp )\n") + "     @duration( eventDuration )\n") + " end\n") + " \n") + " rule R1\n") + " when\n") + "     $A : BasicEvent()\n") + "     $B : BasicEvent( this starts[5ms] $A )\n") + " then \n") + " end\n";
        testEventsExpiredInThePast(drl);
    }
}

