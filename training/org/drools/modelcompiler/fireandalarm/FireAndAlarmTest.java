package org.drools.modelcompiler.fireandalarm;


import java.util.Arrays;
import org.drools.model.Model;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.drools.modelcompiler.fireandalarm.model.Alarm;
import org.drools.modelcompiler.fireandalarm.model.Fire;
import org.drools.modelcompiler.fireandalarm.model.Room;
import org.drools.modelcompiler.fireandalarm.model.Sprinkler;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;


public class FireAndAlarmTest {
    @Test
    public void testFireAndAlarm() {
        Variable<Room> room = any(Room.class);
        Variable<Fire> fire = any(Fire.class);
        Variable<Sprinkler> sprinkler = any(Sprinkler.class);
        Variable<Alarm> alarm = any(Alarm.class);
        Rule r1 = rule("When there is a fire turn on the sprinkler").build(expr(sprinkler, ( s) -> !(s.isOn())), expr(sprinkler, fire, ( s, f) -> s.getRoom().equals(f.getRoom())), on(sprinkler).execute(( drools, s) -> {
            System.out.println(("Turn on the sprinkler for room " + (s.getRoom().getName())));
            s.setOn(true);
            drools.update(s, "on");
        }));
        Rule r2 = rule("When the fire is gone turn off the sprinkler").build(expr(sprinkler, Sprinkler::isOn), not(fire, sprinkler, ( f, s) -> f.getRoom().equals(s.getRoom())), on(sprinkler).execute(( drools, s) -> {
            System.out.println(("Turn off the sprinkler for room " + (s.getRoom().getName())));
            s.setOn(false);
            drools.update(s, "on");
        }));
        Rule r3 = rule("Raise the alarm when we have one or more fires").build(exists(fire), execute(( drools) -> {
            System.out.println("Raise the alarm");
            drools.insert(new Alarm());
        }));
        Rule r4 = rule("Lower the alarm when all the fires have gone").build(not(fire), input(alarm), on(alarm).execute(( drools, a) -> {
            System.out.println("Lower the alarm");
            drools.delete(a);
        }));
        Rule r5 = rule("Status output when things are ok").build(not(alarm), not(sprinkler, Sprinkler::isOn), execute(() -> System.out.println("Everything is ok")));
        Model model = new ModelImpl().withRules(Arrays.asList(r1, r2, r3, r4, r5));
        KieBase kieBase = KieBaseBuilder.createKieBaseFromModel(model);
        KieSession ksession = kieBase.newKieSession();
        // phase 1
        Room room1 = new Room("Room 1");
        ksession.insert(room1);
        FactHandle fireFact1 = ksession.insert(new Fire(room1));
        ksession.fireAllRules();
        // phase 2
        Sprinkler sprinkler1 = new Sprinkler(room1);
        ksession.insert(sprinkler1);
        ksession.fireAllRules();
        Assert.assertTrue(sprinkler1.isOn());
        // phase 3
        ksession.delete(fireFact1);
        ksession.fireAllRules();
    }
}

