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
package org.drools.decisiontable;


import DecisionTableInputType.CSV;
import ResourceType.DTABLE;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.drools.compiler.compiler.DecisionTableFactory;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.junit.Assert;
import org.junit.Test;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;


public class ColumnReplaceTest {
    @Test
    public void testAutoFocusToLockOnActiveReplacement() throws FileNotFoundException {
        DecisionTableConfiguration dTableConfiguration = KnowledgeBuilderFactory.newDecisionTableConfiguration();
        dTableConfiguration.setInputType(CSV);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("columnReplaceTest.csv", getClass()), DTABLE, dTableConfiguration);
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors());
            Assert.fail("Knowledge builder cannot compile package!");
        }
        System.out.println(DecisionTableFactory.loadFromInputStream(new FileInputStream(new File("src/test/resources/org/drools/decisiontable/columnReplaceTest.csv")), dTableConfiguration));
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        Assert.assertTrue(isLockOnActive());
        // lock-on-active was not set on autoFocusRule, so it should be by default false
        Assert.assertFalse(isLockOnActive());
        Assert.assertFalse(getAutoFocus());
        // auto-focus was set to be true, so it should be true
        Assert.assertTrue(getAutoFocus());
    }
}

