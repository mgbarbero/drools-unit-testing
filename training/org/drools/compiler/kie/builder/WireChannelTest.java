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
package org.drools.compiler.kie.builder;


import KieServices.Factory;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;


public class WireChannelTest {
    private static final List<Object> channelMessages = new ArrayList<Object>();

    @Test
    public void testWireChannel() throws Exception {
        KieServices ks = Factory.get();
        ReleaseId releaseId = ks.newReleaseId("org.kie", "listener-test", "1.0");
        build(ks, releaseId);
        KieContainer kieContainer = ks.newKieContainer(releaseId);
        KieSession ksession = kieContainer.newKieSession();
        ksession.fireAllRules();
        Assert.assertEquals(1, WireChannelTest.channelMessages.size());
        Assert.assertEquals("Test Message", WireChannelTest.channelMessages.get(0));
    }

    public static class RecordingChannel implements Channel {
        @Override
        public void send(Object object) {
            WireChannelTest.channelMessages.add(object);
        }
    }
}

