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
package org.drools.testcoverage.regression;


import KieServices.Factory;
import ResourceType.DRL;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.testcoverage.common.listener.TrackingAgendaEventListener;
import org.drools.testcoverage.common.model.Event;
import org.drools.testcoverage.common.model.EventA;
import org.drools.testcoverage.common.model.EventB;
import org.drools.testcoverage.common.model.Message;
import org.drools.testcoverage.common.model.MessageEvent;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.drools.testcoverage.common.util.KieBaseUtil;
import org.drools.testcoverage.common.util.TestConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.time.SessionPseudoClock;
import org.kie.internal.utils.KieHelper;

import static org.drools.testcoverage.common.model.MessageEvent.Type.received;


/**
 * Test to verify BRMS-582 (use of 'after' and 'before' operators ends with NPE)
 * is fixed.
 */
@RunWith(Parameterized.class)
public class FusionAfterBeforeTest {
    private final KieBaseTestConfiguration kieBaseTestConfiguration;

    public FusionAfterBeforeTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        this.kieBaseTestConfiguration = kieBaseTestConfiguration;
    }

    @Test
    public void testAfterBeforeOperators() {
        final Resource drlResource = Factory.get().getResources().newClassPathResource("fusionAfterBeforeTest.drl", getClass());
        final KieBase kieBase = KieBaseUtil.getKieBaseFromKieModuleFromResources(TestConstants.PACKAGE_REGRESSION, kieBaseTestConfiguration, drlResource);
        final KieSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksconf.setOption(ClockTypeOption.get("pseudo"));
        final KieSession ksession = kieBase.newKieSession(ksconf, null);
        final TrackingAgendaEventListener listener = new TrackingAgendaEventListener();
        ksession.addEventListener(listener);
        final EntryPoint stream = ksession.getEntryPoint("EventStream");
        SessionPseudoClock clock = ksession.getSessionClock();
        try {
            for (int i = 0; i < 3; i++) {
                final MessageEvent tc = new MessageEvent(received, new Message());
                stream.insert(tc);
                ksession.fireAllRules();
                clock.advanceTime(8, TimeUnit.SECONDS);
            }
            ksession.fireAllRules();
        } finally {
            ksession.dispose();
        }
        Assertions.assertThat(listener.isRuleFired("AfterMessageEvent")).as("Rule 'AfterMessageEvent' was no fired!").isTrue();
        Assertions.assertThat(listener.isRuleFired("BeforeMessageEvent")).as("Rule 'BeforeMessageEvent' was no fired!").isTrue();
        // each rules should be fired 2 times
        int firedCount = 2;
        int actuallyFired = listener.ruleFiredCount("AfterMessageEvent");
        Assertions.assertThat(firedCount).as((("Rule 'AfterMessageEvent' should be fired 2 times, but was fired " + firedCount) + " time(s)!")).isEqualTo(actuallyFired);
        firedCount = listener.ruleFiredCount("BeforeMessageEvent");
        Assertions.assertThat(firedCount).as((("Rule 'BeforeMessageEvent' should be fired 2 times, but was fired " + firedCount) + " time(s)!")).isEqualTo(actuallyFired);
    }

    @Test(timeout = 10000)
    public void testExpireEventsWhenSharingAllRules() throws IllegalAccessException, InstantiationException {
        final StringBuilder drlBuilder = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            drlBuilder.append(((" import " + (EventA.class.getCanonicalName())) + ";\n"));
            drlBuilder.append(((" import " + (EventB.class.getCanonicalName())) + ";\n"));
            drlBuilder.append(((" declare " + (EventA.class.getName())) + " @role( event ) @duration(duration) end"));
            drlBuilder.append(((" declare " + (EventB.class.getName())) + " @role( event ) @duration(duration) end"));
            drlBuilder.append(((" rule R" + i) + " when \n"));
            drlBuilder.append((("   $event1: " + (EventA.class.getName())) + "()\n"));
            drlBuilder.append((("   $event2: " + (EventB.class.getName())) + "(this != $event1, this after [1,10] $event1)\n"));
            drlBuilder.append("then end\n");
        }
        final SortedSet<Event> events = new TreeSet<Event>();
        events.addAll(getEvents(EventA.class, (64 / 2), 2, 100, 0));
        events.addAll(getEvents(EventB.class, (64 / 2), 5, 100, 0));
        final KieSessionConfiguration sessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        sessionConf.setOption(ClockTypeOption.get("pseudo"));
        final KieSession kieSession = new KieHelper().addContent(drlBuilder.toString(), DRL).build(kieBaseTestConfiguration.getKieBaseConfiguration()).newKieSession(sessionConf, null);
        Assertions.assertThat(insertEventsAndFire(kieSession, events)).isEqualTo(2048);
    }
}

