package com.lightbend.akka.sample.iot;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Frankie on 2018/2/25.
 */
public class TemperatureStatus {

    public static class AllTemperaturesRequest {

        private final long requestId;

        public AllTemperaturesRequest(long requestId) {
            this.requestId = requestId;
        }

        public long getRequestId() {
            return requestId;
        }
    }

    public static class AllTemperaturesResponse {

        private final long requestId;

        private final Map<String, Temperature> allTemperatures;

        public AllTemperaturesResponse(long requestId, Map<String, Temperature> allTemperatures) {
            this.requestId = requestId;
            this.allTemperatures = allTemperatures;
        }

        public long getRequestId() {
            return requestId;
        }

        public Map<String, Temperature> getAllTemperatures() {
            return allTemperatures;
        }
    }

    public interface Temperature { }

    public static class TemperatureValue implements Temperature {

        private final double value;

        public TemperatureValue(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }

    public static class TemperatureNotAvailable implements Temperature { }

    public static class DeviceNotAvailable implements Temperature { }

    public static class DeviceTimeOut implements Temperature { }

    public static class CollectionTimeout { }

    public static class TemperatureUpdateRequest {

        private final long requestId;
        private final double updateValue;

        public TemperatureUpdateRequest(long requestId, double updateValue) {
            this.requestId = requestId;
            this.updateValue = updateValue;
        }

        public long getRequestId() {
            return requestId;
        }

        public double getUpdateValue() {
            return updateValue;
        }
    }

    public static class TemperatureUpdateResponse {

        private final long requestId;

        public TemperatureUpdateResponse(long requestId) {
            this.requestId = requestId;
        }

        public long getRequestId() {
            return requestId;
        }
    }

    public static class TemperatureRequest {

        private final long requestId;

        public TemperatureRequest(long requestId) {
            this.requestId = requestId;
        }

        public long getRequestId() {
            return requestId;
        }
    }

    public static class TemperatureResponse {

        private final long requestId;

        private final Optional<Double> value;

        public TemperatureResponse(long requestId, Optional<Double> value) {
            this.requestId = requestId;
            this.value = value;
        }

        public long getRequestId() {
            return requestId;
        }

        public Optional<Double> getValue() {
            return value;
        }
    }
}
