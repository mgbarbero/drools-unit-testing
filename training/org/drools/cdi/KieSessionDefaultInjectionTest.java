/**
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.drools.cdi;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.drools.cdi.kproject.AbstractKnowledgeTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.cdi.KReleaseId;
import org.kie.api.runtime.KieSession;


@RunWith(CDITestRunner.class)
public class KieSessionDefaultInjectionTest {
    public static AbstractKnowledgeTest helper;

    @Inject
    private KieSession defaultClassPathKSession;

    @Inject
    @KReleaseId(groupId = "jar1", artifactId = "art1", version = "1.0")
    private KieSession defaultDynamicKSession;

    @Test
    public void tessDefaultClassPathKBase() throws IOException, ClassNotFoundException, InterruptedException {
        Assert.assertNotNull(defaultClassPathKSession);
        List<String> list = new ArrayList<String>();
        defaultClassPathKSession.setGlobal("list", list);
        defaultClassPathKSession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.get(0).endsWith("2.0"));
        Assert.assertTrue(list.get(1).endsWith("2.0"));
    }

    @Test
    public void tessDefaultDynamicKBase() throws IOException, ClassNotFoundException, InterruptedException {
        Assert.assertNotNull(defaultDynamicKSession);
        List<String> list = new ArrayList<String>();
        defaultDynamicKSession.setGlobal("list", list);
        defaultDynamicKSession.fireAllRules();
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.get(0).endsWith("1.0"));
        Assert.assertTrue(list.get(1).endsWith("1.0"));
    }
}

