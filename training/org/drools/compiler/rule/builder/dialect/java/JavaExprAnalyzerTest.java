/**
 * Copyright 2007 Red Hat, Inc. and/or its affiliates.
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
 *
 * Created on Jul 1, 2007
 */
package org.drools.compiler.rule.builder.dialect.java;


import java.util.HashMap;
import java.util.Set;
import org.antlr.runtime.RecognitionException;
import org.drools.compiler.compiler.BoundIdentifiers;
import org.junit.Assert;
import org.junit.Test;


public class JavaExprAnalyzerTest {
    @Test
    public void testAnalyzeBlock() {
        JavaExprAnalyzer analyzer = new JavaExprAnalyzer();
        String codeBlock = "int x;\n" + (((((((("Cheese cheese = new Cheese();\n" + "for( Iterator it = list.iterator(); it.hasNext(); ) {\n") + "    int shouldNotBeIncluded = 1;\n") + "}\n") + "{\n") + "    String anotherNonTopLevelVar = \"test\";\n") + "}\n") + "double thisIsAGoodVar = 0;\n") + "method();\n");
        try {
            JavaAnalysisResult analysis = analyzer.analyzeBlock(codeBlock, new BoundIdentifiers(new HashMap<String, Class<?>>(), null));
            Set<String> vars = analysis.getLocalVariables();
            Assert.assertEquals(3, vars.size());
            Assert.assertTrue(vars.contains("x"));
            Assert.assertTrue(vars.contains("cheese"));
            Assert.assertTrue(vars.contains("thisIsAGoodVar"));
        } catch (RecognitionException e) {
            e.printStackTrace();
            Assert.fail(("Not supposed to raise exception: " + (e.getMessage())));
        }
    }
}

