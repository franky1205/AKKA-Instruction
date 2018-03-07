package com.ruckuswireless.scg.remote.client.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by Frankie on 2018/3/6.
 */
public class MessageLoggingActor extends AbstractActor {

    public static Props props() {
        return Props.create(MessageLoggingActor.class);
    }

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .match(String.class, message -> logger.info("Message receive: [{}]", message))
                .match(List.class, list -> logger.info("List receive: [{}]", list))
                .match(Map.class, map -> logger.info("Map receive: [{}]", map))
                .build();
    }
}
