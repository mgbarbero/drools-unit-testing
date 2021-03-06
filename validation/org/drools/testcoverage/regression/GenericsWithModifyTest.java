/**
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.drools.testcoverage.regression;


import org.drools.testcoverage.common.util.KieBaseTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;


/**
 * Tests generics in RHS with modify - BZ 1142886.
 */
@RunWith(Parameterized.class)
public class GenericsWithModifyTest {
    private static final String DRL = "package defaultKBase;\n" + ((((((("import java.util.Map;\n" + "import java.util.HashMap;\n") + "rule R no-loop when\n") + " $s : String( )\n") + "then\n") + " Map<String,String> a = new HashMap<String,String>();\n") + " modify( $s ) { };\n") + "end\n");

    private KieSession kieSession;

    private final KieBaseTestConfiguration kieBaseTestConfiguration;

    public GenericsWithModifyTest(final KieBaseTestConfiguration kieBaseTestConfiguration) {
        this.kieBaseTestConfiguration = kieBaseTestConfiguration;
    }

    /**
     * Tests generics on rule's RHS with modify.
     */
    @Test
    public void testModifyWithGenericsOnRHS() {
        this.kieSession.insert("1");
        this.kieSession.fireAllRules();
    }
}

