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
package org.drools.compiler.integrationtests;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.drools.compiler.CommonTestMethodBase;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieSession;


public class GeneratedBeansTest extends CommonTestMethodBase {
    @Test
    public void testGeneratedBeans1() throws Exception {
        final KieBase kbase = loadKnowledgeBase("test_GeneratedBeans.drl");
        final FactType cheeseFact = kbase.getFactType("org.drools.generatedbeans", "Cheese");
        final Object cheese = cheeseFact.newInstance();
        cheeseFact.set(cheese, "type", "stilton");
        Assert.assertEquals("stilton", cheeseFact.get(cheese, "type"));
        final FactType personType = kbase.getFactType("org.drools.generatedbeans", "Person");
        final Object ps = personType.newInstance();
        personType.set(ps, "age", 42);
        final Map<String, Object> personMap = personType.getAsMap(ps);
        Assert.assertEquals(42, personMap.get("age"));
        personMap.put("age", 43);
        personType.setFromMap(ps, personMap);
        Assert.assertEquals(43, personType.get(ps, "age"));
        Assert.assertEquals("stilton", cheeseFact.getField("type").get(cheese));
        final KieSession ksession = createKnowledgeSession(kbase);
        final Object cg = cheeseFact.newInstance();
        ksession.setGlobal("cg", cg);
        final List<Object> result = new ArrayList<Object>();
        ksession.setGlobal("list", result);
        ksession.insert(cheese);
        ksession.fireAllRules();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(5, result.get(0));
        final FactType personFact = kbase.getFactType("org.drools.generatedbeans", "Person");
        final Object person = personFact.newInstance();
        personFact.getField("likes").set(person, cheese);
        personFact.getField("age").set(person, 7);
        ksession.insert(person);
        ksession.fireAllRules();
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(person, result.get(1));
    }

    @Test
    public void testGeneratedBeans2() throws Exception {
        final KieBase kbase = loadKnowledgeBase("test_GeneratedBeans2.drl");
        final FactType cheeseFact = kbase.getFactType("org.drools.generatedbeans", "Cheese");
        final Object cheese = cheeseFact.newInstance();
        cheeseFact.set(cheese, "type", "stilton");
        Assert.assertEquals("stilton", cheeseFact.get(cheese, "type"));
        final Object cheese2 = cheeseFact.newInstance();
        cheeseFact.set(cheese2, "type", "stilton");
        Assert.assertEquals(cheese, cheese2);
        final FactType personType = kbase.getFactType("org.drools.generatedbeans", "Person");
        final Object ps = personType.newInstance();
        personType.set(ps, "name", "mark");
        personType.set(ps, "last", "proctor");
        personType.set(ps, "age", 42);
        final Object ps2 = personType.newInstance();
        personType.set(ps2, "name", "mark");
        personType.set(ps2, "last", "proctor");
        personType.set(ps2, "age", 30);
        Assert.assertEquals(ps, ps2);
        personType.set(ps2, "last", "little");
        Assert.assertFalse(ps.equals(ps2));
        final KieSession wm = createKnowledgeSession(kbase);
        final Object cg = cheeseFact.newInstance();
        wm.setGlobal("cg", cg);
        final List result = new ArrayList();
        wm.setGlobal("list", result);
        wm.insert(cheese);
        wm.fireAllRules();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(5, result.get(0));
        final FactType personFact = kbase.getFactType("org.drools.generatedbeans", "Person");
        final Object person = personFact.newInstance();
        personFact.getField("likes").set(person, cheese);
        personFact.getField("age").set(person, 7);
        wm.insert(person);
        wm.fireAllRules();
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(person, result.get(1));
    }

    @Test
    public void testGeneratedBeansSerializable() throws Exception {
        final KieBase kbase = loadKnowledgeBase("test_GeneratedBeansSerializable.drl");
        final FactType cheeseFact = kbase.getFactType("org.drools.generatedbeans", "Cheese");
        Assert.assertTrue("Generated beans must be serializable", Serializable.class.isAssignableFrom(cheeseFact.getFactClass()));
        final Object cheese = cheeseFact.newInstance();
        cheeseFact.set(cheese, "type", "stilton");
        final Object cheese2 = cheeseFact.newInstance();
        cheeseFact.set(cheese2, "type", "brie");
        final KieSession ksession = createKnowledgeSession(kbase);
        final List<Number> results = new ArrayList<>();
        ksession.setGlobal("results", results);
        ksession.insert(cheese);
        ksession.insert(cheese2);
        ksession.fireAllRules();
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(2, results.get(0).intValue());
    }
}

