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


import ForceEagerActivationOption.YES;
import ResourceType.DRL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.drools.compiler.Address;
import org.drools.compiler.Person;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.reteoo.PathMemory;
import org.drools.core.reteoo.ReteDumper;
import org.drools.core.reteoo.TerminalNode;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;


public class SegmentMemoryPrototypeTest {
    private static final String DRL = (((((((((((((((((((((((((((((((((((((((((((((("import " + (DynamicRulesChangesTest.class.getCanonicalName())) + "\n ") + "global java.util.List events\n") + "rule \"Raise the alarm when we have one or more fires\"\n") + "when\n") + "    exists DynamicRulesChangesTest.Fire()\n") + "then\n") + "    insert( new DynamicRulesChangesTest.Alarm() );\n") + "    events.add( \"Raise the alarm\" );\n") + "end") + "\n") + "rule \"When there is a fire turn on the sprinkler\"\n") + "when\n") + "    $fire: DynamicRulesChangesTest.Fire($room : room)\n") + "    $sprinkler : DynamicRulesChangesTest.Sprinkler( room == $room, on == false )\n") + "then\n") + "    modify( $sprinkler ) { setOn( true ) };\n") + "    events.add( \"Turn on the sprinkler for room \" + $room.getName() );\n") + "end") + "\n") + "rule \"When the fire is gone turn off the sprinkler\"\n") + "when\n") + "    $room : DynamicRulesChangesTest.Room( )\n") + "    $sprinkler : DynamicRulesChangesTest.Sprinkler( room == $room, on == true )\n") + "    not DynamicRulesChangesTest.Fire( room == $room )\n") + "then\n") + "    modify( $sprinkler ) { setOn( false ) };\n") + "    events.add( \"Turn off the sprinkler for room \" + $room.getName() );\n") + "end") + "\n") + "rule \"Cancel the alarm when all the fires have gone\"\n") + "when\n") + "    not DynamicRulesChangesTest.Fire()\n") + "    $alarm : DynamicRulesChangesTest.Alarm()\n") + "then\n") + "    retract( $alarm );\n") + "    events.add( \"Cancel the alarm\" );\n") + "end") + "\n") + "rule \"Status output when things are ok\"\n") + "when\n") + "    not DynamicRulesChangesTest.Fire()\n") + "    not DynamicRulesChangesTest.Alarm()\n") + "    not DynamicRulesChangesTest.Sprinkler( on == true )\n") + "then\n") + "    events.add( \"Everything is ok\" );\n") + "end";

    @Test
    public void testSegmentMemoryPrototype() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(SegmentMemoryPrototypeTest.DRL.getBytes()), ResourceType.DRL);
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        KieSession ksession = kbase.newKieSession();
        try {
            checkKieSession(ksession);
        } finally {
            ksession.dispose();
        }
        // Create a 2nd KieSession (that will use segment memory prototype) and check that it works as the former one
        KieSession ksession2 = kbase.newKieSession();
        try {
            checkKieSession(ksession2);
        } finally {
            ksession2.dispose();
        }
    }

    @Test
    public void testSessionCache() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(SegmentMemoryPrototypeTest.DRL.getBytes()), ResourceType.DRL);
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        StatefulKnowledgeSessionImpl ksession = ((StatefulKnowledgeSessionImpl) (kbase.newKieSession()));
        try {
            checkKieSession(ksession);
        } finally {
            try {
                ksession.reset();
                checkKieSession(ksession);
            } finally {
                ksession.dispose();
            }
        }
    }

    @Test
    public void testEnsureRiaSegmentCreationUsingPrototypes() {
        // DROOLS-1739
        String str = (((((((((((((((((("import " + (Person.class.getCanonicalName())) + "\n") + "import ") + (Address.class.getCanonicalName())) + "\n") + "import java.util.List;\n") + "\n") + "rule rule1 when\n") + "    $personE : List()\n") + "    Person( ) from $personE\n") + "then end\n") + "\n") + "rule rule2 when\n") + "    $personE : List()\n") + "    Person( $addresses : addresses ) from $personE\n") + "    $address : Address( ) from $addresses\n") + "    not (Address( this != $address ) from $addresses)\n") + "    String(  )\n") + "then end";
        KieBase kbase = new KieHelper().addContent(str, ResourceType.DRL).build();
        KieSessionConfiguration conf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        conf.setOption(YES);
        KieSession ksession = kbase.newKieSession(conf, null);
        try {
            ksession.insert(Arrays.asList(new Person()));
            ksession.insert("test");
            Assert.assertEquals(1, ksession.fireAllRules());
        } finally {
            ksession.dispose();
            try {
                ksession = kbase.newKieSession(conf, null);
                ksession.insert(Arrays.asList(new Person()));
                ksession.insert("test");
                Assert.assertEquals(1, ksession.fireAllRules());
            } finally {
                ksession.dispose();
            }
        }
    }

    @Test
    public void testSessionReset() {
        String str = (((((((((((((((("import " + (Person.class.getCanonicalName())) + "\n") + "import ") + (Address.class.getCanonicalName())) + "\n") + "import java.util.List;\n") + "\n") + "rule R1 when\n") + "    $i : Integer()\n") + "    String( length == $i )\n") + "    Long()\n") + "then end\n") + "rule R2 when\n") + "    $i : Integer()\n") + "    String( length == $i )\n") + "    Boolean()\n") + "then end";
        KieBase kbase = new KieHelper().addContent(str, ResourceType.DRL).build();
        List<TerminalNode> terminalNodes = ReteDumper.collectRete(kbase).stream().filter(TerminalNode.class::isInstance).map(TerminalNode.class::cast).collect(Collectors.toList());
        StatefulKnowledgeSessionImpl ksession = ((StatefulKnowledgeSessionImpl) (kbase.newKieSession()));
        ksession.insert(4);
        ksession.insert(4L);
        ksession.insert(true);
        ksession.insert("test");
        Assert.assertEquals(2, ksession.fireAllRules());
        Assert.assertTrue(terminalNodes.stream().map(ksession::getNodeMemory).map(PathMemory.class::cast).allMatch(PathMemory::isRuleLinked));
        ksession.reset();
        Assert.assertFalse(terminalNodes.stream().map(ksession::getNodeMemory).map(PathMemory.class::cast).anyMatch(PathMemory::isRuleLinked));
        ksession.insert(4);
        ksession.insert(4L);
        ksession.insert("test");
        Assert.assertEquals(1, ksession.fireAllRules());
    }
}

