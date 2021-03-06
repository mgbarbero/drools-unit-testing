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


import java.io.Serializable;
import org.drools.compiler.CommonTestMethodBase;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;


public class DroolsTest extends CommonTestMethodBase {
    private static final int NUM_FACTS = 20;

    private static int counter;

    public static class Foo implements Serializable {
        private final int id;

        public Foo(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static class Bar implements Serializable {
        private final int id;

        public Bar(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    @Test
    public void test1() throws Exception {
        String str = "package org.drools.compiler.integrationtests;\n";
        str += ("import " + (DroolsTest.class.getName())) + ";\n";
        str += ("import " + (DroolsTest.class.getName())) + ".Foo;\n";
        str += ("import " + (DroolsTest.class.getName())) + ".Bar;\n";
        str += "rule test\n";
        str += "when\n";
        str += ("      Foo($p : id, id < " + (Integer.toString(DroolsTest.NUM_FACTS))) + ")\n";
        str += "      Bar(id == $p)\n";
        str += "then\n";
        str += "   DroolsTest.incCounter();\n";
        str += "end\n";
        DroolsTest.counter = 0;
        KieBase kbase = loadKnowledgeBaseFromString(str);
        KieSession wm = createKnowledgeSession(kbase);
        for (int i = 0; i < (DroolsTest.NUM_FACTS); i++) {
            wm.insert(new DroolsTest.Foo(i));
            wm.insert(new DroolsTest.Bar(i));
        }
        wm.fireAllRules();
        System.out.println((((DroolsTest.counter) + ":") + ((DroolsTest.counter) == (DroolsTest.NUM_FACTS) ? "passed" : "failed")));
    }
}

