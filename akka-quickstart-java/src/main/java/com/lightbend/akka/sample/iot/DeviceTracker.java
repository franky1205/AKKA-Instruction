package com.lightbend.akka.sample.iot;

import akka.actor.ActorRef;

import java.util.List;
import java.util.Set;

/**
 * Created by Frankie on 2018/2/20.
 */
public class DeviceTracker {

    public static final class GroupActorRefRequest {

        private final long requestId;

        private final String groupId;

        public GroupActorRefRequest(long requestId, String groupId) {
            this.requestId = requestId;
            this.groupId = groupId;
        }

        public long getRequestId() {
            return this.requestId;
        }

        public String getGroupId() {
            return groupId;
        }
    }

    public static final class GroupActorRefResponse {

        private final long requestId;

        private final ActorRef groupActorRef;

        public GroupActorRefResponse(long requestId, ActorRef groupActorRef) {
            this.requestId = requestId;
            this.groupActorRef = groupActorRef;
        }

        public long getRequestId() {
            return requestId;
        }

        public ActorRef getGroupActorRef() {
            return groupActorRef;
        }
    }

    public static final class DeviceListRequest {

        private final long requestId;

        public DeviceListRequest(long requestId) {
            this.requestId = requestId;
        }

        public long getRequestId() {
            return requestId;
        }
    }

    public static final class DeviceListResponse {

        private final long requestId;
        private final Set<String> deviceIds;

        public DeviceListResponse(long requestId, Set<String> deviceIds) {
            this.requestId = requestId;
            this.deviceIds = deviceIds;
        }

        public long getRequestId() {
            return requestId;
        }

        public Set<String> getDeviceIds() {
            return deviceIds;
        }
    }

    public static final class DeviceTrackingRequest {

        private final String groupId;
        private final String deviceId;

        public DeviceTrackingRequest(String groupId, String deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getDeviceId() {
            return deviceId;
        }
    }

    public static final class DeviceTrackingResponse {

    }
}
