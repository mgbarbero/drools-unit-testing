/**
 * Copyright 2005 Red Hat, Inc. and/or its affiliates.
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
package org.drools.compiler.builder.impl;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Policy;
import org.drools.core.test.model.DroolsTestCase;
import org.junit.Assert;
import org.junit.Test;


public class KnowledgeBuilderWithSecurityManagerTest extends DroolsTestCase {
    private static SecurityManager oldSecurityManager;

    private static Policy oldPolicy;

    @Test
    public void testSecurityManager() throws Exception {
        final KnowledgeBuilderImpl builder = new KnowledgeBuilderImpl();
        InputStream is = this.getClass().getResourceAsStream("/com/security/example.drl");
        builder.addPackageFromDrl(new InputStreamReader(is));
        Assert.assertTrue(((builder.getErrors().getErrors().length) == 0));
    }
}

