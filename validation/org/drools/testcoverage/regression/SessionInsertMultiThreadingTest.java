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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.assertj.core.api.Assertions;
import org.drools.testcoverage.common.model.Message;
import org.drools.testcoverage.common.model.Person;
import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test to verify BRMS-532 (Drools Session insert
 * ConcurrentModificationException in Multithreading Environment) is fixed
 */
@RunWith(Parameterized.class)
public class SessionInsertMultiThreadingTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionInsertMultiThreadingTest.class);

    private static final int THREADS = 50;

    private static final int RUNS_PER_THREAD = 100;

    private KieBase kbase;

    private ExecutorService executor;

    private final KieBaseTestConfiguration kieBaseTestConfiguration;

    public SessionInsertMultiThreadingTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        this.kieBaseTestConfiguration = kieBaseTestConfiguration;
    }

    @Test
    public void testCommonBase() throws Exception {
        final List<Future<?>> futures = new ArrayList<Future<?>>();
        for (int i = 0; i < (SessionInsertMultiThreadingTest.RUNS_PER_THREAD); i++) {
            for (int j = 0; j < (SessionInsertMultiThreadingTest.THREADS); j++) {
                futures.add(executor.submit(new SessionInsertMultiThreadingTest.KieBaseRunnable(kbase)));
            }
        }
        waitForCompletion(futures);
    }

    @Test
    public void testCommonSession() throws Exception {
        for (int i = 0; i < (SessionInsertMultiThreadingTest.RUNS_PER_THREAD); i++) {
            testSingleCommonSession();
        }
    }

    /**
     * Reproducer for BZ 1187070.
     */
    @Test
    public void testCommonStatelessSessionBZ1187070() throws Exception {
        for (int i = 0; i < (SessionInsertMultiThreadingTest.RUNS_PER_THREAD); i++) {
            testSingleCommonStatelessSession();
        }
    }

    /**
     * The Runnable performing the test on a given shared StatelessKieSession.
     */
    public static class StatelessKieSessionRunnable implements Runnable {
        protected final StatelessKieSession statelessKieSession;

        public StatelessKieSessionRunnable(StatelessKieSession statelessKieSession) {
            this.statelessKieSession = statelessKieSession;
        }

        @Override
        public void run() {
            final Message m = new Message();
            final Person p = new Person();
            final KieCommands kieCommands = Factory.get().getCommands();
            final List<Command<?>> commandList = new ArrayList<Command<?>>();
            commandList.add(kieCommands.newInsert(m));
            commandList.add(kieCommands.newInsert(p));
            commandList.add(kieCommands.newFireAllRules());
            statelessKieSession.execute(kieCommands.newBatchExecution(commandList));
            Assertions.assertThat(p.getName()).isNotNull();
            Assertions.assertThat(m.getMessage()).isNotNull();
        }
    }

    /**
     * The Runnable performing the test on a given shared KieSession.
     */
    public static class KieSessionRunnable implements Runnable {
        protected final KieSession ksession;

        public KieSessionRunnable(KieSession ksession) {
            this.ksession = ksession;
        }

        @Override
        public void run() {
            final Message m = new Message();
            final Person p = new Person();
            ksession.insert(m);
            ksession.insert(p);
            ksession.fireAllRules();
            Assertions.assertThat(p.getName()).isNotNull();
            Assertions.assertThat(m.getMessage()).isNotNull();
        }
    }

    /**
     * The Runnable performing the test on a given shared KieBase.
     */
    public static class KieBaseRunnable extends SessionInsertMultiThreadingTest.KieSessionRunnable {
        public KieBaseRunnable(KieBase kbase) {
            super(kbase.newKieSession());
        }

        @Override
        public void run() {
            try {
                super.run();
            } finally {
                ksession.dispose();
            }
        }
    }
}

