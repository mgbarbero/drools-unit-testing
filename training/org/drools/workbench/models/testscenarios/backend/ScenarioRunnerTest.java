/**
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
package org.drools.workbench.models.testscenarios.backend;


import FieldData.TYPE_ENUM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.drools.workbench.models.testscenarios.shared.ActivateRuleFlowGroup;
import org.drools.workbench.models.testscenarios.shared.ExecutionTrace;
import org.drools.workbench.models.testscenarios.shared.Expectation;
import org.drools.workbench.models.testscenarios.shared.FactData;
import org.drools.workbench.models.testscenarios.shared.Field;
import org.drools.workbench.models.testscenarios.shared.FieldData;
import org.drools.workbench.models.testscenarios.shared.Fixture;
import org.drools.workbench.models.testscenarios.shared.RetractFact;
import org.drools.workbench.models.testscenarios.shared.Scenario;
import org.drools.workbench.models.testscenarios.shared.VerifyFact;
import org.drools.workbench.models.testscenarios.shared.VerifyField;
import org.drools.workbench.models.testscenarios.shared.VerifyRuleFired;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.soup.project.datamodel.commons.types.ClassTypeResolver;
import org.kie.soup.project.datamodel.commons.types.TypeResolver;
import org.kie.soup.project.datamodel.imports.Import;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


public class ScenarioRunnerTest extends RuleUnit {
    private KieSession ksession;

    @Test
    public void testImports() throws Exception {
        Scenario scenario = new Scenario();
        scenario.setPackageName("org.drools.workbench.models.testscenarios.backend");
        ScenarioRunner runner = new ScenarioRunner(ksession);
        scenario.getFixtures().add(new FactData("Cheese", "f1", Collections.EMPTY_LIST, false));
        runner.run(scenario);
        Assert.assertTrue(scenario.wasSuccessful());
    }

    @Test
    public void testPopulateFactsWithInterfaces() throws Exception {
        Scenario sc = new Scenario();
        sc.getImports().addImport(new Import("java.util.List"));
        List facts = ls(new FactData("List", "ls", new ArrayList(), false));
        List globals = ls(new FactData("List", "ls", new ArrayList(), false));
        sc.getFixtures().addAll(facts);
        sc.getGlobals().addAll(globals);
        ScenarioRunner runner = new ScenarioRunner(ksession);
        runner.run(sc);
    }

    @Test
    public void testVerifyFacts() throws Exception {
        Scenario scenario = new Scenario();
        scenario.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        scenario.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Person"));
        ScenarioRunner runner = new ScenarioRunner(ksession);
        scenario.getFixtures().add(new FactData("Cheese", "f1", Arrays.<Field>asList(new FieldData("type", "cheddar"), new FieldData("price", "42")), false));
        scenario.getFixtures().add(new FactData("Person", "f2", Arrays.<Field>asList(new FieldData("name", "michael"), new FieldData("age", "33")), false));
        // test all true
        VerifyFact verifyCheddar = new VerifyFact();
        verifyCheddar.setName("f1");
        verifyCheddar.setFieldValues(Arrays.asList(new VerifyField("type", "cheddar", "=="), new VerifyField("price", "42", "==")));
        scenario.getFixtures().add(verifyCheddar);
        VerifyFact michaelVerifyFact = new VerifyFact();
        michaelVerifyFact.setName("f2");
        michaelVerifyFact.setFieldValues(Arrays.asList(new VerifyField("name", "michael", "=="), new VerifyField("age", "33", "==")));
        scenario.getFixtures().add(michaelVerifyFact);
        // test one false
        VerifyFact markVerifyFact = new VerifyFact();
        markVerifyFact.setName("f2");
        markVerifyFact.setFieldValues(Arrays.asList(new VerifyField("name", "mark", "=="), new VerifyField("age", "33", "==")));
        scenario.getFixtures().add(markVerifyFact);
        // test 2 false
        VerifyFact mark2VerifyFact = new VerifyFact();
        mark2VerifyFact.setName("f2");
        mark2VerifyFact.setFieldValues(Arrays.asList(new VerifyField("name", "mark", "=="), new VerifyField("age", "32", "==")));
        scenario.getFixtures().add(mark2VerifyFact);
        runner.run(scenario);
        for (VerifyField verifyField : verifyCheddar.getFieldValues()) {
            Assert.assertTrue(verifyField.getSuccessResult());
        }
        for (VerifyField verifyField : michaelVerifyFact.getFieldValues()) {
            Assert.assertTrue(verifyField.getSuccessResult());
        }
        Assert.assertFalse(markVerifyFact.getFieldValues().get(0).getSuccessResult());
        Assert.assertTrue(markVerifyFact.getFieldValues().get(1).getSuccessResult());
        Assert.assertEquals("michael", markVerifyFact.getFieldValues().get(0).getActualResult());
        Assert.assertEquals("mark", markVerifyFact.getFieldValues().get(0).getExpected());
        Assert.assertFalse(mark2VerifyFact.getFieldValues().get(0).getSuccessResult());
        Assert.assertFalse(mark2VerifyFact.getFieldValues().get(1).getSuccessResult());
        Assert.assertEquals("michael", mark2VerifyFact.getFieldValues().get(0).getActualResult());
        Assert.assertEquals("mark", mark2VerifyFact.getFieldValues().get(0).getExpected());
        Assert.assertEquals("33", mark2VerifyFact.getFieldValues().get(1).getActualResult());
        Assert.assertEquals("32", mark2VerifyFact.getFieldValues().get(1).getExpected());
    }

    @Test
    public void testVerifyFactsWithEnum() throws Exception {
        FieldData fieldData = new FieldData("cheeseType", "CheeseType.CHEDDAR");
        fieldData.setNature(TYPE_ENUM);
        FactData cheeseFactData = new FactData("Cheese", "c1", Arrays.<Field>asList(fieldData), false);
        FieldData cheeseType = new FieldData("cheeseType", "CheeseType.CHEDDAR");
        cheeseType.setNature(TYPE_ENUM);
        FactData f1 = new FactData("Cheese", "f1", Arrays.<Field>asList(cheeseType), false);
        Scenario scenario = new Scenario();
        scenario.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        scenario.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.CheeseType"));
        scenario.getFixtures().add(cheeseFactData);
        scenario.getFixtures().add(f1);
        ScenarioRunner runner = new ScenarioRunner(ksession);
        VerifyFact vf = new VerifyFact();
        vf.setName("f1");
        VerifyField verifyField = new VerifyField("cheeseType", "CheeseType.CHEDDAR", "==");
        verifyField.setNature(VerifyField.TYPE_ENUM);
        vf.setFieldValues(ls(verifyField));
        scenario.getFixtures().add(vf);
        runner.run(scenario);
        for (VerifyField field : vf.getFieldValues()) {
            Assert.assertTrue(field.getSuccessResult());
        }
    }

    @Test
    public void testVerifyFactsWithEnumWildCardImport() throws Exception {
        FieldData cheeseType = new FieldData("cheeseType", "CheeseType.CHEDDAR");
        cheeseType.setNature(TYPE_ENUM);
        FactData f1 = new FactData("Cheese", "f1", Arrays.<Field>asList(cheeseType), false);
        Scenario scenario = new Scenario();
        scenario.setPackageName("org.drools.workbench.models.testscenarios.backend");
        scenario.getFixtures().add(f1);
        ScenarioRunner runner = new ScenarioRunner(ksession);
        runner.run(scenario);
    }

    @Test
    public void testTestingEventListener() throws Exception {
        Scenario sc = new Scenario();
        sc.getRules().add("foo");
        sc.getRules().add("bar");
        ExecutionTrace ext = new ExecutionTrace();
        sc.getFixtures().add(ext);
        PseudoClockScheduler clock = new PseudoClockScheduler();
        long time = new Date().getTime();
        clock.setStartupTime(time);
        Mockito.when(ksession.getSessionClock()).thenReturn(clock);
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Mockito.verify(ksession).addEventListener(ArgumentMatchers.any(TestingEventListener.class));
        Assert.assertEquals(2, sc.getRules().size());
        Assert.assertTrue(sc.getRules().contains("foo"));
        Assert.assertTrue(sc.getRules().contains("bar"));
    }

    /**
     * Check if global list is empty.
     */
    @Test
    public void testWithGlobalList() throws Exception {
        Scenario sc = new Scenario();
        sc.getImports().addImport(new Import("java.util.List"));
        ArrayList testList = new ArrayList();
        sc.getGlobals().add(new FactData("List", "testList", testList, false));
        Expectation[] assertions = new Expectation[2];
        assertions[0] = new VerifyFact("testList", ls(new VerifyField("empty", "true", "==")));
        assertions[1] = new VerifyFact("testList", ls(new VerifyField("size", "0", "==")));
        sc.getFixtures().addAll(Arrays.asList(assertions));
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Mockito.verify(ksession).setGlobal("testList", testList);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSimulatedDate() throws Exception {
        Scenario sc = new Scenario();
        PseudoClockScheduler clock = new PseudoClockScheduler();
        long time = new Date().getTime();
        clock.setStartupTime(time);
        Mockito.when(ksession.getSessionClock()).thenReturn(clock);
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Assert.assertEquals(time, ksession.getSessionClock().getCurrentTime());
        ExecutionTrace ext = new ExecutionTrace();
        ext.setScenarioSimulatedDate(new Date("10-Jul-1974"));
        sc.getFixtures().add(ext);
        run = new ScenarioRunner(ksession);
        run.run(sc);
        long expected = ext.getScenarioSimulatedDate().getTime();
        Assert.assertEquals(expected, ksession.getSessionClock().getCurrentTime());
    }

    /**
     * Do a kind of end to end test with some real rules.
     */
    @Test
    public void testIntegrationWithSuccess() throws Exception {
        Scenario sc = new Scenario();
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Person"));
        FactData[] facts = new FactData[]{ new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("type", "cheddar"), new FieldData("price", "42")), false) };
        sc.getGlobals().add(new FactData("Person", "p", new ArrayList(), false));
        sc.getFixtures().addAll(Arrays.asList(facts));
        ExecutionTrace executionTrace = new ExecutionTrace();
        sc.getRules().add("rule1");
        sc.getRules().add("rule2");
        sc.setInclusive(true);
        sc.getFixtures().add(executionTrace);
        Expectation[] assertions = new Expectation[5];
        assertions[0] = new VerifyFact("c1", ls(new VerifyField("type", "cheddar", "==")));
        assertions[1] = new VerifyFact("p", ls(new VerifyField("name", "rule1", "=="), new VerifyField("status", "rule2", "==")));
        assertions[2] = new VerifyRuleFired("rule1", 1, null);
        assertions[3] = new VerifyRuleFired("rule2", 1, null);
        assertions[4] = new VerifyRuleFired("rule3", 0, null);
        sc.getFixtures().addAll(Arrays.asList(assertions));
        KieSession ksession = getKieSession("test_rules2.drl");
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Assert.assertEquals(2, executionTrace.getNumberOfRulesFired().intValue());
        Assert.assertTrue(sc.wasSuccessful());
        Thread.sleep(50);
        Assert.assertTrue(new Date().after(sc.getLastRunResult()));
        Assert.assertTrue(((executionTrace.getExecutionTimeResult()) != null));
        Assert.assertTrue(((executionTrace.getRulesFired().length) > 0));
    }

    @Test
    public void testIntegrationInfiniteLoop() throws Exception {
        Scenario sc = new Scenario();
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Person"));
        FactData[] facts = new FactData[]{ new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("type", "cheddar"), new FieldData("price", "42")), false) };
        sc.getGlobals().add(new FactData("Person", "p", new ArrayList(), false));
        sc.getFixtures().addAll(Arrays.asList(facts));
        ExecutionTrace executionTrace = new ExecutionTrace();
        sc.getRules().add("rule1");
        sc.getRules().add("rule2");
        sc.setInclusive(true);
        sc.getFixtures().add(executionTrace);
        Expectation[] assertions = new Expectation[5];
        assertions[0] = new VerifyFact("c1", ls(new VerifyField("type", "cheddar", "==")));
        assertions[1] = new VerifyFact("p", ls(new VerifyField("name", "rule1", "=="), new VerifyField("status", "rule2", "==")));
        assertions[2] = new VerifyRuleFired("rule1", 1, null);
        assertions[3] = new VerifyRuleFired("rule2", 1, null);
        assertions[4] = new VerifyRuleFired("rule3", 0, null);
        sc.getFixtures().addAll(Arrays.asList(assertions));
        KieSession ksession = getKieSession("test_rules_infinite_loop.drl");
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Assert.assertEquals(sc.getMaxRuleFirings(), executionTrace.getNumberOfRulesFired().intValue());
    }

    @Test
    public void testIntegrationWithDeclaredTypes() throws Exception {
        Scenario scenario = new Scenario();
        scenario.getImports().addImport(new Import("foo.bar.Coolness"));
        FactData[] facts = new FactData[]{ new FactData("Coolness", "c", Arrays.<Field>asList(new FieldData("num", "42"), new FieldData("name", "mic")), false) };
        scenario.getFixtures().addAll(Arrays.asList(facts));
        ExecutionTrace executionTrace = new ExecutionTrace();
        scenario.getRules().add("rule1");
        scenario.setInclusive(true);
        scenario.getFixtures().add(executionTrace);
        Expectation[] assertions = new Expectation[2];
        assertions[0] = new VerifyFact("c", ls(new VerifyField("num", "42", "==")));
        assertions[1] = new VerifyRuleFired("rule1", 1, null);
        scenario.getFixtures().addAll(Arrays.asList(assertions));
        KieSession ksession = getKieSession("test_rules3.drl");
        ClassLoader cl = getRootClassLoader();
        HashSet<String> imports = new HashSet<String>();
        imports.add("foo.bar.*");
        Assert.assertNotNull(cl.loadClass("foo.bar.Coolness"));
        ClassLoader cl_ = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        // resolver will need to have generated beans in it - possibly using a composite classloader from the package,
        // including whatever CL has the generated beans...
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(scenario);
        Assert.assertEquals(1, executionTrace.getNumberOfRulesFired().intValue());
        Assert.assertTrue(scenario.wasSuccessful());
        Thread.currentThread().setContextClassLoader(cl_);
    }

    @Test
    public void testRuleFlowGroupActivation() throws Exception {
        Scenario scenario = new Scenario();
        scenario.getImports().addImport(new Import("foo.bar.Coolness"));
        Fixture[] given = new Fixture[]{ new FactData("Coolness", "c", Arrays.<Field>asList(new FieldData("num", "42"), new FieldData("name", "mic")), false) };
        scenario.getFixtures().addAll(Arrays.asList(given));
        ExecutionTrace executionTrace = new ExecutionTrace();
        scenario.getRules().add("rule1");
        scenario.setInclusive(true);
        scenario.getFixtures().add(executionTrace);
        Expectation[] assertions = new Expectation[2];
        assertions[0] = new VerifyFact("c", ls(new VerifyField("num", "42", "==")));
        assertions[1] = new VerifyRuleFired("rule1", 1, null);
        scenario.getFixtures().addAll(Arrays.asList(assertions));
        KieSession ksession = getKieSession("rule_flow_actication.drl");
        ClassLoader classLoader = getRootClassLoader();
        HashSet<String> imports = new HashSet<String>();
        imports.add("foo.bar.*");
        TypeResolver resolver = new ClassTypeResolver(imports, classLoader);
        Class<?> coolnessClass = classLoader.loadClass("foo.bar.Coolness");
        Assert.assertNotNull(coolnessClass);
        ClassLoader cl_ = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        // resolver will need to have generated beans in it - possibly using a composite classloader from the package,
        // including whatever CL has the generated beans...
        ScenarioRunner scenarioRunner = new ScenarioRunner(ksession);
        scenarioRunner.run(scenario);
        Assert.assertEquals(0, executionTrace.getNumberOfRulesFired().intValue());
        Assert.assertFalse(scenario.wasSuccessful());
        // Activate rule flow
        scenario.getFixtures().clear();
        given = new Fixture[]{ new FactData("Coolness", "c", Arrays.<Field>asList(new FieldData("num", "42"), new FieldData("name", "mic")), false), new ActivateRuleFlowGroup("asdf") };
        scenario.getFixtures().addAll(Arrays.asList(given));
        scenario.getFixtures().add(executionTrace);
        setAutoDeactivate(false);
        scenarioRunner = new ScenarioRunner(ksession);
        scenarioRunner.run(scenario);
        Assert.assertTrue(scenario.wasSuccessful());
        Thread.currentThread().setContextClassLoader(cl_);
    }

    @Test
    public void testIntgerationStateful() throws Exception {
        Scenario sc = new Scenario();
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        sc.getFixtures().add(new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("price", "1")), false));
        ExecutionTrace ex = new ExecutionTrace();
        sc.getFixtures().add(ex);
        sc.getFixtures().add(new FactData("Cheese", "c2", Arrays.<Field>asList(new FieldData("price", "2")), false));
        sc.getFixtures().add(new VerifyFact("c1", ls(new VerifyField("type", "rule1", "=="))));
        ex = new ExecutionTrace();
        sc.getFixtures().add(ex);
        sc.getFixtures().add(new VerifyFact("c1", ls(new VerifyField("type", "rule2", "=="))));
        KieSession ksession = getKieSession("test_stateful.drl");
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Assert.assertTrue(sc.wasSuccessful());
    }

    @Test
    public void testIntegrationWithModify() throws Exception {
        Scenario sc = new Scenario();
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        sc.getFixtures().add(new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("price", "1")), false));
        sc.getFixtures().add(new ExecutionTrace());
        sc.getFixtures().add(new VerifyFact("c1", ls(new VerifyField("type", "rule1", "=="))));
        sc.getFixtures().add(new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("price", "42")), true));
        sc.getFixtures().add(new ExecutionTrace());
        sc.getFixtures().add(new VerifyFact("c1", ls(new VerifyField("type", "rule3", "=="))));
        KieSession ksession = getKieSession("test_stateful.drl");
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Assert.assertTrue(sc.wasSuccessful());
    }

    @Test
    public void testIntegrationWithRetract() throws Exception {
        Scenario sc = new Scenario();
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        sc.getFixtures().add(new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("price", "46"), new FieldData("type", "XXX")), false));
        sc.getFixtures().add(new FactData("Cheese", "c2", Arrays.<Field>asList(new FieldData("price", "42")), false));
        sc.getFixtures().add(new ExecutionTrace());
        sc.getFixtures().add(new VerifyFact("c1", ls(new VerifyField("type", "XXX", "=="))));
        sc.getFixtures().add(new RetractFact("c2"));
        sc.getFixtures().add(new ExecutionTrace());
        sc.getFixtures().add(new VerifyFact("c1", ls(new VerifyField("type", "rule4", "=="))));
        KieSession ksession = getKieSession("test_stateful.drl");
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Assert.assertTrue(sc.wasSuccessful());
    }

    @Test
    public void testIntegrationWithFailure() throws Exception {
        Scenario sc = new Scenario();
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        sc.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Person"));
        Expectation[] assertions = populateScenarioForFailure(sc);
        KieSession ksession = getKieSession("test_rules2.drl");
        ScenarioRunner run = new ScenarioRunner(ksession);
        run.run(sc);
        Assert.assertFalse(sc.wasSuccessful());
        VerifyFact vf = ((VerifyFact) (assertions[1]));
        Assert.assertFalse(vf.getFieldValues().get(0).getSuccessResult());
        Assert.assertEquals("XXX", vf.getFieldValues().get(0).getExpected());
        Assert.assertEquals("rule1", vf.getFieldValues().get(0).getActualResult());
        Assert.assertNotNull(vf.getFieldValues().get(0).getExplanation());
        VerifyRuleFired vr = ((VerifyRuleFired) (assertions[4]));
        Assert.assertFalse(vr.getSuccessResult());
        Assert.assertEquals(2, vr.getExpectedCount().intValue());
        Assert.assertEquals(0, vr.getActualResult().intValue());
    }

    @Test
    public void testCollection() throws Exception {
        KieSession ksession = getKieSession("test_rules2.drl");
        ScenarioRunner run = new ScenarioRunner(ksession);
        Scenario scenario = new Scenario();
        scenario.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheese"));
        scenario.getImports().addImport(new Import("org.drools.workbench.models.testscenarios.backend.Cheesery"));
        run.run(scenario);
    }
}

