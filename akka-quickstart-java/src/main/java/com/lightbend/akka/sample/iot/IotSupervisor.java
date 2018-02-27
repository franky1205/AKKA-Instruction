package com.lightbend.akka.sample.iot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Created by Frankie on 2018/2/19.
 */
public class IotSupervisor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(IotSupervisor.class);
    }

    @Override
    public void preStart() throws Exception {
        logger.info("IoT Application started");
    }

    @Override
    public void postStop() throws Exception {
        logger.info("IoT Application stopped");
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .build();
    }
}
