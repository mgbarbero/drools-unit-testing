/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.drools.workbench.models.guided.dtable.shared.model;


import DataType.DataTypes.BOOLEAN;
import DataType.DataTypes.DATE;
import DataType.DataTypes.NUMERIC_INTEGER;
import DataType.DataTypes.NUMERIC_LONG;
import DataType.DataTypes.STRING;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;


// These tests check legacy (pre-6.4.CR1) objects that have been de-serialized can be correctly read.
// DTCellValue52.valueString was set to an empty String statically and hence when the object was de-serialized
// it was possible for instances to have both a 'valueString (=="")' and '<another>Value' for types other than
// DataTypes.DataType.TYPE_STRING. For example. valueBoolean=true, valueString="", dataType=BOOLEAN.
public class DTCellValue52Test {
    private DTCellValue52 dcv;

    private Field fieldBoolean;

    private Field fieldDate;

    private Field fieldNumeric;

    private Field fieldString;

    private Field fieldDataType;

    private static final Date now = Calendar.getInstance().getTime();

    @Test
    public void testGetBooleanValue() throws Exception {
        dcv.setBooleanValue(true);
        fieldDate.set(dcv, DTCellValue52Test.now);
        fieldNumeric.set(dcv, 1L);
        fieldString.set(dcv, "woot");
        Assert.assertEquals(BOOLEAN, dcv.getDataType());
        Assert.assertTrue(dcv.getBooleanValue());
        Assert.assertNull(dcv.getDateValue());
        Assert.assertNull(dcv.getNumericValue());
        Assert.assertNull(dcv.getStringValue());
    }

    @Test
    public void testGetDateValue() throws Exception {
        fieldBoolean.set(dcv, true);
        dcv.setDateValue(DTCellValue52Test.now);
        fieldNumeric.set(dcv, 1L);
        fieldString.set(dcv, "woot");
        Assert.assertEquals(DATE, dcv.getDataType());
        Assert.assertNull(dcv.getBooleanValue());
        Assert.assertEquals(DTCellValue52Test.now, dcv.getDateValue());
        Assert.assertNull(dcv.getNumericValue());
        Assert.assertNull(dcv.getStringValue());
    }

    @Test
    public void testGetNumericValue() throws Exception {
        fieldBoolean.set(dcv, true);
        fieldDate.set(dcv, DTCellValue52Test.now);
        dcv.setNumericValue(1L);
        fieldString.set(dcv, "woot");
        Assert.assertEquals(NUMERIC_LONG, dcv.getDataType());
        Assert.assertNull(dcv.getBooleanValue());
        Assert.assertNull(dcv.getDateValue());
        Assert.assertEquals(1L, dcv.getNumericValue());
        Assert.assertNull(dcv.getStringValue());
    }

    @Test
    public void testGetStringValue() throws Exception {
        fieldBoolean.set(dcv, true);
        fieldDate.set(dcv, DTCellValue52Test.now);
        fieldNumeric.set(dcv, 1L);
        dcv.setStringValue("woot");
        Assert.assertEquals(STRING, dcv.getDataType());
        Assert.assertNull(dcv.getBooleanValue());
        Assert.assertNull(dcv.getDateValue());
        Assert.assertNull(dcv.getNumericValue());
        Assert.assertEquals("woot", dcv.getStringValue());
    }

    @Test
    public void testDefaultValue() throws Exception {
        final DTCellValue52 defaultValue = new DTCellValue52(1);
        final DTCellValue52 clone = new DTCellValue52(defaultValue);
        Assert.assertEquals(NUMERIC_INTEGER, clone.getDataType());
        Assert.assertNull(clone.getBooleanValue());
        Assert.assertNull(clone.getDateValue());
        Assert.assertEquals(1, clone.getNumericValue());
        Assert.assertNull(clone.getStringValue());
    }

    @Test
    public void testDefaultValueNull() throws Exception {
        final DTCellValue52 defaultValue = null;
        final DTCellValue52 clone = new DTCellValue52(defaultValue);
        Assert.assertEquals(STRING, clone.getDataType());
        Assert.assertNull(clone.getBooleanValue());
        Assert.assertNull(clone.getDateValue());
        Assert.assertNull(clone.getNumericValue());
        Assert.assertNull(clone.getStringValue());
    }
}

