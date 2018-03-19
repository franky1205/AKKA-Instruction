package com.lightbend.akka.sample.failure;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Optional;

/**
 * Created by Frankie on 2018/2/19.
 */
public class Supervised extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public void preStart() throws Exception {
        System.out.println(Thread.currentThread().getName() + " Supervised Started");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println(Thread.currentThread().getName() + " Supervised Stop");
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
        System.out.println(Thread.currentThread().getName() +
                " Supervised is going to restart due to reason: " + reason.getMessage());
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        System.out.println(Thread.currentThread().getName() +
                " Supervised is restarted due to reason: " + reason.getMessage());
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .matchEquals("failed", message -> {
                    logger.error("Throw new Failure");
                    throw new RuntimeException("Supervised Actor failed.");
                })
                .build();
    }
}
