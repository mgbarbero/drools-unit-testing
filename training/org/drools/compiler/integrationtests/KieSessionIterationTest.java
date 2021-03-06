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
package org.drools.compiler.integrationtests;


import org.drools.compiler.CommonTestMethodBase;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;


/**
 * Tests iteration through the list of KieSessions of a KieBase.
 */
public class KieSessionIterationTest extends CommonTestMethodBase {
    private KieBase kieBase;

    /**
     * Tests that disposing KieSessions does not throw ConcurrentModificationException when iterating through the list of KieBase's KieSessions
     * (related to BZ 1326329).
     */
    @Test
    public void testDisposingSeveralKieSessions() throws Exception {
        for (KieSession kieSession : this.kieBase.getKieSessions()) {
            kieSession.dispose();
        }
        Assert.assertTrue("All KieSessions of the KieBase should have been disposed.", this.kieBase.getKieSessions().isEmpty());
    }
}

