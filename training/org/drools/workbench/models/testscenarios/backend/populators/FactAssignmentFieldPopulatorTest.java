/**
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.workbench.models.testscenarios.backend.populators;


import java.util.HashMap;
import java.util.HashSet;
import org.drools.workbench.models.testscenarios.backend.MatryoshkaDoll;
import org.drools.workbench.models.testscenarios.backend.Mouse;
import org.drools.workbench.models.testscenarios.shared.FactAssignmentField;
import org.drools.workbench.models.testscenarios.shared.FieldData;
import org.junit.Assert;
import org.junit.Test;
import org.kie.soup.project.datamodel.commons.types.ClassTypeResolver;
import org.kie.soup.project.datamodel.commons.types.TypeResolver;


public class FactAssignmentFieldPopulatorTest {
    @Test
    public void testFactAssignmentField() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        TypeResolver typeResolver = new ClassTypeResolver(new HashSet<String>(), classLoader);
        typeResolver.addImport("org.drools.workbench.models.testscenarios.backend.Cheese");
        Mouse mouse = new Mouse();
        FactAssignmentField factAssignmentField = new FactAssignmentField("cheese", "Cheese");
        FactAssignmentFieldPopulator factAssignmentFieldPopulator = new FactAssignmentFieldPopulator(mouse, factAssignmentField, typeResolver);
        factAssignmentFieldPopulator.populate(new HashMap<String, Object>());
        Assert.assertNotNull(mouse.getCheese());
    }

    @Test
    public void testSimpleFields() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        TypeResolver typeResolver = new ClassTypeResolver(new HashSet<String>(), classLoader);
        typeResolver.addImport("org.drools.workbench.models.testscenarios.backend.Cheese");
        Mouse mouse = new Mouse();
        FactAssignmentField factAssignmentField = new FactAssignmentField("cheese", "Cheese");
        factAssignmentField.getFact().getFieldData().add(new FieldData("type", "Best cheddar EVER! (tm)"));
        FactAssignmentFieldPopulator factAssignmentFieldPopulator = new FactAssignmentFieldPopulator(mouse, factAssignmentField, typeResolver);
        factAssignmentFieldPopulator.populate(new HashMap<String, Object>());
        Assert.assertEquals("Best cheddar EVER! (tm)", mouse.getCheese().getType());
    }

    @Test
    public void testMatryoshkaDollSituation() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        TypeResolver typeResolver = new ClassTypeResolver(new HashSet<String>(), classLoader);
        typeResolver.addImport("org.drools.workbench.models.testscenarios.backend.MatryoshkaDoll");
        MatryoshkaDoll matryoshkaDoll = new MatryoshkaDoll();
        FactAssignmentField factAssignmentField = createFactAssignmentField();
        addFactAssignmentFieldIntoFactAssignmentField(factAssignmentField, 5);
        FactAssignmentFieldPopulator factAssignmentFieldPopulator = new FactAssignmentFieldPopulator(matryoshkaDoll, factAssignmentField, typeResolver);
        factAssignmentFieldPopulator.populate(new HashMap<String, Object>());
        Assert.assertNotNull(matryoshkaDoll.getMatryoshkaDoll());
        Assert.assertNotNull(matryoshkaDoll.getMatryoshkaDoll().getMatryoshkaDoll());
        Assert.assertNotNull(matryoshkaDoll.getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll());
        Assert.assertNotNull(matryoshkaDoll.getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll());
        Assert.assertNotNull(matryoshkaDoll.getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll());
        Assert.assertNotNull(matryoshkaDoll.getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll());
        Assert.assertNull(matryoshkaDoll.getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll().getMatryoshkaDoll());
    }
}

