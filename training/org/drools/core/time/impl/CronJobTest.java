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
package org.drools.core.time.impl;


import ClockType.PSEUDO_CLOCK;
import java.util.concurrent.TimeUnit;
import org.drools.core.SessionConfiguration;
import org.drools.core.time.TimerServiceFactory;
import org.junit.Assert;
import org.junit.Test;


public class CronJobTest {
    @Test
    public void testCronTriggerJob() throws Exception {
        SessionConfiguration config = SessionConfiguration.newInstance();
        config.setClockType(PSEUDO_CLOCK);
        PseudoClockScheduler timeService = ((PseudoClockScheduler) (TimerServiceFactory.getTimerService(config)));
        timeService.advanceTime(0, TimeUnit.MILLISECONDS);
        CronTrigger trigger = new CronTrigger(0, null, null, (-1), "15 * * * * ?", null, null);
        JDKTimerServiceTest.HelloWorldJobContext ctx = new JDKTimerServiceTest.HelloWorldJobContext("hello world", timeService);
        timeService.scheduleJob(new JDKTimerServiceTest.HelloWorldJob(), ctx, trigger);
        Assert.assertEquals(0, ctx.getList().size());
        timeService.advanceTime(10, TimeUnit.SECONDS);
        Assert.assertEquals(0, ctx.getList().size());
        timeService.advanceTime(10, TimeUnit.SECONDS);
        Assert.assertEquals(1, ctx.getList().size());
        timeService.advanceTime(30, TimeUnit.SECONDS);
        Assert.assertEquals(1, ctx.getList().size());
        timeService.advanceTime(30, TimeUnit.SECONDS);
        Assert.assertEquals(2, ctx.getList().size());
    }
}

