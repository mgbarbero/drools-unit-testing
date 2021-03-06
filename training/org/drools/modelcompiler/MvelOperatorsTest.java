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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.drools.modelcompiler.domain.Person;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;


public class MvelOperatorsTest extends BaseModelTest {
    public MvelOperatorsTest(BaseModelTest.RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testIn() {
        String str = "rule R when\n" + (("    String(this in (\"a\", \"b\"))" + "then\n") + "end ");
        KieSession ksession = getKieSession(str);
        ksession.insert("b");
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testStr() {
        String str = "rule R when\n" + (("    String(this str[startsWith] \"M\")" + "then\n") + "end ");
        KieSession ksession = getKieSession(str);
        ksession.insert("Mario");
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testStrNot() {
        String str = "rule R when\n" + (("    String(this not str[startsWith] \"M\")" + "then\n") + "end ");
        KieSession ksession = getKieSession(str);
        ksession.insert("Mario");
        ksession.insert("Luca");
        ksession.insert("Edoardo");
        Assert.assertEquals(2, ksession.fireAllRules());
    }

    @Test
    public void testStrHalf() {
        String str = "rule R when\n" + (("    String(this str[startsWith] \"M\" || str[endsWith] \"a\" || str[length] 10)" + "then\n") + "end ");
        KieSession ksession = getKieSession(str);
        ksession.insert("Mario");
        ksession.insert("Luca");
        ksession.insert("Edoardo");
        ksession.insert("Valentina");
        Assert.assertEquals(3, ksession.fireAllRules());
    }

    @Test
    public void testRange() {
        String str = ((((((("import " + (Person.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "rule R when\n") + "    Person( age > 30 && <= 40, $name : name )") + "then\n") + "    list.add($name);") + "end ";
        KieSession ksession = getKieSession(str);
        List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert(new Person("Mario", 44));
        ksession.insert(new Person("Mark", 40));
        ksession.insert(new Person("Edson", 31));
        ksession.insert(new Person("Luca", 30));
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.containsAll(Arrays.asList("Mark", "Edson")));
    }

    @Test
    public void testExcludedRange() {
        String str = ((((((("import " + (Person.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "rule R when\n") + "    Person( age <= 30 || > 40, $name : name )") + "then\n") + "    list.add($name);") + "end ";
        KieSession ksession = getKieSession(str);
        List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert(new Person("Mario", 44));
        ksession.insert(new Person("Mark", 40));
        ksession.insert(new Person("Edson", 31));
        ksession.insert(new Person("Luca", 30));
        ksession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.containsAll(Arrays.asList("Luca", "Mario")));
    }

    @Test
    public void testBinding() {
        String str = ((((((("import " + (Person.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "rule R when\n") + "    Person( $name : name in (\"Mario\", \"Mark\"))") + "then\n") + "    list.add($name);") + "end ";
        KieSession ksession = getKieSession(str);
        List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        Person person1 = new Person("Mario");
        ksession.insert(person1);
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("Mario", list.get(0));
    }

    @Test
    public void testMatches() {
        String str = "rule R when\n" + (("    String(this matches \"\\\\w\")" + "then\n") + "end ");
        KieSession ksession = getKieSession(str);
        ksession.insert("b");
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testExcludes() {
        String str = "import java.util.List\n" + ((("rule R when\n" + "    List(this excludes \"test\")") + "then\n") + "end ");
        KieSession ksession = getKieSession(str);
        ksession.insert(Arrays.asList("ciao", "test"));
        Assert.assertEquals(0, ksession.fireAllRules());
        ksession.insert(Arrays.asList("hello", "world"));
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testNotContains() {
        String str = "import java.util.List\n" + ((("rule R when\n" + "    List(this not contains \"test\")") + "then\n") + "end ");
        KieSession ksession = getKieSession(str);
        ksession.insert(Arrays.asList("ciao", "test"));
        Assert.assertEquals(0, ksession.fireAllRules());
        ksession.insert(Arrays.asList("hello", "world"));
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testNotIn() {
        String str = ((((((("import " + (Person.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "rule R when\n") + "    Person( $name : name, age not in (40, 37))") + "then\n") + "    list.add($name);") + "end ";
        KieSession ksession = getKieSession(str);
        List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert(new Person("Mark", 40));
        ksession.insert(new Person("Mario", 44));
        ksession.insert(new Person("Edson", 37));
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("Mario", list.get(0));
    }

    @Test
    public void testNotInUsingShort() {
        String str = ((((((("import " + (Person.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "rule R when\n") + "    Person( $name : name, ageAsShort not in (40, 37))") + "then\n") + "    list.add($name);") + "end ";
        KieSession ksession = getKieSession(str);
        List<String> list = new ArrayList<>();
        ksession.setGlobal("list", list);
        ksession.insert(new Person("Mark", 40));
        ksession.insert(new Person("Mario", 44));
        ksession.insert(new Person("Edson", 37));
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("Mario", list.get(0));
    }
}

