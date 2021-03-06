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
package org.drools.compiler.factmodel.traits;


import ResourceType.DRL;
import java.util.ArrayList;
import java.util.List;
import org.drools.compiler.CommonTestMethodBase;
import org.drools.compiler.ReviseTraitTestWithPRAlwaysCategory;
import org.drools.core.factmodel.traits.TraitFactory;
import org.drools.core.factmodel.traits.VirtualPropertyMode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieBase;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.conf.PropertySpecificOption;


@RunWith(Parameterized.class)
public class TraitFieldsAndLegacyClassesTest extends CommonTestMethodBase {
    public VirtualPropertyMode mode;

    public TraitFieldsAndLegacyClassesTest(VirtualPropertyMode m) {
        this.mode = m;
    }

    @Test
    public void testTraitFieldUpdate0() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits0;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Thing;\n") + "import java.util.*\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Parent;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Child;\n") + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : Child\n") + "    age : int = 24\n") + "end\n") + "declare Parent\n") + "@Traitable\n") + "@propertyReactive\n") + "end\n") + "rule \"Init\" \n") + "\n") + "when\n") + "    \n") + "then\n") + "   Parent p = new Parent(\"parent\", null);\n") + "   Map map = new HashMap();\n") + "   map.put( \"parent\", ParentTrait.class );\n") + "   insert(p);\n") + "   insert(map);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "   $map : HashMap([parent] != null)\n") + "then\n") + "   Object p = don ( $p , (Class) $map.get(\"parent\") );\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitFieldUpdate1() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Trait;\n") + "") + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : ChildTrait\n")// <<<<<<<
         + "    age : int = 24\n") + "end\n") + "declare trait ChildTrait\n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "end\n") + "declare Parent\n") + "@Traitable( logical = true ) \n") + "@propertyReactive\n") + "   name : String\n") + "   child : Child\n") + "end\n") + "declare Child\n") + "@Traitable \n") + "@propertyReactive\n") + "   gender : String = \"male\"\n") + "end\n") + "\n") + "rule \"Init\" \n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\",c);\n") + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "salience -1\n") + "when\n") + "    $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "rule \"trait child\" \n") + "when\n") + "    $c : Child( gender == \"male\" )\n") + "then\n") + "   ChildTrait c = don ( $c , ChildTrait.class );\n") + "end\n") + "\n") + "rule \"test parent and child traits\" \n") + "when\n") + "    $p : ParentTrait( $c : child isA ChildTrait.class )\n") + "then\n") + "   //shed ( $p , ParentTrait.class );\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitFieldUpdate2() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits2;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : ChildTrait \n")// ><><><><><
         + "    age : int = 24\n") + "end\n") + "declare trait ChildTrait\n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "end\n") + "declare Parent\n") + "@Traitable( logical=true )\n")// ><><><><><
         + "@propertyReactive\n") + "   name : String\n") + "   child : Child\n") + "end\n") + "declare Child\n") + "@Traitable\n") + "@propertyReactive\n") + "   gender : String = \"male\"\n") + "end\n") + "\n") + "rule \"Init\" \n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\", null);\n")// <<<<<
         + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "rule \"trait child\" \n") + "when\n") + "   $c : Child( gender == \"male\" )\n") + "then\n") + "   ChildTrait c = don ( $c , ChildTrait.class );\n") + "end\n") + "\n") + "rule \"assign child to parent\" \n")// <<<<<<
         + "when\n") + "   $c : Child( gender == \"male\" )\n") + "   $p : Parent( name == \"parent\" )\n") + "   ParentTrait( child not isA ChildTrait.class )\n") + "   ChildTrait()\n") + "then\n") + "   ") + "   modify ( $p ) { \n") + "       setChild($c);\n") + "   }\n") + "end\n") + "\n") + "rule \"test parent and child traits\" \n") + "when\n") + "    $p : ParentTrait( child isA ChildTrait.class )\n") + "then\n") + "   //shed ( $p , ParentTrait.class );\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        KieSession knowledgeSession = kBase.newKieSession();
        TraitFactory.setMode(mode, kBase);
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitFieldUpdate3() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits3;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : ChildTrait\n") + "    age : int = 24\n") + "end\n") + "declare trait ChildTrait\n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "end\n") + "declare Parent\n") + "@Traitable( logical = true )\n") + "@propertyReactive\n") + "   name : String\n") + "   child : Child\n") + "end\n") + "declare Child\n") + "@Traitable\n") + "@propertyReactive\n") + "   gender : String = \"male\"\n") + "end\n") + "\n") + "rule \"Init\" \n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\", null);\n") + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "rule \"trait child\" \n") + "when\n") + "   $c : Child( gender == \"male\" )\n") + "then\n") + "   ChildTrait c = don ( $c , ChildTrait.class );\n") + "end\n") + "\n") + "rule \"assign child to parent\" \n") + "when\n") + "   Child( gender == \"male\" )\n") + "   $p : Parent( name == \"parent\" )\n") + "   ParentTrait( child not isA ChildTrait.class )\n") + "   $c : ChildTrait()\n")// <<<<<
         + "then\n") + "   $p.setChild((Child)$c.getCore());\n")// <<<<<
         + "   update($p);\n") + "end\n") + "\n") + "rule \"test parent and child traits\" \n") + "when\n") + "    $p : ParentTrait( child isA ChildTrait.class )\n") + "then\n") + "   //shed ( $p , ParentTrait.class );\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Category(ReviseTraitTestWithPRAlwaysCategory.class)
    @Test
    public void testTraitFieldUpdate4() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits4;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : ChildTrait\n") + "    age : int = 24\n") + "end\n") + "declare trait ChildTrait\n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "end\n") + "declare Parent\n") + "@Traitable(logical=true)\n")// <<<<<<   @propertyReactive is removed
         + "   name : String\n") + "   child : Child\n") + "end\n") + "declare Child\n") + "@Traitable\n") + "@propertyReactive\n") + "   gender : String = \"male\"\n") + "end\n") + "\n") + "rule \"Init\" \n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\", c);\n")// <<<<<
         + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait child\" \n") + "when\n") + "   $p : Parent( $c := child not isA ChildTrait )\n") + "   $c := Child( gender == \"male\" )\n") + "then\n") + "   ChildTrait c = don ( $c , ChildTrait.class );\n") + // this modify is necessary to tell the engine that the Parent's Child has gained a type
        // if enabled, "logical" mode traits render this unnecessary
        "   modify ( $p ) {}; \n") + "end\n") + "\n") + "rule \"test parent and a child trait\" \n") + "when\n") + "    $p : Parent( child isA ChildTrait.class ) \n")// <<<<<
         + "then\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = new org.kie.internal.utils.KieHelper(PropertySpecificOption.ALLOWED).addContent(drl, DRL).build();
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitFieldUpdate5() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits5;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Trait;\n") + "global java.util.List list;\n") + "\n") + "") + "") + "declare trait ParentTrait\n") + "") + "@propertyReactive\n") + "    child : ChildTrait\n") + "    age : int = 24\n") + "end\n") + "declare trait ChildTrait\n") + "@Trait(logical=true) \n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "end\n") + "declare Parent\n") + "@Traitable(logical=true)\n") + "   name : String\n") + "   child : Child\n") + "end\n") + "declare Child\n") + "@Traitable\n") + "@propertyReactive\n") + "   gender : String = \"male\"\n") + "end\n") + "\n") + "rule \"Init\" \n") + "\n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\", c);\n") + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "rule \"test parent and child traits\" \n") + "\n") + "when\n") + "    $p : ParentTrait( $c : child isA ChildTrait.class ) \n")// <<<<<
         + "then\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitFieldUpdate6() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits6;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Trait;\n") + "import org.drools.core.factmodel.traits.Thing;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Child;\n")// <<<<<<
         + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : ChildTrait\n") + "    age : int = 24\n") + "end\n") + "declare trait ChildTrait\n") + "@Trait(logical=true) \n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "end\n") + "declare Parent\n") + "@Traitable(logical=true)\n") + "@propertyReactive\n") + "   name : String\n") + "   child : Child\n") + "end\n") + "declare Child\n")// <<<<<
         + "@Traitable(logical=true)\n") + "@propertyReactive\n") + // "   gender : String = \"male\"\n"+
        "end\n") + "\n") + "rule \"Init\" \n") + "\n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\", c);\n") + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "") + "rule \"Side effect\" \n") + "when \n") + "  $p : Parent( child isA ChildTrait ) \n") + "then \n") + "   list.add(\"correct2\");\n") + "end \n") + "rule \"test parent and child traits\" \n") + "\n") + "when\n") + "    $p : ParentTrait( child isA ChildTrait.class )\n") + "then\n") + "   //shed ( $p , ParentTrait.class );\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertTrue(list.contains("correct2"));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testTraitFieldUpdate7() {
        String drl = "" + ((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Trait;\n") + "import org.drools.core.factmodel.traits.Thing;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Child;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Parent;\n")// <<<<<
         + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : ChildTrait  @position(1)\n")// <<<<<
         + "    age : int = 24 @position(0)\n") + "end\n") + "declare trait ChildTrait\n") + "@Trait( logical = true ) \n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "   gender : String\n") + "end\n") + "declare Parent\n") + "@Traitable( logical=true ) \n") + "@propertyReactive\n") + "end\n") + "declare Child\n") + "@Traitable( logical=true ) \n") + "@propertyReactive\n") + "end\n") + "\n") + "rule \"Init\" \n") + "\n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent( \"parent\", c );\n") + "   insert(c); insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "rule \"test parent and child traits\" \n") + "\n") + "when\n") + // "   $c : Child( $gender := gender )\n"+
        "   $p : ParentTrait( child isA ChildTrait )\n")// <<<<<
         + "then\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitFieldUpdate8() {
        String drl = "" + ((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits8;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Thing;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Child;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Parent;\n")// <<<<<
         + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : ChildTrait\n") + "    age : int = 24\n") + "end\n") + "declare trait ChildTrait\n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "end\n") + "declare Parent\n")// <<<<<
         + "@Traitable(logical=true)\n") + "@propertyReactive\n") + // "   name : String\n"+
        // "   child : Child\n"+
        "end\n") + "declare Child\n") + "@Traitable(logical=true)\n") + "@propertyReactive\n") + // "   gender : String = \"male\"\n"+
        "end\n") + "\n") + "rule \"Init\" \n") + "\n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\", c);\n") + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "rule \"test parent and child traits\" \n") + "\n") + "when\n") + "    $p : ParentTrait( child isA ChildTrait.class )\n") + "then\n") + "   //shed ( $p , ParentTrait.class );\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitFieldUpdate9() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits9;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Thing;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Child;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Parent;\n")// <<<<<
         + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : Child\n")// <<<<<
         + "    age : int = 24\n") + "end\n") + "declare trait ChildTrait\n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "end\n") + "declare Parent\n")// <<<<<
         + "@Traitable\n") + "@propertyReactive\n") + "end\n") + "declare Child\n") + "@Traitable\n") + "@propertyReactive\n") + "end\n") + "\n") + "rule \"Init\" \n") + "\n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\", c);\n") + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "rule \"trait and assign the child\" \n") + "\n") + "when\n") + "   $c : Child( gender == \"male\", this not isA ChildTrait )\n") + "   $p : Parent( this isA ParentTrait )\n") + "then\n") + "   ChildTrait c =  don ( $c , ChildTrait.class );\n")// <<<<<<
         + "   modify($p){\n") + "       setChild((Child)c.getCore());}\n") + "end\n") + "\n") + "rule \"test parent and child traits\" \n") + "\n") + "when\n") + "    $p : ParentTrait( child isA ChildTrait.class, child.gender == \"male\" )\n")// <<<<<
         + "then\n") + "   //shed ( $p , ParentTrait.class );\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitFieldUpdate10() {
        String drl = "" + ((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Thing;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Child;\n") + "import org.drools.compiler.factmodel.traits.TraitFieldsAndLegacyClassesTest.Parent;\n")// <<<<<
         + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : Child  @position(1)\n")// <<<<<
         + "    age : int = 24 @position(0)\n") + "end\n") + "declare trait ChildTrait\n") + "@propertyReactive\n") + "   name : String = \"child\"\n") + "   gender : String\n")// <<<<<
         + "end\n") + "declare Parent\n")// <<<<<
         + "@Traitable\n") + "@propertyReactive\n") + "end\n") + "declare Child\n") + "@Traitable\n") + "@propertyReactive\n") + "end\n") + "\n") + "rule \"Init\" \n") + "\n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child();\n") + "   Parent p = new Parent(\"parent\", c);\n") + "   insert(c);insert(p);\n") + "end\n") + "\n") + "rule \"trait parent\" \n") + "\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait p = don ( $p , ParentTrait.class );\n") + "end\n") + "\n") + "rule \"trait and assign the child\" \n") + "\n") + "when\n") + "   $c : Child( gender == \"male\", this not isA ChildTrait )\n") + "   $p : Parent( this isA ParentTrait )\n") + "then\n") + "   ChildTrait c =  don ( $c , ChildTrait.class );\n")// <<<<<<
         + "   modify($p){\n") + "       setChild((Child)c.getCore());}\n") + "end\n") + "\n") + "rule \"test parent and child traits\" salience 10\n") + "\n") + "when\n") + "   $c : Child( $gender := gender)\n") + "   $p : ParentTrait( $age, $c; )\n")// <<<<<
         + "then\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testTraitTwoParentOneChild() {
        String drl = "" + (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("package org.drools.factmodel.traits;\n" + "\n") + "import org.drools.core.factmodel.traits.Traitable;\n") + "import org.drools.core.factmodel.traits.Thing;\n") + "global java.util.List list;\n") + "\n") + "declare trait ParentTrait\n") + "@propertyReactive\n") + "    child : Child  \n") + "    age : int = 24 \n") + "end\n") + "\n") + "declare trait GrandParentTrait\n")// <<<<
         + "@propertyReactive\n") + "    grandChild : Child \n") + "    age : int = 64 \n") + "end\n") + "declare trait FatherTrait extends ParentTrait, GrandParentTrait \n")// <<<<<
         + "@propertyReactive\n") + "   name : String = \"child\"\n") + "   gender : String\n") + "end\n") + "declare Parent\n") + "@Traitable\n") + "@propertyReactive\n") + "   name : String\n") + "   child : Child\n") + "end\n") + "declare Child\n") + "@Traitable\n") + "@propertyReactive\n") + "   name : String\n") + "   gender : String = \"male\"\n") + "end\n") + "\n") + "rule \"Init\" \n") + "\n") + "when\n") + "    \n") + "then\n") + "   Child c = new Child(\"C1\",\"male\");\n") + "   Child c2 = new Child(\"C2\",\"male\");\n")// <<<<
         + "   Parent p = new Parent(\"parent\", c);\n") + "   insert(c);insert(p);\n") + "   insert(c2);\n") + "end\n") + "\n") + "rule \"trait as father\" \n") + "salience -1000\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   FatherTrait p = don ( $p , FatherTrait.class );\n") + "end\n") + "\n") + "rule \"trait as parent\" \n") + "\n") + "when\n") + "   $p : Parent( name == \"parent\" )\n") + "then\n") + "   ParentTrait c =  don ( $p , ParentTrait.class );\n")// <<<<<<
         + "end\n") + "\n") + "rule \"trait and assign the grandchild\" \n") + "\n") + "when\n") + "   $c : Child( name == \"C1\" )\n") + "   $p : Parent( child == $c )\n") + "then\n") + "   GrandParentTrait c =  don ( $p , GrandParentTrait.class );\n")// <<<<<<
         + "   modify(c){\n") + "       setGrandChild( $c );}\n") + "end\n") + "\n") + "rule \"test three traits\" \n") + "\n") + "when\n") + "   $p : FatherTrait( this isA ParentTrait, this isA GrandParentTrait )\n")// <<<<<
         + "then\n") + "   list.add(\"correct\");\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession knowledgeSession = kBase.newKieSession();
        List list = new ArrayList();
        knowledgeSession.setGlobal("list", list);
        knowledgeSession.fireAllRules();
        Assert.assertTrue(list.contains("correct"));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void singlePositionTraitTest() {
        String drl = "" + (((((((((((((((((((((((("package org.drools.traits.test;\n" + "import org.drools.core.factmodel.traits.Traitable;\n") + "\n") + "global java.util.List list;\n") + "\n") + "\n") + "declare Pos\n") + "@propertyReactive\n") + "@Traitable\n") + "end\n") + "\n") + "declare trait PosTrait\n") + "@propertyReactive\n") + "    field0 : int = 100  //@position(0)\n") + "    field1 : int = 101  //@position(1)\n") + "    field2 : int = 102  //@position(0)\n") + "end\n") + "\n") + "declare trait MultiInhPosTrait extends PosTrait\n") + "@propertyReactive\n") + "    mfield0 : int = 200 //@position(0)\n") + "    mfield1 : int = 201 @position(2)\n") + "end\n") + "\n") + "\n");
        KieBase kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        KieSession kSession = kBase.newKieSession();
        FactType parent = kBase.getFactType("org.drools.traits.test", "PosTrait");
        Assert.assertEquals(0, getIndex());
        Assert.assertEquals(1, getIndex());
        Assert.assertEquals(2, getIndex());
        FactType child = kBase.getFactType("org.drools.traits.test", "MultiInhPosTrait");
        Assert.assertEquals(0, getIndex());
        Assert.assertEquals(1, getIndex());
        Assert.assertEquals(2, getIndex());
        Assert.assertEquals(3, getIndex());
        Assert.assertEquals(4, getIndex());
        drl = "" + (((((((((((((((((((((((("package org.drools.traits.test;\n" + "import org.drools.core.factmodel.traits.Traitable;\n") + "\n") + "global java.util.List list;\n") + "\n") + "\n") + "declare Pos\n") + "@propertyReactive\n") + "@Traitable\n") + "end\n") + "\n") + "declare trait PosTrait\n") + "@propertyReactive\n") + "    field0 : int = 100  //@position(0)\n") + "    field1 : int = 101  //@position(1)\n") + "    field2 : int = 102  @position(1)\n") + "end\n") + "\n") + "declare trait MultiInhPosTrait extends PosTrait\n") + "@propertyReactive\n") + "    mfield0 : int = 200 @position(0)\n") + "    mfield1 : int = 201 @position(2)\n") + "end\n") + "\n") + "\n");
        kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        parent = kBase.getFactType("org.drools.traits.test", "PosTrait");
        Assert.assertEquals(0, getIndex());
        Assert.assertEquals(1, getIndex());
        Assert.assertEquals(2, getIndex());
        child = kBase.getFactType("org.drools.traits.test", "MultiInhPosTrait");
        Assert.assertEquals(0, getIndex());
        Assert.assertEquals(1, getIndex());
        Assert.assertEquals(2, getIndex());
        Assert.assertEquals(3, getIndex());
        Assert.assertEquals(4, getIndex());
        drl = "" + (((((((((((((((((((((((("package org.drools.traits.test;\n" + "import org.drools.core.factmodel.traits.Traitable;\n") + "\n") + "global java.util.List list;\n") + "\n") + "\n") + "declare Pos\n") + "@propertyReactive\n") + "@Traitable\n") + "end\n") + "\n") + "declare trait PosTrait\n") + "@propertyReactive\n") + "    field0 : int = 100  @position(5)\n") + "    field1 : int = 101  @position(0)\n") + "    field2 : int = 102  @position(1)\n") + "end\n") + "\n") + "declare trait MultiInhPosTrait extends PosTrait\n") + "@propertyReactive\n") + "    mfield0 : int = 200 @position(0)\n") + "    mfield1 : int = 201 @position(1)\n") + "end\n") + "\n") + "\n");
        kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        parent = kBase.getFactType("org.drools.traits.test", "PosTrait");
        Assert.assertEquals(0, getIndex());
        Assert.assertEquals(1, getIndex());
        Assert.assertEquals(2, getIndex());
        child = kBase.getFactType("org.drools.traits.test", "MultiInhPosTrait");
        Assert.assertEquals(0, getIndex());
        Assert.assertEquals(1, getIndex());
        Assert.assertEquals(2, getIndex());
        Assert.assertEquals(3, getIndex());
        Assert.assertEquals(4, getIndex());
        drl = "" + (((((((((((((((((((((((("package org.drools.traits.test;\n" + "import org.drools.core.factmodel.traits.Traitable;\n") + "\n") + "global java.util.List list;\n") + "\n") + "\n") + "declare Pos\n") + "@propertyReactive\n") + "@Traitable\n") + "end\n") + "\n") + "declare trait PosTrait\n") + "@propertyReactive\n") + "    field0 : int = 100  //@position(5)\n") + "    field1 : int = 101  //@position(0)\n") + "    field2 : int = 102  //@position(1)\n") + "end\n") + "\n") + "declare trait MultiInhPosTrait extends PosTrait\n") + "@propertyReactive\n") + "    mfield0 : int = 200 //@position(0)\n") + "    mfield1 : int = 201 //@position(1)\n") + "end\n") + "\n") + "\n");
        kBase = loadKnowledgeBaseFromString(drl);
        TraitFactory.setMode(mode, kBase);
        parent = kBase.getFactType("org.drools.traits.test", "PosTrait");
        Assert.assertEquals(0, getIndex());
        Assert.assertEquals(1, getIndex());
        Assert.assertEquals(2, getIndex());
        child = kBase.getFactType("org.drools.traits.test", "MultiInhPosTrait");
        Assert.assertEquals(0, getIndex());
        Assert.assertEquals(1, getIndex());
        Assert.assertEquals(2, getIndex());
        Assert.assertEquals(3, getIndex());
        Assert.assertEquals(4, getIndex());
    }

    public static class Parent {
        public String name;

        public TraitFieldsAndLegacyClassesTest.Child child;

        public String getName() {
            return name;
        }

        public TraitFieldsAndLegacyClassesTest.Child getChild() {
            return child;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setChild(TraitFieldsAndLegacyClassesTest.Child child) {
            this.child = child;
        }

        public Parent(String name, TraitFieldsAndLegacyClassesTest.Child child) {
            this.name = name;
            this.child = child;
        }

        @Override
        public String toString() {
            return ((((("Parent{" + "name='") + (name)) + '\'') + ", child=") + (child)) + '}';
        }
    }

    public static class Child {
        private String gender = "male";

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        @Override
        public String toString() {
            return ((("Child{" + "gender='") + (gender)) + '\'') + '}';
        }
    }
}

