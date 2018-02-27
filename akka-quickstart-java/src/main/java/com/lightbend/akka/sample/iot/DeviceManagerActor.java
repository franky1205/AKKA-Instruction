package com.lightbend.akka.sample.iot;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceTrackingRequest;
import com.lightbend.akka.sample.iot.DeviceTracker.GroupActorRefRequest;
import com.lightbend.akka.sample.iot.DeviceTracker.GroupActorRefResponse;

/**
 * Created by Frankie on 2018/2/25.
 */
public class DeviceManagerActor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(DeviceManagerActor.class);
    }

    private final BiMap<String, ActorRef> groupActors = HashBiMap.create();

    @Override
    public void preStart() throws Exception {
        logger.info("DeviceManagerActor started.");
    }

    @Override
    public void postStop() throws Exception {
        logger.info("DeviceManagerActor stopped.");
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .match(DeviceTrackingRequest.class, this::trackingDevice)
                .match(GroupActorRefRequest.class, this::onGroupActorRefRequest)
                .match(Terminated.class, this::onTerminate)
                .build();
    }

    private void trackingDevice(DeviceTrackingRequest deviceTrackingRequest) {
        ActorRef groupActorRef = this.groupActors.computeIfAbsent(deviceTrackingRequest.getGroupId(), groupId -> {
            ActorRef newGroupActorRef = this.getContext().actorOf(DeviceGroupActor.props(groupId), "Device-Group-" + groupId);
            this.getContext().watch(newGroupActorRef);
            logger.info("Create a DeviceGroupActor with GroupId: [{}], ActorRef: [{}]", groupId, newGroupActorRef);
            return newGroupActorRef;
        });
        groupActorRef.forward(deviceTrackingRequest, this.getContext());
    }

    private void onTerminate(Terminated terminated) {
        this.groupActors.inverse().computeIfPresent(terminated.getActor(), (actorRef, groupId) -> {
            logger.info("Group actor with Group ID: [{}] has been terminated.", groupId);
            return null;
        });
    }

    private void onGroupActorRefRequest(GroupActorRefRequest groupActorRefRequest) {
        this.groupActors.computeIfPresent(groupActorRefRequest.getGroupId(), (groupId, groupActorRef) -> {
            this.getSender().tell(new GroupActorRefResponse(groupActorRefRequest.getRequestId(), groupActorRef), this.getSelf());
            return groupActorRef;
        });
    }
}
