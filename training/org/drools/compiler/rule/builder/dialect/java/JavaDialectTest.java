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
package org.drools.compiler.rule.builder.dialect.java;


import ResourceType.DRL;
import java.io.StringReader;
import java.util.List;
import org.drools.compiler.Person;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.rule.PredicateConstraint;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.spi.CompiledInvoker;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.PredicateExpression;
import org.junit.Assert;
import org.junit.Test;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;


public class JavaDialectTest {
    @Test
    public void testEvalDetectionInAlphaNode() {
        // Tests evals are generated and executed with Java dialect
        String drl = "";
        drl += "package org.drools.compiler.test\n";
        drl += "import org.drools.compiler.Person\n";
        drl += "global java.util.List list\n";
        drl += "rule test1\n";
        drl += "when\n";
        drl += "   $p1 : Person( eval( name \n != null ), name == ( new String(\"xxx\") ) )\n";
        drl += "then\n";
        drl += "end\n";
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newReaderResource(new StringReader(drl)), DRL);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
        Assert.assertFalse(kbuilder.hasErrors());
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        List<ObjectTypeNode> nodes = getRete().getObjectTypeNodes();
        ObjectTypeNode node = null;
        for (ObjectTypeNode n : nodes) {
            if ((getClassType()) == (Person.class)) {
                node = n;
                break;
            }
        }
        AlphaNode alphanode = ((AlphaNode) (node.getObjectSinkPropagator().getSinks()[0]));
        PredicateConstraint c = ((PredicateConstraint) (alphanode.getConstraint()));
        Assert.assertTrue(((c.getPredicateExpression()) instanceof PredicateExpression));
        Assert.assertTrue(((c.getPredicateExpression()) instanceof CompiledInvoker));
        alphanode = ((AlphaNode) (alphanode.getObjectSinkPropagator().getSinks()[0]));
        AlphaNodeFieldConstraint constraint = alphanode.getConstraint();
        if (constraint instanceof MvelConstraint) {
            FieldValue fieldVal = getField();
            Assert.assertEquals("xxx", fieldVal.getValue());
        }
    }

    @Test
    public void testEvalDetectionInBetaNode() {
        // Tests evals are generated and executed with Java dialect
        String drl = "";
        drl += "package org.drools.compiler.test\n";
        drl += "import org.drools.compiler.Person\n";
        drl += "global java.util.List list\n";
        drl += "rule test1\n";
        drl += "when\n";
        drl += "   $s  : String()\n";
        drl += "   $p1 : Person( eval( name \n != $s ), name == ( new String($s+\"xxx\") ) )\n";
        drl += "then\n";
        drl += "end\n";
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newReaderResource(new StringReader(drl)), DRL);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (kbuilder.hasErrors()) {
            Assert.fail(kbuilder.getErrors().toString());
        }
        Assert.assertFalse(kbuilder.hasErrors());
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        List<ObjectTypeNode> nodes = getRete().getObjectTypeNodes();
        ObjectTypeNode node = null;
        for (ObjectTypeNode n : nodes) {
            if ((getClassType()) == (Person.class)) {
                node = n;
                break;
            }
        }
        BetaNode betaanode = ((BetaNode) (node.getObjectSinkPropagator().getSinks()[0]));
        BetaNodeFieldConstraint[] constraint = betaanode.getConstraints();
        PredicateConstraint c = ((PredicateConstraint) (constraint[0]));
        Assert.assertTrue(((c.getPredicateExpression()) instanceof PredicateExpression));
        Assert.assertTrue(((c.getPredicateExpression()) instanceof CompiledInvoker));
    }
}

