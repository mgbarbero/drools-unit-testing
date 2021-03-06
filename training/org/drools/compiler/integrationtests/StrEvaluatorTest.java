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
import org.drools.compiler.CommonTestMethodBase;
import org.drools.compiler.Person;
import org.drools.compiler.RoutingMessage;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;


public class StrEvaluatorTest extends CommonTestMethodBase {
    @Test
    public void testStrStartsWith() {
        KieBase kbase = readKnowledgeBase();
        KieSession ksession = createKnowledgeSession(kbase);
        try {
            List list = new ArrayList();
            ksession.setGlobal("list", list);
            RoutingMessage m = new RoutingMessage();
            m.setRoutingValue("R1:messageBody");
            ksession.insert(m);
            ksession.fireAllRules();
            Assert.assertTrue(((list.size()) == 4));
            Assert.assertTrue(list.get(0).equals("Message starts with R1"));
            Assert.assertTrue(list.get(1).equals("Message length is not 17"));
            Assert.assertTrue(list.get(2).equals("Message does not start with R2"));
            Assert.assertTrue(list.get(3).equals("Message does not end with R1"));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testStrEndsWith() {
        KieBase kbase = readKnowledgeBase();
        KieSession ksession = createKnowledgeSession(kbase);
        try {
            List list = new ArrayList();
            ksession.setGlobal("list", list);
            RoutingMessage m = new RoutingMessage();
            m.setRoutingValue("messageBody:R2");
            ksession.insert(m);
            ksession.fireAllRules();
            Assert.assertTrue(((list.size()) == 4));
            Assert.assertTrue(list.get(0).equals("Message ends with R2"));
            Assert.assertTrue(list.get(1).equals("Message length is not 17"));
            Assert.assertTrue(list.get(2).equals("Message does not start with R2"));
            Assert.assertTrue(list.get(3).equals("Message does not end with R1"));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testStrLengthEquals() {
        KieBase kbase = readKnowledgeBase();
        KieSession ksession = createKnowledgeSession(kbase);
        try {
            List list = new ArrayList();
            ksession.setGlobal("list", list);
            RoutingMessage m = new RoutingMessage();
            m.setRoutingValue("R1:messageBody:R2");
            ksession.insert(m);
            ksession.fireAllRules();
            Assert.assertEquals(6, list.size());
            Assert.assertTrue(list.contains("Message length is 17"));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testStrNotStartsWith() {
        KieBase kbase = readKnowledgeBase();
        KieSession ksession = createKnowledgeSession(kbase);
        try {
            List list = new ArrayList();
            ksession.setGlobal("list", list);
            RoutingMessage m = new RoutingMessage();
            m.setRoutingValue("messageBody");
            ksession.insert(m);
            ksession.fireAllRules();
            Assert.assertTrue(((list.size()) == 3));
            Assert.assertTrue(list.get(1).equals("Message does not start with R2"));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testStrNotEndsWith() {
        KieBase kbase = readKnowledgeBase();
        KieSession ksession = createKnowledgeSession(kbase);
        try {
            List list = new ArrayList();
            ksession.setGlobal("list", list);
            RoutingMessage m = new RoutingMessage();
            m.setRoutingValue("messageBody");
            ksession.insert(m);
            ksession.fireAllRules();
            Assert.assertTrue(((list.size()) == 3));
            Assert.assertTrue(list.get(0).equals("Message length is not 17"));
            Assert.assertTrue(list.get(1).equals("Message does not start with R2"));
            Assert.assertTrue(list.get(2).equals("Message does not end with R1"));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testStrLengthNoEquals() {
        KieBase kbase = readKnowledgeBase();
        KieSession ksession = createKnowledgeSession(kbase);
        try {
            List list = new ArrayList();
            ksession.setGlobal("list", list);
            RoutingMessage m = new RoutingMessage();
            m.setRoutingValue("messageBody");
            ksession.insert(m);
            ksession.fireAllRules();
            Assert.assertTrue(((list.size()) == 3));
            Assert.assertTrue(list.get(0).equals("Message length is not 17"));
            Assert.assertTrue(list.get(1).equals("Message does not start with R2"));
            Assert.assertTrue(list.get(2).equals("Message does not end with R1"));
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testStrWithLogicalOr() {
        String drl = "package org.drools.compiler.integrationtests\n" + ((((("import org.drools.compiler.RoutingMessage\n" + "rule R1\n") + " when\n") + " RoutingMessage( routingValue == \"R2\" || routingValue str[startsWith] \"R1\" )\n") + " then\n") + "end\n");
        KieBase kbase = loadKnowledgeBaseFromString(drl);
        KieSession ksession = kbase.newKieSession();
        try {
            for (String msgValue : new String[]{ "R1something", "R2something", "R2" }) {
                RoutingMessage msg = new RoutingMessage();
                msg.setRoutingValue(msgValue);
                ksession.insert(msg);
            }
            Assert.assertEquals("Wrong number of rules fired", 2, ksession.fireAllRules());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testStrWithInlineCastAndFieldOnThis() {
        String drl = ((((((((("package org.drools.compiler.integrationtests " + "import ") + (Person.class.getName())) + "; ") + "rule R1 ") + " when ") + " Object( this#") + (Person.class.getName())) + ".name str[startsWith] \"M\" ) ") + " then ") + "end ";
        KieBase kbase = loadKnowledgeBaseFromString(drl);
        KieSession ksession = kbase.newKieSession();
        try {
            ksession.insert(new Person("Mark"));
            Assert.assertEquals("Wrong number of rules fired", 1, ksession.fireAllRules());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testStrWithInlineCastOnThis() {
        String drl = "package org.drools.compiler.integrationtests " + (((("rule R1 " + " when ") + " Object( this#String str[startsWith] \"M\" ) ") + " then ") + "end ");
        KieBase kbase = loadKnowledgeBaseFromString(drl);
        KieSession ksession = kbase.newKieSession();
        try {
            ksession.insert("Mark");
            Assert.assertEquals("Wrong number of rules fired", 1, ksession.fireAllRules());
        } finally {
            ksession.dispose();
        }
    }
}

