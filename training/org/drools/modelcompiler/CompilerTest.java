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


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.drools.modelcompiler.domain.Address;
import org.drools.modelcompiler.domain.Adult;
import org.drools.modelcompiler.domain.Child;
import org.drools.modelcompiler.domain.Overloaded;
import org.drools.modelcompiler.domain.Person;
import org.drools.modelcompiler.domain.Result;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;


public class CompilerTest extends BaseModelTest {
    public CompilerTest(BaseModelTest.RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test(timeout = 5000)
    public void testPropertyReactvity() {
        String str = (((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person(name == \"Mario\")\n") + "then\n") + "  modify($p) { setAge($p.getAge()+1) }\n") + "end";
        KieSession ksession = getKieSession(str);
        Person me = new Person("Mario", 40);
        ksession.insert(me);
        ksession.fireAllRules();
        Assert.assertEquals(41, me.getAge());
    }

    @Test
    public void testPropertyReactvityOnFinalField() throws Exception {
        String str = "rule R when\n" + ((("    $a : String( length > 3 )\n" + "then\n") + "  System.out.println($a);\n") + "end\n");
        KieSession ksession = getKieSession(str);
        ksession.insert("abcd");
        ksession.insert("xy");
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testEqualityCheckOnNull() {
        String str = ((((((((("import " + (Person.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "rule R when\n") + "  Person($name : name == \"Mario\")\n") + "then\n") + "  insert(new Result($name));\n") + "end";
        KieSession ksession = getKieSession(str);
        final Person mario = new Person("Mario", 40);
        final Person luca = new Person(null, 33);
        ksession.insert(mario);
        ksession.insert(luca);
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Mario", results.iterator().next().getValue());
    }

    @Test
    public void testOrWithFixedLeftOperand() {
        String str = ((((((((("import " + (Person.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person(name == \"Mario\" || == \"Luca\" || == \"Edoardo\")\n") + "then\n") + "  insert(new Result($p));\n") + "end";
        KieSession ksession = getKieSession(str);
        final Person mario = new Person("Mario", 40);
        final Person luca = new Person("Luca", 33);
        final Person edoardo = new Person("Edoardo", 31);
        final Person matteo = new Person("Matteo", 36);
        ksession.insert(mario);
        ksession.insert(luca);
        ksession.insert(edoardo);
        ksession.insert(matteo);
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(3, results.size());
        Assertions.assertThat(results.stream().map(( r) -> r.getValue())).containsExactlyInAnyOrder(mario, luca, edoardo);
    }

    @Test
    public void testBeta() {
        String str = ((((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $r : Result()\n") + "  $markV : Person(name == \"Mark\")\n") + "  $olderV : Person(name != \"Mark\", age > $markV.age)\n") + "then\n") + "  $r.setValue($olderV.getName() + \" is older than \" + $markV.getName());\n") + "end";
        KieSession ksession = getKieSession(str);
        Result result = new Result();
        ksession.insert(result);
        Person mark = new Person("Mark", 37);
        Person edson = new Person("Edson", 35);
        Person mario = new Person("Mario", 40);
        FactHandle markFH = ksession.insert(mark);
        FactHandle edsonFH = ksession.insert(edson);
        FactHandle marioFH = ksession.insert(mario);
        ksession.fireAllRules();
        Assert.assertEquals("Mario is older than Mark", result.getValue());
        result.setValue(null);
        ksession.delete(marioFH);
        ksession.fireAllRules();
        Assert.assertNull(result.getValue());
        mark.setAge(34);
        ksession.update(markFH, mark, "age");
        ksession.fireAllRules();
        Assert.assertEquals("Edson is older than Mark", result.getValue());
    }

    @Test
    public void testRuleExtends() {
        String str = ((((((((((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R1 when\n") + "  $r : Result()\n") + "then\n") + "end\n") + "rule R2 extends R1 when\n") + "  $p1 : Person(name == \"Mark\")\n") + "then\n") + "end\n") + "rule R3 extends R2 when\n") + "  $p2 : Person(name != \"Mark\", age > $p1.age)\n") + "then\n") + "  $r.setValue($p2.getName() + \" is older than \" + $p1.getName());\n") + "end";
        KieSession ksession = getKieSession(str);
        Result result = new Result();
        ksession.insert(result);
        Assert.assertEquals(1, ksession.fireAllRules());
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        Assert.assertEquals(0, ksession.fireAllRules());
        ksession.insert(new Person("Mark", 37));
        Assert.assertEquals(2, ksession.fireAllRules());
        Assert.assertEquals("Mario is older than Mark", result.getValue());
    }

    @Test
    public void testBetaWithDeclaration() {
        String str = ((((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $r : Result()\n") + "  $p1 : Person(name == \"Mark\", $markAge : age)\n") + "  $p2 : Person(name != \"Mark\", age > $markAge)\n") + "then\n") + "  $r.setValue($p2.getName() + \" is older than \" + $p1.getName());\n") + "end";
        KieSession ksession = getKieSession(str);
        Result result = new Result();
        ksession.insert(result);
        Person mark = new Person("Mark", 37);
        Person edson = new Person("Edson", 35);
        Person mario = new Person("Mario", 40);
        FactHandle markFH = ksession.insert(mark);
        FactHandle edsonFH = ksession.insert(edson);
        FactHandle marioFH = ksession.insert(mario);
        ksession.fireAllRules();
        Assert.assertEquals("Mario is older than Mark", result.getValue());
        result.setValue(null);
        ksession.delete(marioFH);
        ksession.fireAllRules();
        Assert.assertNull(result.getValue());
        mark.setAge(34);
        ksession.update(markFH, mark, "age");
        ksession.fireAllRules();
        Assert.assertEquals("Edson is older than Mark", result.getValue());
    }

    @Test
    public void test3Patterns() {
        String str = (((((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $mark : Person(name == \"Mark\")\n") + "  $p : Person(age > $mark.age)\n") + "  $s: String(this == $p.name)\n") + "then\n") + "  System.out.println(\"Found: \" + $s);\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert("Mario");
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
    }

    @Test
    public void testSimpleInsert() {
        String str = (((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( name.length == 4 )\n") + "then\n") + "  Result r = new Result($p.getName());") + "  insert(r);\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Mark", results.iterator().next().getValue());
    }

    @Test
    public void testSimpleInsertWithProperties() {
        String str = (((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( address.city.startsWith(\"M\"))\n") + "then\n") + "  Result r = new Result($p.getName());") + "  insert(r);\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Mark", 37, new Address("London")));
        ksession.insert(new Person("Luca", 32, new Address("Milan")));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Luca", results.iterator().next().getValue());
    }

    @Test
    public void testSimpleDelete() {
        String str = ((((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( name.length == 4 )\n") + "then\n") + "  Result r = new Result($p.getName());") + "  insert(r);\n") + "  delete($p);\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Mark", results.iterator().next().getValue());
        Assert.assertEquals(1, BaseModelTest.getObjectsIntoList(ksession, Person.class).size());
    }

    @Test
    public void testSimpleInsertDeleteExplicitScope() {
        String str = ((((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( name.length == 4 )\n") + "then\n") + "  Result r = new Result($p.getName());") + "  drools.insert(r);\n") + "  drools.delete($p);\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Mark", results.iterator().next().getValue());
        Assert.assertEquals(1, BaseModelTest.getObjectsIntoList(ksession, Person.class).size());
    }

    @Test
    public void testSimpleUpdate() {
        String str = ((((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( name.length == 4 )\n") + "then\n") + "  $p.setAge($p.getAge()+1);") + "  update($p);\n") + "end";
        KieSession ksession = getKieSession(str);
        Person mark = new Person("Mark", 37);
        Person mario = new Person("Mario", 40);
        ksession.insert(mark);
        ksession.insert(mario);
        ksession.fireAllRules();
        Assert.assertEquals(38, mark.getAge());
        Assert.assertEquals(40, mario.getAge());
    }

    @Test
    public void testSimpleModify() {
        String str = (((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( name.length == 4 )\n") + "then\n") + "  modify($p) { setAge($p.getAge()+1) }\n") + "end";
        KieSession ksession = getKieSession(str);
        Person mark = new Person("Mark", 37);
        Person mario = new Person("Mario", 40);
        ksession.insert(mark);
        ksession.insert(mario);
        ksession.fireAllRules();
        Assert.assertEquals(38, mark.getAge());
        Assert.assertEquals(40, mario.getAge());
    }

    @Test
    public void testEmptyPattern() {
        String str = ((((((((("import " + (Person.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "rule R when\n") + "  Person() \n") + "then\n") + "  insert(new Result(\"ok\"));\n") + "end";
        KieSession ksession = getKieSession(str);
        Person mario = new Person("Mario", 40);
        ksession.insert(mario);
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("ok", results.iterator().next().getValue());
    }

    @Test
    public void testEmptyPatternWithBinding() {
        String str = ((((((((("import " + (Person.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person() \n") + "then\n") + "  insert(new Result($p.getName()));\n") + "end";
        KieSession ksession = getKieSession(str);
        Person mario = new Person("Mario", 40);
        ksession.insert(mario);
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Mario", results.iterator().next().getValue());
    }

    @Test
    public void testNamedConsequence() {
        String str = (((((((((((((("import " + (Result.class.getCanonicalName())) + ";\n") + "import ") + (Person.class.getCanonicalName())) + ";\n") + "rule R when\n") + "  $r : Result()\n") + "  $p1 : Person(name == \"Mark\")\n") + "  do[FoundMark]\n") + "  $p2 : Person(name != \"Mark\", age > $p1.age)\n") + "then\n") + "  $r.addValue($p2.getName() + \" is older than \" + $p1.getName());\n") + "then[FoundMark]\n") + "  $r.addValue(\"Found \" + $p1.getName());\n") + "end";
        KieSession ksession = getKieSession(str);
        Result result = new Result();
        ksession.insert(result);
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Collection results = ((Collection) (result.getValue()));
        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.containsAll(Arrays.asList("Found Mark", "Mario is older than Mark")));
    }

    @Test
    public void testBreakingNamedConsequence() {
        String str = (((((((((((((((((((("import " + (Result.class.getCanonicalName())) + ";\n") + "import ") + (Person.class.getCanonicalName())) + ";\n") + "rule R when\n") + "  $r : Result()\n") + "  $p1 : Person(name == \"Mark\")\n") + "  if ( age < 30 ) break[FoundYoungMark]") + "  else if ( age > 50) break[FoundOldMark]\n") + "  else break[FoundMark]\n") + "  $p2 : Person(name != \"Mark\", age > $p1.age)\n") + "then\n") + "  $r.addValue($p2.getName() + \" is older than \" + $p1.getName());\n") + "then[FoundYoungMark]\n") + "  $r.addValue(\"Found young \" + $p1.getName());\n") + "then[FoundOldMark]\n") + "  $r.addValue(\"Found old \" + $p1.getName());\n") + "then[FoundMark]\n") + "  $r.addValue(\"Found \" + $p1.getName());\n") + "end";
        KieSession ksession = getKieSession(str);
        Result result = new Result();
        ksession.insert(result);
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Collection results = ((Collection) (result.getValue()));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Found Mark", results.iterator().next());
    }

    @Test
    public void testNonBreakingNamedConsequence() {
        String str = (((((((((((((((((((("import " + (Result.class.getCanonicalName())) + ";\n") + "import ") + (Person.class.getCanonicalName())) + ";\n") + "rule R when\n") + "  $r : Result()\n") + "  $p1 : Person(name == \"Mark\")\n") + "  if ( age < 30 ) break[FoundYoungMark]") + "  else if ( age > 50) break[FoundOldMark]\n") + "  else do[FoundMark]\n") + "  $p2 : Person(name != \"Mark\", age > $p1.age)\n") + "then\n") + "  $r.addValue($p2.getName() + \" is older than \" + $p1.getName());\n") + "then[FoundYoungMark]\n") + "  $r.addValue(\"Found young \" + $p1.getName());\n") + "then[FoundOldMark]\n") + "  $r.addValue(\"Found old \" + $p1.getName());\n") + "then[FoundMark]\n") + "  $r.addValue(\"Found \" + $p1.getName());\n") + "end";
        KieSession ksession = getKieSession(str);
        Result result = new Result();
        ksession.insert(result);
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Collection results = ((Collection) (result.getValue()));
        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.containsAll(Arrays.asList("Found Mark", "Mario is older than Mark")));
    }

    @Test
    public void testFrom() {
        String str = (((((((((((((("import " + (Result.class.getCanonicalName())) + ";\n") + "import ") + (Adult.class.getCanonicalName())) + ";\n") + "import ") + (Child.class.getCanonicalName())) + ";\n") + "rule R when\n") + "  $r : Result()\n") + "  $a : Adult()\n") + "  $c : Child( age > 8 ) from $a.children\n") + "then\n") + "  $r.setValue($c.getName());\n") + "end";
        KieSession ksession = getKieSession(str);
        Result result = new Result();
        ksession.insert(result);
        Adult dad = new Adult("dad", 40);
        dad.addChild(new Child("Alan", 10));
        dad.addChild(new Child("Betty", 7));
        ksession.insert(dad);
        ksession.fireAllRules();
        Assert.assertEquals("Alan", result.getValue());
    }

    @Test
    public void testConcatenatedFrom() {
        checkConcatenatedFrom(true);
    }

    @Test
    public void testConcatenatedFromWithCondition() {
        checkConcatenatedFrom(false);
    }

    @Test
    public void testAgeWithSum() {
        String str = (((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( personAge : age )\n") + "  $plusTwo : Person(age == personAge + 2 )\n") + "then\n") + "  insert(new Result($plusTwo.getName()));\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Mario", 40));
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Mark", results.iterator().next().getValue());
    }

    @Test
    public void testAgeWithSumUsing2DeclarationInBeta() {
        String str = (((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( personAge : age )\n") + "  $plusTwo : Person(age == personAge + 2 + $p.age - $p.age )\n") + "then\n") + "  insert(new Result($plusTwo.getName()));\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Mario", 40));
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        System.out.println(results);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Mark", results.iterator().next().getValue());
    }

    @Test
    public void testFunction3() {
        String str = (((((((((((("import " + (Result.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "function Boolean isFortyYearsOld(Person p, Boolean booleanParameter) {\n") + "    return p.getAge() == 40; \n") + "}") + "rule R when\n") + "  $p : Person(isFortyYearsOld(this, true))\n") + "then\n") + "  insert(new Result($p.getName()));\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Person("Mario", 40));
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Mario", results.iterator().next().getValue());
    }

    @Test
    public void testInsertLogical() {
        String str = "rule R when\n" + ((("  Integer()" + "then\n") + "  insertLogical(\"Hello World\");\n") + "end");
        KieSession ksession = getKieSession(str);
        FactHandle fh_47 = ksession.insert(47);
        ksession.fireAllRules();
        Collection<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertTrue(results.contains("Hello World"));
        ksession.delete(fh_47);
        ksession.fireAllRules();
        results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertFalse(results.contains("Hello World"));
    }

    @Test
    public void testModifyRewriteAvoidTwiceThePreceeding() {
        String str = ((((((((((("import " + (Person.class.getCanonicalName())) + ";") + "global java.util.List globalA \n") + "global java.util.List globalB \n") + "rule R \n") + "when\n") + "  $p : Person()") + "then\n") + "  globalA.add(\"A\");\n") + "  modify( $p ) { setAge(47); }\n") + "  globalB.add(\"B\");\n") + "end";
        KieSession ksession = getKieSession(str);
        List globalA = new ArrayList<>();
        List globalB = new ArrayList<>();
        ksession.setGlobal("globalA", globalA);
        ksession.setGlobal("globalB", globalB);
        ksession.insert(new Person("person1"));
        ksession.fireAllRules();
        Assert.assertEquals(1, globalA.size());
        Assert.assertEquals(1, globalB.size());
    }

    @Test
    public void testEmptyModifyRewrite() {
        String str = "rule R \n" + (((((("no-loop \n" + "when\n") + "  $s : String()") + "then\n") + "  System.out.println(\"intentional empty modify on $s\" + $s);") + "  modify( $s ) { }\n") + "end");
        KieSession ksession = getKieSession(str);
        ksession.insert("Hello World");
        int fired = ksession.fireAllRules();
        Assert.assertEquals(1, fired);
    }

    @Test
    public void testModifyRewriteWithComments() {
        String str = ((((((((((((((((("import " + (Person.class.getCanonicalName())) + ";") + "global java.util.List globalA \n") + "global java.util.List globalB \n") + "rule R \n") + "when\n") + "  $p : Person()") + "then\n") + "  globalA.add(\"A\");\n") + "  modify( $p ) {\n") + "    // modify ; something\n") + "    /* modify ; something */\n") + "    setAge(47)\n") + "  }\n") + "  globalB.add(\"B\");\n") + "  // modify ; something\n") + "  /* modify ; something */\n") + "end";
        KieSession ksession = getKieSession(str);
        List globalA = new ArrayList<>();
        List globalB = new ArrayList<>();
        ksession.setGlobal("globalA", globalA);
        ksession.setGlobal("globalB", globalB);
        Person person1 = new Person("person1");
        ksession.insert(person1);
        ksession.fireAllRules();
        Assert.assertEquals(1, globalA.size());
        Assert.assertEquals(1, globalB.size());
        Assert.assertEquals(47, person1.getAge());
    }

    @Test
    public void testConstraintContainingAMethodCallWithParams() {
        String str = ((((((("import " + (Overloaded.class.getCanonicalName())) + ";") + "rule OverloadedMethods\n") + "when\n") + "  o : Overloaded( method(5, 9, \"x\") == 15 )\n") + "then\n") + "  insert(\"matched\");\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(new Overloaded());
        ksession.fireAllRules();
        Collection<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testSimpleModifyUsingNameRefFollowedByMethodCall() {
        String str = (((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( name.length() == 4 )\n") + "then\n") + "  modify($p) { setAge($p.getAge()+1) }\n") + "end";
        KieSession ksession = getKieSession(str);
        Person mark = new Person("Mark", 37);
        Person mario = new Person("Mario", 40);
        ksession.insert(mark);
        ksession.insert(mario);
        ksession.fireAllRules();
        Assert.assertEquals(38, mark.getAge());
        Assert.assertEquals(40, mario.getAge());
    }

    @Test
    public void testChainOfMethodCallInConstraint() {
        String str = (((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( getAddress().getCity().length() == 5 )\n") + "then\n") + "  insert(\"matched\");\n") + "end";
        KieSession ksession = getKieSession(str);
        Person john = new Person("John", 47);
        Address a = new Address("Italy");
        john.setAddress(a);
        ksession.insert(john);
        ksession.fireAllRules();
        Collection<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testChainOfMethodCallInConstraintSub() {
        String str = (((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( address.(city.startsWith(\"I\") &&  city.length() == 5  ) )\n")// DRL feature "Grouped accessors for nested objects" is addressed by the RuleDescr directly.
         + "then\n") + "  insert(\"matched\");\n") + "end";
        KieSession ksession = getKieSession(str);
        Person john = new Person("John", 47);
        Address a = new Address("Italy");
        john.setAddress(a);
        ksession.insert(john);
        ksession.fireAllRules();
        Collection<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testChainFieldAccessorsAndMethodCall() {
        String str = (((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( address.getCity().length == 5 )\n") + "then\n") + "  insert(\"matched\");\n") + "end";
        KieSession ksession = getKieSession(str);
        Person john = new Person("John", 47);
        Address a = new Address("Italy");
        john.setAddress(a);
        ksession.insert(john);
        ksession.fireAllRules();
        Collection<String> results = BaseModelTest.getObjectsIntoList(ksession, String.class);
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testInnerBindingWithOr() {
        String str = "global java.util.List list\n" + ((((("\n" + "rule R when\n") + " s: String( s.toString() == \"x\" || s.toString() == \"y\" )\n") + "then\n") + " list.add(s);\n") + "end");
        KieSession ksession = getKieSession(str);
        List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);
        ksession.insert("y");
        ksession.fireAllRules();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("y", list.get(0));
    }

    @Test
    public void testRHS() {
        String str = "rule R when\n" + ((((((((("        //conditions\n" + "    then\n") + "        drools.halt();\n") + "        drools.getWorkingMemory();\n") + "        drools.setFocus(\"agenda-group\");\n") + "        drools.getRule();\n") + "        drools.getTuple();\n") + "        System.out.println(kcontext);\n") + "        kcontext.getKnowledgeRuntime();\n") + "end");
        KieSession ksession = getKieSession(str);
    }

    @Test
    public void testBindWith2Arguments() {
        String str = (((((((((((("import " + (Adult.class.getCanonicalName())) + ";\n") + "import ") + (Child.class.getCanonicalName())) + ";\n") + "import ") + (Result.class.getCanonicalName())) + ";\n") + "rule R when\n") + "  $y : Adult( $sum : (name.length + age) )\n") + "then\n") + "  insert(new Result($sum));\n") + "end";
        KieSession ksession = getKieSession(str);
        Adult a = new Adult("Mario", 43);
        ksession.insert(a);
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(((Number) (results.iterator().next().getValue())).intValue(), 48);
    }

    @Test
    public void testLockOnActiveWithModify() {
        String str = ((((((((((((((((((((((("package org.drools.test; \n" + "import ") + (Person.class.getCanonicalName())) + ";\n") + "rule \"Rule1\" \n") + "@Propagation(EAGER) \n") + "salience 1 \n") + "lock-on-active true\n") + "when\n") + "  $p: Person()\n") + "then\n") + "  System.out.println( \"Rule1\" ); \n") + "  modify( $p ) { setAge( 44 ); }\n") + "end;\n") + "\n") + "rule \"Rule2\"\n") + "@Propagation(EAGER) \n") + "lock-on-active true\n") + "when\n") + "  $p: Person() \n") + "  String() from $p.getName() \n") + "then\n") + "  System.out.println( \"Rule2\" + $p ); ") + "  modify ( $p ) { setName( \"john\" ); } \n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.fireAllRules();
        Person p = new Person("mark", 76);
        ksession.insert(p);
        ksession.fireAllRules();
        Assert.assertEquals(44, p.getAge());
        Assert.assertEquals("john", p.getName());
    }

    @Test
    public void testAlphaConstraintOn2Properties() {
        String str = ((((("import " + (Person.class.getCanonicalName())) + ";") + "rule R when\n") + "  $p : Person( age > name.length )\n") + "then\n") + "end";
        KieSession ksession = getKieSession(str);
        Person me = new Person("Mario", 40);
        ksession.insert(me);
        ksession.fireAllRules();
    }

    @Test
    public void testAlphaNull() {
        String str = ((((((((((((((((((("import " + (Person.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "rule R1 when\n") + "  $p : Person( name == null)\n") + "then\n") + "  insert(new Result($p.getName()));\n") + "end\n") + "rule R2 when\n") + "  $p : Person(  name == \"Luca\")\n") + "then\n") + "  insert(new Result($p.getName()));\n") + "end\n") + "rule R3 when\n") + "  $p : Person(  name == \"Pippo\")\n") + "then\n") + "  insert(new Result($p.getName()));\n") + "end";
        KieSession ksession = getKieSession(str);
        Person first = new Person(null, 40);
        Person second = new Person("Luca", 40);
        Person third = new Person("Mario", 40);
        ksession.insert(first);
        ksession.insert(second);
        ksession.insert(third);
        ksession.fireAllRules();
        List<Object> results = BaseModelTest.getObjectsIntoList(ksession, Result.class).stream().map(Result::getValue).collect(Collectors.toList());
        Assert.assertEquals(2, results.size());
        Assertions.assertThat(results).containsExactlyInAnyOrder("Luca", null);
    }

    @Test
    public void testAlphaNullBoolean() {
        String str = ((((((((("import " + (Person.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "rule R1 when\n") + "  $p : Person( employed == true)\n") + "then\n") + "  insert(new Result($p.getName()));\n") + "end\n";
        KieSession ksession = getKieSession(str);
        Person first = new Person("First", 40);
        first.setEmployed(null);
        Person second = new Person("Second", 40);
        second.setEmployed(true);
        ksession.insert(first);
        ksession.insert(second);
        ksession.fireAllRules();
        List<Object> results = BaseModelTest.getObjectsIntoList(ksession, Result.class).stream().map(Result::getValue).collect(Collectors.toList());
        Assert.assertEquals(1, results.size());
        Assertions.assertThat(results).containsExactlyInAnyOrder("Second");
    }

    @Test
    public void testStringValueOf() {
        String str = (((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import ") + (Result.class.getCanonicalName())) + ";\n") + "rule R when\n") + "  Integer( $i : intValue )\n") + "  Person( name == (String.valueOf($i)) )\n") + "then\n") + "  insert(new Result($i));\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.insert(44);
        ksession.insert(new Person("44", 44));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(((Number) (results.iterator().next().getValue())).intValue(), 44);
    }

    @Test
    public void testBigDecimalBigIntegerCoercion() {
        String str = ((((((((((((((((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import ") + (BigInteger.class.getCanonicalName())) + ";\n") + "rule \"rule1\"\n") + "when\n") + "    Person( money == new BigInteger( \"1\" ) )\n") + "then\n") + "end\n") + "rule \"rule2\"\n") + "when\n") + "    Person( money == new BigInteger( \"2\" ) )\n") + "then\n") + "end\n") + "rule \"rule3\"\n") + "when\n") + "    Person( money == new BigInteger( \"3\" ) )\n") + "then\n") + "end\n";
        KieSession ksession1 = getKieSession(str);
        Person p1 = new Person();
        p1.setMoney(new BigDecimal(1));
        ksession1.insert(p1);
        Assert.assertEquals(1, ksession1.fireAllRules());
    }

    @Test
    public void testSingleQuoteString() {
        String str = "rule R1 when\n" + (((((("  String( this == \'x\' )\n" + "then\n") + "end\n") + "rule R2 when\n") + "  String( this == \'xx\' )\n") + "then\n") + "end");
        KieSession ksession = getKieSession(str);
        ksession.insert("x");
        ksession.insert("xx");
        Assert.assertEquals(2, ksession.fireAllRules());
    }

    @Test
    public void testIntToLongComparison() {
        String str = "rule R when\n" + ((("    $i : Integer()\n" + "    $l : Long( this > $i )\n") + "then\n") + "end");
        KieSession ksession = getKieSession(str);
        ksession.insert(1);
        ksession.insert(2L);
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testUseGlobalInLHS() {
        // DROOLS-1025
        final String drl1 = ((((((("import " + (Result.class.getCanonicalName())) + ";\n") + "global java.util.concurrent.atomic.AtomicInteger globalInt\n") + "rule R1 when\n") + "\t exists Integer() from globalInt.get()\n") + "then\n") + "  insert(new Result(\"match\"));\n") + "end\n";
        KieSession ksession = getKieSession(drl1);
        ksession.setGlobal("globalInt", new AtomicInteger(0));
        ksession.fireAllRules();
        Collection<Result> results = BaseModelTest.getObjectsIntoList(ksession, Result.class);
        Assert.assertEquals(results.iterator().next().getValue().toString(), "match");
    }

    @Test
    public void testMapAccess() {
        final String drl1 = "import java.util.Map;\n" + ((("rule R1 when\n" + "\t Map(this[\'type\'] == \'Goods\' )\n") + "then\n") + "end\n");
        KieSession ksession = getKieSession(drl1);
        final Map<String, Object> map = new HashMap<>();
        map.put("type", "Goods");
        ksession.insert(map);
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testMapAccessProperty() {
        final String drl1 = (((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import java.util.Map;\n") + "rule R1 when\n") + "   Person( items[1] == 2000 )") + "then\n") + "end\n";
        KieSession ksession = getKieSession(drl1);
        final Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 2000);
        map.put(2, 2000);
        final Person luca = new Person("Luca");
        luca.setItems(map);
        ksession.insert(luca);
        final Person mario = new Person("Mario");
        ksession.insert(mario);
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testMapWithBinding() {
        // DROOLS-3558
        final String drl1 = ((((((((((("package org.drools.compiler\n" + "import ") + (Person.class.getCanonicalName())) + ";\n") + "import ") + (Address.class.getCanonicalName())) + ";\n") + "rule R1\n") + "    when\n") + "        $p : Person()\n") + "        $a : Address( number == $p.items[1] )\n") + "    then\n") + "end\n";
        KieSession ksession = getKieSession(drl1);
        final Person john = new Person("John");
        HashMap<Integer, Integer> items = new HashMap<Integer, Integer>();
        items.put(1, 20);
        john.setItems(items);
        items.values().iterator().next();
        ksession.insert(john);
        final Address address = new Address("Tasman", 20, "Nelson");
        ksession.insert(address);
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testMapAccessPropertyWithCast() {
        final String drl1 = (((((("import " + (Person.class.getCanonicalName())) + ";\n") + "import java.util.Map;\n") + "rule R1 when\n") + "   Person( items[(Integer) 1] == 2000 )") + "then\n") + "end\n";
        KieSession ksession = getKieSession(drl1);
        final Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 2000);
        map.put(2, 2000);
        final Person luca = new Person("Luca");
        luca.setItems(map);
        ksession.insert(luca);
        final Person mario = new Person("Mario");
        ksession.insert(mario);
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testArrayAccess() {
        final String drl = ((((((((("package org.drools.compiler.test\n" + "import ") + (Person.class.getCanonicalName())) + "\n") + "global java.util.List list\n") + "rule test1\n") + "when\n") + "   $p1  : Person($name : name )\n") + "   Person(addresses[0].street == $p1.name)\n")// indexed
         + "then\n") + "end\n";
        KieSession ksession = getKieSession(drl);
        Assert.assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testInOperators() {
        final String drl1 = ((((((("package org.drools.compiler\n" + "import ") + (Person.class.getCanonicalName())) + ";\n") + "rule \"eval rewrite with \'in\'\"\n") + "    when\n") + "        $p : Person( age in ( 1, (1 + 1) ))\n") + "    then\n") + "end\n";
        KieSession ksession = getKieSession(drl1);
        final Person luca = new Person("Luca");
        luca.setAge(2);
        ksession.insert(luca);
        final Person mario = new Person("Mario");
        mario.setAge(12);
        ksession.insert(mario);
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    public static class TestFact {
        private String aBcde;

        public TestFact(String aBcde) {
            this.aBcde = aBcde;
        }

        public String getaBcde() {
            return aBcde;
        }

        public void setaBcde(String aBcde) {
            this.aBcde = aBcde;
        }
    }

    @Test
    public void testGetterSetterCase() {
        // DROOLS-2724
        final String drl = (((((("import " + (CompilerTest.TestFact.class.getCanonicalName())) + ";\n") + "import java.util.List;\n") + "rule R1\n") + "when \n") + "   TestFact(aBcde == \"test\")\n") + "then end";
        KieSession kieSession = getKieSession(drl);
        kieSession.insert(new CompilerTest.TestFact("test"));
        Assert.assertEquals(1, kieSession.fireAllRules());
    }

    @Test
    public void testCommaInModify() {
        // DROOLS-3505
        final String drl = ((((((("import " + (Person.class.getCanonicalName())) + "\n") + "dialect \"java\"\n") + "rule R1 when\n") + "   $p : Person( name == \"John\" )\n") + "then\n") + "   modify($p) { setAge(1), setLikes(\"bread\"); }\n") + "end\n";
        KieSession kieSession = getKieSession(drl);
        Person john = new Person("John", 24);
        kieSession.insert(john);
        Assert.assertEquals(1, kieSession.fireAllRules());
        Assert.assertEquals(john.getAge(), 1);
        Assert.assertEquals(john.getLikes(), "bread");
    }

    public static class Message {
        public static final int HELLO = 0;

        public static final int GOODBYE = 1;

        private String message;

        private int status;

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return this.status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    @Test
    public void testStaticFieldClashingWithClassName() {
        // DROOLS-3560
        final String drl = ((((((((((((((((("import " + (CompilerTest.Message.class.getCanonicalName())) + "\n") + "rule \"Hello World\"\n") + "    when\n") + "        m : Message( status == Message.HELLO, myMessage : message ) \n") + "    then\n") + "        System.out.println( myMessage );\n") + "        m.setMessage( \"Goodbye cruel world\" ); \n") + "        m.setStatus( Message.GOODBYE ); \n") + "        update( m );\n") + "end\n") + "\n") + "rule \"GoodBye\"\n") + "    when\n") + "        Message( status == Message.GOODBYE, myMessage : message ) \n") + "    then\n") + "        System.out.println( myMessage );\n") + "end\n";
        KieSession kieSession = getKieSession(drl);
        CompilerTest.Message message = new CompilerTest.Message();
        message.setMessage("Hi");
        message.setStatus(CompilerTest.Message.HELLO);
        kieSession.insert(message);
        Assert.assertEquals(2, kieSession.fireAllRules());
    }

    @Test
    public void testDoubleModify() {
        // DROOLS-3560
        final String drl = (((((((((((((((((("import " + (CompilerTest.Message.class.getCanonicalName())) + "\n") + "rule \"Hello World\"\n") + "    when\n") + "        m : Message( status == Message.HELLO, myMessage : message ) \n") + "    then\n") + "        System.out.println( myMessage );\n") + "        m.setMessage( \"Goodbye cruel world\" );") + "        update(m); \n") + "        m.setStatus( Message.GOODBYE ); \n") + "        update( m );\n") + "end\n") + "\n") + "rule \"GoodBye\"\n") + "    when\n") + "        Message( status == Message.GOODBYE, myMessage : message ) \n") + "    then\n") + "        System.out.println( myMessage );\n") + "end\n";
        KieSession kieSession = getKieSession(drl);
        CompilerTest.Message message = new CompilerTest.Message();
        message.setMessage("Hi");
        message.setStatus(CompilerTest.Message.HELLO);
        kieSession.insert(message);
        Assert.assertEquals(2, kieSession.fireAllRules());
    }
}

