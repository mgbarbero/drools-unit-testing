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
package org.drools.compiler.test;


import ResourceType.DRL;
import java.util.ArrayList;
import org.drools.compiler.CommonTestMethodBase;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.io.impl.ByteArrayResource;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;


public class PositionalTest extends CommonTestMethodBase {
    @Test
    public void testPositional() {
        String drl = (((((((((("import " + (Man.class.getCanonicalName())) + ";\n") + "\n") + "global java.util.List list;") + "\n") + "rule \"To be or not to be\"\n") + "when\n") + "    $m : Man( \"john\" , 18 , $w ; )\n") + "then\n") + "    list.add($w); ") + "end";
        KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        knowledgeBuilder.add(new ByteArrayResource(drl.getBytes()), DRL);
        System.out.println(knowledgeBuilder.getErrors().toString());
        Assert.assertFalse(knowledgeBuilder.hasErrors());
        InternalKnowledgeBase kBase = KnowledgeBaseFactory.newKnowledgeBase();
        kBase.addPackages(knowledgeBuilder.getKnowledgePackages());
        KieSession kSession = createKnowledgeSession(kBase);
        ArrayList list = new ArrayList();
        kSession.setGlobal("list", list);
        kSession.insert(new Man("john", 18, 84.2));
        kSession.fireAllRules();
        Assert.assertTrue(list.contains(84.2));
    }

    @Test(timeout = 5000)
    public void testPositionalWithNull() {
        // DROOLS-51
        String str = "declare Bean\n" + (((((((((((((((("  value : String\n" + "end\n") + "\n") + "rule \"Init\"\n") + "when\n") + "then\n") + "  insert( new Bean( null ) );\n") + "  insert( \"test\" );\n") + "end\n") + "\n") + "rule \"Bind\"\n") + "when\n") + "  $s : String(  )\n") + "  $b : Bean( null ; )\n") + "then\n") + "  modify ( $b ) { setValue( $s ); }\n") + "end");
        KieBase kbase = loadKnowledgeBaseFromString(str);
        KieSession ksession = kbase.newKieSession();
        Assert.assertEquals(2, ksession.fireAllRules());
    }
}

