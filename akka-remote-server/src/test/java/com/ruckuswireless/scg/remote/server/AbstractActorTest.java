package com.ruckuswireless.scg.remote.server;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;

/**
 * Created by Frankie on 2018/2/19.
 */
public abstract class AbstractActorTest {

    protected ActorSystem system;

    protected TestKit probe;

    @Before
    public void setup() {
        this.system = ActorSystem.create("akka-remote-server");
        this.probe = new TestKit(this.system);
    }

    @After
    public void shutdown() {
        TestKit.shutdownActorSystem(this.system);
        this.system = null;
    }
}
