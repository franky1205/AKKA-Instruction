package com.lightbend.akka.sample.failure;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Created by Frankie on 2018/2/19.
 */
public class Supervisor extends AbstractActor {

    ActorRef supervisedActorRef = this.getContext().actorOf(
            Props.create(Supervised.class), "SuperviesdActor");

    @Override
    public void preStart() throws Exception {
        System.out.println(Thread.currentThread().getName() + " Supervisor started");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println(Thread.currentThread().getName() + " Supervisor stop");
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .matchEquals("failedChild", message -> {
                    System.out.println(Thread.currentThread().getName() + " Invoke Child to fail.");
                    supervisedActorRef.tell("failed", this.getSelf());
                })
                .build();
    }
}
