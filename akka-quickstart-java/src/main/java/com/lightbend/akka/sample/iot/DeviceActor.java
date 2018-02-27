package com.lightbend.akka.sample.iot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceTrackingRequest;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceTrackingResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureUpdateRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureUpdateResponse;

import java.util.Optional;

/**
 * Created by Frankie on 2018/2/19.
 */
public class DeviceActor extends AbstractActor {

    public static Props props(String groupId, String deviceId) {
        return Props.create(DeviceActor.class, groupId, deviceId);
    }

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final String groupId;

    private final String deviceId;

    private Optional<Double> latestReportedValue;

    public DeviceActor(String groupId, String deviceId) {
        this.groupId = groupId;
        this.deviceId = deviceId;
    }

    @Override
    public void preStart() throws Exception {
        logger.info("DeviceActor actor {}-{} started", groupId, deviceId);
        latestReportedValue = Optional.empty();
    }

    @Override
    public void postStop() throws Exception {
        logger.info("DeviceActor actor {}-{} stopped", groupId, deviceId);
    }

    @Override
    public Receive createReceive() {
        return this.receiveBuilder()
                .match(DeviceTrackingRequest.class, deviceTrackingRequest -> {
                    logger.info("Receive DeviceTracking request of [{}]-[{}].",
                            deviceTrackingRequest.getGroupId(), deviceTrackingRequest.getDeviceId());
                    if (!this.groupId.equals(deviceTrackingRequest.getGroupId()) ||
                            !this.deviceId.equals(deviceTrackingRequest.getDeviceId())) {
                        logger.warning("Ignore unmatched request IDs of [{}]-[{}] with current [{}]-[{}]",
                                deviceTrackingRequest.getGroupId(), deviceTrackingRequest.getDeviceId(), this.groupId, this.deviceId);
                        return ;
                    }
                    getSender().tell(new DeviceTrackingResponse(), getSelf());
                })
                .match(TemperatureUpdateRequest.class, temperatureUpdateRequest -> {
                    logger.info("Receive temperature update value:[{}], ID:[{}]",
                            temperatureUpdateRequest.getUpdateValue(), temperatureUpdateRequest.getRequestId());
                    latestReportedValue = Optional.of(temperatureUpdateRequest.getUpdateValue());
                    getSender().tell(new TemperatureUpdateResponse(temperatureUpdateRequest.getRequestId()), getSelf());
                })
                .match(TemperatureRequest.class, temperatureRequest -> {
                    logger.info("Receive temperature request ID:[{}]", temperatureRequest.getRequestId());
                    getSender().tell(new TemperatureResponse(temperatureRequest.getRequestId(), latestReportedValue), getSelf());
                })
                .build();
    }
}
