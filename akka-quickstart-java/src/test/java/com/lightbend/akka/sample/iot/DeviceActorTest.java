package com.lightbend.akka.sample.iot;

import akka.actor.ActorRef;
import com.lightbend.akka.sample.AbstractActorTest;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureUpdateRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureUpdateResponse;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Created by Frankie on 2018/2/19.
 */
public class DeviceActorTest extends AbstractActorTest {

    @Test
    public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
        ActorRef deviceActor = system.actorOf(DeviceActor.props("group", "device"), "deviceActor");
        deviceActor.tell(new TemperatureRequest(42L), probe.getRef());
        TemperatureResponse response = probe.expectMsgClass(TemperatureResponse.class);
        assertEquals(42L, response.getRequestId());
        assertEquals(Optional.empty(), response.getValue());
    }

    @Test
    public void testReplyWithUpdatedTemperature() {
        ActorRef deviceActor = system.actorOf(DeviceActor.props("group", "device"), "deviceActor");
        deviceActor.tell(new TemperatureUpdateRequest(1L, 18.5), probe.getRef());
        deviceActor.tell(new TemperatureRequest(2L), probe.getRef());

        assertEquals(1L, probe.expectMsgClass(TemperatureUpdateResponse.class).getRequestId());
        TemperatureResponse temperatureResponse = probe.expectMsgClass(TemperatureResponse.class);
        assertEquals(2L, temperatureResponse.getRequestId());
        assertEquals(Optional.of(18.5), temperatureResponse.getValue());
    }

    @Test
    public void testReplyToDeviceTrackerRequest() {
        ActorRef deviceActor = system.actorOf(DeviceActor.props("group", "device"), "deviceActor");
        deviceActor.tell(new DeviceTracker.DeviceTrackingRequest("group", "device"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTracker.DeviceTrackingResponse.class);

        assertEquals(deviceActor, probe.getLastSender());
    }

    @Test
    public void testIgnoreInvalidGroupIdOrDeviceId() {
        ActorRef deviceActor = system.actorOf(DeviceActor.props("group", "device"), "deviceActor");
        deviceActor.tell(new DeviceTracker.DeviceTrackingRequest("wrongGroup", "device"), this.probe.getRef());
        this.probe.expectNoMsg();

        deviceActor.tell(new DeviceTracker.DeviceTrackingRequest("group", "wrongDevice"), this.probe.getRef());
        this.probe.expectNoMsg();
    }
}
