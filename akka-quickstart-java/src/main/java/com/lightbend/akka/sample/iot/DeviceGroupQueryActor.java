package com.lightbend.akka.sample.iot;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.ImmutableMap;
import com.lightbend.akka.sample.iot.TemperatureStatus.AllTemperaturesResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.CollectionTimeout;
import com.lightbend.akka.sample.iot.TemperatureStatus.DeviceNotAvailable;
import com.lightbend.akka.sample.iot.TemperatureStatus.DeviceTimeOut;
import com.lightbend.akka.sample.iot.TemperatureStatus.Temperature;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureNotAvailable;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureValue;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frankie on 2018/2/25.
 */
public class DeviceGroupQueryActor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final ImmutableMap<ActorRef, String> deviceActorRefs;

    private final long requestId;

    private final ActorRef requestSender;

    private final Cancellable queryTimeoutTimer;

    public static Props props(Map<ActorRef, String> deviceActorRefs, long requestId,
                              ActorRef requestSender, FiniteDuration timeout) {
        return Props.create(DeviceGroupQueryActor.class, deviceActorRefs, requestId, requestSender, timeout);
    }

    public DeviceGroupQueryActor(Map<ActorRef, String> deviceActorRefs, long requestId, ActorRef requestSender, FiniteDuration timeout) {
        this.deviceActorRefs = ImmutableMap.copyOf(deviceActorRefs);
        this.requestId = requestId;
        this.requestSender = requestSender;
        this.queryTimeoutTimer = this.getContext().system().scheduler().scheduleOnce(
                timeout, this.getSelf(), new CollectionTimeout(), this.getContext().dispatcher(), this.getSelf());
    }

    @Override
    public void preStart() throws Exception {
        logger.info("DeviceGroupQueryActor started and send TemperatureRequest to [{}] devices.", this.deviceActorRefs.size());
        this.deviceActorRefs.keySet().stream()
                .forEach(this::sendTemperatureRequest);
    }

    private void sendTemperatureRequest(ActorRef deviceActorRef) {
        this.getContext().watch(deviceActorRef);
        deviceActorRef.tell(new TemperatureRequest(1L), this.getSelf());
    }

    @Override
    public void postStop() throws Exception {
        this.queryTimeoutTimer.cancel();
    }

    @Override
    public Receive createReceive() {
        return this.newReceive(new HashMap<>(this.deviceActorRefs), new HashMap<>());
    }

    private Receive newReceive(Map<ActorRef, String> pendingDeviceActors, Map<String, Temperature> temperatures) {
        return this.receiveBuilder()
                .match(Terminated.class,
                        terminated -> this.onDeviceTerminated(pendingDeviceActors, temperatures, terminated))
                .match(CollectionTimeout.class,
                        collectionTimeout -> this.onCollectionTimeout(pendingDeviceActors, temperatures))
                .match(TemperatureResponse.class,
                        temperatureResponse -> this.onTemperatureResponse(pendingDeviceActors, temperatures, temperatureResponse))
                .build();
    }

    private void onDeviceTerminated(Map<ActorRef, String> pendingDeviceActors, Map<String, Temperature> temperatures,
                                    Terminated terminated) {
        this.receivedResponse(pendingDeviceActors, temperatures, terminated.actor(), new DeviceNotAvailable());
    }

    private void onCollectionTimeout(Map<ActorRef, String> pendingDeviceActors, Map<String, Temperature> temperatures) {
        pendingDeviceActors.values().stream()
                .forEach(deviceId -> temperatures.put(deviceId, new DeviceTimeOut()));
        this.requestSender.tell(new AllTemperaturesResponse(this.requestId, temperatures), this.getSelf());
        this.getContext().stop(this.getSelf());
    }

    private void onTemperatureResponse(Map<ActorRef, String> pendingDeviceActors, Map<String, Temperature> temperatures,
                                       TemperatureResponse temperatureResponse) {
        Temperature temperatureResult = temperatureResponse.getValue()
                .map(TemperatureValue::new)
                .map(this::toTemperature)
                .orElse(new TemperatureNotAvailable());
        this.receivedResponse(pendingDeviceActors, temperatures, this.getSender(), temperatureResult);
    }

    private Temperature toTemperature(Temperature temperature) {
        return temperature;
    }

    private void receivedResponse(Map<ActorRef, String> pendingDeviceActors, Map<String, Temperature> temperatures,
                                  ActorRef deviceActorRef, Temperature temperature) {
        pendingDeviceActors.computeIfPresent(deviceActorRef, (deviceActor, deviceId) -> {
            this.getContext().unwatch(deviceActor);
            temperatures.put(deviceId, temperature);
            return null;
        });
        if (!pendingDeviceActors.isEmpty()) {
            this.getContext().become(this.newReceive(pendingDeviceActors, temperatures));
            return;
        }
        this.requestSender.tell(new AllTemperaturesResponse(this.requestId, temperatures), this.getSelf());
        this.getContext().stop(this.getSelf());
    }
}
