package com.ruckuswireless.scg.remote.core.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Created by Frankie on 2018/3/6.
 */
public class ControlBladeServiceActor extends AbstractActor {

    public static Props props(String uuid) {
        return Props.create(ControlBladeServiceActor.class, uuid);
    }

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final String uuid;

    private final String hostName;

    public ControlBladeServiceActor(String uuid) {
        this.uuid = uuid;
        this.hostName = "AkkaControlBlade";
    }

    @Override
    public void preStart() throws Exception {
        logger.info("{} is going to be started...", this.getClass().getSimpleName());
    }

    @Override
    public void postStop() throws Exception {
        logger.info("{} has been stopped.", this.getClass().getSimpleName());
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .matchEquals("getId", message -> getSender().tell(uuid, getSelf()))
                .matchEquals("getHostName", message -> getSender().tell(hostName, getSelf()))
                .build();
    }
}
