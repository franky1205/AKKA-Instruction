package com.lightbend.akka.sample.iot;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.testkit.javadsl.TestKit;
import com.google.common.collect.ImmutableMap;
import com.lightbend.akka.sample.AbstractActorTest;
import com.lightbend.akka.sample.iot.TemperatureStatus.AllTemperaturesResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.DeviceNotAvailable;
import com.lightbend.akka.sample.iot.TemperatureStatus.DeviceTimeOut;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureNotAvailable;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureRequest;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureResponse;
import com.lightbend.akka.sample.iot.TemperatureStatus.TemperatureValue;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by Frankie on 2018/2/27.
 */
public class DeviceGroupQueryActorTest extends AbstractActorTest {

    @Test
    public void testWithAvailableTemperatureDevices() {
        TestKit device1 = new TestKit(this.system);
        TestKit device2 = new TestKit(this.system);

        ActorRef deviceGroupQueryActor = this.system.actorOf(DeviceGroupQueryActor.props(
                ImmutableMap.of(device1.getRef(), "device1", device2.getRef(), "device2"),
                0L,
                this.probe.getRef(),
                new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(1L, device1.expectMsgClass(TemperatureRequest.class).getRequestId());
        assertEquals(1L, device2.expectMsgClass(TemperatureRequest.class).getRequestId());

        deviceGroupQueryActor.tell(new TemperatureResponse(1L, Optional.of(1.0)), device1.getRef());
        deviceGroupQueryActor.tell(new TemperatureResponse(1L, Optional.of(2.0)), device2.getRef());

        AllTemperaturesResponse response = this.probe.expectMsgClass(AllTemperaturesResponse.class);
        assertEquals(0L, response.getRequestId());

        assertTrue(response.getAllTemperatures().containsKey("device1"));
        assertEquals(response.getAllTemperatures().get("device1"), new TemperatureValue(1.0));
        assertTrue(response.getAllTemperatures().containsKey("device2"));
        assertEquals(response.getAllTemperatures().get("device2"), new TemperatureValue(2.0));
    }

    @Test
    public void testWithNotAvailableTemperatureDevice() {
        TestKit device1 = new TestKit(this.system);
        TestKit device2 = new TestKit(this.system);

        ActorRef deviceGroupQueryActor = this.system.actorOf(DeviceGroupQueryActor.props(
                ImmutableMap.of(device1.getRef(), "device1", device2.getRef(), "device2"),
                0L,
                this.probe.getRef(),
                new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(1L, device1.expectMsgClass(TemperatureRequest.class).getRequestId());
        assertEquals(1L, device2.expectMsgClass(TemperatureRequest.class).getRequestId());

        deviceGroupQueryActor.tell(new TemperatureResponse(1L, Optional.of(1.0)), device1.getRef());
        deviceGroupQueryActor.tell(new TemperatureResponse(1L, Optional.empty()), device2.getRef());

        AllTemperaturesResponse response = this.probe.expectMsgClass(AllTemperaturesResponse.class);
        assertEquals(0L, response.getRequestId());

        assertTrue(response.getAllTemperatures().containsKey("device1"));
        assertEquals(response.getAllTemperatures().get("device1"), new TemperatureValue(1.0));
        assertTrue(response.getAllTemperatures().containsKey("device2"));
        assertEquals(response.getAllTemperatures().get("device2").getClass(), TemperatureNotAvailable.class);
    }

    @Test
    public void testWithNotAvailableDevice() {
        TestKit device1 = new TestKit(this.system);
        TestKit device2 = new TestKit(this.system);

        ActorRef deviceGroupQueryActor = this.system.actorOf(DeviceGroupQueryActor.props(
                ImmutableMap.of(device1.getRef(), "device1", device2.getRef(), "device2"),
                0L,
                this.probe.getRef(),
                new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(1L, device1.expectMsgClass(TemperatureRequest.class).getRequestId());
        assertEquals(1L, device2.expectMsgClass(TemperatureRequest.class).getRequestId());

        deviceGroupQueryActor.tell(new TemperatureResponse(1L, Optional.of(1.0)), device1.getRef());
        device2.getRef().tell(PoisonPill.getInstance(), ActorRef.noSender());

        AllTemperaturesResponse response = this.probe.expectMsgClass(AllTemperaturesResponse.class);
        assertEquals(0L, response.getRequestId());

        assertTrue(response.getAllTemperatures().containsKey("device1"));
        assertEquals(response.getAllTemperatures().get("device1"), new TemperatureValue(1.0));
        assertTrue(response.getAllTemperatures().containsKey("device2"));
        assertEquals(response.getAllTemperatures().get("device2").getClass(), DeviceNotAvailable.class);
    }

    @Test
    public void testWithAvailableTemperatureDevicesBeforeTerminated() {
        TestKit device1 = new TestKit(this.system);
        TestKit device2 = new TestKit(this.system);

        ActorRef deviceGroupQueryActor = this.system.actorOf(DeviceGroupQueryActor.props(
                ImmutableMap.of(device1.getRef(), "device1", device2.getRef(), "device2"),
                0L,
                this.probe.getRef(),
                new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(1L, device1.expectMsgClass(TemperatureRequest.class).getRequestId());
        assertEquals(1L, device2.expectMsgClass(TemperatureRequest.class).getRequestId());

        deviceGroupQueryActor.tell(new TemperatureResponse(1L, Optional.of(1.0)), device1.getRef());
        deviceGroupQueryActor.tell(new TemperatureResponse(1L, Optional.of(2.0)), device2.getRef());
        device2.getRef().tell(PoisonPill.getInstance(), ActorRef.noSender());

        AllTemperaturesResponse response = this.probe.expectMsgClass(AllTemperaturesResponse.class);
        assertEquals(0L, response.getRequestId());

        assertTrue(response.getAllTemperatures().containsKey("device1"));
        assertEquals(response.getAllTemperatures().get("device1"), new TemperatureValue(1.0));
        assertTrue(response.getAllTemperatures().containsKey("device2"));
        assertEquals(response.getAllTemperatures().get("device2"), new TemperatureValue(2.0));
    }

    @Test
    public void testWithTimeoutDevice() {
        TestKit device1 = new TestKit(this.system);
        TestKit device2 = new TestKit(this.system);

        ActorRef deviceGroupQueryActor = this.system.actorOf(DeviceGroupQueryActor.props(
                ImmutableMap.of(device1.getRef(), "device1", device2.getRef(), "device2"),
                0L,
                this.probe.getRef(),
                new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(1L, device1.expectMsgClass(TemperatureRequest.class).getRequestId());
        assertEquals(1L, device2.expectMsgClass(TemperatureRequest.class).getRequestId());

        deviceGroupQueryActor.tell(new TemperatureResponse(1L, Optional.of(1.0)), device1.getRef());

        AllTemperaturesResponse response = this.probe.expectMsgClass(
                new FiniteDuration(5, TimeUnit.SECONDS), AllTemperaturesResponse.class);

        assertEquals(0L, response.getRequestId());

        assertTrue(response.getAllTemperatures().containsKey("device1"));
        assertEquals(response.getAllTemperatures().get("device1"), new TemperatureValue(1.0));
        assertTrue(response.getAllTemperatures().containsKey("device2"));
        assertEquals(response.getAllTemperatures().get("device2").getClass(), DeviceTimeOut.class);
    }
}