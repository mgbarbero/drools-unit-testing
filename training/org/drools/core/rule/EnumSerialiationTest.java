/**
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
package org.drools.core.rule;


import org.drools.core.integrationtests.SerializationHelper;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created by IntelliJ IDEA. User: Ming Jin Date: Mar 19, 2008 Time: 11:11:45 AM To change this template use File |
 * Settings | File Templates.
 */
public class EnumSerialiationTest {
    private static final String TEST_NAME = "test name";

    @Test
    public void testTypeDeclaration() throws Exception {
        TypeDeclaration typeDec1 = new TypeDeclaration(EnumSerialiationTest.TEST_NAME);
        TypeDeclaration typeDec2 = SerializationHelper.serializeObject(typeDec1);
        Assert.assertEquals(typeDec1, typeDec2);
    }
}

