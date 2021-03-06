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


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.drools.workbench.models.testscenarios.shared.FactData;
import org.drools.workbench.models.testscenarios.shared.Field;
import org.drools.workbench.models.testscenarios.shared.FieldData;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.soup.project.datamodel.commons.types.ClassTypeResolver;
import org.kie.soup.project.datamodel.commons.types.TypeResolver;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


public class GlobalFactPopulatorTest {
    @Test
    public void testWithGlobals() throws Exception {
        FactData global = new FactData("Cheese", "c", Arrays.<Field>asList(new FieldData("type", "cheddar")), false);
        TypeResolver resolver = new ClassTypeResolver(new HashSet<String>(), Thread.currentThread().getContextClassLoader());
        resolver.addImport("org.drools.workbench.models.testscenarios.backend.Cheese");
        KieSession ksession = Mockito.mock(KieSession.class);
        Map<String, Object> populatedData = new HashMap<String, Object>();
        Map<String, Object> globalData = new HashMap<String, Object>();
        GlobalFactPopulator globalFactPopulator = new GlobalFactPopulator(populatedData, resolver, global, globalData);
        globalFactPopulator.populate(ksession, new HashMap<String, org.kie.api.runtime.rule.FactHandle>());
        Mockito.verify(ksession).setGlobal(ArgumentMatchers.eq(global.getName()), ArgumentMatchers.any(Object.class));
        Assert.assertEquals(1, globalData.size());
        Assert.assertEquals(0, populatedData.size());
    }
}

