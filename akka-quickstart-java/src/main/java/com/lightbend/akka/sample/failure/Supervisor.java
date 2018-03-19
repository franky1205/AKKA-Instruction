package com.lightbend.akka.sample.failure;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.stream.IntStream;

/**
 * Created by Frankie on 2018/2/19.
 */
public class Supervisor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    public Supervisor() {
        ActorRef supervisedActorRef = this.getContext().actorOf(
                Props.create(Supervised.class), "SuperviesdActor");
        this.getContext().watch(supervisedActorRef);
    }

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
                    logger.info("Invoke Child to fail.");
                    this.getContext().getChildren()
//                            .forEach(childActorRef -> supervisedActorRef.tell("failed", this.getSelf()));
                        .forEach(childActorRef -> childActorRef.tell(PoisonPill.getInstance(), this.getSelf()));
                })
                .match(Terminated.class, terminated -> {
                    logger.info("Receive terminated Child reference: [{}]", terminated.getActor());
                })
                .build();
    }
}
