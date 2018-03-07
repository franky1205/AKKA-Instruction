package com.ruckuswireless.scg.remote.core.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frankie on 2018/3/6.
 */
public class ControlBladeQueryActor extends AbstractActor {

    public static Props props(Map<ActorRef, String> controlBladeActorRefs, ActorRef requestSender) {
        return Props.create(ControlBladeQueryActor.class, controlBladeActorRefs, requestSender);
    }

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final ImmutableMap<ActorRef, String> controlBladeActorRefs;

    private final ActorRef requestSender;

    public ControlBladeQueryActor(Map<ActorRef, String> controlBladeActorRefs, ActorRef requestSender) {
        this.controlBladeActorRefs = ImmutableMap.copyOf(controlBladeActorRefs);
        this.requestSender = requestSender;
    }

    @Override
    public void preStart() throws Exception {
        logger.info("ControlBladeQueryActor is going to be started...");
        this.controlBladeActorRefs.keySet().forEach(this::sendHostNameRequest);
    }

    private void sendHostNameRequest(ActorRef controlBladeActorRef) {
        this.getContext().watch(controlBladeActorRef);
        controlBladeActorRef.tell("getHostName", this.getSelf());
    }

    @Override
    public void postStop() throws Exception {
        logger.info("ControlBladeQueryActor has been stopped.");
    }

    @Override
    public Receive createReceive() {
        return this.newReceive(new HashMap<>(this.controlBladeActorRefs), new HashMap<>());
    }

    private Receive newReceive(Map<ActorRef, String> pendingControlBlades, Map<String, String> hostNames) {
        return this.receiveBuilder()
                .match(String.class, message -> onMessage(pendingControlBlades, hostNames, message))
                .build();
    }

    private void onMessage(Map<ActorRef, String> pendingControlBlades, Map<String, String> hostNames, String message) {
        logger.info("Receive HostName: [{}] result from [{}]", message, this.getSender());
        pendingControlBlades.computeIfPresent(this.getSender(), (senderActorRef, uuid) -> {
            this.getContext().unwatch(senderActorRef);
            pendingControlBlades.remove(senderActorRef);
            hostNames.put(uuid, message);
            return null;
        });
        if (!pendingControlBlades.isEmpty()) {
            this.getContext().become(this.newReceive(pendingControlBlades, hostNames));
            return ;
        }
        this.requestSender.tell(hostNames, this.getSelf());
        this.getContext().stop(this.getSelf());
    }
}
