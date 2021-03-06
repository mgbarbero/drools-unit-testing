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


import java.util.ArrayList;
import java.util.List;
import org.drools.compiler.Cheese;
import org.drools.compiler.CommonTestMethodBase;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;


public class FunctionsTest extends CommonTestMethodBase {
    @SuppressWarnings("unchecked")
    @Test
    public void testFunction() throws Exception {
        KieBase kbase = loadKnowledgeBase("test_FunctionInConsequence.drl");
        KieSession ksession = createKnowledgeSession(kbase);
        final List<Integer> list = new ArrayList<Integer>();
        ksession.setGlobal("list", list);
        final Cheese stilton = new Cheese("stilton", 5);
        ksession.insert(stilton);
        ksession.fireAllRules();
        Assert.assertEquals(new Integer(5), ((List<Integer>) (ksession.getGlobal("list"))).get(0));
    }

    @Test
    public void testFunctionException() throws Exception {
        KieBase kbase = loadKnowledgeBase("test_FunctionException.drl");
        KieSession ksession = createKnowledgeSession(kbase);
        final Cheese brie = new Cheese("brie", 12);
        ksession.insert(brie);
        try {
            ksession.fireAllRules();
            Assert.fail("Should throw an Exception from the Function");
        } catch (final Exception e) {
            Assert.assertEquals("this should throw an exception", e.getCause().getMessage());
        }
    }

    @Test
    public void testFunctionWithPrimitives() throws Exception {
        KieBase kbase = loadKnowledgeBase("test_FunctionWithPrimitives.drl");
        KieSession ksession = createKnowledgeSession(kbase);
        final List<Integer> list = new ArrayList<Integer>();
        ksession.setGlobal("list", list);
        final Cheese stilton = new Cheese("stilton", 5);
        ksession.insert(stilton);
        ksession.fireAllRules();
        Assert.assertEquals(new Integer(10), list.get(0));
    }

    @Test
    public void testFunctionCallingFunctionWithEclipse() throws Exception {
        KnowledgeBuilderConfiguration kbconf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        kbconf.setProperty("drools.dialect.java.compiler", "ECLIPSE");
        KieBase kbase = loadKnowledgeBase(kbconf, "test_functionCallingFunction.drl");
        KieSession ksession = createKnowledgeSession(kbase);
        final List<Integer> list = new ArrayList<Integer>();
        ksession.setGlobal("results", list);
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(12, list.get(0).intValue());
    }

    @Test
    public void testFunctionCallingFunctionWithJanino() throws Exception {
        KnowledgeBuilderConfiguration kbconf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        kbconf.setProperty("drools.dialect.java.compiler", "JANINO");
        KieBase kbase = loadKnowledgeBase(kbconf, "test_functionCallingFunction.drl");
        KieSession ksession = createKnowledgeSession(kbase);
        final List<Integer> list = new ArrayList<Integer>();
        ksession.setGlobal("results", list);
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(12, list.get(0).intValue());
    }

    @Test
    public void testJBRULES3117() {
        String str = "package org.kie\n" + ((((((("function boolean isOutOfRange( Object value, int lower ) { return true; }\n" + "function boolean isNotContainedInt( Object value, int[] values ) { return true; }\n") + "rule R1\n") + "when\n") + "then\n") + "    boolean x = isOutOfRange( Integer.MAX_VALUE, 1 );\n") + "    boolean y = isNotContainedInt( Integer.MAX_VALUE, new int[] { 1, 2, 3 } );\n") + "end\n");
        KieBase kbase = loadKnowledgeBaseFromString(str);
        KieSession ksession = createKnowledgeSession(kbase);
        int rulesFired = ksession.fireAllRules();
        Assert.assertEquals(1, rulesFired);
    }
}

