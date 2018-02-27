package com.lightbend.akka.sample;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by Frankie on 2018/2/19.
 */
public abstract class AbstractActorTest {

    protected ActorSystem system;

    protected TestKit probe;

    @Before
    public void setup() {
        this.system = ActorSystem.create();
        this.probe = new TestKit(this.system);
    }

    @After
    public void shutdown() {
        TestKit.shutdownActorSystem(this.system);
        this.system = null;
    }
}
