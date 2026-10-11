package com.safedriving.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cấu hình kết nối MQTT cấp cho thiết bị Mobile trong ca lái")
public class MqttConfigResponse {

    @JsonProperty("broker_endpoint")
    @Schema(description = "Địa chỉ kết nối MQTT Broker (TCP)", example = "tcp://localhost:1883")
    private String brokerEndpoint;

    @JsonProperty("client_id")
    @Schema(description = "Client ID duy nhất tránh đứt kết nối (Flapping)", example = "DRIVERGUARD_f8a12345_s9999999_8f2a")
    private String clientId;

    @JsonProperty("telemetry_topic")
    @Schema(description = "Topic gửi tọa độ GPS định kỳ (QoS 0)", example = "driverguard/telemetry/f8a12345-0000-0000-0000-000000000001/gps")
    private String telemetryTopic;

    @JsonProperty("alert_topic")
    @Schema(description = "Topic gửi cảnh báo an toàn vi phạm (QoS 1)", example = "driverguard/alerts/f8a12345-0000-0000-0000-000000000001")
    private String alertTopic;

    @JsonProperty("heartbeat_topic")
    @Schema(description = "Topic gửi trạng thái thiết bị pin, nhiệt độ, FPS", example = "driverguard/heartbeat/device_xyz")
    private String heartbeatTopic;
}
