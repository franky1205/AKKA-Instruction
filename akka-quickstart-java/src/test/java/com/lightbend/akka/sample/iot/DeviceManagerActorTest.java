package com.lightbend.akka.sample.iot;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import com.lightbend.akka.sample.AbstractActorTest;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceTrackingRequest;
import com.lightbend.akka.sample.iot.DeviceTracker.DeviceTrackingResponse;
import com.lightbend.akka.sample.iot.DeviceTracker.GroupActorRefRequest;
import com.lightbend.akka.sample.iot.DeviceTracker.GroupActorRefResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Frankie on 2018/2/25.
 */
public class DeviceManagerActorTest extends AbstractActorTest {

    @Test
    public void testWithNewlyCreatedGroupAndDevice() {
        ActorRef deviceManagerActor = this.system.actorOf(DeviceManagerActor.props(), "DeviceManagerActor");
        deviceManagerActor.tell(new DeviceTrackingRequest("groupId", "deviceId"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);
    }

    @Test
    public void testWithShutdownOneGroup() {
        ActorRef deviceManagerActor = this.system.actorOf(DeviceManagerActor.props(), "DeviceManagerActor");
        deviceManagerActor.tell(new DeviceTrackingRequest("groupId-1", "deviceId-1"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceManagerActor.tell(new DeviceTrackingRequest("groupId-1", "deviceId-2"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceManagerActor.tell(new DeviceTrackingRequest("groupId-2", "deviceId-1"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceManagerActor.tell(new DeviceTrackingRequest("groupId-2", "deviceId-2"), this.probe.getRef());
        this.probe.expectMsgClass(DeviceTrackingResponse.class);

        deviceManagerActor.tell(new GroupActorRefRequest(1L, "groupId-2"), this.probe.getRef());
        GroupActorRefResponse groupActorRefResponse = this.probe.expectMsgClass(GroupActorRefResponse.class);
        assertEquals(1L, groupActorRefResponse.getRequestId());

        ActorRef groupActorRef = groupActorRefResponse.getGroupActorRef();
        groupActorRef.tell(PoisonPill.getInstance(), ActorRef.noSender());

        probe.awaitAssert(() -> {
            deviceManagerActor.tell(new GroupActorRefRequest(2L, "groupId-2"), this.probe.getRef());
            probe.expectNoMsg();
            return null;
        });
    }
}
