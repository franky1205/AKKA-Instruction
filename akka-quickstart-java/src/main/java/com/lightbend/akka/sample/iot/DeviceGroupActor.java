package com.lightbend.akka.sample.iot;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceListRequest;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceListResponse;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceTrackingRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.AllTemperaturesRequest;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frankie on 2018/2/20.
 */
public class DeviceGroupActor extends AbstractActor {

    public static Props props(String groupId) {
        return Props.create(DeviceGroupActor.class, groupId);
    }

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final String groupId;

    public DeviceGroupActor(String groupId) {
        this.groupId = groupId;
    }

    private final BiMap<String, ActorRef> deviceActors = HashBiMap.create();

    @Override
    public void preStart() throws Exception {
        logger.info("DeviceGroupActor started with GroupID: [{}]", this.groupId);
    }

    @Override
    public void postStop() throws Exception {
        logger.info("DeviceGroupActor stopped with GroupID: [{}]", this.groupId);
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .match(DeviceTrackingRequest.class, this::trackingDevice)
                .match(DeviceListRequest.class, this::listDeviceIds)
                .match(Terminated.class, this::terminateDeviceActor)
                .match(AllTemperaturesRequest.class, this::onAllTemperaturesRequest)
                .build();
    }

    private void trackingDevice(DeviceTrackingRequest deviceTrackingRequest) {
        if (!this.groupId.equals(deviceTrackingRequest.getGroupId())) {
            logger.warning("Ignoring DeviceTracking request for ID: [{}]. This actor is responsible for ID: [{}].",
                    deviceTrackingRequest.getGroupId(), this.groupId);
            return;
        }
        ActorRef deviceActorRef = this.deviceActors.computeIfAbsent(deviceTrackingRequest.getDeviceId(), deviceId -> {
            logger.info("Creating Device Actor for Device ID: [{}]", deviceId);
            ActorRef actorRef = this.getContext().actorOf(DeviceActor.props(this.groupId, deviceId), "device-" + deviceId);
            this.getContext().watch(actorRef);
            return actorRef;
        });
        deviceActorRef.forward(deviceTrackingRequest, this.getContext());
    }

    private void listDeviceIds(DeviceListRequest deviceListRequest) {
        logger.info("Return listed device Ids: [{}]", this.deviceActors.keySet());
        this.getSender().tell(new DeviceListResponse(deviceListRequest.getRequestId(),
                this.deviceActors.keySet()), this.getSelf());
    }

    private void terminateDeviceActor(Terminated terminatedActorRef) {
        this.deviceActors.inverse().computeIfPresent(terminatedActorRef.actor(), (deviceActorRef, deviceId) -> {
            logger.info("Device actor with device ID: [{}] has been terminated.", deviceId);
            return null;
        });
    }

    private void onAllTemperaturesRequest(AllTemperaturesRequest allTemperaturesRequest) {
        this.getContext().actorOf(DeviceGroupQueryActor.props(
                deviceActors.inverse(),
                allTemperaturesRequest.getRequestId(),
                this.getSender(),
                new FiniteDuration(allTemperaturesRequest.getTimeoutMillis(), TimeUnit.MILLISECONDS)), "DeviceGroupQueryActor");
    }
}
