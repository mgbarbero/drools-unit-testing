/**
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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


import FieldData.TYPE_COLLECTION;
import FieldData.TYPE_ENUM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.workbench.models.testscenarios.backend.Cheese;
import org.drools.workbench.models.testscenarios.backend.CheeseType;
import org.drools.workbench.models.testscenarios.backend.Cheesery;
import org.drools.workbench.models.testscenarios.backend.MyCollectionWrapper;
import org.drools.workbench.models.testscenarios.backend.OuterFact;
import org.drools.workbench.models.testscenarios.backend.Person;
import org.drools.workbench.models.testscenarios.backend.SqlDateWrapper;
import org.drools.workbench.models.testscenarios.shared.CollectionFieldData;
import org.drools.workbench.models.testscenarios.shared.FactData;
import org.drools.workbench.models.testscenarios.shared.Field;
import org.drools.workbench.models.testscenarios.shared.FieldData;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.soup.project.datamodel.commons.types.TypeResolver;


public class FactPopulatorTest {
    static {
        try {
            Class.forName("org.drools.core.base.mvel.MVELCompilationUnit");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private KieSession workingMemory;

    private Map<String, Object> populatedData;

    private FactPopulator factPopulator;

    @Test
    public void testPopulateFacts() throws Exception {
        FactData factData = new FactData("Person", "p1", Arrays.<Field>asList(new FieldData("name", "mic"), new FieldData("age", "=30 + 3")), false);
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), factData));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("p1"));
        Person person = ((Person) (populatedData.get("p1")));
        Assert.assertEquals("mic", person.getName());
        Assert.assertEquals(33, person.getAge());
    }

    @Test
    public void testPopulateEnum() throws Exception {
        FieldData fieldData = new FieldData("cheeseType", "CheeseType.CHEDDAR");
        fieldData.setNature(TYPE_ENUM);
        FactData factData = new FactData("Cheese", "c1", Arrays.asList(((Field) (fieldData))), false);
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), factData));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("c1"));
        Cheese cheese = ((Cheese) (populatedData.get("c1")));
        Assert.assertEquals(CheeseType.CHEDDAR, cheese.getCheeseType());
    }

    @Test
    public void testPopulateNested() throws Exception {
        TypeResolver typeResolver = getTypeResolver();
        FactData cheeseFactData = new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("type", "cheddar"), new FieldData("price", "42")), false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, cheeseFactData));
        FactData outerFactData = new FactData("OuterFact", "p1", Arrays.<Field>asList(new FieldData("name", "mic"), new FieldData("innerFact", "=c1")), false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, outerFactData));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("c1"));
        Assert.assertTrue(populatedData.containsKey("p1"));
        OuterFact o = ((OuterFact) (populatedData.get("p1")));
        Assert.assertEquals(populatedData.get("c1"), o.getInnerFact());
    }

    @Test
    public void testPopulateNestedWrongOrder() throws Exception {
        TypeResolver typeResolver = getTypeResolver();
        FactData outerFactData = new FactData("OuterFact", "p1", Arrays.<Field>asList(new FieldData("name", "mic"), new FieldData("innerFact", "=c1")), false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, outerFactData));
        FactData cheeseFactData = new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("type", "cheddar"), new FieldData("price", "42")), false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, cheeseFactData));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("c1"));
        Assert.assertTrue(populatedData.containsKey("p1"));
        OuterFact o = ((OuterFact) (populatedData.get("p1")));
        Assert.assertEquals(populatedData.get("c1"), o.getInnerFact());
    }

    @Test
    public void testPopulateFactWithoutFields() throws Exception {
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), new FactData("Cheese", "c1", new ArrayList<Field>(), false)));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("c1"));
        Assert.assertTrue(((populatedData.get("c1")) instanceof Cheese));
    }

    @Test
    public void testPopulateEmptyIntegerField() throws Exception {
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("price", "")), false)));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("c1"));
        Assert.assertTrue(((populatedData.get("c1")) instanceof Cheese));
    }

    @Test
    public void testPopulatingExistingFact() throws Exception {
        Cheese cheese = new Cheese();
        cheese.setType("whee");
        cheese.setPrice(1);
        Map<String, Object> populatedData = new HashMap<String, Object>();
        populatedData.put("x", cheese);
        factPopulator.add(new ExistingFactPopulator(populatedData, getTypeResolver(), new FactData("Cheese", "x", Arrays.<Field>asList(new FieldData("type", null), new FieldData("price", "42")), false)));
        factPopulator.populate();
        Assert.assertEquals("whee", cheese.getType());
        Assert.assertEquals(42, cheese.getPrice());
    }

    @Test
    public void testDateField() throws Exception {
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("type", "cheddar"), new FieldData("usedBy", "10-Jul-2008")), false)));
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), new FactData("OuterFact", "p1", Arrays.<Field>asList(new FieldData("name", "mic"), new FieldData("innerFact", "=c1")), false)));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("c1"));
        Assert.assertTrue(populatedData.containsKey("p1"));
        Cheese c = ((Cheese) (populatedData.get("c1")));
        Assert.assertNotNull(c.getUsedBy());
    }

    @Test
    public void testSQLDateField() throws Exception {
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), new FactData("SqlDateWrapper", "c1", Arrays.<Field>asList(new FieldData("sqlDate", "10-Jul-2008")), false)));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("c1"));
        SqlDateWrapper sqlDateWrapper = ((SqlDateWrapper) (populatedData.get("c1")));
        Assert.assertNotNull(sqlDateWrapper.getSqlDate());
    }

    @Test
    public void testPopulateFactsWithExpressions() throws Exception {
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), new FactData("Cheese", "c1", Arrays.<Field>asList(new FieldData("type", "cheddar"), new FieldData("price", "42")), false)));
        factPopulator.add(new NewFactPopulator(populatedData, getTypeResolver(), new FactData("Cheese", "c2", Arrays.<Field>asList(new FieldData("type", "= c1.type")), false)));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("c1"));
        Assert.assertTrue(populatedData.containsKey("c2"));
        Cheese c = ((Cheese) (populatedData.get("c1")));
        Assert.assertEquals("cheddar", c.getType());
        Assert.assertEquals(42, c.getPrice());
        Cheese c2 = ((Cheese) (populatedData.get("c2")));
        Assert.assertEquals(c.getType(), c2.getType());
    }

    @Test
    public void testPopulateEmptyString() throws Exception {
        Cheese cheese = new Cheese();
        cheese.setType("whee");
        cheese.setPrice(1);
        populatedData.put("x", cheese);
        Assert.assertEquals(1, cheese.getPrice());
        // An empty String is a 'value' as opposed to null
        factPopulator.add(new ExistingFactPopulator(populatedData, getTypeResolver(), new FactData("Cheese", "x", Arrays.<Field>asList(new FieldData("type", ""), new FieldData("price", "42")), false)));
        factPopulator.populate();
        Assert.assertEquals("", cheese.getType());
        Assert.assertEquals(42, cheese.getPrice());
    }

    @Test
    public void testCollectionFieldInFacts() throws Exception {
        TypeResolver typeResolver = getTypeResolver();
        FactData fd1 = new FactData("Cheese", "f1", Arrays.<Field>asList(new FieldData("type", ""), new FieldData("price", "42")), false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, fd1));
        FactData fd2 = new FactData("Cheese", "f2", Arrays.<Field>asList(new FieldData("type", ""), new FieldData("price", "43")), false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, fd2));
        FactData fd3 = new FactData("Cheese", "f3", Arrays.<Field>asList(new FieldData("type", ""), new FieldData("price", "45")), false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, fd3));
        FieldData field = new FieldData();
        field.setName("cheeses");
        field.setNature(TYPE_COLLECTION);
        field.setValue("=[f1,f2,f3]");
        List<Field> lstField = new ArrayList<Field>();
        lstField.add(field);
        FactData lst = new FactData("Cheesery", "listChesse", lstField, false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, lst));
        factPopulator.populate();
        Cheesery listChesse = ((Cheesery) (populatedData.get("listChesse")));
        Cheese f1 = ((Cheese) (populatedData.get("f1")));
        Cheese f2 = ((Cheese) (populatedData.get("f2")));
        Cheese f3 = ((Cheese) (populatedData.get("f3")));
        Assert.assertEquals(3, listChesse.getCheeses().size());
        Assert.assertTrue(listChesse.getCheeses().contains(f1));
        Assert.assertTrue(listChesse.getCheeses().contains(f2));
        Assert.assertTrue(listChesse.getCheeses().contains(f3));
    }

    @Test
    public void testCollection() throws Exception {
        TypeResolver typeResolver = getTypeResolver();
        List<Field> fieldData = new ArrayList<Field>();
        CollectionFieldData collectionFieldData = new CollectionFieldData();
        collectionFieldData.setName("cheeses");
        fieldData.add(collectionFieldData);
        collectionFieldData.getCollectionFieldList().add(new FieldData("cheeses", "=cheese1"));
        collectionFieldData.getCollectionFieldList().add(new FieldData("cheeses", "=cheese2"));
        FactData cheeseryFactData = new FactData("Cheesery", "cheesery", fieldData, false);
        FactData cheeseFactData1 = new FactData("Cheese", "cheese1", Collections.<Field>emptyList(), false);
        FactData cheeseFactData2 = new FactData("Cheese", "cheese2", Collections.<Field>emptyList(), false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, cheeseryFactData));
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, cheeseFactData1));
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, cheeseFactData2));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("cheesery"));
        Cheesery cheesery = ((Cheesery) (populatedData.get("cheesery")));
        Assert.assertNotNull(cheesery);
        Assert.assertEquals(2, cheesery.getCheeses().size());
        Assert.assertNotNull(cheesery.getCheeses().get(0));
        Assert.assertTrue(((cheesery.getCheeses().get(0)) instanceof Cheese));
        Assert.assertNotNull(cheesery.getCheeses().get(1));
        Assert.assertTrue(((cheesery.getCheeses().get(1)) instanceof Cheese));
    }

    @Test
    public void testCollectionSums() throws Exception {
        TypeResolver typeResolver = getTypeResolver();
        List<Field> fieldData = new ArrayList<Field>();
        CollectionFieldData collectionFieldData = new CollectionFieldData();
        collectionFieldData.setName("list");
        fieldData.add(collectionFieldData);
        collectionFieldData.getCollectionFieldList().add(new FieldData("list", "=1+3"));
        FactData wrapperFactData = new FactData("MyCollectionWrapper", "wrapper", fieldData, false);
        factPopulator.add(new NewFactPopulator(populatedData, typeResolver, wrapperFactData));
        factPopulator.populate();
        Assert.assertTrue(populatedData.containsKey("wrapper"));
        MyCollectionWrapper wrapper = ((MyCollectionWrapper) (populatedData.get("wrapper")));
        Assert.assertNotNull(wrapper);
        Assert.assertEquals(1, wrapper.getList().size());
        Assert.assertNotNull(wrapper.getList().get(0));
        Assert.assertEquals(4, wrapper.getList().get(0));
    }
}

