/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.drools.workbench.models.guided.dtable.shared.model;


import java.util.List;
import org.drools.workbench.models.datamodel.workitems.PortableStringParameterDefinition;
import org.drools.workbench.models.datamodel.workitems.PortableWorkDefinition;
import org.junit.Assert;
import org.junit.Test;


public class ActionWorkItemCol52Test extends ColumnTestBase {
    private ActionWorkItemCol52 column1;

    private ActionWorkItemCol52 column2;

    @Test
    public void testDiffEmpty() {
        checkDiffEmpty(column1, column2);
    }

    @Test
    public void testWorkItemDefinition_SimpleAndNull() {
        PortableWorkDefinition def1 = new PortableWorkDefinition();
        def1.setName("def1name");
        column1.setWorkItemDefinition(def1);
        column2.setWorkItemDefinition(null);
        checkSingleDiff(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_NAME, "def1name", null, column1, column2);
    }

    @Test
    public void testWorkItemDefinition_ParamAndNull() {
        PortableWorkDefinition def1 = new PortableWorkDefinition();
        def1.setName("def1name");
        PortableStringParameterDefinition param1 = new PortableStringParameterDefinition();
        param1.setName("param1");
        param1.setValue("value1");
        def1.addParameter(param1);
        column1.setWorkItemDefinition(null);
        column2.setWorkItemDefinition(def1);
        List<BaseColumnFieldDiff> diff = column1.diff(column2);
        Assert.assertNotNull(diff);
        Assert.assertEquals(3, diff.size());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_NAME, diff.get(0).getFieldName());
        Assert.assertEquals(null, diff.get(0).getOldValue());
        Assert.assertEquals("def1name", diff.get(0).getValue());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_PARAMETER_NAME, diff.get(1).getFieldName());
        Assert.assertEquals(null, diff.get(1).getOldValue());
        Assert.assertEquals("param1", diff.get(1).getValue());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_PARAMETER_VALUE, diff.get(2).getFieldName());
        Assert.assertEquals(null, diff.get(2).getOldValue());
        Assert.assertEquals("\"value1\"", diff.get(2).getValue());
    }

    @Test
    public void testWorkItemDefinition_TwoComplexDefs() {
        PortableStringParameterDefinition param1 = new PortableStringParameterDefinition();
        param1.setName("param1");
        param1.setValue("value1");
        PortableStringParameterDefinition param2 = new PortableStringParameterDefinition();
        param2.setName("param2");
        param2.setValue("value2");
        PortableWorkDefinition def1 = new PortableWorkDefinition();
        def1.setName("def1name");
        def1.addParameter(param1);
        def1.addParameter(param2);
        PortableStringParameterDefinition param3 = new PortableStringParameterDefinition();// different param -> diff + 2

        param3.setName("param3");
        param3.setValue("value3");
        PortableStringParameterDefinition param4 = new PortableStringParameterDefinition();// same as param1

        param4.setName("param1");
        param4.setValue("value1");
        PortableStringParameterDefinition param5 = new PortableStringParameterDefinition();// different param -> diff + 2

        param5.setName("param5");
        param5.setBinding("binding5");
        PortableStringParameterDefinition param6 = new PortableStringParameterDefinition();// as param2, different value -> diff + 1

        param6.setName("param2");
        param6.setValue("value6");
        PortableWorkDefinition def2 = new PortableWorkDefinition();
        def2.setName("def2name");
        def2.addParameter(param3);
        def2.addParameter(param4);
        def2.addParameter(param5);
        def2.addParameter(param6);
        column1.setWorkItemDefinition(def1);
        column2.setWorkItemDefinition(def2);// different definition -> diff + 1

        List<BaseColumnFieldDiff> diff = column1.diff(column2);
        Assert.assertNotNull(diff);
        for (BaseColumnFieldDiff item : diff) {
            System.out.println(item.getFieldName());
            System.out.println(item.getOldValue());
            System.out.println(item.getValue());
        }
        Assert.assertEquals(6, diff.size());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_NAME, diff.get(0).getFieldName());
        Assert.assertEquals("def1name", diff.get(0).getOldValue());
        Assert.assertEquals("def2name", diff.get(0).getValue());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_PARAMETER_NAME, diff.get(1).getFieldName());
        Assert.assertEquals(null, diff.get(1).getOldValue());
        Assert.assertEquals("param3", diff.get(1).getValue());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_PARAMETER_VALUE, diff.get(2).getFieldName());
        Assert.assertEquals(null, diff.get(2).getOldValue());
        Assert.assertEquals("\"value3\"", diff.get(2).getValue());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_PARAMETER_NAME, diff.get(3).getFieldName());
        Assert.assertEquals(null, diff.get(3).getOldValue());
        Assert.assertEquals("param5", diff.get(3).getValue());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_PARAMETER_VALUE, diff.get(4).getFieldName());
        Assert.assertEquals(null, diff.get(4).getOldValue());
        Assert.assertEquals("binding5", diff.get(4).getValue());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_PARAMETER_VALUE, diff.get(5).getFieldName());
        Assert.assertEquals("\"value2\"", diff.get(5).getOldValue());
        Assert.assertEquals("\"value6\"", diff.get(5).getValue());
    }

    @Test
    public void testDiffAll() {
        PortableWorkDefinition def1 = new PortableWorkDefinition();
        def1.setName("def1name");
        column1.setWorkItemDefinition(def1);
        column1.setHeader("header1");
        column1.setHideColumn(false);
        column1.setDefaultValue(new DTCellValue52("default1"));
        PortableWorkDefinition def2 = new PortableWorkDefinition();
        def2.setName("def2name");
        column2.setWorkItemDefinition(def2);
        column2.setHeader("header2");
        column2.setHideColumn(true);
        column2.setDefaultValue(new DTCellValue52("default2"));
        List<BaseColumnFieldDiff> diff = column1.diff(column2);
        Assert.assertNotNull(diff);
        Assert.assertEquals(4, diff.size());
        Assert.assertEquals(DTColumnConfig52.FIELD_HIDE_COLUMN, diff.get(0).getFieldName());
        Assert.assertEquals(false, diff.get(0).getOldValue());
        Assert.assertEquals(true, diff.get(0).getValue());
        Assert.assertEquals(DTColumnConfig52.FIELD_DEFAULT_VALUE, diff.get(1).getFieldName());
        Assert.assertEquals("default1", diff.get(1).getOldValue());
        Assert.assertEquals("default2", diff.get(1).getValue());
        Assert.assertEquals(DTColumnConfig52.FIELD_HEADER, diff.get(2).getFieldName());
        Assert.assertEquals("header1", diff.get(2).getOldValue());
        Assert.assertEquals("header2", diff.get(2).getValue());
        Assert.assertEquals(ActionWorkItemCol52.FIELD_WORKITEM_DEFINITION_NAME, diff.get(3).getFieldName());
        Assert.assertEquals("def1name", diff.get(3).getOldValue());
        Assert.assertEquals("def2name", diff.get(3).getValue());
    }
}

