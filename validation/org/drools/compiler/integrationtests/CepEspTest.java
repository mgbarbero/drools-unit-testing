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
import KieSessionTestConfiguration.STATEFUL_PSEUDO;
import KieSessionTestConfiguration.STATEFUL_REALTIME;
import Role.Type;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.assertj.core.api.Assertions;
import org.drools.core.WorkingMemory;
import org.drools.core.audit.WorkingMemoryFileLogger;
import org.drools.core.base.evaluators.TimeIntervalParser;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.drools.testcoverage.common.model.OrderEvent;
import org.drools.testcoverage.common.model.StockTick;
import org.drools.testcoverage.common.model.StockTickEvent;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieBaseUtil;
import org.drools.testcoverage.common.util.KieUtil;
import org.drools.testcoverage.common.util.SerializationHelper;
import org.drools.testcoverage.common.util.TimeUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.builder.KieBuilder;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.FactType;
import org.kie.api.definition.type.Role;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.TimerJobFactoryOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.time.SessionPseudoClock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;


@RunWith(Parameterized.class)
public class CepEspTest extends AbstractCepEspTest {
    public CepEspTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        super(kieBaseTestConfiguration);
    }

    @Test(timeout = 10000)
    public void testComplexTimestamp() {
        final String drl = (((((((("package " + (CepEspTest.Message.class.getPackage().getName())) + "\n") + "declare ") + (CepEspTest.Message.class.getCanonicalName())) + "\n") + "   @role( event ) \n") + "   @timestamp( getProperties().get( \'timestamp\' ) - 1 ) \n") + "   @duration( getProperties().get( \'duration\' ) + 1 ) \n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final CepEspTest.Message msg = new CepEspTest.Message();
            final Properties props = new Properties();
            props.put("timestamp", 99);
            props.put("duration", 52);
            msg.setProperties(props);
            final EventFactHandle efh = ((EventFactHandle) (ksession.insert(msg)));
            Assert.assertEquals(98, efh.getStartTimestamp());
            Assert.assertEquals(53, efh.getDuration());
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testJavaSqlTimestamp() {
        final String drl = (((((((("package " + (CepEspTest.Message.class.getPackage().getName())) + "\n") + "declare ") + (CepEspTest.Message.class.getCanonicalName())) + "\n") + "   @role( event ) \n") + "   @timestamp( startTime ) \n") + "   @duration( duration )\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final CepEspTest.Message msg = new CepEspTest.Message();
            msg.setStartTime(new Timestamp(10000));
            msg.setDuration(1000L);
            final EventFactHandle efh = ((EventFactHandle) (ksession.insert(msg)));
            Assert.assertEquals(10000, efh.getStartTimestamp());
            Assert.assertEquals(1000, efh.getDuration());
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testEventAssertion() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_SimpleEventAssertion.drl");
        final KieSession session = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = session.getSessionClock();
            final List results = new ArrayList();
            session.setGlobal("results", results);
            final StockTick tick1 = new StockTick(1, "DROO", 50, 10000);
            final StockTick tick2 = new StockTick(2, "ACME", 10, 10010);
            final StockTick tick3 = new StockTick(3, "ACME", 10, 10100);
            final StockTick tick4 = new StockTick(4, "DROO", 50, 11000);
            final InternalFactHandle handle1 = ((InternalFactHandle) (session.insert(tick1)));
            clock.advanceTime(10, TimeUnit.SECONDS);
            final InternalFactHandle handle2 = ((InternalFactHandle) (session.insert(tick2)));
            clock.advanceTime(30, TimeUnit.SECONDS);
            final InternalFactHandle handle3 = ((InternalFactHandle) (session.insert(tick3)));
            clock.advanceTime(20, TimeUnit.SECONDS);
            final InternalFactHandle handle4 = ((InternalFactHandle) (session.insert(tick4)));
            clock.advanceTime(10, TimeUnit.SECONDS);
            Assert.assertNotNull(handle1);
            Assert.assertNotNull(handle2);
            Assert.assertNotNull(handle3);
            Assert.assertNotNull(handle4);
            Assert.assertTrue(handle1.isEvent());
            Assert.assertTrue(handle2.isEvent());
            Assert.assertTrue(handle3.isEvent());
            Assert.assertTrue(handle4.isEvent());
            session.fireAllRules();
            Assert.assertEquals(2, ((List) (session.getGlobal("results"))).size());
        } finally {
            session.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testAnnotatedEventAssertion() {
        final String drl = ((((((((((("package org.drools.compiler.test;\n" + ("\n" + "import ")) + (StockTickEvent.class.getCanonicalName())) + ";\n") + "\n") + "global java.util.List results;\n") + "\n") + "rule \"Check event\"\n") + "when\n") + "    $st : StockTickEvent( company == \"ACME\" )\n") + "then\n") + "    results.add( $st );\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession session = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = session.getSessionClock();
            final List results = new ArrayList();
            session.setGlobal("results", results);
            final StockTick tick1 = new StockTickEvent(1, "DROO", 50, 10000);
            final StockTick tick2 = new StockTickEvent(2, "ACME", 10, 10010);
            final StockTick tick3 = new StockTickEvent(3, "ACME", 10, 10100);
            final StockTick tick4 = new StockTickEvent(4, "DROO", 50, 11000);
            final InternalFactHandle handle1 = ((InternalFactHandle) (session.insert(tick1)));
            clock.advanceTime(10, TimeUnit.SECONDS);
            final InternalFactHandle handle2 = ((InternalFactHandle) (session.insert(tick2)));
            clock.advanceTime(30, TimeUnit.SECONDS);
            final InternalFactHandle handle3 = ((InternalFactHandle) (session.insert(tick3)));
            clock.advanceTime(20, TimeUnit.SECONDS);
            final InternalFactHandle handle4 = ((InternalFactHandle) (session.insert(tick4)));
            clock.advanceTime(10, TimeUnit.SECONDS);
            Assert.assertNotNull(handle1);
            Assert.assertNotNull(handle2);
            Assert.assertNotNull(handle3);
            Assert.assertNotNull(handle4);
            Assert.assertTrue(handle1.isEvent());
            Assert.assertTrue(handle2.isEvent());
            Assert.assertTrue(handle3.isEvent());
            Assert.assertTrue(handle4.isEvent());
            session.fireAllRules();
            Assert.assertEquals(2, ((List) (session.getGlobal("results"))).size());
        } finally {
            session.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    @Test(timeout = 10000)
    public void testPackageSerializationWithEvents() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_SimpleEventAssertion.drl");
        final KieSession session = kbase.newKieSession();
        try {
            final List<StockTick> results = new ArrayList<>();
            session.setGlobal("results", results);
            final StockTick tick1 = new StockTick(1, "DROO", 50, 10000);
            final StockTick tick2 = new StockTick(2, "ACME", 10, 10010);
            final InternalFactHandle handle1 = ((InternalFactHandle) (session.insert(tick1)));
            final InternalFactHandle handle2 = ((InternalFactHandle) (session.insert(tick2)));
            Assert.assertNotNull(handle1);
            Assert.assertNotNull(handle2);
            Assert.assertTrue(handle1.isEvent());
            Assert.assertTrue(handle2.isEvent());
            session.fireAllRules();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(tick2, results.get(0));
        } finally {
            session.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testEventAssertionWithDuration() {
        final String drl = ((((((((((((((((("package org.drools.compiler\n" + ("\n" + "import ")) + (StockTick.class.getCanonicalName())) + ";\n") + "\n") + "global java.util.List results;\n") + "\n") + "declare StockTick \n") + "    @role( event )\n") + "    @duration( duration )\n") + "    @timestamp( time )\n") + "end\n") + "\n") + "rule \"Check event\"\n") + "when\n") + "    $st : StockTick( company == \"ACME\" )\n") + "then\n") + "    results.add( $st );\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession session = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List results = new ArrayList();
            session.setGlobal("results", results);
            final StockTick tick1 = new StockTick(1, "DROO", 50, 10000, 5);
            final StockTick tick2 = new StockTick(2, "ACME", 10, 11000, 10);
            final StockTick tick3 = new StockTick(3, "ACME", 10, 12000, 8);
            final StockTick tick4 = new StockTick(4, "DROO", 50, 13000, 7);
            final InternalFactHandle handle1 = ((InternalFactHandle) (session.insert(tick1)));
            final InternalFactHandle handle2 = ((InternalFactHandle) (session.insert(tick2)));
            final InternalFactHandle handle3 = ((InternalFactHandle) (session.insert(tick3)));
            final InternalFactHandle handle4 = ((InternalFactHandle) (session.insert(tick4)));
            Assert.assertNotNull(handle1);
            Assert.assertNotNull(handle2);
            Assert.assertNotNull(handle3);
            Assert.assertNotNull(handle4);
            Assert.assertTrue(handle1.isEvent());
            Assert.assertTrue(handle2.isEvent());
            Assert.assertTrue(handle3.isEvent());
            Assert.assertTrue(handle4.isEvent());
            final EventFactHandle eh1 = ((EventFactHandle) (handle1));
            final EventFactHandle eh2 = ((EventFactHandle) (handle2));
            final EventFactHandle eh3 = ((EventFactHandle) (handle3));
            final EventFactHandle eh4 = ((EventFactHandle) (handle4));
            Assert.assertEquals(tick1.getTime(), eh1.getStartTimestamp());
            Assert.assertEquals(tick2.getTime(), eh2.getStartTimestamp());
            Assert.assertEquals(tick3.getTime(), eh3.getStartTimestamp());
            Assert.assertEquals(tick4.getTime(), eh4.getStartTimestamp());
            Assert.assertEquals(tick1.getDuration(), eh1.getDuration());
            Assert.assertEquals(tick2.getDuration(), eh2.getDuration());
            Assert.assertEquals(tick3.getDuration(), eh3.getDuration());
            Assert.assertEquals(tick4.getDuration(), eh4.getDuration());
            session.fireAllRules();
            Assert.assertEquals(2, results.size());
        } finally {
            session.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testEventAssertionWithDateTimestamp() {
        final String drl = (((((((((((((((("package org.drools.compiler\n" + ("\n" + "import ")) + (StockTick.class.getCanonicalName())) + ";\n") + "\n") + "global java.util.List results;\n") + "\n") + "declare StockTick \n") + "    @role( event )\n") + "    @timestamp( dateTimestamp )\n") + "end\n") + "\n") + "rule \"Check event\"\n") + "when\n") + "    $st : StockTick( company == \"ACME\" )\n") + "then\n") + "    results.add( $st );\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession session = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List results = new ArrayList();
            session.setGlobal("results", results);
            final StockTick tick1 = new StockTick(1, "DROO", 50, 10000, 5);
            final StockTick tick2 = new StockTick(2, "ACME", 10, 11000, 10);
            final StockTick tick3 = new StockTick(3, "ACME", 10, 12000, 8);
            final StockTick tick4 = new StockTick(4, "DROO", 50, 13000, 7);
            final InternalFactHandle handle1 = ((InternalFactHandle) (session.insert(tick1)));
            final InternalFactHandle handle2 = ((InternalFactHandle) (session.insert(tick2)));
            final InternalFactHandle handle3 = ((InternalFactHandle) (session.insert(tick3)));
            final InternalFactHandle handle4 = ((InternalFactHandle) (session.insert(tick4)));
            Assert.assertNotNull(handle1);
            Assert.assertNotNull(handle2);
            Assert.assertNotNull(handle3);
            Assert.assertNotNull(handle4);
            Assert.assertTrue(handle1.isEvent());
            Assert.assertTrue(handle2.isEvent());
            Assert.assertTrue(handle3.isEvent());
            Assert.assertTrue(handle4.isEvent());
            final EventFactHandle eh1 = ((EventFactHandle) (handle1));
            final EventFactHandle eh2 = ((EventFactHandle) (handle2));
            final EventFactHandle eh3 = ((EventFactHandle) (handle3));
            final EventFactHandle eh4 = ((EventFactHandle) (handle4));
            Assert.assertEquals(tick1.getTime(), eh1.getStartTimestamp());
            Assert.assertEquals(tick2.getTime(), eh2.getStartTimestamp());
            Assert.assertEquals(tick3.getTime(), eh3.getStartTimestamp());
            Assert.assertEquals(tick4.getTime(), eh4.getStartTimestamp());
            session.fireAllRules();
            Assert.assertEquals(2, results.size());
        } finally {
            session.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testEventExpiration() {
        final String drl = (((((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n") + "global java.util.List results;\n") + "\n") + "declare StockTick \n") + "    @role( event )\n") + "    @timestamp( dateTimestamp )\n") + "    @expires( 1h30m )\n") + "end\n") + "\n") + "rule \"Check event\"\n") + "when\n") + "    $st : StockTick( company == \"ACME\" )\n") + "then\n") + "    results.add( $st );\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        // read in the source
        final TypeDeclaration factType = getTypeDeclaration(StockTick.class);
        Assert.assertEquals(TimeIntervalParser.parse("1h30m")[0], factType.getExpirationOffset());
    }

    @Test(timeout = 10000)
    public void testEventExpiration2() {
        testEventExpiration("15m", "15m");
    }

    @Test(timeout = 10000)
    public void testEventExpiration3() {
        testEventExpiration("5m", "5m");
    }

    @Test(timeout = 10000)
    public void testEventExpiration4() {
        final String drl = (((((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n") + "global java.util.List results;\n") + "\n") + "declare StockTick \n") + "    @role( event )\n") + "    @expires( 10s )\n") + "end\n") + "\n") + "rule \"TestEventReceived\"\n") + "no-loop\n") + "when\n") + "   $st1 : StockTick( company == \"ACME\" ) over window:time( 10s ) from entry-point \"Event Stream\"\n") + "then\n") + "   results.add( $st1 );\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final EntryPoint eventStream = ksession.getEntryPoint("Event Stream");
            final SessionPseudoClock clock = ksession.getSessionClock();
            final List results = new ArrayList();
            ksession.setGlobal("results", results);
            final EventFactHandle handle1 = ((EventFactHandle) (eventStream.insert(new StockTick(1, "ACME", 50, System.currentTimeMillis(), 3))));
            ksession.fireAllRules();
            clock.advanceTime(11, TimeUnit.SECONDS);
            // clock.advance() will put the event expiration in the queue to be executed,
            // but it has to wait for a "thread" to do that
            // so we fire rules again here to get that
            // alternative could run fireUntilHalt()
            ksession.fireAllRules();
            Assert.assertEquals(1, results.size());
            Assert.assertTrue(handle1.isExpired());
            Assert.assertFalse(ksession.getFactHandles().contains(handle1));
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testTimeRelationalOperators() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_TimeRelationalOperators.drl");
        final KieSession wm = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = wm.getSessionClock();
            clock.setStartupTime(1000);
            final List results_coincides = new ArrayList();
            final List results_before = new ArrayList();
            final List results_after = new ArrayList();
            final List results_meets = new ArrayList();
            final List results_met_by = new ArrayList();
            final List results_overlaps = new ArrayList();
            final List results_overlapped_by = new ArrayList();
            final List results_during = new ArrayList();
            final List results_includes = new ArrayList();
            final List results_starts = new ArrayList();
            final List results_started_by = new ArrayList();
            final List results_finishes = new ArrayList();
            final List results_finished_by = new ArrayList();
            wm.setGlobal("results_coincides", results_coincides);
            wm.setGlobal("results_before", results_before);
            wm.setGlobal("results_after", results_after);
            wm.setGlobal("results_meets", results_meets);
            wm.setGlobal("results_met_by", results_met_by);
            wm.setGlobal("results_overlaps", results_overlaps);
            wm.setGlobal("results_overlapped_by", results_overlapped_by);
            wm.setGlobal("results_during", results_during);
            wm.setGlobal("results_includes", results_includes);
            wm.setGlobal("results_starts", results_starts);
            wm.setGlobal("results_started_by", results_started_by);
            wm.setGlobal("results_finishes", results_finishes);
            wm.setGlobal("results_finished_by", results_finished_by);
            final StockTick tick1 = new StockTick(1, "DROO", 50, System.currentTimeMillis(), 3);
            final StockTick tick2 = new StockTick(2, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick3 = new StockTick(3, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick4 = new StockTick(4, "DROO", 50, System.currentTimeMillis(), 5);
            final StockTick tick5 = new StockTick(5, "ACME", 10, System.currentTimeMillis(), 5);
            final StockTick tick6 = new StockTick(6, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick7 = new StockTick(7, "ACME", 10, System.currentTimeMillis(), 5);
            final StockTick tick8 = new StockTick(8, "ACME", 10, System.currentTimeMillis(), 3);
            final InternalFactHandle handle1 = ((InternalFactHandle) (wm.insert(tick1)));
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            final InternalFactHandle handle2 = ((InternalFactHandle) (wm.insert(tick2)));
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            final InternalFactHandle handle3 = ((InternalFactHandle) (wm.insert(tick3)));
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            final InternalFactHandle handle4 = ((InternalFactHandle) (wm.insert(tick4)));
            final InternalFactHandle handle5 = ((InternalFactHandle) (wm.insert(tick5)));
            clock.advanceTime(1, TimeUnit.MILLISECONDS);
            final InternalFactHandle handle6 = ((InternalFactHandle) (wm.insert(tick6)));
            final InternalFactHandle handle7 = ((InternalFactHandle) (wm.insert(tick7)));
            clock.advanceTime(2, TimeUnit.MILLISECONDS);
            final InternalFactHandle handle8 = ((InternalFactHandle) (wm.insert(tick8)));
            Assert.assertNotNull(handle1);
            Assert.assertNotNull(handle2);
            Assert.assertNotNull(handle3);
            Assert.assertNotNull(handle4);
            Assert.assertNotNull(handle5);
            Assert.assertNotNull(handle6);
            Assert.assertNotNull(handle7);
            Assert.assertNotNull(handle8);
            Assert.assertTrue(handle1.isEvent());
            Assert.assertTrue(handle2.isEvent());
            Assert.assertTrue(handle3.isEvent());
            Assert.assertTrue(handle4.isEvent());
            Assert.assertTrue(handle6.isEvent());
            Assert.assertTrue(handle7.isEvent());
            Assert.assertTrue(handle8.isEvent());
            wm.fireAllRules();
            Assert.assertEquals(1, results_coincides.size());
            Assert.assertEquals(tick5, results_coincides.get(0));
            Assert.assertEquals(1, results_before.size());
            Assert.assertEquals(tick2, results_before.get(0));
            Assert.assertEquals(1, results_after.size());
            Assert.assertEquals(tick3, results_after.get(0));
            Assert.assertEquals(1, results_meets.size());
            Assert.assertEquals(tick3, results_meets.get(0));
            Assert.assertEquals(1, results_met_by.size());
            Assert.assertEquals(tick2, results_met_by.get(0));
            Assert.assertEquals(1, results_met_by.size());
            Assert.assertEquals(tick2, results_met_by.get(0));
            Assert.assertEquals(1, results_overlaps.size());
            Assert.assertEquals(tick4, results_overlaps.get(0));
            Assert.assertEquals(1, results_overlapped_by.size());
            Assert.assertEquals(tick8, results_overlapped_by.get(0));
            Assert.assertEquals(1, results_during.size());
            Assert.assertEquals(tick6, results_during.get(0));
            Assert.assertEquals(1, results_includes.size());
            Assert.assertEquals(tick4, results_includes.get(0));
            Assert.assertEquals(1, results_starts.size());
            Assert.assertEquals(tick6, results_starts.get(0));
            Assert.assertEquals(1, results_started_by.size());
            Assert.assertEquals(tick7, results_started_by.get(0));
            Assert.assertEquals(1, results_finishes.size());
            Assert.assertEquals(tick8, results_finishes.get(0));
            Assert.assertEquals(1, results_finished_by.size());
            Assert.assertEquals(tick7, results_finished_by.get(0));
        } finally {
            wm.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testBeforeOperator() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_BeforeOperator.drl");
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = ksession.getSessionClock();
            clock.setStartupTime(1000);
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            final StockTick tick1 = new StockTick(1, "DROO", 50, System.currentTimeMillis(), 3);
            final StockTick tick2 = new StockTick(2, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick3 = new StockTick(3, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick4 = new StockTick(4, "DROO", 50, System.currentTimeMillis(), 5);
            final StockTick tick5 = new StockTick(5, "ACME", 10, System.currentTimeMillis(), 5);
            final StockTick tick6 = new StockTick(6, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick7 = new StockTick(7, "ACME", 10, System.currentTimeMillis(), 5);
            final StockTick tick8 = new StockTick(8, "ACME", 10, System.currentTimeMillis(), 3);
            ksession.insert(tick1);
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            ksession.insert(tick2);
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            ksession.insert(tick3);
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            ksession.insert(tick4);
            ksession.insert(tick5);
            clock.advanceTime(1, TimeUnit.MILLISECONDS);
            ksession.insert(tick6);
            ksession.insert(tick7);
            clock.advanceTime(2, TimeUnit.MILLISECONDS);
            ksession.insert(tick8);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
            final StockTick[] stocks = ((StockTick[]) (list.get(0)));
            Assert.assertSame(tick4, stocks[0]);
            Assert.assertSame(tick2, stocks[1]);
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testMetByOperator() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_MetByOperator.drl");
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = ksession.getSessionClock();
            clock.setStartupTime(1000);
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            final StockTick tick1 = new StockTick(1, "DROO", 50, System.currentTimeMillis(), 3);
            final StockTick tick2 = new StockTick(2, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick3 = new StockTick(3, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick4 = new StockTick(4, "DROO", 50, System.currentTimeMillis(), 5);
            final StockTick tick5 = new StockTick(5, "ACME", 10, System.currentTimeMillis(), 5);
            final StockTick tick6 = new StockTick(6, "ACME", 10, System.currentTimeMillis(), 3);
            final StockTick tick7 = new StockTick(7, "ACME", 10, System.currentTimeMillis(), 5);
            final StockTick tick8 = new StockTick(8, "ACME", 10, System.currentTimeMillis(), 3);
            ksession.insert(tick1);
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            ksession.insert(tick2);
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            ksession.insert(tick3);
            clock.advanceTime(4, TimeUnit.MILLISECONDS);
            ksession.insert(tick4);
            ksession.insert(tick5);
            clock.advanceTime(1, TimeUnit.MILLISECONDS);
            ksession.insert(tick6);
            ksession.insert(tick7);
            clock.advanceTime(2, TimeUnit.MILLISECONDS);
            ksession.insert(tick8);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
            final StockTick[] stocks = ((StockTick[]) (list.get(0)));
            Assert.assertSame(tick1, stocks[0]);
            Assert.assertSame(tick2, stocks[1]);
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testComplexOperator() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_ComplexOperator.drl");
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            final PseudoClockScheduler clock = ksession.getSessionClock();
            clock.setStartupTime(1000);
            final AgendaEventListener ael = Mockito.mock(AgendaEventListener.class);
            ksession.addEventListener(ael);
            final StockTick tick1 = new StockTick(1, "DROO", 50, 0, 3);
            final StockTick tick2 = new StockTick(2, "ACME", 10, 4, 3);
            final StockTick tick3 = new StockTick(3, "ACME", 10, 8, 3);
            final StockTick tick4 = new StockTick(4, "DROO", 50, 12, 5);
            final StockTick tick5 = new StockTick(5, "ACME", 10, 12, 5);
            final StockTick tick6 = new StockTick(6, "ACME", 10, 13, 3);
            final StockTick tick7 = new StockTick(7, "ACME", 10, 13, 5);
            final StockTick tick8 = new StockTick(8, "ACME", 10, 15, 3);
            ksession.insert(tick1);
            ksession.insert(tick2);
            ksession.insert(tick3);
            ksession.insert(tick4);
            ksession.insert(tick5);
            ksession.insert(tick6);
            ksession.insert(tick7);
            ksession.insert(tick8);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
            final StockTick[] stocks = ((StockTick[]) (list.get(0)));
            Assert.assertSame(tick4, stocks[0]);
            Assert.assertSame(tick2, stocks[1]);
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testAfterOnArbitraryDates() {
        testArbitraryDates("org/drools/compiler/integrationtests/test_CEP_AfterOperatorDates.drl", 100000, 104000);
    }

    @Test(timeout = 10000)
    public void testBeforeOnArbitraryDates() {
        testArbitraryDates("org/drools/compiler/integrationtests/test_CEP_BeforeOperatorDates.drl", 104000, 100000);
    }

    @Test(timeout = 10000)
    public void testCoincidesOnArbitraryDates() {
        testArbitraryDates("org/drools/compiler/integrationtests/test_CEP_CoincidesOperatorDates.drl", 100000, 100050);
    }

    @Test(timeout = 10000)
    public void testSimpleTimeWindow() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_SimpleTimeWindow.drl");
        final KieSession wm = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List results = new ArrayList();
            wm.setGlobal("results", results);
            final SessionPseudoClock clock = wm.getSessionClock();
            clock.advanceTime(5, TimeUnit.SECONDS);// 5 seconds

            final EventFactHandle handle1 = ((EventFactHandle) (wm.insert(new OrderEvent("1", "customer A", 70))));
            Assert.assertEquals(5000, handle1.getStartTimestamp());
            Assert.assertEquals(0, handle1.getDuration());
            wm.fireAllRules();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(70, ((Number) (results.get(0))).intValue());
            // advance clock and assert new data
            clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

            final EventFactHandle handle2 = ((EventFactHandle) (wm.insert(new OrderEvent("2", "customer A", 60))));
            Assert.assertEquals(15000, handle2.getStartTimestamp());
            Assert.assertEquals(0, handle2.getDuration());
            wm.fireAllRules();
            Assert.assertEquals(2, results.size());
            Assert.assertEquals(65, ((Number) (results.get(1))).intValue());
            // advance clock and assert new data
            clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

            final EventFactHandle handle3 = ((EventFactHandle) (wm.insert(new OrderEvent("3", "customer A", 50))));
            Assert.assertEquals(25000, handle3.getStartTimestamp());
            Assert.assertEquals(0, handle3.getDuration());
            wm.fireAllRules();
            Assert.assertEquals(3, results.size());
            Assert.assertEquals(60, ((Number) (results.get(2))).intValue());
            // advance clock and assert new data
            clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

            final EventFactHandle handle4 = ((EventFactHandle) (wm.insert(new OrderEvent("4", "customer A", 25))));
            Assert.assertEquals(35000, handle4.getStartTimestamp());
            Assert.assertEquals(0, handle4.getDuration());
            wm.fireAllRules();
            // first event should have expired, making average under the rule threshold, so no additional rule fire
            Assert.assertEquals(3, results.size());
            // advance clock and assert new data
            clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

            final EventFactHandle handle5 = ((EventFactHandle) (wm.insert(new OrderEvent("5", "customer A", 70))));
            Assert.assertEquals(45000, handle5.getStartTimestamp());
            Assert.assertEquals(0, handle5.getDuration());
            wm.fireAllRules();
            // still under the threshold, so no fire
            Assert.assertEquals(3, results.size());
            // advance clock and assert new data
            clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

            final EventFactHandle handle6 = ((EventFactHandle) (wm.insert(new OrderEvent("6", "customer A", 115))));
            Assert.assertEquals(55000, handle6.getStartTimestamp());
            Assert.assertEquals(0, handle6.getDuration());
            wm.fireAllRules();
            Assert.assertEquals(4, results.size());
            Assert.assertEquals(70, ((Number) (results.get(3))).intValue());
        } finally {
            wm.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testSimpleLengthWindow() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_SimpleLengthWindow.drl");
        final KieSession wm = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List results = new ArrayList();
            wm.setGlobal("results", results);
            wm.insert(new OrderEvent("1", "customer A", 70));
            wm.fireAllRules();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(70, ((Number) (results.get(0))).intValue());
            // assert new data
            wm.insert(new OrderEvent("2", "customer A", 60));
            wm.fireAllRules();
            Assert.assertEquals(2, results.size());
            Assert.assertEquals(65, ((Number) (results.get(1))).intValue());
            // assert new data
            wm.insert(new OrderEvent("3", "customer A", 50));
            wm.fireAllRules();
            Assert.assertEquals(3, results.size());
            Assert.assertEquals(60, ((Number) (results.get(2))).intValue());
            // assert new data
            wm.insert(new OrderEvent("4", "customer A", 25));
            wm.fireAllRules();
            // first event should have expired, making average under the rule threshold, so no additional rule fire
            Assert.assertEquals(3, results.size());
            // assert new data
            wm.insert(new OrderEvent("5", "customer A", 70));
            wm.fireAllRules();
            // still under the threshold, so no fire
            Assert.assertEquals(3, results.size());
            // assert new data
            wm.insert(new OrderEvent("6", "customer A", 115));
            wm.fireAllRules();
            Assert.assertEquals(4, results.size());
            Assert.assertEquals(70, ((Number) (results.get(3))).intValue());
        } finally {
            wm.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testDelayingNot() {
        final String drl = ((((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n") + "global java.util.List results;\n") + "\n") + "declare StockTick\n") + "    @role( event )\n") + "end\n") + "\n") + "rule \"Delaying Not\"\n") + "when\n") + "    $s1: StockTick( $symbol : company, $price : price )\n") + "    not( StockTick( company == $symbol, price > $price, this after[ 1s, 10s ] $s1 ) )\n") + "then\n") + "    results.add( $s1 );\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession wm = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final RuleImpl rule = ((RuleImpl) (kbase.getRule("org.drools.compiler", "Delaying Not")));
            Assert.assertEquals(10000, getDuration());
            final List results = new ArrayList();
            wm.setGlobal("results", results);
            final SessionPseudoClock clock = wm.getSessionClock();
            clock.advanceTime(10, TimeUnit.SECONDS);
            final StockTick st1O = new StockTick(1, "DROO", 100, clock.getCurrentTime());
            wm.insert(st1O);
            wm.fireAllRules();
            // should not fire, because it must wait 10 seconds
            Assert.assertEquals(0, results.size());
            clock.advanceTime(5, TimeUnit.SECONDS);
            wm.insert(new StockTick(1, "DROO", 80, clock.getCurrentTime()));
            wm.fireAllRules();
            // should still not fire, because it must wait 5 more seconds, and st2 has lower price (80)
            Assert.assertEquals(0, results.size());
            // assert new data
            wm.fireAllRules();
            clock.advanceTime(6, TimeUnit.SECONDS);
            wm.fireAllRules();
            // should fire, because waited for 10 seconds and no other event arrived with a price increase
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(st1O, results.get(0));
        } finally {
            wm.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testSimpleLengthWindowWithQueue() throws Exception {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_SimpleLengthWindow.drl");
        KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List results = new ArrayList();
            ksession.setGlobal("results", results);
            ksession.insert(new OrderEvent("1", "customer A", 80));
            ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            // assert new data
            ksession.insert(new OrderEvent("2", "customer A", 70));
            ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            // assert new data
            ksession.insert(new OrderEvent("3", "customer A", 60));
            ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            // assert new data
            ksession.insert(new OrderEvent("4", "customer A", 50));
            ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            ksession.fireAllRules();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(60, ((Number) (results.get(0))).intValue());
            // assert new data
            ksession.insert(new OrderEvent("5", "customer A", 10));
            ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            ksession.fireAllRules();
            Assert.assertEquals(1, results.size());
            ksession.insert(new OrderEvent("6", "customer A", 90));
            ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            ksession.fireAllRules();
            Assert.assertEquals(2, results.size());
            Assert.assertEquals(50, ((Number) (results.get(1))).intValue());
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testDelayingNot2() {
        final String drl = "package org.drools.compiler\n" + (((((((((("declare A @role(event) symbol : String end\n" + "declare B @role(event) symbol : String end\n") + "rule Setup when\n") + "then\n") + "    insert( new A() );\n") + "end\n") + "rule X\n") + "when\n") + "    $a : A() and not( B( this after $a ) )\n") + "then\n") + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            // rule X should not be delayed as the delay would be infinite
            final int rules = ksession.fireAllRules();
            Assert.assertEquals(2, rules);
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testDelayingNotWithPreEpochClock() {
        final String drl = "package org.drools.compiler\n" + (((((((((("declare A @role(event) symbol : String end\n" + "declare B @role(event) symbol : String end\n") + "rule Setup when\n") + "then\n") + "    insert( new A() );\n") + "end\n") + "rule X\n") + "when\n") + "    $a : A() and not( B( this after $a ) )\n") + "then\n") + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            // Getting a pre-epoch date (i.e., before 1970)
            final Calendar ts = Calendar.getInstance();
            ts.set(1900, Calendar.FEBRUARY, 1);
            // Initializing the clock to that date
            final SessionPseudoClock clock = ksession.getSessionClock();
            clock.advanceTime(ts.getTimeInMillis(), TimeUnit.MILLISECONDS);
            // rule X should not be delayed as the delay would be infinite
            final int rules = ksession.fireAllRules();
            Assert.assertEquals(2, rules);
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testIdleTime() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_SimpleEventAssertion.drl");
        final KieSession session = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = session.getSessionClock();
            final List results = new ArrayList();
            session.setGlobal("results", results);
            final StockTick tick1 = new StockTick(1, "DROO", 50, 10000);
            final StockTick tick2 = new StockTick(2, "ACME", 10, 10010);
            final StockTick tick3 = new StockTick(3, "ACME", 10, 10100);
            final StockTick tick4 = new StockTick(4, "DROO", 50, 11000);
            Assert.assertEquals(0, getIdleTime());
            final InternalFactHandle handle1 = ((InternalFactHandle) (session.insert(tick1)));
            clock.advanceTime(10, TimeUnit.SECONDS);
            Assert.assertEquals(10000, getIdleTime());
            final InternalFactHandle handle2 = ((InternalFactHandle) (session.insert(tick2)));
            Assert.assertEquals(0, getIdleTime());
            clock.advanceTime(15, TimeUnit.SECONDS);
            Assert.assertEquals(15000, getIdleTime());
            clock.advanceTime(15, TimeUnit.SECONDS);
            Assert.assertEquals(30000, getIdleTime());
            final InternalFactHandle handle3 = ((InternalFactHandle) (session.insert(tick3)));
            Assert.assertEquals(0, getIdleTime());
            clock.advanceTime(20, TimeUnit.SECONDS);
            final InternalFactHandle handle4 = ((InternalFactHandle) (session.insert(tick4)));
            clock.advanceTime(10, TimeUnit.SECONDS);
            Assert.assertNotNull(handle1);
            Assert.assertNotNull(handle2);
            Assert.assertNotNull(handle3);
            Assert.assertNotNull(handle4);
            Assert.assertTrue(handle1.isEvent());
            Assert.assertTrue(handle2.isEvent());
            Assert.assertTrue(handle3.isEvent());
            Assert.assertTrue(handle4.isEvent());
            Assert.assertEquals(10000, getIdleTime());
            session.fireAllRules();
            Assert.assertEquals(0, getIdleTime());
            Assert.assertEquals(2, ((List) (session.getGlobal("results"))).size());
        } finally {
            session.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testIdleTimeAndTimeToNextJob() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_SimpleTimeWindow.drl");
        final StatefulKnowledgeSessionImpl wm = ((StatefulKnowledgeSessionImpl) (kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null)));
        try {
            final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger(((WorkingMemory) (wm)));
            final File testTmpDir = new File("target/test-tmp/");
            testTmpDir.mkdirs();
            logger.setFileName("target/test-tmp/testIdleTimeAndTimeToNextJob-audit");
            try {
                final List results = new ArrayList();
                wm.setGlobal("results", results);
                // how to initialize the clock?
                // how to configure the clock?
                final SessionPseudoClock clock = ((SessionPseudoClock) (wm.getSessionClock()));
                clock.advanceTime(5, TimeUnit.SECONDS);// 5 seconds

                // there is no next job, so returns -1
                Assert.assertEquals((-1), wm.getTimeToNextJob());
                wm.insert(new OrderEvent("1", "customer A", 70));
                wm.fireAllRules();
                Assert.assertEquals(0, wm.getIdleTime());
                // now, there is a next job in 30 seconds: expire the event
                Assert.assertEquals(30000, wm.getTimeToNextJob());
                wm.fireAllRules();
                Assert.assertEquals(1, results.size());
                Assert.assertEquals(70, ((Number) (results.get(0))).intValue());
                // advance clock and assert new data
                clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

                // next job is in 20 seconds: expire the event
                Assert.assertEquals(20000, wm.getTimeToNextJob());
                wm.insert(new OrderEvent("2", "customer A", 60));
                wm.fireAllRules();
                Assert.assertEquals(2, results.size());
                Assert.assertEquals(65, ((Number) (results.get(1))).intValue());
                // advance clock and assert new data
                clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

                // next job is in 10 seconds: expire the event
                Assert.assertEquals(10000, wm.getTimeToNextJob());
                wm.insert(new OrderEvent("3", "customer A", 50));
                wm.fireAllRules();
                Assert.assertEquals(3, results.size());
                Assert.assertEquals(60, ((Number) (results.get(2))).intValue());
                // advance clock and assert new data
                clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

                // advancing clock time will cause events to expire
                Assert.assertEquals(0, wm.getIdleTime());
                // next job is in 10 seconds: expire another event
                // assertEquals( 10000, iwm.getTimeToNextJob());
                wm.insert(new OrderEvent("4", "customer A", 25));
                wm.fireAllRules();
                // first event should have expired, making average under the rule threshold, so no additional rule fire
                Assert.assertEquals(3, results.size());
                // advance clock and assert new data
                clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

                wm.insert(new OrderEvent("5", "customer A", 70));
                Assert.assertEquals(0, wm.getIdleTime());
                // wm  = SerializationHelper.serializeObject(wm);
                wm.fireAllRules();
                // still under the threshold, so no fire
                Assert.assertEquals(3, results.size());
            } finally {
                logger.writeToDisk();
            }
        } finally {
            wm.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testCollectWithWindows() {
        final KieBase kbase = KieBaseUtil.getKieBaseFromClasspathResources("cep-esp-test", kieBaseTestConfiguration, "org/drools/compiler/integrationtests/test_CEP_CollectWithWindows.drl");
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger(ksession);
            final File testTmpDir = new File("target/test-tmp/");
            testTmpDir.mkdirs();
            logger.setFileName("target/test-tmp/testCollectWithWindows-audit");
            final List<Number> timeResults = new ArrayList<>();
            final List<Number> lengthResults = new ArrayList<>();
            ksession.setGlobal("timeResults", timeResults);
            ksession.setGlobal("lengthResults", lengthResults);
            final SessionPseudoClock clock = ksession.getSessionClock();
            try {
                // First interaction
                clock.advanceTime(5, TimeUnit.SECONDS);// 5 seconds

                ksession.insert(new OrderEvent("1", "customer A", 70));
                ksession.fireAllRules();
                Assert.assertEquals(1, timeResults.size());
                Assert.assertEquals(1, timeResults.get(0).intValue());
                Assert.assertEquals(1, lengthResults.size());
                Assert.assertEquals(1, lengthResults.get(0).intValue());
                // Second interaction: advance clock and assert new data
                clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

                ksession.insert(new OrderEvent("2", "customer A", 60));
                ksession.fireAllRules();
                Assert.assertEquals(2, timeResults.size());
                Assert.assertEquals(2, timeResults.get(1).intValue());
                Assert.assertEquals(2, lengthResults.size());
                Assert.assertEquals(2, lengthResults.get(1).intValue());
                // Third interaction: advance clock and assert new data
                clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

                ksession.insert(new OrderEvent("3", "customer A", 50));
                ksession.fireAllRules();
                Assert.assertEquals(3, timeResults.size());
                Assert.assertEquals(3, timeResults.get(2).intValue());
                Assert.assertEquals(3, lengthResults.size());
                Assert.assertEquals(3, lengthResults.get(2).intValue());
                // Fourth interaction: advance clock and assert new data
                clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

                ksession.insert(new OrderEvent("4", "customer A", 25));
                ksession.fireAllRules();
                // first event should have expired now
                Assert.assertEquals(4, timeResults.size());
                Assert.assertEquals(3, timeResults.get(3).intValue());
                Assert.assertEquals(4, lengthResults.size());
                Assert.assertEquals(3, lengthResults.get(3).intValue());
                // Fifth interaction: advance clock and assert new data
                clock.advanceTime(5, TimeUnit.SECONDS);// 10 seconds

                ksession.insert(new OrderEvent("5", "customer A", 70));
                ksession.fireAllRules();
                Assert.assertEquals(5, timeResults.size());
                Assert.assertEquals(4, timeResults.get(4).intValue());
                Assert.assertEquals(5, lengthResults.size());
                Assert.assertEquals(3, lengthResults.get(4).intValue());
            } finally {
                logger.writeToDisk();
            }
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testPseudoSchedulerRemoveJobTest() {
        final String drl = (((((((((("import " + (CepEspTest.class.getName())) + ".A\n") + "declare A\n") + "    @role( event )\n") + "end\n") + "rule A\n") + "when\n") + "   $a : A()\n") + "   not A(this after [1s,10s] $a)\n") + "then\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final FactHandle h = ksession.insert(new CepEspTest.A());
            ksession.delete(h);
        } finally {
            ksession.dispose();
        }
    }

    public static class A implements Serializable {}

    public static class Message {
        private Properties properties;

        private Timestamp timestamp;

        private Long duration;

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(final Properties properties) {
            this.properties = properties;
        }

        public Timestamp getStartTime() {
            return timestamp;
        }

        public void setStartTime(final Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        public Long getDuration() {
            return duration;
        }

        public void setDuration(final Long duration) {
            this.duration = duration;
        }
    }

    @Test(timeout = 10000)
    public void testEventDeclarationForInterfaces() {
        final String drl = ((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getCanonicalName())) + "\n") + "declare StockTick \n") + "    @role( event )\n") + "    @timestamp( dateTimestamp )\n") + "end\n") + "\n") + "rule \"Check event\"\n") + "when\n") + "    $st : StockTick( company == \"ACME\" )\n") + "then\n") + "    // no-op\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession session = kbase.newKieSession();
        try {
            final StockTick tick1 = new StockTick(1, "DROO", 50, 10000);
            final StockTick tick2 = new StockTick(2, "ACME", 10, 10010);
            final StockTick tick3 = new StockTick(3, "ACME", 10, 10100);
            final StockTick tick4 = new StockTick(4, "DROO", 50, 11000);
            final InternalFactHandle handle1 = ((InternalFactHandle) (session.insert(tick1)));
            final InternalFactHandle handle2 = ((InternalFactHandle) (session.insert(tick2)));
            final InternalFactHandle handle3 = ((InternalFactHandle) (session.insert(tick3)));
            final InternalFactHandle handle4 = ((InternalFactHandle) (session.insert(tick4)));
            Assert.assertTrue(handle1.isEvent());
            Assert.assertTrue(handle2.isEvent());
            Assert.assertTrue(handle3.isEvent());
            Assert.assertTrue(handle4.isEvent());
        } finally {
            session.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testTemporalOperators() {
        final String drl = ((((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n") + "declare StockTick \n") + "    @role( event )\n") + "    @timestamp( time )\n") + "end\n") + "\n") + "rule \"2 operators\"\n") + "when\n") + "    $a : StockTick( company == \"A\" )\n") + "    not( StockTick( company == \"B\", this after[0,20s] $a ) )\n") + "    not( StockTick( company == \"C\", this after[0,20s] $a ) )\n") + "then\n") + "    // do something\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            ksession.insert(new StockTick(1, "A", 10, 1000));
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testTemporalOperators2() {
        final String drl = (((((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n") + "global java.util.List list;\n") + "\n") + "declare StockTick \n") + "    @role( event )\n") + "end\n") + "\n") + "rule \"2 operators\"\n") + "when\n") + "    $a : StockTick( ) from entry-point \"X\"\n") + "    $b : StockTick( this after[1s,10s] $a ) from entry-point \"X\"\n") + "then\n") + "    list.add( new StockTick[] { $a, $b } );\n") + "end\n") + "\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            final SessionPseudoClock clock = ksession.getSessionClock();
            final EntryPoint ep = ksession.getEntryPoint("X");
            clock.advanceTime(1000, TimeUnit.SECONDS);
            ep.insert(new StockTick(1, "A", 10, clock.getCurrentTime()));
            clock.advanceTime(8, TimeUnit.SECONDS);
            ep.insert(new StockTick(2, "B", 10, clock.getCurrentTime()));
            clock.advanceTime(8, TimeUnit.SECONDS);
            ep.insert(new StockTick(3, "B", 10, clock.getCurrentTime()));
            clock.advanceTime(8, TimeUnit.SECONDS);
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testTemporalOperatorsInfinity() {
        final String drl = (((((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n") + "global java.util.List list;\n") + "\n") + "declare StockTick \n") + "    @role( event )\n") + "end\n") + "\n") + "rule \"infinity\"\n") + "when\n") + "    $a : StockTick( ) from entry-point \"X\"\n") + "    $b : StockTick( this after[5s,*] $a ) from entry-point \"X\"\n") + "then\n") + "    list.add( $a );\n") + "    list.add( $b );\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            final SessionPseudoClock clock = ksession.getSessionClock();
            final EntryPoint ep = ksession.getEntryPoint("X");
            clock.advanceTime(1000, TimeUnit.SECONDS);
            ep.insert(new StockTick(1, "A", 10, clock.getCurrentTime()));
            clock.advanceTime(8, TimeUnit.SECONDS);
            ep.insert(new StockTick(2, "B", 10, clock.getCurrentTime()));
            clock.advanceTime(8, TimeUnit.SECONDS);
            ep.insert(new StockTick(3, "B", 10, clock.getCurrentTime()));
            clock.advanceTime(8, TimeUnit.SECONDS);
            Assert.assertEquals(3, ksession.fireAllRules());
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testMultipleSlidingWindows() {
        final String drl = "declare A\n" + ((((((((((((((((((((((((((((("    @role( event )\n" + "    id : int\n") + "end\n") + "declare B\n") + "    @role( event )\n") + "    id : int\n") + "end\n") + "rule launch\n") + "when\n") + "then\n") + "    insert( new A( 1 ) );\n") + "    insert( new A( 2 ) );\n") + "    insert( new B( 1 ) );\n") + "    insert( new A( 3 ) );\n") + "    insert( new B( 2 ) );\n") + "end\n") + "rule \"ab\" \n") + "when\n") + "    A( $a : id ) over window:length( 1 )\n") + "    B( $b : id ) over window:length( 1 )\n") + "then\n") + "    System.out.println(\"AB: ( \"+$a+\", \"+$b+\" )\");\n") + "end\n") + "rule \"ba\" salience 10\n") + "when\n") + "    B( $b : id ) over window:length( 1 )\n") + "    A( $a : id ) over window:length( 1 )\n") + "then\n") + "    System.out.println(\"BA: ( \"+$b+\", \"+$a+\" )\");\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final AgendaEventListener ael = Mockito.mock(AgendaEventListener.class);
            ksession.addEventListener(ael);
            ksession.fireAllRules();
            final ArgumentCaptor<AfterMatchFiredEvent> captor = ArgumentCaptor.forClass(AfterMatchFiredEvent.class);
            Mockito.verify(ael, Mockito.times(3)).afterMatchFired(captor.capture());
            final List<AfterMatchFiredEvent> values = captor.getAllValues();
            // first rule
            Match act = values.get(0).getMatch();
            Assert.assertThat(act.getRule().getName(), CoreMatchers.is("launch"));
            // second rule
            act = values.get(1).getMatch();
            Assert.assertThat(act.getRule().getName(), CoreMatchers.is("ba"));
            Assert.assertThat(((Number) (act.getDeclarationValue("$a"))).intValue(), CoreMatchers.is(3));
            Assert.assertThat(((Number) (act.getDeclarationValue("$b"))).intValue(), CoreMatchers.is(2));
            // third rule
            act = values.get(2).getMatch();
            Assert.assertThat(act.getRule().getName(), CoreMatchers.is("ab"));
            Assert.assertThat(((Number) (act.getDeclarationValue("$a"))).intValue(), CoreMatchers.is(3));
            Assert.assertThat(((Number) (act.getDeclarationValue("$b"))).intValue(), CoreMatchers.is(2));
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testSalienceWithEventsPseudoClock() {
        final String drl = (((((((((((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getName())) + "\n") + "declare StockTick\n") + "        @role ( event )\n") + "end\n") + "rule R1 salience 1000\n") + "    when\n") + "        $s1 : StockTick( company == \'RHT\' )\n") + "        $s2 : StockTick( company == \'ACME\', this after[0s,1m] $s1 )\n") + "    then\n") + "end\n") + "rule R2 salience 1000\n") + "    when\n") + "        $s1 : StockTick( company == \'RHT\' )\n") + "        not StockTick( company == \'ACME\', this after[0s,1m] $s1 )\n") + "    then\n") + "end\n") + "rule R3 salience 100\n") + "    when\n") + "        $s2 : StockTick( company == \'ACME\' )\n") + "    then\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final AgendaEventListener ael = Mockito.mock(AgendaEventListener.class);
            ksession.addEventListener(ael);
            final SessionPseudoClock clock = ksession.getSessionClock();
            clock.advanceTime(1000000, TimeUnit.MILLISECONDS);
            ksession.insert(new StockTick(1, "RHT", 10, 1000));
            clock.advanceTime(5, TimeUnit.SECONDS);
            ksession.insert(new StockTick(2, "RHT", 10, 1000));
            clock.advanceTime(5, TimeUnit.SECONDS);
            ksession.insert(new StockTick(3, "RHT", 10, 1000));
            clock.advanceTime(5, TimeUnit.SECONDS);
            ksession.insert(new StockTick(4, "ACME", 10, 1000));
            clock.advanceTime(5, TimeUnit.SECONDS);
            final int rulesFired = ksession.fireAllRules();
            Assert.assertEquals(4, rulesFired);
            final ArgumentCaptor<AfterMatchFiredEvent> captor = ArgumentCaptor.forClass(AfterMatchFiredEvent.class);
            Mockito.verify(ael, Mockito.times(4)).afterMatchFired(captor.capture());
            final List<AfterMatchFiredEvent> aafe = captor.getAllValues();
            Assert.assertThat(aafe.get(0).getMatch().getRule().getName(), CoreMatchers.is("R1"));
            Assert.assertThat(aafe.get(1).getMatch().getRule().getName(), CoreMatchers.is("R1"));
            Assert.assertThat(aafe.get(2).getMatch().getRule().getName(), CoreMatchers.is("R1"));
            Assert.assertThat(aafe.get(3).getMatch().getRule().getName(), CoreMatchers.is("R3"));
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testSalienceWithEventsRealtimeClock() throws InterruptedException {
        final String drl = (((((((((((((((((((((("package org.drools.compiler\n" + "import ") + (StockTick.class.getName())) + "\n") + "declare StockTick\n") + "        @role ( event )\n") + "end\n") + "rule R1 salience 1000\n") + "    when\n") + "        $s1 : StockTick( company == \'RHT\' )\n") + "        $s2 : StockTick( company == \'ACME\', this after[0s,1m] $s1 )\n") + "    then\n") + "end\n") + "rule R2 salience 1000\n") + "    when\n") + "        $s1 : StockTick( company == \'RHT\' )\n") + "        not StockTick( company == \'ACME\', this after[0s,1m] $s1 )\n") + "    then\n") + "end\n") + "rule R3 salience 100\n") + "    when\n") + "        $s2 : StockTick( company == \'ACME\' )\n") + "    then\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final AgendaEventListener ael = Mockito.mock(AgendaEventListener.class);
            ksession.addEventListener(ael);
            ksession.insert(new StockTick(1, "RHT", 10, 1000));
            ksession.insert(new StockTick(2, "RHT", 10, 1000));
            ksession.insert(new StockTick(3, "RHT", 10, 1000));
            // sleep for 2 secs
            Thread.sleep(2000);
            ksession.insert(new StockTick(4, "ACME", 10, 1000));
            // sleep for 1 sec
            Thread.sleep(1000);
            final int rulesFired = ksession.fireAllRules();
            Assert.assertEquals(4, rulesFired);
            final ArgumentCaptor<AfterMatchFiredEvent> captor = ArgumentCaptor.forClass(AfterMatchFiredEvent.class);
            Mockito.verify(ael, Mockito.times(4)).afterMatchFired(captor.capture());
            final List<AfterMatchFiredEvent> aafe = captor.getAllValues();
            Assert.assertThat(aafe.get(0).getMatch().getRule().getName(), CoreMatchers.is("R1"));
            Assert.assertThat(aafe.get(1).getMatch().getRule().getName(), CoreMatchers.is("R1"));
            Assert.assertThat(aafe.get(2).getMatch().getRule().getName(), CoreMatchers.is("R1"));
            Assert.assertThat(aafe.get(3).getMatch().getRule().getName(), CoreMatchers.is("R3"));
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testExpireEventOnEndTimestamp() {
        // DROOLS-40
        final String drl = (((((((((((((((("package org.drools.compiler;\n" + "import ") + (StockTick.class.getName())) + "\n") + "global java.util.List resultsAfter;\n") + "\n") + "declare StockTick\n") + "    @role( event )\n") + "    @duration( duration )\n") + "end\n") + "\n") + "rule \"after[60,80]\"\n") + "when\n") + "$a : StockTick( company == \"DROO\" )\n") + "$b : StockTick( company == \"ACME\", this after[60,80] $a )\n") + "then\n") + "       resultsAfter.add( $b );\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = ksession.getSessionClock();
            final List<StockTick> resultsAfter = new ArrayList<>();
            ksession.setGlobal("resultsAfter", resultsAfter);
            // inserting new StockTick with duration 30 at time 0 => rule
            // after[60,80] should fire when ACME lasts at 100-120
            ksession.insert(new StockTick(1, "DROO", 0, 0, 30));
            clock.advanceTime(100, TimeUnit.MILLISECONDS);
            ksession.insert(new StockTick(2, "ACME", 0, 0, 20));
            ksession.fireAllRules();
            Assert.assertEquals(1, resultsAfter.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testEventExpirationDuringAccumulate() throws Exception {
        // DROOLS-70
        final String drl = "package org.drools.integrationtests\n" + ((((((((((((((((("\n" + "import java.util.List;\n") + "\n") + "declare Stock\n") + "    @role( event )\n") + "    @expires( 1s ) // setting to a large value causes the test to pass\n") + "    name : String\n") + "    value : Double\n") + "end\n") + "\n") + "rule \"collect events\"\n") + "when\n") + "    stocks := List()\n") + "        from accumulate( $zeroStock : Stock( value == 0.0 );\n") + "                         collectList( $zeroStock ) )\n") + "then\n") + "    // empty consequence\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final CepEspTest.StockFactory stockFactory = new CepEspTest.StockFactory(kbase);
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                final Future sessionFuture = executor.submit(((Runnable) (ksession::fireUntilHalt)));
                try {
                    for (int iteration = 0; iteration < 100; iteration++) {
                        this.populateSessionWithStocks(ksession, stockFactory);
                    }
                    // let the engine finish its job
                    Thread.sleep(2000);
                } finally {
                    ksession.halt();
                    // not to swallow possible exception
                    sessionFuture.get();
                    ksession.dispose();
                }
            } finally {
                executor.shutdownNow();
            }
        } finally {
            ksession.dispose();
        }
    }

    /**
     * Factory creating events used in the test.
     */
    private static class StockFactory {
        private static final String DRL_PACKAGE_NAME = "org.drools.integrationtests";

        private static final String DRL_FACT_NAME = "Stock";

        private final KieBase kbase;

        public StockFactory(final KieBase kbase) {
            this.kbase = kbase;
        }

        public Object createStock(final String name, final Double value) {
            try {
                return this.createDRLStock(name, value);
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Unable to create Stock instance defined in DRL", e);
            }
        }

        private Object createDRLStock(final String name, final Double value) throws IllegalAccessException, InstantiationException {
            final FactType stockType = kbase.getFactType(CepEspTest.StockFactory.DRL_PACKAGE_NAME, CepEspTest.StockFactory.DRL_FACT_NAME);
            final Object stock = stockType.newInstance();
            stockType.set(stock, "name", name);
            stockType.set(stock, "value", value);
            return stock;
        }
    }

    @Test(timeout = 10000)
    public void testEventExpirationInSlidingWindow() throws Exception {
        // DROOLS-70
        final String drl = "package org.drools.integrationtests\n" + (((((((((((("\n" + "declare Stock\n") + "    @role( event )\n") + "    name : String\n") + "    value : Double\n") + "end\n") + "\n") + "rule \"collect time window contents\"\n") + "when\n") + "    Stock( value == 0.0 ) over window:time(2s)\n") + "then\n") + "    // empty consequence\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        final CepEspTest.StockFactory stockFactory = new CepEspTest.StockFactory(kbase);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            final Future sessionFuture = executor.submit(((Runnable) (ksession::fireUntilHalt)));
            try {
                for (int iteration = 0; iteration < 100; iteration++) {
                    this.populateSessionWithStocks(ksession, stockFactory);
                }
                // let the engine finish its job
                Thread.sleep(2000);
            } finally {
                ksession.halt();
                // not to swallow possible exception
                sessionFuture.get();
                ksession.dispose();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test(timeout = 10000)
    public void testSlidingWindowsAccumulateExternalJoin() {
        // DROOLS-106
        // The logic may not be optimal, but was used to detect a WM corruption
        final String drl = (((((((((((((((((((("package testing2;\n" + (("\n" + "import java.util.*;\n") + "import ")) + (StockTick.class.getCanonicalName())) + "\n") + "") + "global List list;\n") + "") + "declare StockTick\n") + " @role( event )\n") + " @duration( duration )\n") + "end\n") + "\n") + "rule test\n") + "when\n") + " $primary : StockTick( $name : company ) over window:length(1)\n") + " accumulate ( ") + " $tick : StockTick( company == $name ) , ") + " $num : count( $tick ) )\n") + "then\n") + " list.add( $num.intValue() ); \n") + "end\n") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            int seq = 0;
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            ksession.insert(new StockTick((seq++), "AAA", 10.0, 10L));
            ksession.fireAllRules();
            Assert.assertEquals(list, Collections.singletonList(1));
            ksession.insert(new StockTick((seq++), "AAA", 15.0, 10L));
            ksession.fireAllRules();
            Assert.assertEquals(list, Arrays.asList(1, 2));
            ksession.insert(new StockTick((seq++), "CCC", 10.0, 10L));
            ksession.fireAllRules();
            Assert.assertEquals(list, Arrays.asList(1, 2, 1));
            ksession.insert(new StockTick((seq++), "DDD", 13.0, 20L));
            ksession.fireAllRules();
            Assert.assertEquals(list, Arrays.asList(1, 2, 1, 1));
            ksession.insert(new StockTick(seq, "AAA", 11.0, 20L));
            ksession.fireAllRules();
            Assert.assertEquals(list, Arrays.asList(1, 2, 1, 1, 3));
            // NPE Here
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testTimeAndLengthWindowConflict() {
        // JBRULES-3671
        final String drl = (((((((((((((((((((((((((("package org.drools.compiler;\n" + ("import java.util.List\n" + "import ")) + (OrderEvent.class.getCanonicalName())) + "\n") + "\n") + "global List timeResults;\n") + "global List lengthResults;\n") + "\n") + "declare OrderEvent\n") + " @role( event )\n") + "end\n") + "\n") + "rule \"collect with time window\"\n") + "when\n") + " $list : List( empty == false ) from collect(\n") + " $o : OrderEvent() over window:time(30s) )\n") + "then\n") + " timeResults.add( $list.size() );\n") + "end\n") + "\n") + "rule \"collect with length window\"\n") + "when\n") + " accumulate (\n") + " $o : OrderEvent( $tot : total ) over window:length(3),") + " $avg : average( $tot ) )\n") + "then\n") + " lengthResults.add( $avg );\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List<Number> timeResults = new ArrayList<>();
            final List<Number> lengthResults = new ArrayList<>();
            ksession.setGlobal("timeResults", timeResults);
            ksession.setGlobal("lengthResults", lengthResults);
            final SessionPseudoClock clock = ksession.getSessionClock();
            clock.advanceTime(5, TimeUnit.SECONDS);// 5 seconds

            ksession.insert(new OrderEvent("1", "customer A", 70));
            ksession.fireAllRules();
            System.out.println(lengthResults);
            Assert.assertTrue(lengthResults.contains(70.0));
            clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

            ksession.insert(new OrderEvent("2", "customer A", 60));
            ksession.fireAllRules();
            System.out.println(lengthResults);
            Assert.assertTrue(lengthResults.contains(65.0));
            // Third interaction: advance clock and assert new data
            clock.advanceTime(10, TimeUnit.SECONDS);// 10 seconds

            ksession.insert(new OrderEvent("3", "customer A", 50));
            ksession.fireAllRules();
            System.out.println(lengthResults);
            Assert.assertTrue(lengthResults.contains(60.0));
            // Fourth interaction: advance clock and assert new data
            clock.advanceTime(60, TimeUnit.SECONDS);// 60 seconds

            ksession.insert(new OrderEvent("4", "customer A", 25));
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testTimeStampOnNonExistingField() {
        // BZ-985942
        final String drl = (((((("package org.drools.compiler;\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n") + "declare StockTick\n") + " @role( event )\n") + " @timestamp( nonExistingField ) \n") + "end\n";
        final KieBuilder kieBuilder = KieUtil.getKieBuilderFromDrls(kieBaseTestConfiguration, false, drl);
        Assertions.assertThat(kieBuilder.getResults().getMessages()).isNotEmpty();
    }

    @Test(timeout = 10000)
    public void testTimeWindowWithPastEvents() {
        // JBRULES-2258
        final String drl = ((((((((((((((((((("package org.drools.compiler;\n" + ("import java.util.List\n" + "import ")) + (StockTick.class.getCanonicalName())) + ";\n") + "\n") + "global List timeResults;\n") + "\n") + "declare StockTick\n") + " @role( event )\n") + " @timestamp( time ) \n") + "end\n") + "\n") + "rule \"collect with time window\"\n") + "when\n") + " accumulate(\n") + " $o : StockTick() over window:time(10ms),") + " $tot : count( $o );") + " $tot > 0 )\n") + "then\n") + " timeResults.add( $tot );\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List<Number> timeResults = new ArrayList<>();
            ksession.setGlobal("timeResults", timeResults);
            final SessionPseudoClock clock = ksession.getSessionClock();
            int count = 0;
            final StockTick tick1 = new StockTick((count++), "X", 0.0, 1);
            final StockTick tick2 = new StockTick((count++), "X", 0.0, 3);
            final StockTick tick3 = new StockTick((count++), "X", 0.0, 7);
            final StockTick tick4 = new StockTick((count++), "X", 0.0, 9);
            final StockTick tick5 = new StockTick(count, "X", 0.0, 15);
            clock.advanceTime(30, TimeUnit.MILLISECONDS);
            ksession.insert(tick1);
            ksession.insert(tick2);
            ksession.insert(tick3);
            ksession.insert(tick4);
            ksession.insert(tick5);
            ksession.fireAllRules();
            Assert.assertTrue(timeResults.isEmpty());
            clock.advanceTime(0, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertTrue(timeResults.isEmpty());
            clock.advanceTime(3, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertTrue(timeResults.isEmpty());
            clock.advanceTime(10, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertTrue(timeResults.isEmpty());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testLeakingActivationsWithDetachedExpiredNonCancelling() throws Exception {
        // JBRULES-3558 - DROOLS 311
        // TODO: it is still possible to get multiple insertions of the Recording object
        // if you set the @expires of Motion to 1ms, maybe because the event expires too soon
        final String drl = "package org.drools;\n" + (((((((((((((((((((((((((((("\n" + "import java.util.List\n") + "\n") + "global List list; \n") + "") + "declare Motion\n") + "    @role( event )\n") + "    @expires( 10ms )\n") + "    @timestamp( timestamp )\n") + "    timestamp : long\n") + "end\n") + "\n") + "declare Recording\n") + "end\n") + "\n") + "") + "rule Init salience 1000 when\n") + "    $l : Long() \n") + "then\n") + "    insert( new Motion( $l ) ); \n") + "end\n") + "") + "rule \"StartRecording\" when\n") + "   $mot : Motion()\n") + "   not Recording()\n") + " then\n") + "   list.add( $mot ); \n ") + "   insert(new Recording());\n") + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final List<Number> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.insert(1000L);
            ksession.insert(1001L);
            ksession.insert(1002L);
            Thread.sleep(1000);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testTwoWindowsInsideCEAndOut() {
        final String drl = (((((((((((((((((("package org.drools.compiler;\n" + ("import java.util.List\n" + "import ")) + (OrderEvent.class.getCanonicalName())) + ";\n") + "\n") + "global List timeResults;\n") + "\n") + "declare ") + (OrderEvent.class.getCanonicalName())) + "\n") + " @role( event )\n") + "end\n") + "\n") + "rule \"r1\"\n") + "when\n") + "    $o1 : OrderEvent() over window:length(3) \n") + "        accumulate(  $o2 : OrderEvent() over window:length(3);\n") + "                     $avg : average( $o2.getTotal() ) )\n") + "then\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession wm = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            wm.insert(new OrderEvent("1", "customer A", 70));
            wm.insert(new OrderEvent("2", "customer A", 60));
            wm.insert(new OrderEvent("3", "customer A", 50));
            wm.insert(new OrderEvent("4", "customer A", 40));
            wm.insert(new OrderEvent("5", "customer A", 30));
            wm.insert(new OrderEvent("6", "customer A", 20));
            wm.insert(new OrderEvent("7", "customer A", 10));
            wm.fireAllRules();
        } finally {
            wm.dispose();
        }
    }

    @Test
    public void testUpdateEventThroughEntryPoint() {
        final String drl = (((((((((((((("import " + (CepEspTest.TestEvent.class.getCanonicalName())) + "\n") + "\n") + "declare TestEvent\n") + "    @role( event )\n") + "    @expires( 4s )\n") + "end\n") + "\n") + "rule \"TestEventReceived\"\n") + "    no-loop\n") + "    when\n") + "        $event : TestEvent ( name != null ) over window:time( 4s ) from entry-point EventStream\n") + "    then\n") + "        // insert( new Message( $event.getValue().toString() ) );\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession kieSession = kbase.newKieSession();
        try {
            final EntryPoint entryPoint = kieSession.getEntryPoint("EventStream");
            final CepEspTest.TestEvent event = new CepEspTest.TestEvent("testEvent1");
            final FactHandle handle = entryPoint.insert(event);
            final CepEspTest.TestEvent event2 = new CepEspTest.TestEvent("testEvent2");
            entryPoint.update(handle, event2);
            // make sure the event is in the entry-point
            Assert.assertFalse(entryPoint.getObjects().contains(event));
            Assert.assertTrue(entryPoint.getObjects().contains(event2));
            Assert.assertEquals(entryPoint.getObject(handle), event2);
        } finally {
            kieSession.dispose();
        }
    }

    public static class TestEvent implements Serializable {
        private final String name;

        public TestEvent(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return String.format("TestEvent[name=%s]", name);
        }
    }

    @Test
    public void testStreamModeWithSubnetwork() {
        // BZ-1009348
        final String drl = "package org.drools.compiler.integrationtests\n" + (((((((((((((((((("\n" + "declare Event\n") + "  @role(event)\n") + "  name : String\n") + "end\n") + "\n") + "global java.util.List list\n") + "\n") + "rule \"firstRule\"\n") + "\n") + "    when\n") + "        not (\n") + "            $e : Event() over window:length(3)\n") + "            and Event( this == $e ) // test pass when you comment this line\n") + "        )\n") + "    then\n") + "        list.add(\"firstRule\");\n") + "\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final ArrayList<String> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
        } finally {
            ksession.dispose();
        }
    }

    public static class Event {
        private int type;

        private int value;

        private long time;

        public Event(final int type, final int value, final long time) {
            this.type = type;
            this.value = value;
            this.time = time;
        }

        public int getType() {
            return type;
        }

        public void setType(final int type) {
            this.type = type;
        }

        public int getValue() {
            return value;
        }

        public void setValue(final int value) {
            this.value = value;
        }

        public long getTime() {
            return time;
        }

        public void setTime(final long time) {
            this.time = time;
        }

        @Override
        public String toString() {
            return (((((("Event{" + "type=") + (type)) + ", value=") + (value)) + ", time=") + ((time) % 10000)) + '}';
        }
    }

    @Test
    public void testEventTimestamp() {
        // DROOLS-268
        final String drl = ((((((((((((((((((((((("\n" + "import ") + (CepEspTest.Event.class.getCanonicalName())) + "; \n") + "global java.util.List list; \n") + "global org.kie.api.time.SessionPseudoClock clock; \n") + "") + "declare Event \n") + " @role( event )\n") + " @timestamp( time ) \n") + " @expires( 10000000 ) \n") + "end \n") + "") + "") + "rule \"inform about E1\"\n") + "when\n") + " $event1 : Event( type == 1 )\n") + " //there is an event (T2) with value 0 between 0,2m after doorClosed\n") + " $event2: Event( type == 2, value == 1, this after [0, 1200ms] $event1, $timestamp : time )\n") + " //there is no newer event (T2) within the timeframe\n") + " not Event( type == 2, this after [0, 1200ms] $event1, time > $timestamp ) \n") + "then\n") + " list.add( clock.getCurrentTime() ); \n ") + "end\n") + "\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final ArrayList list = new ArrayList();
            ksession.setGlobal("list", list);
            final SessionPseudoClock clock = ksession.getSessionClock();
            ksession.setGlobal("clock", clock);
            ksession.insert(new CepEspTest.Event(1, (-1), clock.getCurrentTime()));// 0

            clock.advanceTime(600, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            ksession.insert(new CepEspTest.Event(2, 0, clock.getCurrentTime()));// 600

            clock.advanceTime(100, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            ksession.insert(new CepEspTest.Event(2, 0, clock.getCurrentTime()));// 700

            clock.advanceTime(300, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            ksession.insert(new CepEspTest.Event(2, 0, clock.getCurrentTime()));// 1000

            clock.advanceTime(100, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            ksession.insert(new CepEspTest.Event(2, 1, clock.getCurrentTime()));// 1100

            clock.advanceTime(100, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            clock.advanceTime(100, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            ksession.insert(new CepEspTest.Event(2, 0, clock.getCurrentTime()));// 1300

            clock.advanceTime(1000, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertFalse(list.isEmpty());
            Assert.assertEquals(1, list.size());
            final Long time = ((Long) (list.get(0)));
            Assert.assertTrue(((time > 1000) && (time < 1500)));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testEventTimestamp2() {
        // DROOLS-268
        final String drl = (((((((((((((((((((((("\n" + "import ") + (CepEspTest.Event.class.getCanonicalName())) + ";\n") + "global java.util.List list; \n") + "global org.kie.api.time.SessionPseudoClock clock; \n") + "") + "declare Event \n") + " @role( event )\n") + " @timestamp( time ) \n") + " @expires( 10000000 ) \n") + "end \n") + "") + "") + "rule \"inform about E1\"\n") + "when\n") + " $event0 : Event( type == 0 )\n") + " $event1 : Event( type == 1 )\n") + " $event2: Event( type == 2 )\n") + " not Event( type == 3, this after [0, 1000ms] $event1 ) \n") + "then\n") + " list.add( clock.getCurrentTime() ); \n ") + "end\n") + "\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final ArrayList list = new ArrayList();
            ksession.setGlobal("list", list);
            final SessionPseudoClock clock = ksession.getSessionClock();
            ksession.setGlobal("clock", clock);
            ksession.insert(new CepEspTest.Event(0, 0, clock.getCurrentTime()));
            clock.advanceTime(100, TimeUnit.MILLISECONDS);
            ksession.insert(new CepEspTest.Event(1, 0, clock.getCurrentTime()));
            clock.advanceTime(600, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            ksession.insert(new CepEspTest.Event(2, 0, clock.getCurrentTime()));
            clock.advanceTime(600, TimeUnit.MILLISECONDS);
            ksession.insert(new CepEspTest.Event(3, 0, clock.getCurrentTime()));
            ksession.fireAllRules();
            Assert.assertFalse(list.isEmpty());
            Assert.assertEquals(1, list.size());
            final long time = ((Long) (list.get(0)));
            Assert.assertEquals(1300, time);
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testModifyInStreamMode() {
        // BZ-1012933
        final String drl = (((((((((((((("import " + (CepEspTest.SimpleFact.class.getCanonicalName())) + ";\n") + "global java.util.List list;\n") + "declare SimpleFact\n") + "    @role( event )\n") + "end\n") + "\n") + "rule \"MyRule\"\n") + "when\n") + "    $f : SimpleFact( status == \"NOK\" )\n") + "then\n") + "    list.add(\"Firing\");") + "    $f.setStatus(\"OK\");\n") + "    update ($f);\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            final CepEspTest.SimpleFact fact = new CepEspTest.SimpleFact("id1");
            ksession.insert(fact);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals("OK", fact.getStatus());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testCollectAfterRetract() {
        // BZ-1015109
        final String drl = (((((((((((((((((((((((("import " + (CepEspTest.SimpleFact.class.getCanonicalName())) + ";\n") + "import java.util.List;\n") + "global List list;\n") + "\n") + "declare SimpleFact\n") + "    @role( event )\n") + "end\n") + "\n") + "rule \"Retract facts if 2 or more\" salience 1000\n") + "when\n") + "    $facts : List( size >= 2 ) from collect( SimpleFact() )\n") + "then\n") + "    for (Object f: new java.util.LinkedList($facts)) {\n") + "        System.out.println(\"Retracting \"+f);\n") + "        retract(f);\n") + "    }\n") + "end\n") + "\n") + "rule \"Still facts in WM\"\n") + "when\n") + "    $facts : List( size != 0 ) from collect( SimpleFact() )\n") + "then\n") + "    list.add( $facts.size() );\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            ksession.insert(new CepEspTest.SimpleFact("id1"));
            ksession.insert(new CepEspTest.SimpleFact("id2"));
            ksession.insert(new CepEspTest.SimpleFact("id3"));
            ksession.fireAllRules();
            Assert.assertEquals(0, list.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testCollectAfterUpdate() {
        // DROOLS-295
        final String drl = ((((((((((((((((((((((((("import " + (CepEspTest.SimpleFact.class.getCanonicalName())) + ";\n") + "import java.util.List;\n") + "\n") + "declare SimpleFact\n") + "    @role( event )\n") + "end\n") + "\n") + "rule \"Rule1\"\n") + "    when\n") + "        event : SimpleFact( status == \"NOK\" )\n") + "        list: List(size < 4) from collect( SimpleFact(this != event, status==\"1\") )\n") + "    then\n") + "        event.setStatus(\"1\");\n") + "        update(event);\n") + "    end\n") + "\n") + "rule \"Rule2\"\n") + "    when\n") + "        event : SimpleFact( status == \"NOK\" )\n") + "        list: List(size >= 4) from collect( SimpleFact(this != event, status==\"1\") )\n") + "    then\n") + "        for (Object ev2: new java.util.LinkedList(list)) retract(ev2);\n") + "        event.setStatus(\"2\");\n") + "        update(event);\n") + "    end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            for (int i = 0; i < 4; i++) {
                ksession.insert(new CepEspTest.SimpleFact(("id" + i)));
            }
            ksession.fireAllRules();
            Assert.assertEquals("all events should be in WM", 4, ksession.getFactCount());
            ksession.insert(new CepEspTest.SimpleFact("last"));
            ksession.fireAllRules();
            Assert.assertEquals("only one event should be still in WM", 1, ksession.getFactCount());
        } finally {
            ksession.dispose();
        }
    }

    public static class SimpleFact {
        private String status = "NOK";

        private final String id;

        public SimpleFact(final String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(final String s) {
            status = s;
        }

        @Override
        public String toString() {
            return (((((getClass().getSimpleName()) + " (id=") + (id)) + ", status=") + (status)) + ")";
        }
    }

    public static class ProbeEvent {
        private int value = 1;

        public int getValue() {
            return value;
        }

        public ProbeEvent(final int value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if ((this) == o) {
                return true;
            }
            if ((o == null) || ((getClass()) != (o.getClass()))) {
                return false;
            }
            final CepEspTest.ProbeEvent that = ((CepEspTest.ProbeEvent) (o));
            return (value) == (that.value);
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public String toString() {
            return (("ProbeEvent{" + "value=") + (value)) + '}';
        }
    }

    public static class ProbeCounter {
        private long total = 0;

        public void setTotal(final long total) {
            this.total = total;
        }

        public long getTotal() {
            return total;
        }

        public void addValue() {
            total += 1;
        }
    }

    @Test
    public void AfterOperatorInCEPQueryTest() {
        final String drl = ((((((((((((((((((((("package org.drools;\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n") + "\n") + "declare StockTick\n") + "    @role( event )\n") + "end\n") + "\n") + "query EventsBeforeNineSeconds\n") + "   $event : StockTick() from entry-point EStream\n") + "   $result : StockTick ( this after [0s, 9s] $event) from entry-point EventStream\n") + "end\n") + "\n") + "query EventsBeforeNineteenSeconds\n") + "   $event : StockTick() from entry-point EStream\n") + "   $result : StockTick ( this after [0s, 19s] $event) from entry-point EventStream\n") + "end\n") + "\n") + "query EventsBeforeHundredSeconds\n") + "   $event : StockTick() from entry-point EStream\n") + "   $result : StockTick ( this after [0s, 100s] $event) from entry-point EventStream\n") + "end\n";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = ksession.getSessionClock();
            final EntryPoint ePoint = ksession.getEntryPoint("EStream");
            final EntryPoint entryPoint = ksession.getEntryPoint("EventStream");
            ePoint.insert(new StockTick(0L, "zero", 0.0, 0));
            entryPoint.insert(new StockTick(1L, "one", 0.0, 0));
            clock.advanceTime(10, TimeUnit.SECONDS);
            entryPoint.insert(new StockTick(2L, "two", 0.0, 0));
            clock.advanceTime(10, TimeUnit.SECONDS);
            entryPoint.insert(new StockTick(3L, "three", 0.0, 0));
            QueryResults results = ksession.getQueryResults("EventsBeforeNineSeconds");
            Assert.assertEquals(0, results.size());
            results = ksession.getQueryResults("EventsBeforeNineteenSeconds");
            Assert.assertEquals(0, results.size());
            results = ksession.getQueryResults("EventsBeforeHundredSeconds");
            Assert.assertEquals(1, results.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testFromWithEvents() {
        final String drl = "\n" + ((((((((((((((((((((((((((((((((((("\n" + "package org.drools.test\n") + "global java.util.List list; \n") + "\n") + "declare MyEvent\n") + "@role(event)\n") + "@timestamp( stamp )\n") + "id : int\n") + "stamp : long\n") + "end\n") + "\n") + "declare MyBean\n") + "id : int\n") + "event : MyEvent\n") + "end\n") + "\n") + "rule \"Init\"\n") + "when\n") + "then\n") + "MyEvent ev = new MyEvent( 1, 1000 );\n") + "MyBean bin = new MyBean( 99, ev );\n") + "MyEvent ev2 = new MyEvent( 2, 2000 );\n") + "\n") + "drools.getWorkingMemory().getWorkingMemoryEntryPoint( \"X\" ).insert( ev2 );\n") + "insert( bin );\n") + "end\n") + "\n") + "rule \"Check\"\n") + "when\n") + "$e2 : MyEvent( id == 2 ) from entry-point \"X\" \n") + "$b1 : MyBean( id == 99, $ev : event )\n") + "MyEvent( this before $e2 ) from $ev\n") + "then\n") + "System.out.println( \"Success\" );\n") + "list.add( 1 ); \n") + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ks = kbase.newKieSession();
        try {
            final ArrayList list = new ArrayList(1);
            ks.setGlobal("list", list);
            ks.fireAllRules();
            Assert.assertEquals(Collections.singletonList(1), list);
        } finally {
            ks.dispose();
        }
    }

    @Test
    public void testDeserializationWithTrackableTimerJob() throws InterruptedException {
        final String drl = ((((((((((((((((((((((("package org.drools.test;\n" + "import ") + (StockTick.class.getCanonicalName())) + "; \n") + "global java.util.List list;\n") + "\n") + "declare StockTick\n") + "  @role( event )\n") + "  @expires( 1s )\n") + "end\n") + "\n") + "rule \"One\"\n") + "when\n") + "  StockTick( $id : seq, company == \"AAA\" ) over window:time( 1s )\n") + "then\n") + "  list.add( $id ); \n") + "end\n") + "\n") + "rule \"Two\"\n") + "when\n") + "  StockTick( $id : seq, company == \"BBB\" ) \n") + "then\n") + "  System.out.println( $id ); \n") + "  list.add( $id );\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSessionConfiguration kieSessionConfiguration = STATEFUL_REALTIME.getKieSessionConfiguration();
        kieSessionConfiguration.setOption(TimerJobFactoryOption.get("trackable"));
        KieSession ks = kbase.newKieSession(kieSessionConfiguration, null);
        try {
            ks.insert(new StockTick(2, "BBB", 1.0, 0));
            Thread.sleep(1100);
            try {
                ks = SerializationHelper.getSerialisedStatefulKnowledgeSession(ks, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            ks.addEventListener(new DebugAgendaEventListener());
            final ArrayList list = new ArrayList();
            ks.setGlobal("list", list);
            ks.fireAllRules();
            ks.insert(new StockTick(3, "BBB", 1.0, 0));
            ks.fireAllRules();
            Assert.assertEquals(2, list.size());
            Assert.assertEquals(Arrays.asList(2L, 3L), list);
        } finally {
            ks.dispose();
        }
    }

    @Test
    public void testDeserializationWithExpiringEventAndAccumulate() throws InterruptedException {
        final String drl = ((((((((((((((((("package org.drools.test;\n" + "import ") + (StockTick.class.getCanonicalName())) + "; \n") + "global java.util.List list;\n") + "\n") + "declare StockTick\n") + "  @role( event )\n") + "  @expires( 1s )\n") + "end\n") + "\n") + "rule R\n") + "when\n") + "  accumulate ( StockTick( company == \"BBB\", $p : price), ") + "              $sum : sum( $p );") + "              $sum > 0 )\n") + "then\n") + "  list.add( $sum ); \n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        KieSession ks = kbase.newKieSession();
        try {
            ks.insert(new StockTick(1, "BBB", 1.0, 0));
            Thread.sleep(1000);
            ks.insert(new StockTick(2, "BBB", 2.0, 0));
            Thread.sleep(100);
            try {
                ks = SerializationHelper.getSerialisedStatefulKnowledgeSession(ks, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            final List<Double> list = new ArrayList<>();
            ks.setGlobal("list", list);
            ks.fireAllRules();
            ks.insert(new StockTick(3, "BBB", 3.0, 0));
            ks.fireAllRules();
            Assert.assertEquals(2, list.size());
            Assert.assertEquals(Arrays.asList(2.0, 5.0), list);
        } finally {
            ks.dispose();
        }
    }

    @Test
    public void testDeserializationWithCompositeTrigger() {
        final String drl = (((((((((((((((("package org.drools.test;\n" + "import ") + (StockTick.class.getCanonicalName())) + "; \n") + "global java.util.List list;\n") + "\n") + "declare StockTick\n") + "  @role( event )\n") + "  @expires( 1s )\n") + "end\n") + "\n") + "rule \"One\"\n") + "when\n") + "  $event : StockTick( )\n") + "  not StockTick( company == \"BBB\", this after[0,96h] $event )\n") + "  not StockTick( company == \"CCC\", this after[0,96h] $event )\n") + "then\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSessionConfiguration kieSessionConfiguration = STATEFUL_REALTIME.getKieSessionConfiguration();
        kieSessionConfiguration.setOption(TimerJobFactoryOption.get("trackable"));
        KieSession ks = kbase.newKieSession(kieSessionConfiguration, null);
        try {
            ks.insert(new StockTick(2, "AAA", 1.0, 0));
            try {
                ks = SerializationHelper.getSerialisedStatefulKnowledgeSession(ks, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        } finally {
            ks.dispose();
        }
    }

    @Test
    public void testWindowExpireActionDeserialization() throws InterruptedException {
        final String drl = (((((((((((((((("package org.drools.test;\n" + "import ") + (StockTick.class.getCanonicalName())) + "; \n") + "global java.util.List list; \n") + "\n") + "declare StockTick\n") + "  @role( event )\n") + "end\n") + "\n") + "rule \"One\"\n") + "when\n") + "  StockTick( $id : seq, company == \"BBB\" ) over window:time( 1s )\n") + "then\n") + "  list.add( $id );\n") + "end\n") + "\n") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        KieSession ks = kbase.newKieSession();
        try {
            ks.insert(new StockTick(2, "BBB", 1.0, 0));
            Thread.sleep(1500);
            try {
                ks = SerializationHelper.getSerialisedStatefulKnowledgeSession(ks, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            final ArrayList list = new ArrayList();
            ks.setGlobal("list", list);
            ks.fireAllRules();
            ks.insert(new StockTick(3, "BBB", 1.0, 0));
            ks.fireAllRules();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals(Collections.singletonList(3L), list);
        } finally {
            ks.dispose();
        }
    }

    @Test
    public void testDuplicateFiring1() {
        final String drl = ((((((((((((((((((((((((((("package org.test;\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n ") + "") + "global java.util.List list \n") + "") + "declare StockTick @role(event) end \n") + "") + "rule \"slidingTimeCount\"\n") + "when\n") + "  accumulate ( $e: StockTick() over window:time(300ms) from entry-point SensorEventStream, ") + "              $n : count( $e );") + "              $n > 0 )\n") + "then\n") + "  list.add( $n ); \n") + "  System.out.println( \"Events in last 3 seconds: \" + $n );\n") + "end") + "") + "\n") + "rule \"timerRuleAfterAllEvents\"\n") + "        timer ( int: 2s )\n") + "when\n") + "        $room : String( )\n") + "then\n") + "  list.add( -1 ); \n") + "  System.out.println(\"2sec after room was modified\");\n") + "end ") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = ksession.getSessionClock();
            final ArrayList list = new ArrayList();
            ksession.setGlobal("list", list);
            // entry point for sensor events
            final EntryPoint sensorEventStream = ksession.getEntryPoint("SensorEventStream");
            ksession.insert("Go");
            // insert events
            for (int i = 2; i < 8; i++) {
                final StockTick event = new StockTick((i - 1), "XXX", 1.0, 0);
                sensorEventStream.insert(event);
                ksession.fireAllRules();
                clock.advanceTime(105, TimeUnit.MILLISECONDS);
            }
            // let thread sleep for another 1m to see if dereffered rules fire (timers, (not) after rules)
            clock.advanceTime((100 * 40), TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(Arrays.asList(1L, 2L, 3L, 3L, 3L, 3L, (-1)), list);
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testPastEventExipration() {
        // DROOLS-257
        final String drl = ((((((((((((((("package org.test;\n" + "import ") + (StockTick.class.getCanonicalName())) + ";\n ") + "") + "global java.util.List list; \n") + "") + "declare StockTick @role(event) @timestamp( time ) @expires( 200ms ) end \n") + "") + "rule \"slidingTimeCount\"\n") + "when\n") + "  accumulate ( $e: StockTick() over window:length(10), $n : count($e) )\n") + "then\n") + "  list.add( $n ); \n") + "  System.out.println( \"Events in last X seconds: \" + $n );\n") + "end") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = ksession.getSessionClock();
            final ArrayList list = new ArrayList();
            ksession.setGlobal("list", list);
            final long now = 0;
            final StockTick event1 = new StockTick(1, "XXX", 1.0, now);
            final StockTick event2 = new StockTick(2, "XXX", 1.0, (now + 240));
            final StockTick event3 = new StockTick(2, "XXX", 1.0, (now + 380));
            final StockTick event4 = new StockTick(2, "XXX", 1.0, (now + 500));
            ksession.insert(event1);
            ksession.insert(event2);
            ksession.insert(event3);
            ksession.insert(event4);
            clock.advanceTime(220, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            clock.advanceTime(400, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(Arrays.asList(3L, 1L), list);
        } finally {
            ksession.dispose();
        }
    }

    public static class MyEvent {
        private long timestamp;

        public MyEvent(final long timestamp) {
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(final long timestamp) {
            this.timestamp = timestamp;
        }

        public String toString() {
            return (("MyEvent{" + "timestamp=") + (timestamp)) + '}';
        }
    }

    @Test
    public void testEventStreamWithEPsAndDefaultPseudo() {
        // DROOLS-286
        final String drl = (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("\n" + ("import java.util.*;\n" + "import ")) + (CepEspTest.MyEvent.class.getCanonicalName())) + ";\n") + "") + "declare MyEvent\n") + "    @role(event)\n") + "    @timestamp(timestamp)\n") + "end\n") + "\n") + "") + "global java.util.List list; \n") + "") + "rule \"over 0.3s\"\n") + "salience 1 \n") + "    when\n") + "        $list: List() from collect(MyEvent() over window:time(300ms))\n") + "    then\n") + "        System.out.println(\"Rule: with in 0.3s --> \" + $list);\n") + "        list.add( \'r1:\' + $list.size() ); \n") + "end\n") + "\n") + "rule \"over 1s\"\n") + "salience 2 \n") + "    when\n") + "        $list: List() from collect(MyEvent() over window:time(1s))\n") + "    then\n") + "        System.out.println(\"Rule: with in 1s --> \" + $list);\n") + "        list.add( \'r2:\' + $list.size() ); \n") + "end\n") + "\n") + "rule \"over 3s\"\n") + "salience 3 \n") + "    when\n") + "        $list: List() from collect(MyEvent() over window:time(3s))\n") + "    then\n") + "        System.out.println(\"Rule: with in 3s --> \" + $list);\n") + "        list.add( \'r3:\' + $list.size() ); \n") + "end\n") + "\n") + "rule \"over 0.3s ep\"\n") + "salience 4 \n") + "    when\n") + "        $list: List() from collect(MyEvent() over window:time(300ms) from entry-point \"stream\")\n") + "    then\n") + "        System.out.println(\"Rule: with in 0.3s use ep --> \" + $list);\n") + "        list.add( \'r4:\' + $list.size() ); \n") + "end\n") + "\n") + "rule \"over 1s ep\"\n") + "salience 5 \n") + "    when\n") + "        $list: List() from collect(MyEvent() over window:time(1s) from entry-point \"stream\")\n") + "    then\n") + "        System.out.println(\"Rule: with in 1s use ep --> \" + $list);\n") + "        list.add( \'r5:\' + $list.size() ); \n") + "end\n") + "\n") + "rule \"over 3s ep\"\n") + "salience 6 \n") + "    when\n") + "        $list: List() from collect(MyEvent() over window:time(3s) from entry-point \"stream\")\n") + "    then\n") + "        System.out.println(\"Rule: with in 3s use ep --> \" + $list);\n") + "        list.add( \'r6:\' + $list.size() ); \n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = ksession.getSessionClock();
            final ArrayList list = new ArrayList();
            ksession.setGlobal("list", list);
            ksession.fireAllRules();
            list.clear();
            for (int j = 0; j < 5; j++) {
                clock.advanceTime(500, TimeUnit.MILLISECONDS);
                ksession.insert(new CepEspTest.MyEvent(clock.getCurrentTime()));
                ksession.getEntryPoint("stream").insert(new CepEspTest.MyEvent(clock.getCurrentTime()));
                clock.advanceTime(500, TimeUnit.MILLISECONDS);
                ksession.fireAllRules();
                System.out.println(list);
                switch (j) {
                    case 0 :
                        Assert.assertEquals(Arrays.asList("r6:1", "r5:1", "r3:1", "r2:1"), list);
                        break;
                    case 1 :
                        Assert.assertEquals(Arrays.asList("r6:2", "r5:1", "r3:2", "r2:1"), list);
                        break;
                    case 2 :
                    case 3 :
                    case 4 :
                        Assert.assertEquals(Arrays.asList("r6:3", "r5:1", "r3:3", "r2:1"), list);
                        break;
                    default :
                        Assert.fail();
                }
                list.clear();
            }
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testExpirationOnModification() {
        // DROOLS-374
        final String drl = "\n" + (((((((((((((((((((((((("import java.util.*;\n" + "global List list; ") + "declare SomeEvent\n") + "    @role( event )\n") + "    @expires( 200ms )\n") + "  done : boolean = false \n") + "end\n") + "rule Count \n") + "  no-loop \n ") + "    when ") + "        $ev : SomeEvent( done == false  ) ") + "        accumulate ( SomeEvent() over window:time( 10s ),") + "                     $num : count( 1 ) )\n") + "    then\n") + // ok, modifies should never happen. But they are allowed, and if they do happen, the behaviour should be consistent
        "        modify ( $ev ) { setDone( true ); } ") + "        list.add( $num ); \n") + "end\n") + "rule Init \n") + "    when\n") + "      $s : String() ") + "    then\n") + "      retract( $s ); ") + "      insert( new SomeEvent() ); ") + "end\n") + "");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = ksession.getSessionClock();
            final ArrayList list = new ArrayList();
            ksession.setGlobal("list", list);
            ksession.insert("go");
            ksession.fireAllRules();
            clock.advanceTime(100, TimeUnit.MILLISECONDS);
            ksession.insert("go");
            ksession.fireAllRules();
            clock.advanceTime(500, TimeUnit.MILLISECONDS);
            ksession.insert("go");
            ksession.fireAllRules();
            Assert.assertEquals(Arrays.asList(1L, 2L, 1L), list);
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testTemporalEvaluatorsWithEventsFromNode() {
        // DROOLS-421
        final String drl = "\n" + (((((((((((((((((((((((((((("import java.util.*; " + "global List list; ") + "declare Delivery ") + "  @role( event ) @timestamp( effectiveDate ) ") + "  effectiveDate : Date ") + "  configs : List ") + "end ") + "declare Config ") + "  @role( event ) @timestamp( todate ) ") + "  todate : Date ") + "end ") + "rule Control ") + "when ") + "  $dpo: Delivery() ") + "  $gCfg: Config( todate == null || this after[ 0d ] $dpo ) from $dpo.configs ") + "then ") + "  list.add( 0 ); ") + "end ") + "rule Init ") + "when ") + "then ") + "  Delivery dpo = new Delivery( new Date(), new ArrayList() ); ") + "  Config gCfg = new Config(); ") + "      gCfg.setTodate( new Date( new Date().getTime() + 1000 ) ); ") + "  dpo.getConfigs().add( gCfg ); ") + "  ") + "  insert( dpo ); ") + "end ") + "");
        testTemporalEvaluators(drl);
    }

    @Test
    public void testTemporalEvaluatorsUsingRawDateFields() {
        // DROOLS-421
        final String drl = "\n" + ((((((((((((((((((((((((("import java.util.*; " + "global List list; ") + "declare Delivery ") + "  effectiveDate : Date ") + "end ") + "declare Config ") + "  todate : Date ") + "end ") + "rule Control ") + "when ") + "  $dpo: Delivery( $eff : effectiveDate ) ") + "  $gCfg: Config( todate == null || todate after[ 0d ] $eff ) ") + "then ") + "  list.add( 0 ); ") + "end ") + "rule Init ") + "when ") + "then ") + "  Delivery dpo = new Delivery( new Date( 1000 ) ); ") + "  Config gCfg = new Config(); ") + "      gCfg.setTodate( new Date( 2000 ) ); ") + "  ") + "  insert( dpo ); ") + "  insert( gCfg ); ") + "end ") + "");
        testTemporalEvaluators(drl);
    }

    @Test
    public void testTemporalEvaluatorsUsingRawDateFieldsFromFrom() {
        // DROOLS-421
        final String drl = "\n" + ((((((((((((((("import java.util.*; " + "global List list; ") + "declare Delivery ") + "  effectiveDate : Date ") + "end ") + "declare Config ") + "  todate : Date ") + "end ") + "rule Control ") + "when ") + "  $dpo: Delivery( $eff : effectiveDate ) from new Delivery( new Date( 1000 ) ) ") + "  $gCfg: Config( todate == null || todate after[ 0d ] $eff ) from new Config( new Date( 2000 ) ) ") + "then ") + "  list.add( 0 ); ") + "end ") + "");
        testTemporalEvaluators(drl);
    }

    @Test
    public void testTemporalEvaluatorsUsingSelfDates() {
        // DROOLS-421
        final String drl = "\n" + ((((((((((((("import java.util.*; " + "global List list; ") + "declare Delivery ") + "  thisDate : Date ") + "  thatDate : Date ") + "end ") + "rule Init when then insert( new Delivery( new Date( 1000 ), new Date( 200 ) ) ); end ") + "rule Control ") + "when ") + "  Delivery( thisDate == null || thisDate after[ 0d ] thatDate ) ") + "then ") + "  list.add( 0 ); ") + "end ") + "");
        testTemporalEvaluators(drl);
    }

    @Test
    public void testEventOffsetExpirationOverflow() {
        // DROOLS-455
        final String drl = "\n" + (((((((((((((((((((((("import java.util.*; " + "") + "declare LongLastingEvent \n") + " @role( event )") + " @timestamp( start ) ") + " @duration( duration ) ") + "      start : long ") + "      duration : long ") + "end \n") + "") + "rule Insert ") + "  when ") + "  then ") + "      insert( new LongLastingEvent( 100, Long.MAX_VALUE ) ); ") + "  end ") + " ") + " ") + "rule Collect \n") + "when\n") + " accumulate( $x: LongLastingEvent() over window:time(1h), $num : count($x) ) \n") + "then ") + "end ") + "");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = ksession.getSessionClock();
            // generate the event
            ksession.fireAllRules();
            clock.advanceTime(10, TimeUnit.SECONDS);
            ksession.fireAllRules();
            // The event should still be there...
            Assert.assertEquals(1, ksession.getObjects().size());
        } finally {
            ksession.dispose();
        }
    }

    public static class SynthEvent {
        private final long id;

        private final Date timestamp;

        public SynthEvent(final long id) {
            this.id = id;
            timestamp = new Date();
        }

        public long getId() {
            return id;
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }

    @Test
    public void testExpiredEventModification() {
        // BZ-1082990
        final String drl = (((((((((((((((((((((("import " + (CepEspTest.SimpleEvent.class.getCanonicalName())) + "\n") + "import java.util.Date\n") + "\n") + "declare OtherFact\n") + "    @role( event )\n") + "end\n") + "\n") + "declare SimpleEvent\n") + "    @role( event )\n") + "    @expires( 1h )\n") + "    @timestamp( dateEvt )\n") + "end\n") + "\n") + "\n") + "rule R no-loop\n") + "    when\n") + "        $e : SimpleEvent()\n") + "        not OtherFact( this after[1ms, 1h] $e )\n") + "    then\n") + "        modify($e) {setCode(\"code2\")};\n") + "    end\n ") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = ksession.getSessionClock();
            clock.setStartupTime(System.currentTimeMillis());
            final CepEspTest.SimpleEvent event = new CepEspTest.SimpleEvent("code1");
            event.setDateEvt(((System.currentTimeMillis()) - (((2 * 60) * 60) * 1000)));
            ksession.insert(event);
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
    }

    public static class SimpleEvent {
        private String code;

        private Long dateEvt;

        public SimpleEvent(final String aCode) {
            this.code = aCode;
            this.dateEvt = System.currentTimeMillis();
        }

        public SimpleEvent(final String aCode, final Long dateEvt) {
            this.code = aCode;
            this.dateEvt = dateEvt;
        }

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public Long getDateEvt() {
            return dateEvt;
        }

        public void setDateEvt(final Long dateEvt) {
            this.dateEvt = dateEvt;
        }

        @Override
        public String toString() {
            return (((getClass().getSimpleName()) + " (code=") + (code)) + ")";
        }
    }

    @Test
    public void testTemporalOperatorWithConstant() {
        // BZ-1096243
        final String drl = (((((((((((((((("import " + (CepEspTest.SimpleEvent.class.getCanonicalName())) + "\n") + "import java.util.Date\n") + "global java.util.List list") + "\n") + "declare SimpleEvent\n") + "    @role( event )\n") + "    @timestamp( dateEvt )\n") + "end\n") + "\n") + "rule R \n") + "    when\n") + "        $e : SimpleEvent( this after \"01-Jan-2014\"  )\n") + "    then\n") + "        list.add(\"1\");\n") + "    end\n ") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            final CepEspTest.SimpleEvent event = new CepEspTest.SimpleEvent("code1", org.drools.core.util.DateUtils.parseDate("18-Mar-2014").getTime());
            ksession.insert(event);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testTemporalOperatorWithConstantAndJoin() {
        // BZ 1096243
        final String drl = ((((((((((((((((("import " + (CepEspTest.SimpleEvent.class.getCanonicalName())) + "\n") + "import java.util.Date\n") + "global java.util.List list") + "\n") + "declare SimpleEvent\n") + "    @role( event )\n") + "    @timestamp( dateEvt )\n") + "end\n") + "\n") + "rule R \n") + "    when\n") + "        $e1 : SimpleEvent( this after \"01-Jan-2014\"  )\n") + "        $e2 : SimpleEvent( this after $e1 ) \n") + "    then\n") + "        list.add(\"1\");\n") + "    end\n ") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            final CepEspTest.SimpleEvent event1 = new CepEspTest.SimpleEvent("code1", org.drools.core.util.DateUtils.parseDate("18-Mar-2014").getTime());
            ksession.insert(event1);
            final CepEspTest.SimpleEvent event2 = new CepEspTest.SimpleEvent("code2", org.drools.core.util.DateUtils.parseDate("19-Mar-2014").getTime());
            ksession.insert(event2);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testDynamicSalienceInStreamMode() {
        // DROOLS-526
        final String drl = "import java.util.concurrent.atomic.AtomicInteger;\n" + ((((((((((((((((((((((((((("\n" + "global AtomicInteger salience1\n") + "global AtomicInteger salience2\n") + "global java.util.List list\n") + "\n") + "declare Integer\n") + " @role(event)\n") + "end\n") + "\n") + "rule R1\n") + "salience salience1.get()\n") + "when\n") + " $i : Integer()\n") + "then\n") + " retract($i);\n") + " salience1.decrementAndGet();\n") + " list.add(1);\n") + "end \n") + "\n") + "rule R2\n") + "salience salience2.get()\n") + "when\n") + " $i : Integer()\n") + "then\n") + " retract($i);\n") + " salience2.decrementAndGet();\n") + " list.add(2);\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final List<Integer> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.setGlobal("salience1", new AtomicInteger(9));
            ksession.setGlobal("salience2", new AtomicInteger(10));
            for (int i = 0; i < 10; i++) {
                ksession.insert(i);
                ksession.fireAllRules();
            }
            Assert.assertEquals(list, Arrays.asList(2, 1, 2, 1, 2, 1, 2, 1, 2, 1));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void test2NotsWithTemporalConstraints() {
        // BZ-1122738 DROOLS-479
        final String drl = (((((((((((((((((((((("import " + (CepEspTest.SimpleEvent.class.getCanonicalName())) + "\n") + "import java.util.Date\n") + "\n") + "declare OtherFact\n") + "    @role( event )\n") + "end\n") + "\n") + "declare SimpleEvent\n") + "    @role( event )\n") + "    @timestamp( dateEvt )\n") + "end\n") + "\n") + "\n") + "rule R\n") + "    when\n") + "        $e : SimpleEvent()\n") + "        not OtherFact( this after[0, 1h] $e )\n") + "        not OtherFact( this after[0, 1h] $e )\n") + "    then\n") + "        $e.setCode(\"code2\");\n") + "    end\n ") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = ksession.getSessionClock();
            clock.setStartupTime(System.currentTimeMillis());
            final CepEspTest.SimpleEvent event = new CepEspTest.SimpleEvent("code1");
            event.setDateEvt(((System.currentTimeMillis()) - (((2 * 60) * 60) * 1000)));
            ksession.insert(event);
            ksession.fireAllRules();
            Assert.assertEquals("code2", event.getCode());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testRetractFromWindow() {
        // DROOLS-636
        final String drl = (((((((((((((("import " + (StockTick.class.getCanonicalName())) + ";\n ") + "declare StockTick\n") + " @role( event )\n") + "end\n") + "rule R1 when\n") + "    $i: Integer()\n") + "    $s: StockTick( price > 10 )\n") + "then\n") + "    modify($s) { setPrice(8) };\n") + "end\n") + "rule R2 when\n") + "    $s: StockTick( price > 15 ) over window:length(1)\n") + "then\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            ksession.insert(42);
            ksession.insert(new StockTick(1L, "DROOLS", 20));
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testCEPNamedCons() {
        final String drl = "package org.drools " + ((((((((((((((((((((((((((((((((((((((("global java.util.List list; " + "declare  Msg ") + "    @role( event )") + "    sender : String  @key ") + "end ") + "rule Init ") + "when ") + "  $s : String() ") + "then ") + "  System.out.println( 'Msg ' + $s ); ") + "  insert( new Msg( $s ) ); ") + "end ") + "rule 'Expect_Test_Rule Fulfill' ") + "when ") + "    $trigger : Msg( 'John' ; ) ") + "    Msg( 'Peter' ; this after[0,100000ms] $trigger ) ") + "    do[fulfill] ") + "then ") + "  System.out.println( 'Expectation fulfilled' ); ") + "  list.add( 1 ); ") + "then[fulfill] ") + "  System.out.println( 'insert fulf fact' ); ") + "  list.add( 2 ); ") + "end ") + "rule 'Expect_Test_Rule Violation' ") + "when ") + "    $trigger : Msg( 'John' ; ) do[asap]") + "    not Msg( 'Peter' ; this after[0,100000ms] $trigger ) ") + "    do[viol]  \n") + "then ") + "  System.out.println( 'Expectation violated' ); ") + "  list.add( -1 ); ") + "then[viol] ") + "  System.out.println( 'insert viol fact' ); ") + "  list.add( -2 ); ") + "then[asap] ") + "  System.out.println( 'Did it anyway' ); ") + "  list.add( 0 ); ") + "end ") + "");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            ksession.insert("John");
            ksession.fireAllRules();
            advanceTime(10, TimeUnit.MILLISECONDS);
            ksession.insert("Peter");
            ksession.fireAllRules();
            Assert.assertTrue(list.contains(0));
            Assert.assertTrue(list.contains(1));
            Assert.assertTrue(list.contains(2));
            Assert.assertFalse(list.contains((-1)));
            Assert.assertFalse(list.contains((-2)));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testCEPNamedConsTimers() {
        final String drl = "package org.drools " + ((((((((((((((((((((("global java.util.List list; " + "declare  Msg ") + "    @role( event ) ") + "    sender : String  @key ") + "end ") + "rule Init ") + "when ") + "  $s : String() ") + "then ") + "  System.out.println( 'Msg ' + $s ); ") + "  insert( new Msg( $s ) ); ") + "end ") + "rule 'Viol' ") + "when ") + "    $trigger : Msg( 'John' ; ) ") + "    not Msg( 'Peter' ; this after[0, 100ms] $trigger ) do[viol]") + "then ") + "  list.add( 0 ); ") + "then[viol] ") + "  list.add( -2 ); ") + "end ") + "");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List list = new ArrayList();
            ksession.setGlobal("list", list);
            ksession.insert("John");
            ksession.fireAllRules();
            Assert.assertTrue(list.isEmpty());
            advanceTime(1000, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertTrue(list.contains((-2)));
            Assert.assertTrue(list.contains(0));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void test2TimersWithNamedCons() {
        final String drl = "package org.drools " + ((((((((((((((((((("global java.util.List list; " + "declare  Msg ") + "    @role( event ) ") + "    sender : String  @key ") + "end ") + "rule Init ") + "when ") + "  $s : String() ") + "then ") + "  insert( new Msg( $s ) ); ") + "end ") + "rule 'Viol' when ") + "    $trigger : Msg( \'Alice\' ; )\n") + "    not Msg( \'Bob\' ; this after[0, 100ms] $trigger ) do[t1]\n") + "    not Msg( \'Charles\' ; this after[0, 200ms] $trigger )\n") + "then\n") + "  list.add( 0 );\n") + "then[t1]\n") + "  list.add( 1 );\n") + "end\n");
        test2Timers(drl);
    }

    @Test
    public void test2TimersWith2Rules() {
        final String drl = "package org.drools " + ((((((((((((((((((((((("global java.util.List list; " + "declare  Msg ") + "    @role( event ) ") + "    sender : String  @key ") + "end ") + "rule Init ") + "when ") + "  $s : String() ") + "then ") + "  insert( new Msg( $s ) ); ") + "end ") + "rule 'Viol1' when ") + "    $trigger : Msg( \'Alice\' ; )\n") + "    not Msg( \'Bob\' ; this after[0, 100ms] $trigger ) \n") + "    not Msg( \'Charles\' ; this after[0, 200ms] $trigger )\n") + "then\n") + "  list.add( 0 );\n") + "end\n") + "rule 'Viol2' when ") + "    $trigger : Msg( \'Alice\' ; )\n") + "    not Msg( \'Bob\' ; this after[0, 100ms] $trigger ) \n") + "then\n") + "  list.add( 1 );\n") + "end\n");
        test2Timers(drl);
    }

    @Test
    public void testCEPWith2NamedConsAndEagerRule() {
        final String drl = "package org.drools " + ((((((((((((((((((((((((((("global java.util.List list; " + "declare  Msg ") + "    @role( event ) ") + "    sender : String  @key ") + "end ") + "rule Init1 ") + "when ") + "  $s : String() ") + "then ") + "  insert( new Msg( $s ) ); ") + "end ") + "rule Init2 ") + "when ") + "  Msg( \'Alice\' ; )\n") + "then ") + "  insert( 42 ); ") + "end ") + "rule 'Viol' @Propagation(EAGER) when ") + "    $trigger : Msg( \'Alice\' ; )\n") + "    not Msg( 'Bob' ; this after[0, 100ms] $trigger ) do[t1]") + "    Integer( ) do[t2]\n") + "then\n") + "  list.add( 0 );\n") + "then[t1]\n") + "  list.add( 1 );\n") + "then[t2]\n") + "  list.add( 2 );\n") + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List<Integer> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.insert("Alice");
            ksession.fireAllRules();
            Assert.assertTrue(list.isEmpty());
            advanceTime(150, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(3, list.size());
            Assert.assertTrue(list.contains(0));
            Assert.assertTrue(list.contains(1));
            Assert.assertTrue(list.contains(2));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testExpireLogicalEvent() {
        final String drl = "package org.drools; " + (((((((("declare Foo " + "  @role(event) ") + "  @expires(10ms) ") + "end ") + "rule In ") + "when ") + "then ") + "  insertLogical( new Foo() ); ") + "end ");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            ksession.fireAllRules();
            advanceTime(1, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0, ksession.getObjects().size());
            Assert.assertEquals(0, getTruthMaintenanceSystem().getEqualityKeyMap().size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testSerializationWithEventInPast() {
        // DROOLS-749
        final String drl = (((((((((((((("import " + (CepEspTest.Event1.class.getCanonicalName())) + "\n") + "declare Event1\n") + "    @role( event )\n") + "    @timestamp( timestamp )\n") + "    @expires( 3h )\n") + "end\n") + "\n") + "rule R\n") + "    when\n") + "       $evt: Event1()\n") + "       not Event1(this != $evt, this after[0, 1h] $evt)\n") + "    then\n") + "       System.out.println($evt.getCode());\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            ksession.insert(new CepEspTest.Event1("id1", 0));
            final PseudoClockScheduler clock = ksession.getSessionClock();
            clock.advanceTime(2, TimeUnit.HOURS);
            ksession.fireAllRules();
            ksession.insert(new CepEspTest.Event1("id2", 0));
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
    }

    public static class Event1 implements Serializable {
        private final String code;

        private final long timestamp;

        public Event1(final String code, final long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }

        public String getCode() {
            return code;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return ((((("Event1{" + "code='") + (code)) + '\'') + ", timestamp=") + (timestamp)) + '}';
        }
    }

    @Test
    public void testUseMapAsEvent() {
        // DROOLS-753
        final String drl = "import java.util.Map\n " + ((((((((("declare Map \n" + "  @role(event)\n") + "end\n") + "rule \"sliding window time map\" \n") + "when \n") + "   $m:Map()\n") + "   accumulate(Map() over window:time( 1m ); $count:count(); $count>1 )\n") + "then \n") + "    System.out.println(\"alarm!!!!\");  \n") + "end \n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            ksession.insert(new HashMap<String, Object>());
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testDisconnectedEventFactHandle() {
        // DROOLS-924
        final String drl = "declare String \n" + ("  @role(event)\n" + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final DefaultFactHandle helloHandle = ((DefaultFactHandle) (ksession.insert("hello")));
            final DefaultFactHandle goodbyeHandle = ((DefaultFactHandle) (ksession.insert("goodbye")));
            FactHandle key = DefaultFactHandle.createFromExternalFormat(helloHandle.toExternalForm());
            Assert.assertTrue("FactHandle not deserialized as EventFactHandle", (key instanceof EventFactHandle));
            Assert.assertEquals("hello", ksession.getObject(key));
            key = DefaultFactHandle.createFromExternalFormat(goodbyeHandle.toExternalForm());
            Assert.assertTrue("FactHandle not deserialized as EventFactHandle", (key instanceof EventFactHandle));
            Assert.assertEquals("goodbye", ksession.getObject(key));
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 5000)
    public void testEventWithShortExpiration() throws InterruptedException {
        // DROOLS-921
        final String drl = "declare String\n" + ((((((("  @expires( 1ms )\n" + "  @role( event )\n") + "end\n") + "\n") + "rule R when\n") + "  String( )\n") + "then\n") + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            ksession.insert("test");
            Assert.assertEquals(1, ksession.fireAllRules());
            TimeUtil.sleepMillis(2L);
            Assert.assertEquals(0, ksession.fireAllRules());
            while ((ksession.getObjects().size()) != 0) {
                TimeUtil.sleepMillis(30L);
                // Expire action is put into propagation queue by timer job, so there
                // can be a race condition where it puts it there right after previous fireAllRules
                // flushes the queue. So there needs to be another flush -> another fireAllRules
                // to flush the queue.
                Assert.assertEquals(0, ksession.fireAllRules());
            } 
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testDeleteExpiredEvent() {
        // BZ-1274696
        final String drl = ((((((((((((("import " + (StockTick.class.getCanonicalName())) + "\n") + "declare StockTick\n") + "    @role( event )\n") + "end\n") + "\n") + "rule \"TestEventReceived\"\n") + "no-loop\n") + "when\n") + "  $st1 : StockTick( company == \"ACME\" )\n") + "  not ( StockTick( this != $st1, this after[0s, 1s] $st1) )\n") + "then\n") + "  delete($st1);\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = ksession.getSessionClock();
            final EventFactHandle handle1 = ((EventFactHandle) (ksession.insert(new StockTick(1, "ACME", 50))));
            ksession.fireAllRules();
            clock.advanceTime(2, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertTrue(handle1.isExpired());
            Assert.assertFalse(ksession.getFactHandles().contains(handle1));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testDeleteExpiredEventWithTimestampAndEqualityKey() {
        // DROOLS-1017
        final String drl = ((((((((((((("import " + (StockTick.class.getCanonicalName())) + "\n") + "declare StockTick\n") + "    @role( event )\n") + "    @timestamp( time )\n") + "end\n") + "\n") + "rule \"TestEventReceived\"\n") + "when\n") + "  $st1 : StockTick( company == \"ACME\" )\n") + "  not ( StockTick( this != $st1, this after[0s, 1s] $st1) )\n") + "then\n") + "  delete($st1);\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = ksession.getSessionClock();
            clock.setStartupTime(5000L);
            final EventFactHandle handle1 = ((EventFactHandle) (ksession.insert(new StockTick(1, "ACME", 50, 0L))));
            clock.advanceTime(2, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertTrue(handle1.isExpired());
            Assert.assertFalse(ksession.getFactHandles().contains(handle1));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testSerializationWithWindowLength() {
        // DROOLS-953
        final String drl = ((((((((((("import " + (StockTick.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "declare StockTick\n") + "    @role( event )\n") + "end\n") + "\n") + "rule ReportLastEvent when\n") + "    $e : StockTick() over window:length(1)\n") + "then\n") + "    list.add($e.getCompany());\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            List<String> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.insert(new StockTick(1, "ACME", 50));
            ksession.insert(new StockTick(2, "DROO", 50));
            ksession.insert(new StockTick(3, "JBPM", 50));
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals("JBPM", list.get(0));
            try {
                ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.fireAllRules();
            Assert.assertEquals(0, list.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testSerializationWithWindowLengthAndLiaSharing() {
        // DROOLS-953
        final String drl = ((((((((((((((((("import " + (StockTick.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "declare StockTick\n") + "    @role( event )\n") + "end\n") + "\n") + "rule ReportLastEvent when\n") + "    $e : StockTick() over window:length(1)\n") + "then\n") + "    list.add($e.getCompany());\n") + "end\n") + "\n") + "rule ReportEventInserted when\n") + "   $e : StockTick()\n") + "then\n") + "   System.out.println(\"Event Insert : \" + $e);\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            List<String> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.insert(new StockTick(1, "ACME", 50));
            ksession.insert(new StockTick(2, "DROO", 50));
            ksession.insert(new StockTick(3, "JBPM", 50));
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals("JBPM", list.get(0));
            try {
                ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.fireAllRules();
            Assert.assertEquals(0, list.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testSerializationBeforeFireWithWindowLength() {
        // DROOLS-953
        final String drl = ((((((((((("import " + (StockTick.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "declare StockTick\n") + "    @role( event )\n") + "end\n") + "\n") + "rule ReportLastEvent when\n") + "    $e : StockTick() over window:length(1)\n") + "then\n") + "    list.add($e.getCompany());\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            List<String> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.insert(new StockTick(1, "ACME", 50));
            ksession.insert(new StockTick(2, "DROO", 50));
            ksession.insert(new StockTick(3, "JBPM", 50));
            try {
                ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals("JBPM", list.get(0));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testSubclassWithLongerExpirationThanSuper() {
        // DROOLS-983
        final String drl = (((((((((((((("import " + (CepEspTest.SuperClass.class.getCanonicalName())) + "\n") + "import ") + (CepEspTest.SubClass.class.getCanonicalName())) + "\n") + "\n") + "rule R1 when\n") + "    $e : SuperClass()\n") + "then\n") + "end\n") + "rule R2 when\n") + "    $e : SubClass()\n") + "    not SubClass(this != $e)\n") + "then\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler clock = ksession.getSessionClock();
            final EventFactHandle handle1 = ((EventFactHandle) (ksession.insert(new CepEspTest.SubClass())));
            ksession.fireAllRules();
            clock.advanceTime(15, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertFalse(handle1.isExpired());
            Assert.assertEquals(1, ksession.getObjects().size());
            clock.advanceTime(10, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertTrue(handle1.isExpired());
            Assert.assertEquals(0, ksession.getObjects().size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testSubclassWithLongerExpirationThanSuperWithSerialization() {
        // DROOLS-983
        final String drl = (((((((((((((("import " + (CepEspTest.SuperClass.class.getCanonicalName())) + "\n") + "import ") + (CepEspTest.SubClass.class.getCanonicalName())) + "\n") + "\n") + "rule R1 when\n") + "    $e : SuperClass()\n") + "then\n") + "end\n") + "rule R2 when\n") + "    $e : SubClass()\n") + "    not SubClass(this != $e)\n") + "then\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            PseudoClockScheduler clock = ksession.getSessionClock();
            final EventFactHandle handle1 = ((EventFactHandle) (ksession.insert(new CepEspTest.SubClass())));
            ksession.fireAllRules();
            clock.advanceTime(15, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertFalse(handle1.isExpired());
            Assert.assertEquals(1, ksession.getObjects().size());
            try {
                ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            clock = ksession.getSessionClock();
            clock.advanceTime(10, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0, ksession.getObjects().size());
        } finally {
            ksession.dispose();
        }
    }

    @Role(Type.EVENT)
    @Expires("10s")
    public static class SuperClass implements Serializable {}

    @Role(Type.EVENT)
    @Expires("20s")
    public static class SubClass extends CepEspTest.SuperClass {}

    @Test
    public void testTemporalOperatorWithGlobal() {
        // DROOLS-993
        final String drl = (((((((((((((((((("import " + (CepEspTest.SimpleEvent.class.getCanonicalName())) + "\n") + "global java.util.List list;\n") + "global ") + (CepEspTest.SimpleEvent.class.getCanonicalName())) + " baseEvent;\n") + "\n") + "declare SimpleEvent\n") + "    @role( event )\n") + "    @timestamp( dateEvt )\n") + "end\n") + "\n") + "rule R \n") + "    when\n") + "        $e : SimpleEvent( dateEvt before[10s] baseEvent.dateEvt )\n") + "    then\n") + "        list.add(\"1\");\n") + "    end\n ") + "";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List<String> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.setGlobal("baseEvent", new CepEspTest.SimpleEvent("1", 15000L));
            final CepEspTest.SimpleEvent event1 = new CepEspTest.SimpleEvent("1", 0L);
            ksession.insert(event1);
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testNoExpirationWithNot() {
        // DROOLS-984
        checkNoExpiration(("$s: SimpleEvent ()\n" + "not SimpleEvent (this != $s, this after[0, 30s] $s)\n"));
    }

    @Test
    public void testNoExpirationWithSlidingWindow() {
        // DROOLS-984
        checkNoExpiration("SimpleEvent( ) over window:time(30s)\n");
    }

    @Test
    public void testNoExpirationWithNoTemporalConstraint() {
        // DROOLS-984
        checkNoExpiration("SimpleEvent( )\n");
    }

    @Test
    public void testCancelActivationWithExpiredEvent() {
        // RHBRMS-2463
        final String drl = ((((((((((((((((((((("import " + (CepEspTest.MyEvent.class.getCanonicalName())) + "\n") + "import ") + (AtomicInteger.class.getCanonicalName())) + "\n") + "declare MyEvent\n") + "    @role( event )\n") + "    @timestamp( timestamp )\n") + "    @expires( 10ms )\n") + "end\n") + "\n") + "rule R\n") + "    timer (int: 0 1; start=$startTime, repeat-limit=0 )\n") + "    when\n") + "       $event: MyEvent ($startTime : timestamp)\n") + "       $counter : AtomicInteger(get() > 0)\n") + "    then\n") + "        System.out.println(\"RG_TEST_TIMER WITH \" + $event + \" AND \" + $counter);\n") + "        modify($counter){\n") + "            decrementAndGet()\n") + "        }\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final long now = System.currentTimeMillis();
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            sessionClock.setStartupTime((now - 10000));
            final AtomicInteger counter = new AtomicInteger(1);
            final CepEspTest.MyEvent event1 = new CepEspTest.MyEvent((now - 8000));
            final CepEspTest.MyEvent event2 = new CepEspTest.MyEvent((now - 7000));
            final CepEspTest.MyEvent event3 = new CepEspTest.MyEvent((now - 6000));
            ksession.insert(counter);
            ksession.insert(event1);
            ksession.insert(event2);
            ksession.insert(event3);
            ksession.fireAllRules();// Nothing Happens

            Assert.assertEquals(1, counter.get());
            sessionClock.advanceTime(10000, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0, counter.get());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testRightTupleExpiration() {
        // RHBRMS-2463
        final String drl = ((((((((((((((((((("import " + (CepEspTest.MyEvent.class.getCanonicalName())) + "\n") + "import ") + (AtomicInteger.class.getCanonicalName())) + "\n") + "global AtomicInteger counter;\n") + "declare MyEvent\n") + "    @role( event )\n") + "    @timestamp( timestamp )\n") + "    @expires( 10ms )\n") + "end\n") + "\n") + "rule R when\n") + "       String()\n") + "       MyEvent()\n") + "       Boolean()\n") + "       Integer()\n") + "    then\n") + "       counter.incrementAndGet();\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            sessionClock.setStartupTime(0);
            final AtomicInteger counter = new AtomicInteger(0);
            ksession.setGlobal("counter", counter);
            ksession.insert("test");
            ksession.insert(true);
            ksession.insert(new CepEspTest.MyEvent(0));
            ksession.insert(new CepEspTest.MyEvent(15));
            ksession.fireAllRules();
            Assert.assertEquals(0, counter.get());
            sessionClock.advanceTime(20, TimeUnit.MILLISECONDS);
            ksession.insert(1);
            ksession.fireAllRules();// MyEvent is expired

            Assert.assertEquals(1, counter.get());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testLeftTupleExpiration() {
        // RHBRMS-2463
        final String drl = (((((((((((((((((("import " + (CepEspTest.MyEvent.class.getCanonicalName())) + "\n") + "import ") + (AtomicInteger.class.getCanonicalName())) + "\n") + "global AtomicInteger counter;\n") + "declare MyEvent\n") + "    @role( event )\n") + "    @timestamp( timestamp )\n") + "    @expires( 10ms )\n") + "end\n") + "\n") + "rule R when\n") + "       MyEvent()\n") + "       Boolean()\n") + "       Integer()\n") + "    then\n") + "       counter.incrementAndGet();\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            sessionClock.setStartupTime(0);
            final AtomicInteger counter = new AtomicInteger(0);
            ksession.setGlobal("counter", counter);
            ksession.insert(true);
            ksession.insert(new CepEspTest.MyEvent(0));
            ksession.insert(new CepEspTest.MyEvent(15));
            ksession.fireAllRules();
            Assert.assertEquals(0, counter.get());
            sessionClock.advanceTime(20, TimeUnit.MILLISECONDS);
            ksession.insert(1);
            ksession.fireAllRules();// MyEvent is expired

            Assert.assertEquals(1, counter.get());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testLeftTupleExpirationWithNot() {
        // RHBRMS-2463
        final String drl = (((((((((((((((((("import " + (CepEspTest.MyEvent.class.getCanonicalName())) + "\n") + "import ") + (AtomicInteger.class.getCanonicalName())) + "\n") + "global AtomicInteger counter;\n") + "declare MyEvent\n") + "    @role( event )\n") + "    @timestamp( timestamp )\n") + "    @expires( 10ms )\n") + "end\n") + "\n") + "rule R when\n") + "       MyEvent()\n") + "       Boolean()\n") + "       not Integer()\n") + "    then\n") + "       counter.incrementAndGet();\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            sessionClock.setStartupTime(0);
            final AtomicInteger counter = new AtomicInteger(0);
            ksession.setGlobal("counter", counter);
            ksession.insert(true);
            final FactHandle iFh = ksession.insert(1);
            ksession.insert(new CepEspTest.MyEvent(0));
            ksession.insert(new CepEspTest.MyEvent(15));
            ksession.fireAllRules();
            Assert.assertEquals(0, counter.get());
            sessionClock.advanceTime(20, TimeUnit.MILLISECONDS);
            ksession.delete(iFh);
            ksession.fireAllRules();// MyEvent is expired

            Assert.assertEquals(1, counter.get());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testExpireLogicallyInsertedEvent() {
        // RHBRMS-2515
        final String drl = (((((((((((("import " + (CepEspTest.MyEvent.class.getCanonicalName())) + "\n") + "declare MyEvent\n") + "    @role( event )\n") + "    @timestamp( timestamp )\n") + "    @expires( 10ms )\n") + "end\n") + "\n") + "rule R when\n") + "  $e : MyEvent()\n") + "then\n") + "  insertLogical($e.toString());\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            sessionClock.setStartupTime(0);
            ksession.insert(new CepEspTest.MyEvent(0));
            Assert.assertEquals(1L, ksession.getFactCount());
            ksession.fireAllRules();
            Assert.assertEquals(2L, ksession.getFactCount());
            sessionClock.advanceTime(20, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0L, ksession.getFactCount());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testExpiredEventWithPendingActivations() throws Exception {
        final String drl = "package org.drools.drools_usage_pZB7GRxZp64;\n" + ((((((((((((((((((((((((((((((((((((((((((((((((((("\n" + "declare time_Var\n") + "    @role( event )\n") + "    @expires( 1s )\n") + "    value : Long\n") + "end\n") + "\n") + "declare ExpiringEvent_Var\n") + "    @role( event )\n") + "    @expires( 10s )\n") + "    value : Double\n") + "end\n") + "\n") + "declare window ExpiringEvent_Window1 ExpiringEvent_Var() over window:length(1) end\n") + "\n") + "rule \"Expring variable - Init\"\n") + "activation-group \"ExpiringEvent\"\n") + "    when\n") + "        $t : time_Var($now : Value != null) over window:length(1)\n") + "\n") + "        not ExpiringEvent_Var()\n") + "\n") + "    then\n") + "        System.out.println($now + \" : Init\");\n") + "        insert(new ExpiringEvent_Var(0.0));\n") + "end\n") + "\n") + "rule \"Expiring variable - Rule 1\"\n") + "activation-group \"ExpiringEvent\"\n") + "    when\n") + "        $t : time_Var($now : Value != null) over window:length(1)\n") + "\n") + "        ExpiringEvent_Var(this before $t, $previousValue : Value < 1.0) from window ExpiringEvent_Window1\n") + "\n") + "    then\n") + "        System.out.println($now + \" : Rule 1\");\n") + "        insert(new ExpiringEvent_Var(1.0));\n") + "\n") + "end\n") + "\n") + "rule \"Expiring variable - Rule 2\"\n") + "activation-group \"ExpiringEvent\"\n") + "    when\n") + "        $t : time_Var($now : Value != null) over window:length(1)\n") + "\n") + "        ExpiringEvent_Var(this before $t, $previousValue : Value) from window ExpiringEvent_Window1\n") + "\n") + "    then\n") + "        System.out.println($now + \" : Rule 2\");\n") + "        insert(new ExpiringEvent_Var($previousValue));\n") + "\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession session = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final SessionPseudoClock clock = session.getSessionClock();
            final FactType time_VarType = kbase.getFactType("org.drools.drools_usage_pZB7GRxZp64", "time_Var");
            clock.advanceTime(1472057509000L, TimeUnit.MILLISECONDS);
            final Object time_Var = time_VarType.newInstance();
            time_VarType.set(time_Var, "value", 1472057509000L);
            session.insert(time_Var);
            session.fireAllRules();
            for (int i = 0; i < 10; i++) {
                clock.advanceTime(1, TimeUnit.SECONDS);
                final Object time_VarP1 = time_VarType.newInstance();
                time_VarType.set(time_VarP1, "value", clock.getCurrentTime());
                session.insert(time_VarP1);
                session.fireAllRules();
            }
            clock.advanceTime(1, TimeUnit.SECONDS);
            session.fireAllRules();
            clock.advanceTime(1, TimeUnit.HOURS);
            final Object time_VarP1 = time_VarType.newInstance();
            time_VarType.set(time_VarP1, "value", clock.getCurrentTime());
            session.insert(time_VarP1);
            session.fireAllRules();
            clock.advanceTime(1, TimeUnit.HOURS);
            final Object time_VarP2 = time_VarType.newInstance();
            time_VarType.set(time_VarP2, "value", clock.getCurrentTime());
            session.insert(time_VarP2);
            session.fireAllRules();
            clock.advanceTime(1, TimeUnit.HOURS);
            session.fireAllRules();
            // actually should be empty..
            for (final Object o : session.getFactHandles()) {
                if (o instanceof EventFactHandle) {
                    final EventFactHandle eventFactHandle = ((EventFactHandle) (o));
                    Assert.assertFalse(eventFactHandle.isExpired());
                }
            }
        } finally {
            session.dispose();
        }
    }

    @Test
    public void testTimerWithMillisPrecision() {
        // RHBRMS-2627
        final String drl = ((((((((((((((((((((("import " + (CepEspTest.MyEvent.class.getCanonicalName())) + "\n") + "import ") + (AtomicInteger.class.getCanonicalName())) + "\n") + "declare MyEvent\n") + "    @role( event )\n") + "    @timestamp( timestamp )\n") + "    @expires( 10ms )\n") + "end\n") + "\n") + "rule R\n") + "    timer (int: 0 1; start=$startTime, repeat-limit=0 )\n") + "    when\n") + "       $event: MyEvent ($startTime : timestamp)\n") + "       $counter : AtomicInteger(get() > 0)\n") + "    then\n") + "        System.out.println(\"RG_TEST_TIMER WITH \" + $event + \" AND \" + $counter);\n") + "        modify($counter){\n") + "            decrementAndGet()\n") + "        }\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final long now = 1000;
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            sessionClock.setStartupTime((now - 10));
            final AtomicInteger counter = new AtomicInteger(1);
            final CepEspTest.MyEvent event1 = new CepEspTest.MyEvent((now - 8));
            final CepEspTest.MyEvent event2 = new CepEspTest.MyEvent((now - 7));
            final CepEspTest.MyEvent event3 = new CepEspTest.MyEvent((now - 6));
            ksession.insert(counter);
            ksession.insert(event1);
            ksession.insert(event2);
            ksession.insert(event3);
            ksession.fireAllRules();// Nothing Happens

            Assert.assertEquals(1, counter.get());
            sessionClock.advanceTime(10, TimeUnit.MILLISECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0, counter.get());
        } finally {
            ksession.dispose();
        }
    }

    @Test(timeout = 10000)
    public void testSerializationDeserliaizationWithRectractedExpireFact() {
        // DROOLS-1328
        final String drl = (((((((((((((("package " + (CepEspTest.TestEvent.class.getPackage().getName())) + "\n") + "declare ") + (CepEspTest.TestEvent.class.getCanonicalName())) + "\n") + "   @role( event ) \n") + "   @expires( 60d ) \n") + "end\n") + "rule \"retract test rule\"\n") + "salience 10 \n") + "when\n") + "   $e : TestEvent() over window:length(1)\n") + "then\n") + "   delete($e);\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        KieSession kieSessionDeserialized = null;
        try {
            ksession.insert(new CepEspTest.TestEvent("test1"));
            ksession.fireAllRules();
            try {
                kieSessionDeserialized = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
            } catch (final Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        } finally {
            ksession.dispose();
            Assert.assertNotNull(kieSessionDeserialized);
            kieSessionDeserialized.insert(new CepEspTest.TestEvent("test2"));
            kieSessionDeserialized.fireAllRules();
        }
    }

    @Test
    public void testConflictingRightTuplesUpdate() {
        // DROOLS-1338
        final String drl = "declare Integer @role(event) end\n" + (((("rule R when\n" + "    Integer()\n") + "    not String()\n") + "\n") + "then end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession kieSession = kbase.newKieSession();
        try {
            final FactHandle fhA = kieSession.insert("A");
            final FactHandle fhB = kieSession.insert("B");
            final FactHandle fh1 = kieSession.insert(1);
            Assert.assertEquals(0, kieSession.fireAllRules());
            kieSession.delete(fh1);
            kieSession.update(fhA, "A");
            kieSession.update(fhB, "B");
            kieSession.insert(2);
            Assert.assertEquals(0, kieSession.fireAllRules());
        } finally {
            kieSession.dispose();
        }
    }

    @Test
    public void testModifyEventOverWindow() {
        // DROOLS-1346
        final String drl = ((((((((((((((((((("import " + (AtomicBoolean.class.getCanonicalName())) + "\n") + "declare AtomicBoolean @role(event) end\n") + "global java.util.List list;\n") + "rule R1 when\n") + "    $event : AtomicBoolean(!get())\n") + "    String()\n") + "then\n") + "    retract($event);\n") + "    list.add(\"R1\");\n") + "end\n") + "\n") + "rule R2 when\n") + "    $b : AtomicBoolean() over window:length(10)\n") + "    not String()\n") + "then\n") + "    modify($b) { set(true) }\n") + "    insert(\"check\");\n") + "    list.add(\"R2\");\n") + "end";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            final List<String> list = new ArrayList<>();
            ksession.setGlobal("list", list);
            ksession.insert(new AtomicBoolean(false));
            ksession.fireAllRules();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals("R2", list.get(0));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testExpirationOnAfter() {
        // DROOLS-1227
        final String drl = "declare String @role( event ) end\n" + ((((((("declare Integer @role( event ) end\n" + "\n") + "rule R when\n") + "    $s: String()\n") + "    $i: Integer(this after[0,10s] $s)\n") + "then\n") + "    System.out.println(\"fired\");\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            ksession.insert("test");
            ksession.insert(1);
            Assert.assertEquals(2, ksession.getFactCount());
            ksession.fireAllRules();
            sessionClock.advanceTime(11, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0, ksession.getFactCount());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testExpirationOnBefore() {
        // DROOLS-1227
        final String drl = "declare String @role( event ) end\n" + ((((((("declare Integer @role( event ) end\n" + "\n") + "rule R when\n") + "    $s: String()\n") + "    $i: Integer(this before[0,10s] $s)\n") + "then\n") + "    System.out.println(\"fired\");\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            ksession.insert(1);
            Assert.assertEquals(1, ksession.getFactCount());
            ksession.fireAllRules();
            sessionClock.advanceTime(11, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0, ksession.getFactCount());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testFireExpiredEventOnInactiveGroup() {
        // DROOLS-1523
        final String drl = "global java.util.List list;\n" + (((((((((((((((((((((("declare String  @role(event) @expires( 6d ) end\n" + "declare Integer @role(event) @expires( 3d ) end\n") + "\n") + "rule \"RG_1\"\n") + "    agenda-group \"rf-grp1\"\n") + "    when\n") + "        $event: Integer()\n") + "        not String(this after [1ms, 48h] $event)\n") + "    then\n") + "      System.out.println(\"RG_1 fired\");\n") + "      retract($event);\n") + "      list.add(\"RG_1\");\n") + "end\n") + "\n") + "rule \"RG_2\"\n") + "    agenda-group \"rf-grp1\"\n") + "    when\n") + "        $event: String()\n") + "        not Integer(this after [1ms, 144h] $event)\n") + "    then\n") + "      System.out.println(\"RG_2 fired\");\n") + "      list.add(\"RG_2\");\n") + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession kieSession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            kieSession.addEventListener(new DefaultAgendaEventListener() {
                public void agendaGroupPopped(final AgendaGroupPoppedEvent event) {
                    if (event.getAgendaGroup().getName().equals("rf-grp0")) {
                        event.getKieRuntime().getAgenda().getAgendaGroup("rf-grp1").setFocus();
                    }
                }
            });
            final List<String> list = new ArrayList<>();
            kieSession.setGlobal("list", list);
            final PseudoClockScheduler sessionClock = kieSession.getSessionClock();
            kieSession.insert("DummyEvent");
            kieSession.insert(1);// <- OtherDummyEvent

            kieSession.getAgenda().getAgendaGroup("rf-grp0").setFocus();
            kieSession.fireAllRules();// OK nothing happens

            Assert.assertEquals(2, kieSession.getFactCount());
            sessionClock.advanceTime(145, TimeUnit.HOURS);
            kieSession.getAgenda().getAgendaGroup("rf-grp0").setFocus();
            kieSession.fireAllRules();
            Assert.assertEquals("Expiration occured => no more fact in WM", 0, kieSession.getFactCount());
            Assert.assertEquals("RG_1 should fire once", 1, list.stream().filter(( r) -> r.equals("RG_1")).count());
            Assert.assertEquals("RG_2 should fire once", 1, list.stream().filter(( r) -> r.equals("RG_2")).count());
        } finally {
            kieSession.dispose();
        }
    }

    @Test
    public void testExpireUnusedDeclaredTypeEvent() {
        // DROOLS-1524
        final String drl = "declare String @role( event ) @expires( 1s ) end\n" + ((("\n" + "rule R when\n") + "then\n") + "end");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            ksession.insert("test");
            ksession.fireAllRules();
            Assert.assertEquals(1, ksession.getFactCount());
            sessionClock.advanceTime(2, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0, ksession.getFactCount());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testExpireUnusedDeclaredTypeClass() {
        // DROOLS-1524
        final String drl = "rule R when\n" + (("then\n" + "  System.out.println(\"fired\");\n") + "end\n");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final PseudoClockScheduler sessionClock = ksession.getSessionClock();
            ksession.insert(new CepEspTest.EventWithoutRule());
            ksession.fireAllRules();
            sessionClock.advanceTime(2, TimeUnit.SECONDS);
            ksession.fireAllRules();
            Assert.assertEquals(0, ksession.getFactCount());
        } finally {
            ksession.dispose();
        }
    }

    @Role(Type.EVENT)
    @Expires("1s")
    public class EventWithoutRule {}

    public static class EventA implements Serializable {
        private final String time;

        private final int value;

        private final Date timestamp;

        public EventA(final String time, final int value) {
            this.time = time;
            this.value = value;
            this.timestamp = CepEspTest.EventA.parseDate(time);
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "EventA at " + (time);
        }

        private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private static Date parseDate(final String input) {
            Date d = null;
            try {
                d = CepEspTest.EventA.dateFormatter.parse(input);
            } catch (final ParseException e) {
                e.printStackTrace();
            }
            return d;
        }
    }

    @Test
    public void testDeleteOfDeserializedJob() {
        // DROOLS-1660
        final String drl = (((((((((((((("import " + (CepEspTest.EventA.class.getCanonicalName())) + "\n") + "import java.util.Date\n") + "global java.util.List list\n") + "declare EventA\n") + "\t@role(event)\n") + "\t@timestamp(timestamp)\n") + "end\n") + "rule test\n") + " when\n") + "  \t$event : EventA(value == 1)\n") + "   not(EventA(value == 1, this after [1ms,4m] $event))\n") + " then\n") + "   list.add(\"Fired \"+ $event);\n") + "end\n";
        final KieBase kieBase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kieBase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            final List<String> list = new ArrayList<>();
            final List<CepEspTest.EventA> events = new ArrayList<>();
            events.add(new CepEspTest.EventA("2010-01-01 02:00:00", 0));
            events.add(new CepEspTest.EventA("2010-01-01 03:00:00", 1));
            events.add(new CepEspTest.EventA("2010-01-01 03:01:00", 0));
            events.add(new CepEspTest.EventA("2010-01-01 03:02:00", 1));
            events.add(new CepEspTest.EventA("2010-01-01 03:03:00", 0));
            events.add(new CepEspTest.EventA("2010-01-01 03:04:00", 0));
            events.add(new CepEspTest.EventA("2010-01-01 03:05:00", 0));
            events.add(new CepEspTest.EventA("2010-01-01 03:06:00", 0));
            events.add(new CepEspTest.EventA("2010-01-01 03:07:00", 0));
            // set clock reference
            SessionPseudoClock clock = ksession.getSessionClock();
            clock.advanceTime(events.get(0).getTimestamp().getTime(), TimeUnit.MILLISECONDS);
            byte[] serializedSession = null;
            try {
                final Marshaller marshaller = Factory.get().getMarshallers().newMarshaller(kieBase);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                marshaller.marshall(baos, ksession);
                serializedSession = baos.toByteArray();
            } catch (final IOException e2) {
                e2.printStackTrace();
            }
            for (final CepEspTest.EventA current : events) {
                KieSession ksession2 = null;
                final Marshaller marshaller = Factory.get().getMarshallers().newMarshaller(kieBase);
                try {
                    Assert.assertNotNull(serializedSession);
                    final ByteArrayInputStream bais = new ByteArrayInputStream(serializedSession);
                    ksession2 = marshaller.unmarshall(bais, ksession.getSessionConfiguration(), null);
                    ksession2.setGlobal("list", list);
                    clock = ksession2.getSessionClock();
                    bais.close();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                    Assert.fail(e.getMessage());
                }
                long currTime = clock.getCurrentTime();
                final long nextTime = current.getTimestamp().getTime();
                while (currTime <= (nextTime - 1000)) {
                    clock.advanceTime(1000, TimeUnit.MILLISECONDS);
                    ksession2.fireAllRules();
                    currTime += 1000;
                } 
                final long diff = nextTime - currTime;
                if (diff > 0) {
                    clock.advanceTime(diff, TimeUnit.MILLISECONDS);
                }
                ksession2.insert(current);
                ksession2.fireAllRules();
                // serialize knowledge session
                try {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    marshaller.marshall(baos, ksession2);
                    serializedSession = baos.toByteArray();
                } catch (final IOException e2) {
                    e2.printStackTrace();
                    Assert.fail(e2.getMessage());
                }
                ksession2.dispose();
            }
            Assert.assertEquals(1, list.size());
            Assert.assertEquals("Fired EventA at 2010-01-01 03:02:00", list.get(0));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testInvalidWindowPredicate() {
        // DROOLS-1723
        final String drl = "declare A\n" + ((((((("    @role( event )\n" + "    id : int\n") + "end\n") + "rule \"ab\" \n") + "when\n") + "    A( $a : id ) over window:len( 1 )\n") + "then\n") + "end");
        final KieBuilder kieBuilder = KieUtil.getKieBuilderFromDrls(kieBaseTestConfiguration, false, drl);
        Assertions.assertThat(kieBuilder.getResults().getMessages()).isNotEmpty();
    }

    @Role(Type.EVENT)
    public static class ExpiringEventD {}

    @Test
    public void testInsertLogicalNoExpires() {
        // DROOLS-2182
        final String drl = ((("import " + (CepEspTest.ExpiringEventD.class.getCanonicalName())) + "\n") + "rule Insert when then ") + "   insertLogical( new ExpiringEventD() ); end ";
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testPropertyReactiveWithDurationOnRule() {
        // DROOLS-2238
        final String drl = "package org.drools.test " + (((((((((((((((((((((((" " + "declare Bean ") + "   @PropertyReactive ") + "   label : String ") + "   active : boolean ") + "end ") + " ") + " ") + "rule Init ") + "when ") + "then ") + "   insert( new Bean( \"aaa\", true ) ); ") + "end ") + " ") + "rule Close ") + "  duration (100) ") + "when ") + "    $b : Bean( label == \"aaa\" )   ") + "then ") + "    modify( $b ) {  ") + "       setActive( false ); ") + "    }  ") + "end") + " ");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession(STATEFUL_PSEUDO.getKieSessionConfiguration(), null);
        try {
            Assert.assertEquals(1, ksession.fireAllRules());
            advanceTime(200, TimeUnit.MILLISECONDS);
            Assert.assertEquals(1, ksession.fireAllRules(10));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testNPERiaPathMemWithEvent() {
        // DROOLS-2241
        final String drl = "package org.drools  " + ((((((((((((("declare  Reading  " + "    @role( event ) ") + "    value : Double  @key ") + "end ") + "rule Init when then insert( new Reading( 14.5) ); end ") + "rule Test ") + "when ") + "    $trigger : Reading( $value : value  )   ") + "    not(  ") + "       Number( doubleValue > 10.0 ) from $value  ) ") + "    do[viol]   ") + "then ") + "then[viol] ") + "end ");
        final KieBase kbase = KieBaseUtil.getKieBaseFromKieModuleFromDrl("cep-esp-test", kieBaseTestConfiguration, drl);
        final KieSession ksession = kbase.newKieSession();
        try {
            Assert.assertEquals(1, ksession.fireAllRules());
        } finally {
            ksession.dispose();
        }
    }
}

