/**
 * Copyright 2005 JBoss Inc
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
package org.drools.modelcompiler.exchange;


import java.util.ArrayList;
import java.util.List;
import org.drools.core.reteoo.AsyncMessagesCoordinator;
import org.drools.model.DSL;
import org.drools.model.Global;
import org.drools.model.Model;
import org.drools.model.PatternDSL;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.impl.Exchange;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;


public class SendReceiveTest {
    @Test
    public void testAsync() {
        Global<List> messages = DSL.globalOf(List.class, "defaultpkg", "messages");
        Variable<Integer> length = DSL.declarationOf(Integer.class);
        Exchange<String> exchange = DSL.exchangeOf(String.class);
        Rule send = PatternDSL.rule("send").build(PatternDSL.send(exchange).message(() -> {
            try {
                Thread.sleep(1000L);
            } catch ( e) {
                throw new <e>RuntimeException();
            }
            return "Hello World!";
        }));
        Rule receive = PatternDSL.rule("receive").build(PatternDSL.pattern(length), PatternDSL.receive(exchange).expr(length, ( s, l) -> (s.length()) > l), DSL.on(exchange, length, messages).execute(( s, l, m) -> m.add(((("received message '" + s) + "' longer than ") + l))));
        Model model = new ModelImpl().addRule(send).addRule(receive).addGlobal(messages);
        KieBase kieBase = KieBaseBuilder.createKieBaseFromModel(model);
        KieSession ksession = kieBase.newKieSession();
        List<String> list = new ArrayList<>();
        ksession.setGlobal("messages", list);
        Assert.assertEquals(1, AsyncMessagesCoordinator.get().getListeners().size());
        ksession.insert(10);
        new Thread(() -> ksession.fireUntilHalt()).start();
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ksession.halt();
        ksession.dispose();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("received message 'Hello World!' longer than 10", list.get(0));
        Assert.assertEquals(0, AsyncMessagesCoordinator.get().getListeners().size());
    }

    @Test
    public void testAsyncWith2KBase() {
        Exchange<String> exchange = DSL.exchangeOf(String.class);
        Rule send = PatternDSL.rule("send").build(PatternDSL.send(exchange).message(() -> {
            try {
                Thread.sleep(1000L);
            } catch ( e) {
                throw new <e>RuntimeException();
            }
            return "Hello World!";
        }));
        Variable<Integer> length = DSL.declarationOf(Integer.class);
        Global<List> messages = DSL.globalOf(List.class, "defaultpkg", "messages");
        Rule receive = PatternDSL.rule("receive").build(PatternDSL.pattern(length), PatternDSL.receive(exchange).expr(length, ( s, l) -> (s.length()) > l), DSL.on(exchange, length, messages).execute(( s, l, m) -> m.add(((("received message '" + s) + "' longer than ") + l))));
        Model model1 = new ModelImpl().addRule(send);
        KieBase kieBase1 = KieBaseBuilder.createKieBaseFromModel(model1);
        KieSession ksession1 = kieBase1.newKieSession();
        Model model2 = new ModelImpl().addRule(receive).addGlobal(messages);
        KieBase kieBase2 = KieBaseBuilder.createKieBaseFromModel(model2);
        KieSession ksession2 = kieBase2.newKieSession();
        List<String> list = new ArrayList<>();
        ksession2.setGlobal("messages", list);
        ksession2.insert(10);
        new Thread(() -> ksession2.fireUntilHalt()).start();
        new Thread(() -> ksession1.fireUntilHalt()).start();
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ksession1.halt();
        ksession1.dispose();
        ksession2.halt();
        ksession2.dispose();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("received message 'Hello World!' longer than 10", list.get(0));
    }
}

