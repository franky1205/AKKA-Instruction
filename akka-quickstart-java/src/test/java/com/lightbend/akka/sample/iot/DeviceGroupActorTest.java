package com.lightbend.akka.sample.iot;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import com.lightbend.akka.sample.AbstractActorTest;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceListResponse;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceTrackingRequest;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceTrackingResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.AllTemperaturesRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.AllTemperaturesResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.Temperature;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureNotAvailable;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureUpdateRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureUpdateResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureValue;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Frankie on 2018/2/20.
 */
public class DeviceGroupActorTest extends AbstractActorTest {

    @Test
    public void testReplyWithTwoDifferentDeviceActors() {
        ActorRef deviceGroupActor = this.system.actorOf(DeviceGroupActor.props("groupId"), "deviceGroupActor");
        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device1"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);
        ActorRef deviceActorRef1 = this.probe.getLastSender();

        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device2"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);
        ActorRef deviceActorRef2 = this.probe.getLastSender();
        assertNotEquals(deviceActorRef1, deviceActorRef2);
    }

    @Test
    public void testReplyWithIdenticalDeviceActor() {
        ActorRef deviceGroupActor = this.system.actorOf(DeviceGroupActor.props("groupId"), "deviceGroupActor");
        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device1"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);
        ActorRef deviceActorRefFirstAttempt = this.probe.getLastSender();

        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device1"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);
        ActorRef deviceActorRefSecondAttempt = this.probe.getLastSender();

        assertEquals(deviceActorRefFirstAttempt, deviceActorRefSecondAttempt);
    }

    @Test
    public void testIgnoreRequestsForWrongGroupId() {
        ActorRef deviceGroupActor = this.system.actorOf(DeviceGroupActor.props("groupId"), "deviceGroupActor");
        deviceGroupActor.tell(new DeviceTrackingRequest("wrongGroupId", "device1"), this.probe.getRef());
        this.probe.expectNoMsg();
    }

    @Test
    public void testListActiveDevices() {
        ActorRef deviceGroupActor = this.system.actorOf(DeviceGroupActor.props("groupId"), "deviceGroupActor");
        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device1"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device2"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device3"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceGroupActor.tell(new DeviceTracker.DeviceListRequest(1L), this.probe.getRef());
        DeviceListResponse deviceList = this.probe.expectMsgClass(DeviceListResponse.class);

        assertEquals(1L, deviceList.getRequestId());
        assertEquals(Stream.of("device1", "device2", "device3").collect(Collectors.toSet()), deviceList.getDeviceIds());
    }

    @Test
    public void testListActiveDevicesWithOneShutdown() {
        ActorRef deviceGroupActor = this.system.actorOf(DeviceGroupActor.props("groupId"), "deviceGroupActor");
        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device1"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device2"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceGroupActor.tell(new DeviceTrackingRequest("groupId", "device3"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);
        ActorRef device3ActorRef = this.probe.getLastSender();

        deviceGroupActor.tell(new DeviceTracker.DeviceListRequest(1L), this.probe.getRef());
        DeviceListResponse deviceList = this.probe.expectMsgClass(DeviceListResponse.class);

        assertEquals(1L, deviceList.getRequestId());
        assertEquals(Stream.of("device1", "device2", "device3").collect(Collectors.toSet()), deviceList.getDeviceIds());

        this.probe.watch(device3ActorRef);
        device3ActorRef.tell(PoisonPill.getInstance(), ActorRef.noSender());
        this.probe.expectTerminated(device3ActorRef);

        AtomicInteger attemptCounter = new AtomicInteger(0);
        probe.awaitAssert(() -> {
            System.out.println(Thread.currentThread().getName() + "-Attemp Count: [" + attemptCounter.incrementAndGet() + "]");
            deviceGroupActor.tell(new DeviceTracker.DeviceListRequest(2L), this.probe.getRef());
            DeviceListResponse nextDeviceList = this.probe.expectMsgClass(DeviceListResponse.class);

            assertEquals(2L, nextDeviceList.getRequestId());
            assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), nextDeviceList.getDeviceIds());
            return null;
        });
    }

    @Test
    public void testCollectTemperaturesFromAllActiveDevices() {
        ActorRef deviceGroupActor = system.actorOf(DeviceGroupActor.props("group1"), "deviceGroupActor");

        this.registerAndUpdateDevice(deviceGroupActor, "group1", "device1", 1L, 1.0);
        this.registerAndUpdateDevice(deviceGroupActor, "group1", "device2", 2L, -2);
        this.registerAndUpdateDevice(deviceGroupActor, "group1", "device3", 3L, 3.0);

        deviceGroupActor.tell(new AllTemperaturesRequest(100L), this.probe.getRef());
        AllTemperaturesResponse temperaturesResponse = this.probe.expectMsgClass(AllTemperaturesResponse.class);

        Map<String, Temperature> allTemperatures = temperaturesResponse.getAllTemperatures();
        assertTrue(allTemperatures.containsKey("device1"));
        assertEquals(allTemperatures.get("device1"), new TemperatureValue(1.0));

        assertTrue(allTemperatures.containsKey("device2"));
        assertEquals(allTemperatures.get("device2").getClass(), TemperatureNotAvailable.class);

        assertTrue(allTemperatures.containsKey("device3"));
        assertEquals(allTemperatures.get("device3"), new TemperatureValue(3.0));
    }

    private void registerAndUpdateDevice(ActorRef deviceGroupActor, String groupId, String deviceId, long requestId, double updateValue) {
        deviceGroupActor.tell(new DeviceTrackingRequest(groupId, deviceId), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);
        ActorRef deviceRef = this.probe.getLastSender();
        if (updateValue < 0) {
            return ;
        }
        deviceRef.tell(new TemperatureUpdateRequest(requestId, updateValue), this.probe.getRef());
        assertEquals(requestId, this.probe.expectMsgClass(TemperatureUpdateResponse.class).getRequestId());
    }
}
