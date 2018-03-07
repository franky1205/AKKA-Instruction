package com.ruckuswireless.scg.remote.core.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Frankie on 2018/3/6.
 */
public class ClusterServiceActor extends AbstractActor {

    public static Props props(String clusterName) {
        return Props.create(ClusterServiceActor.class, clusterName);
    }

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final List<String> controlBladeUuids = new ArrayList<>();

    private final Map<ActorRef, String> controlBladeRefs = new HashMap<>();

    private final String clusterName;

    public ClusterServiceActor(String clusterName) {
        this.clusterName = clusterName;
        this.controlBladeUuids.addAll(this.getControlBladeUuidList());
    }

    private List<String> getControlBladeUuidList() {
        // FIXME: Frankie For demo only to generate random control blade UUIDs.
        return Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Override
    public void preStart() {
        logger.info("{} is going to be started...", this.getClass().getSimpleName());
        Map<ActorRef, String> controlBladeRefs = this.controlBladeUuids.stream()
                .map(this::createControlBladeActorRef)
                .collect(Collectors.toMap(ControlBladeActorRef::getActorRef, ControlBladeActorRef::getUuid));
        this.controlBladeRefs.putAll(controlBladeRefs);
    }

    @Override
    public void postStop() throws Exception {
        logger.info("{} has been stopped.", this.getClass().getSimpleName());
    }

    private ControlBladeActorRef createControlBladeActorRef(String controlBladeUuid) {
        return ControlBladeActorRef.builder()
                .uuid(controlBladeUuid)
                .actorRef(this.getContext()
                        .actorOf(ControlBladeServiceActor.props(controlBladeUuid), controlBladeUuid))
                .build();
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .matchEquals("getClusterName", message -> {
                    logger.info("Receive message: [{}] from Sender: [{}]", message, getSender());
                    getSender().tell(clusterName, getSelf());
                })
                .matchEquals("getControlBladeSize", message -> {
                    logger.info("Receive message: [{}] from Sender: [{}]", message, getSender());
                    getSender().tell(String.valueOf(controlBladeRefs.size()), getSelf());
                })
                .matchEquals("getControlBladeIds", message -> {
                    logger.info("Receive message: [{}] from Sender: [{}]", message, getSender());
                    getSender().tell(this.controlBladeUuids, this.getSelf());
                })
                .matchEquals("getControlBladeNames", message -> {
                    logger.info("Receive message: [{}] from Sender: [{}]", message, getSender());
                    this.getContext().actorOf(ControlBladeQueryActor.props(controlBladeRefs, this.getSender()));
                })
                .build();
    }

    @Builder
    private static class ControlBladeActorRef {

        @Getter
        private String uuid;

        @Getter
        private ActorRef actorRef;
    }
}
