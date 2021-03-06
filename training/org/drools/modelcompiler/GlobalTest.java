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


import java.lang.reflect.Method;
import org.drools.modelcompiler.domain.Person;
import org.drools.modelcompiler.domain.Result;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;


public class GlobalTest extends BaseModelTest {
    public GlobalTest(BaseModelTest.RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testGlobalInConsequence() {
        String str = ((((((((((("package org.mypkg;" + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "global Result globalResult;") + "rule X when\n") + "  $p1 : Person(name == \"Mark\")\n") + "then\n") + " globalResult.setValue($p1.getName() + \" is \" + $p1.getAge());\n") + "end";
        KieSession ksession = getKieSession(str);
        Result result = new Result();
        ksession.setGlobal("globalResult", result);
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Assert.assertEquals("Mark is 37", result.getValue());
    }

    @Test
    public void testGlobalInConstraint() {
        String str = (((((((((((("package org.mypkg;" + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "global java.lang.String nameG;") + "global Result resultG;") + "rule X when\n") + "  $p1 : Person(nameG == name)\n") + "then\n") + " resultG.setValue($p1.getName() + \" is \" + $p1.getAge());\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.setGlobal("nameG", "Mark");
        Result result = new Result();
        ksession.setGlobal("resultG", result);
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Assert.assertEquals("Mark is 37", result.getValue());
    }

    public static class Functions {
        public boolean lengthIs4(String s) {
            return (s.length()) == 4;
        }

        public int length(String s) {
            return s.length();
        }

        public boolean arrayContainsInstanceWithParameters(Object[] array, Object[] parms) throws Exception {
            if ((array.length) == 0) {
                return false;
            }
            for (Object o : array) {
                boolean fullmatch = true;
                for (int i = 0; fullmatch && (i < (parms.length)); i++) {
                    if (((parms[i]) instanceof String) && (parms[i].toString().startsWith("get"))) {
                        String methodName = parms[i].toString();
                        if ((i + 1) >= (parms.length)) {
                            throw new RuntimeException("The parameter list is shorter than expected. Should be pairs of accessor method names and expected values.");
                        }
                        ++i;
                        Class<?> c = o.getClass();
                        Method m = c.getMethod(methodName, ((Class[]) (null)));
                        if (m == null) {
                            throw new RuntimeException((((("The method " + methodName) + " does not exist on class ") + (o.getClass().getName())) + "."));
                        }
                        Object result = m.invoke(o, ((Object[]) (null)));
                        if ((result == null) && ((parms[i]) != null)) {
                            fullmatch = false;
                        } else
                            if ((result != null) && ((parms[i]) == null)) {
                                fullmatch = false;
                            } else
                                if (!(result.equals(parms[i]))) {
                                    fullmatch = false;
                                }


                    }
                }
                if (fullmatch) {
                    return true;
                }
            }
            return false;
        }
    }

    @Test
    public void testGlobalBooleanFunction() {
        String str = ((((((((((((((("package org.mypkg;" + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (GlobalTest.Functions.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "global Functions functions;") + "global Result resultG;") + "rule X when\n") + "  $p1 : Person(functions.lengthIs4(name))\n") + "then\n") + " resultG.setValue($p1.getName() + \" is \" + $p1.getAge());\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.setGlobal("functions", new GlobalTest.Functions());
        Result result = new Result();
        ksession.setGlobal("resultG", result);
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Assert.assertEquals("Mark is 37", result.getValue());
    }

    @Test
    public void testGlobalFunctionOnLeft() {
        String str = ((((((((((((((("package org.mypkg;" + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (GlobalTest.Functions.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "global Functions functions;") + "global Result resultG;") + "rule X when\n") + "  $p1 : Person(functions.length(name) == 4)\n") + "then\n") + " resultG.setValue($p1.getName() + \" is \" + $p1.getAge());\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.setGlobal("functions", new GlobalTest.Functions());
        Result result = new Result();
        ksession.setGlobal("resultG", result);
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Assert.assertEquals("Mark is 37", result.getValue());
    }

    @Test
    public void testGlobalFunctionOnRight() {
        String str = ((((((((((((((("package org.mypkg;" + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (GlobalTest.Functions.class.getCanonicalName())) + ";") + "import ") + (Result.class.getCanonicalName())) + ";") + "global Functions functions;") + "global Result resultG;") + "rule X when\n") + "  $p1 : Person(4 == functions.length(name))\n") + "then\n") + " resultG.setValue($p1.getName() + \" is \" + $p1.getAge());\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.setGlobal("functions", new GlobalTest.Functions());
        Result result = new Result();
        ksession.setGlobal("resultG", result);
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Mario", 40));
        ksession.fireAllRules();
        Assert.assertEquals("Mark is 37", result.getValue());
    }

    public static class Family {
        public Object getPersons() {
            return new Object[]{ new Person("Mario", 44), new Person("Mark", 40) };
        }
    }

    @Test
    public void testComplexGlobalFunction() {
        String str = (((((((((((((("package org.mypkg;" + "import ") + (GlobalTest.Family.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (GlobalTest.Functions.class.getCanonicalName())) + ";") + "global Functions functions;") + "rule X when\n") + "  $f : Family(functions.arrayContainsInstanceWithParameters((Object[])$f.getPersons(),\n") + "              new Object[]{\"getAge\", 40}))\n") + "then\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.setGlobal("functions", new GlobalTest.Functions());
        ksession.insert(new GlobalTest.Family());
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testComplexGlobalFunctionWithShort() {
        String str = (((((((((((((("package org.mypkg;" + "import ") + (GlobalTest.Family.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (GlobalTest.Functions.class.getCanonicalName())) + ";") + "global Functions functions;") + "rule X when\n") + "  $f : Family( eval( true == functions.arrayContainsInstanceWithParameters((Object[])$f.getPersons(),\n") + "              new Object[]{\"getAgeAsShort\", (short)40}) ) )\n") + "then\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.setGlobal("functions", new GlobalTest.Functions());
        ksession.insert(new GlobalTest.Family());
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testComplexGlobalFunctionWithShortEvalOnJoin() {
        String str = ((((((((((((((("package org.mypkg;" + "import ") + (GlobalTest.Family.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (GlobalTest.Functions.class.getCanonicalName())) + ";") + "global Functions functions;") + "rule X when\n") + "  $f : Family()\n") + "  $s : String( eval( true == functions.arrayContainsInstanceWithParameters((Object[])$f.getPersons(),\n") + "              new Object[]{\"getAgeAsShort\", (short)40}) ) )\n") + "then\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.setGlobal("functions", new GlobalTest.Functions());
        ksession.insert(new GlobalTest.Family());
        ksession.insert("test");
        Assert.assertEquals(1, ksession.fireAllRules());
    }

    @Test
    public void testComplexGlobalFunctionWithShortNotFiring() {
        String str = (((((((((((((("package org.mypkg;" + "import ") + (GlobalTest.Family.class.getCanonicalName())) + ";") + "import ") + (Person.class.getCanonicalName())) + ";") + "import ") + (GlobalTest.Functions.class.getCanonicalName())) + ";") + "global Functions functions;") + "rule X when\n") + "  $f : Family( eval( true == functions.arrayContainsInstanceWithParameters((Object[])$f.getPersons(),\n") + "              new Object[]{\"getAgeAsShort\", (short)39}) ) )\n") + "then\n") + "end";
        KieSession ksession = getKieSession(str);
        ksession.setGlobal("functions", new GlobalTest.Functions());
        ksession.insert(new GlobalTest.Family());
        Assert.assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testGlobalOnTypeDeclaration() throws Exception {
        String str = "declare MyObject end\n" + "global MyObject event;";
        KieSession ksession = getKieSession(str);
    }
}

