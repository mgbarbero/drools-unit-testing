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
package org.drools.compiler;


import ResourceType.DRL;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;


public class NodeHashingTest {
    @Test
    public void testNodeHashTypeMismatch() throws Exception {
        // BZ-1328380
        // 2 rules -- Mvel coercion
        String drl1 = ((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( status == 1 )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( status == 2 )\n") + "then\n") + "end\n";
        KieSession ksession1 = new KieHelper().addContent(drl1, DRL).build().newKieSession();
        Person p1 = new Person();
        p1.setStatus("1");
        ksession1.insert(p1);
        Assert.assertEquals(1, ksession1.fireAllRules());
        ksession1.dispose();
        // 3 rules -- Node Hashing
        String drl2 = (((((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( status == 1 )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( status == 2 )\n") + "then\n") + "end\n") + "rule \"rule3\"\n") + "when\n") + "    Person( status == 3 )\n") + "then\n") + "end\n";
        KieSession ksession2 = new KieHelper().addContent(drl2, DRL).build().newKieSession();
        Person p2 = new Person();
        p2.setStatus("1");
        ksession2.insert(p2);
        Assert.assertEquals(1, ksession2.fireAllRules());
        ksession2.dispose();
    }

    @Test
    public void testNodeHashTypeMismatchWithBigInteger() throws Exception {
        // BZ-1328380
        // 2 rules -- Mvel coercion
        String drl1 = ((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( bigInteger == \"1\" )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( bigInteger == \"2\" )\n") + "then\n") + "end\n";
        KieSession ksession1 = new KieHelper().addContent(drl1, DRL).build().newKieSession();
        Person p1 = new Person();
        p1.setBigInteger(new BigInteger("1"));
        ksession1.insert(p1);
        Assert.assertEquals(1, ksession1.fireAllRules());
        ksession1.dispose();
        // 3 rules -- Node Hashing
        String drl2 = (((((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( bigInteger == \"1\" )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( bigInteger == \"2\" )\n") + "then\n") + "end\n") + "rule \"rule3\"\n") + "when\n") + "    Person( bigInteger == \"3\" )\n") + "then\n") + "end\n";
        KieSession ksession2 = new KieHelper().addContent(drl2, DRL).build().newKieSession();
        Person p2 = new Person();
        p2.setBigInteger(new BigInteger("1"));
        ksession2.insert(p2);
        Assert.assertEquals(1, ksession2.fireAllRules());
        ksession2.dispose();
    }

    @Test
    public void testNodeHashTypeMismatchWithBigDecimal() throws Exception {
        // BZ-1328380
        // 2 rules -- Mvel coercion
        String drl1 = ((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( bigDecimal == \"1.00\" )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( bigDecimal == \"2.00\" )\n") + "then\n") + "end\n";
        KieSession ksession1 = new KieHelper().addContent(drl1, DRL).build().newKieSession();
        Person p1 = new Person();
        p1.setBigDecimal(new BigDecimal("1.00"));
        ksession1.insert(p1);
        Assert.assertEquals(1, ksession1.fireAllRules());
        ksession1.dispose();
        // 3 rules -- Node Hashing
        String drl2 = (((((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( bigDecimal == \"1.00\" )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( bigDecimal == \"2.00\" )\n") + "then\n") + "end\n") + "rule \"rule3\"\n") + "when\n") + "    Person( bigDecimal == \"3.00\" )\n") + "then\n") + "end\n";
        KieSession ksession2 = new KieHelper().addContent(drl2, DRL).build().newKieSession();
        Person p2 = new Person();
        p2.setBigDecimal(new BigDecimal("1.00"));
        ksession2.insert(p2);
        Assert.assertEquals(1, ksession2.fireAllRules());
        ksession2.dispose();
    }

    @Test
    public void testNodeHashTypeMismatchFromBigDecimal() throws Exception {
        // BZ-1328380
        // 2 rules -- Mvel coercion
        String drl1 = (((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import ") + (BigDecimal.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( age == new BigDecimal( 1 ) )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( age == new BigDecimal( 2 ) )\n") + "then\n") + "end\n";
        KieSession ksession1 = new KieHelper().addContent(drl1, DRL).build().newKieSession();
        Person p1 = new Person();
        p1.setAge(1);
        ksession1.insert(p1);
        Assert.assertEquals(1, ksession1.fireAllRules());
        ksession1.dispose();
        // 3 rules -- Node Hashing
        String drl2 = ((((((((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import ") + (BigDecimal.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( age == new BigDecimal( 1 ) )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( age == new BigDecimal( 2 ) )\n") + "then\n") + "end\n") + "rule \"rule3\"\n") + "when\n") + "    Person( age == new BigDecimal( 3 ) )\n") + "then\n") + "end\n";
        KieSession ksession2 = new KieHelper().addContent(drl2, DRL).build().newKieSession();
        Person p2 = new Person();
        p2.setAge(1);
        ksession2.insert(p2);
        Assert.assertEquals(1, ksession2.fireAllRules());
        ksession2.dispose();
    }

    @Test
    public void testNodeHashTypeMismatchWithPrimitiveDouble() throws Exception {
        // BZ-1328380
        // 2 rules -- Mvel coercion
        String drl1 = (((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import ") + (BigDecimal.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( age == 1.0 )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( age == 2.0 )\n") + "then\n") + "end\n";
        KieSession ksession1 = new KieHelper().addContent(drl1, DRL).build().newKieSession();
        Person p1 = new Person();
        p1.setAge(1);
        ksession1.insert(p1);
        Assert.assertEquals(1, ksession1.fireAllRules());
        ksession1.dispose();
        // 3 rules -- Node Hashing
        String drl2 = ((((((((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import ") + (BigDecimal.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( age == 1.0 )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( age == 2.0 )\n") + "then\n") + "end\n") + "rule \"rule3\"\n") + "when\n") + "    Person( age == 3.0 )\n") + "then\n") + "end\n";
        KieSession ksession2 = new KieHelper().addContent(drl2, DRL).build().newKieSession();
        Person p2 = new Person();
        p2.setAge(1);
        ksession2.insert(p2);
        Assert.assertEquals(1, ksession2.fireAllRules());
        ksession2.dispose();
    }

    @Test
    public void testNodeHashTypeMismatchWithBigIntegerAndDecimal() throws Exception {
        // BZ-1328380
        // 2 rules -- Mvel coercion
        String drl1 = (((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import ") + (BigInteger.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( bigDecimal == new BigInteger( \"1\" ) )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( bigDecimal == new BigInteger( \"2\" ) )\n") + "then\n") + "end\n";
        KieSession ksession1 = new KieHelper().addContent(drl1, DRL).build().newKieSession();
        Person p1 = new Person();
        p1.setBigDecimal(new BigDecimal(1));
        ksession1.insert(p1);
        Assert.assertEquals(1, ksession1.fireAllRules());
        ksession1.dispose();
        // 3 rules -- Node Hashing
        String drl2 = ((((((((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import ") + (BigInteger.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( bigDecimal == new BigInteger( \"1\" ) )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( bigDecimal == new BigInteger( \"2\" ) )\n") + "then\n") + "end\n") + "rule \"rule3\"\n") + "when\n") + "    Person( bigDecimal == new BigInteger( \"3\" ) )\n") + "then\n") + "end\n";
        KieSession ksession2 = new KieHelper().addContent(drl2, DRL).build().newKieSession();
        Person p2 = new Person();
        p2.setBigDecimal(new BigDecimal(1));
        ksession2.insert(p2);
        Assert.assertEquals(1, ksession2.fireAllRules());
        ksession2.dispose();
    }

    public static class DoubleValue {
        private final Double value;

        public DoubleValue(Double value) {
            this.value = value;
        }

        public Double getValue() {
            return value;
        }
    }

    @Test
    public void testNodeHashTypeMismatchWithDouble() throws Exception {
        // BZ-1328380
        // 2 rules -- Mvel coercion
        String drl1 = ((((((((((("import " + (NodeHashingTest.DoubleValue.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    DoubleValue( value == \"1.00\" )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    DoubleValue( value == \"2.00\" )\n") + "then\n") + "end\n";
        KieSession ksession1 = new KieHelper().addContent(drl1, DRL).build().newKieSession();
        ksession1.insert(new NodeHashingTest.DoubleValue(1.0));
        Assert.assertEquals(1, ksession1.fireAllRules());
        ksession1.dispose();
        // 3 rules -- Node Hashing
        String drl2 = (((((((((((((((("import " + (NodeHashingTest.DoubleValue.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    DoubleValue( value == \"1.00\" )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    DoubleValue( value == \"2.00\" )\n") + "then\n") + "end\n") + "rule \"rule3\"\n") + "when\n") + "    DoubleValue( value == \"3.00\" )\n") + "then\n") + "end\n";
        KieSession ksession2 = new KieHelper().addContent(drl2, DRL).build().newKieSession();
        ksession2.insert(new NodeHashingTest.DoubleValue(1.0));
        Assert.assertEquals(1, ksession2.fireAllRules());
        ksession2.dispose();
    }

    @Test
    public void testHashingOnClassConstraint() {
        String drl = ((((((((((((((("import " + (NodeHashingTest.A.class.getCanonicalName())) + "\n") + "rule R1 when\n") + "    A( configClass == String.class );\n") + "then\n") + "end\n") + "\n") + "rule R2 when\n") + "    A( configClass == String.class );\n") + "then\n") + "end\n") + "\n") + "rule R3 when\n") + "    A( configClass == String.class );\n") + "then\n") + "end\n\n";
        KieSession kieSession = new KieHelper().addContent(drl, DRL).build().newKieSession();
        kieSession.insert(new NodeHashingTest.A());
        Assert.assertEquals(3, kieSession.fireAllRules());
    }

    public static class A {
        public Class<?> getConfigClass() {
            return String.class;
        }
    }
}

