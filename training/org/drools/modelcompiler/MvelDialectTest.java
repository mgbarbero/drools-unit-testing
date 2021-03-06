/**
 * Copyright 2005 JBoss Inc
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
package org.drools.modelcompiler;


import Message.Level.ERROR;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.drools.modelcompiler.domain.Address;
import org.drools.modelcompiler.domain.Person;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;


public class MvelDialectTest extends BaseModelTest {
    public MvelDialectTest(BaseModelTest.RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testMVELinsert() {
        String str = "rule R\n" + (((((("dialect \"mvel\"\n" + "when\n") + "  Integer()\n") + "then\n") + "  System.out.println(\"Hello World\");\n") + "  insert(\"Hello World\");\n") + "end");
        KieSession ksession = getKieSession(str);
        FactHandle fh_47 = ksession.insert(47);
        ksession.fireAllRules();
        Collection<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertTrue(results.contains("Hello World"));
    }

    @Test
    public void testMVELmodify() {
        String str = (((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "rule R\n") + "dialect \"mvel\"\n") + "when\n") + "  $p : Person()\n") + "then\n") + "  modify($p) { setAge(1); }\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Matteo", 47));
        ksession.fireAllRules();
        Collection<Person> results = BaseModelTest.getObjectsIntoList(ksession, Person.class);
        Assert.assertEquals(1, results.iterator().next().getAge());
        results.forEach(System.out::println);
    }

    @Test
    public void testMVELmultiple() {
        String str = (((((((((((((((((((((((("package mypackage;" + ("dialect \"mvel\"\n"// MVEL dialect defined at package level.
         + "import ")) + (Person.class.getCanonicalName())) + ";\n") + "rule R1\n") + "when\n") + "  Integer()\n") + "then\n") + "  System.out.println(\"Hello World\")\n")// no ending ; as per MVEL dialect
         + "  insert(new Person(\"Matteo\", 47))\n") + "  insert(\"Hello World\")\n") + "end\n") + "rule R2\n") + "when\n") + "  $p : Person()\n") + "then\n") + "  modify($p) { setAge(1); }\n") + "  insert(\"Modified person age to 1 for: \"+$p.name)\n")// Please notice $p.name is MVEL dialect.
         + "end\n") + "rule R3\n") + "when\n") + "  $s : String( this == \"Hello World\")\n") + "  $p : Person()\n")// this is artificially added to ensure working even with unnecessary declaration passed to on().execute().
         + "then\n") + "  retract($s)") + "end\n";
        KieSession ksession = getKieSession(str);
        FactHandle fh_47 = ksession.insert(47);
        ksession.fireAllRules();
        Collection<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        System.out.println(results);
        Assert.assertFalse(results.contains("Hello World"));
        Assert.assertTrue(results.contains("Modified person age to 1 for: Matteo"));
    }

    @Test
    public void testMVELmultipleStatements() {
        String str = ((((((((("import " + (Person.class.getPackage().getName())) + ".*;\n")// keep the package.* in order for Address to be resolvable in the RHS.
         + "rule R\n") + "dialect \"mvel\"\n") + "when\n") + "  $p : Person()\n") + "then\n") + "  Address a = new Address(\"somewhere\");\n") + "  insert(a);\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Matteo", 47));
        ksession.fireAllRules();
        List<Address> results = BaseModelTest.getObjectsIntoList(ksession, Address.class);
        Assert.assertEquals(1, results.size());
    }

    public static class TempDecl1 {}

    public static class TempDecl2 {}

    public static class TempDecl3 {}

    public static class TempDecl4 {}

    public static class TempDecl5 {}

    public static class TempDecl6 {}

    public static class TempDecl7 {}

    public static class TempDecl8 {}

    public static class TempDecl9 {}

    public static class TempDecl10 {}

    @Test
    public void testMVEL10declarations() {
        String str = ((((((((((((((((((((((((((((((((((((((((((((("\n" + "import ") + (MvelDialectTest.TempDecl1.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl2.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl3.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl4.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl5.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl6.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl7.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl8.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl9.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl10.class.getCanonicalName())) + ";\n") + "rule R\n") + "dialect \"mvel\"\n") + "when\n") + "  $i1 : TempDecl1()\n") + "  $i2 : TempDecl2()\n") + "  $i3 : TempDecl3()\n") + "  $i4 : TempDecl4()\n") + "  $i5 : TempDecl5()\n") + "  $i6 : TempDecl6()\n") + "  $i7 : TempDecl7()\n") + "  $i8 : TempDecl8()\n") + "  $i9 : TempDecl9()\n") + "  $i10 : TempDecl10()\n") + "then\n") + "  insert(\"matched\");\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new MvelDialectTest.TempDecl1());
        ksession.insert(new MvelDialectTest.TempDecl2());
        ksession.insert(new MvelDialectTest.TempDecl3());
        ksession.insert(new MvelDialectTest.TempDecl4());
        ksession.insert(new MvelDialectTest.TempDecl5());
        ksession.insert(new MvelDialectTest.TempDecl6());
        ksession.insert(new MvelDialectTest.TempDecl7());
        ksession.insert(new MvelDialectTest.TempDecl8());
        ksession.insert(new MvelDialectTest.TempDecl9());
        ksession.insert(new MvelDialectTest.TempDecl10());
        ksession.fireAllRules();
        List<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testMVEL10declarationsBis() {
        String str = (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("\n" + "import ") + (MvelDialectTest.TempDecl1.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl2.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl3.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl4.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl5.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl6.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl7.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl8.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl9.class.getCanonicalName())) + ";\n") + "import ") + (MvelDialectTest.TempDecl10.class.getCanonicalName())) + ";\n") + "rule Rinit\n") + "dialect \"mvel\"\n") + "when\n") + "then\n") + "  insert( new TempDecl1() );\n") + "  insert( new TempDecl2() );\n") + "  insert( new TempDecl3() );\n") + "  insert( new TempDecl4() );\n") + "  insert( new TempDecl5() );\n") + "  insert( new TempDecl6() );\n") + "  insert( new TempDecl7() );\n") + "  insert( new TempDecl8() );\n") + "  insert( new TempDecl9() );\n") + "  insert( new TempDecl10());\n") + "end\n") + "rule R\n") + "dialect \"mvel\"\n") + "when\n") + "  $i1 : TempDecl1()\n") + "  $i2 : TempDecl2()\n") + "  $i3 : TempDecl3()\n") + "  $i4 : TempDecl4()\n") + "  $i5 : TempDecl5()\n") + "  $i6 : TempDecl6()\n") + "  $i7 : TempDecl7()\n") + "  $i8 : TempDecl8()\n") + "  $i9 : TempDecl9()\n") + "  $i10 : TempDecl10()\n") + "then\n") + "   insert(\"matched\");\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.fireAllRules();
        List<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testMvelFunctionWithClassArg() {
        final String drl = (((((((((((((((((((("package org.drools.compiler.integrationtests.drl; \n" + "import ") + (Person.class.getCanonicalName())) + ";\n") + "dialect \"mvel\"\n") + "global java.lang.StringBuilder value;\n") + "function String getFieldValue(Person bean) {") + "   return bean.getName();") + "}") + "\n") + "rule R1 \n") + "when \n") + "then \n") + "   insert( new Person( \"mario\" ) ); \n") + "end \n") + "\n") + "rule R2 \n") + "when \n") + "   $bean : Person( ) \n") + "then \n") + "   value.append( getFieldValue($bean) ); \n") + "end";
        KieSession ksession = getKieSession(drl);
        try {
            final StringBuilder sb = new StringBuilder();
            ksession.setGlobal("value", sb);
            ksession.fireAllRules();
            Assert.assertEquals("mario", sb.toString());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testMvelFunctionWithDeclaredTypeArg() {
        final String drl = "package org.drools.compiler.integrationtests.drl; \n" + (((((((((((((((((((("dialect \"mvel\"\n" + "global java.lang.StringBuilder value;\n") + "function String getFieldValue(Bean bean) {") + "   return bean.getField();") + "}") + "declare Bean \n") + "   field : String \n") + "end \n") + "\n") + "rule R1 \n") + "when \n") + "then \n") + "   insert( new Bean( \"mario\" ) ); \n") + "end \n") + "\n") + "rule R2 \n") + "when \n") + "   $bean : Bean( ) \n") + "then \n") + "   value.append( getFieldValue($bean) ); \n") + "end");
        KieSession ksession = getKieSession(drl);
        try {
            final StringBuilder sb = new StringBuilder();
            ksession.setGlobal("value", sb);
            ksession.fireAllRules();
            Assert.assertEquals("mario", sb.toString());
        } finally {
            ksession.dispose();
        }
    }

    @Test
    public void testMultiDrlWithSamePackageMvel() throws Exception {
        // DROOLS-3508
        String drl1 = ((("package org.pkg\n" + "import ") + (Person.class.getCanonicalName())) + "\n") + "dialect \"mvel\"\n";// MVEL dialect defined at package level.

        String drl2 = "package org.pkg\n" + ((((((("rule R1\n" + "no-loop\n") + "when\n") + "   $p : Person( name == \"John\" )\n") + "then\n") + "   $p.age = 1;\n") + "   update($p);\n") + "end\n");
        KieSession ksession = getKieSession(drl1, drl2);
        Person john = new Person("John", 24);
        ksession.insert(john);
        Assert.assertEquals(1, ksession.fireAllRules());
        Assert.assertEquals(1, john.getAge());
    }

    @Test
    public void testMVELNonExistingMethod() {
        // DROOLS-3559
        String drl = (((((((("import " + (Person.class.getCanonicalName())) + "\n") + "dialect \"mvel\"\n") + "rule R\n") + "when\n") + "  $p : Person()\n") + "then\n") + "  modify($p) {likes = nonExistingMethod()};\n") + "end";
        Results results = createKieBuilder(drl).getResults();
        Assert.assertFalse(results.getMessages(ERROR).isEmpty());
    }
}

