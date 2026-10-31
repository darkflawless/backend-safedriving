package com.safedriving.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.entity.DeviceHeartbeat;
import com.safedriving.entity.Driver;
import com.safedriving.entity.HitlReviewCase;
import com.safedriving.entity.SafetyAlertLog;
import com.safedriving.entity.ShiftSession;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.enums.HitlStatus;
import com.safedriving.entity.enums.IncidentSeverity;
import com.safedriving.entity.enums.SafetyAlertType;
import com.safedriving.entity.enums.ShiftStatus;
import com.safedriving.repository.DeviceHeartbeatRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.HitlReviewCaseRepository;
import com.safedriving.repository.SafetyAlertLogRepository;
import com.safedriving.repository.ShiftSessionRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.MqttMessageDispatcher;
import com.safedriving.service.SseBroadcasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttMessageDispatcherImpl implements MqttMessageDispatcher {

    private final ObjectMapper objectMapper;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final ShiftSessionRepository shiftSessionRepository;
    private final SafetyAlertLogRepository safetyAlertLogRepository;
    private final HitlReviewCaseRepository hitlReviewCaseRepository;
    private final DeviceHeartbeatRepository deviceHeartbeatRepository;
    private final SseBroadcasterService sseBroadcasterService;
    private final com.safedriving.service.GeofenceService geofenceService;

    // Cache thông tin biển số và tên tài xế 30 giây để tránh query liên tục khi GPS gửi 1Hz
    private final java.util.Map<String, CachedVehicleInfo> vehicleCache = new java.util.concurrent.ConcurrentHashMap<>();

    private record CachedVehicleInfo(String plateNumber, String driverName, long cachedAt) {
        boolean isValid() {
            return System.currentTimeMillis() - cachedAt < 30_000L;
        }
    }

    @Override
    @Transactional
    public void dispatch(String topic, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            if (topic.contains("/alerts/")) {
                log.info("🚨 [MQTT Ingest Alert] Topic: {}, Payload: {}", topic, payload);
                handleAlertMessage(root);
            } else if (topic.contains("/telemetry/") || topic.endsWith("/gps")) {
                log.debug("📡 [MQTT Telemetry] Topic: {}", topic);
                handleTelemetryMessage(root);
            } else if (topic.contains("/heartbeat/")) {
                log.debug("💓 [MQTT Heartbeat] Topic: {}", topic);
                handleHeartbeatMessage(root);
            } else {
                log.debug("Bỏ qua topic không xác định: {}", topic);
            }

        } catch (Exception e) {
            log.error("❌ Lỗi xử lý MQTT message [topic={}]: {}", topic, e.getMessage(), e);
        }
    }

    private void handleAlertMessage(JsonNode root) {
        String vehicleId = root.path("vehicle_id").asText(null);
        if (vehicleId == null || vehicleId.isBlank()) {
            log.warn("Bỏ qua alert do không có vehicle_id");
            return;
        }

        // Tìm xe
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId);
        if (vehicleOpt.isEmpty()) {
            vehicleOpt = vehicleRepository.findByPlateNumberAndIsDeletedFalse(vehicleId);
        }
        if (vehicleOpt.isEmpty()) {
            log.warn("Không tìm thấy phương tiện với ID/Biển số: {}", vehicleId);
            return;
        }
        Vehicle vehicle = vehicleOpt.get();

        // Tìm ca lái và tài xế
        String shiftId = root.path("shift_id").asText(null);
        ShiftSession shift = null;
        if (shiftId != null && !shiftId.isBlank()) {
            shift = shiftSessionRepository.findById(shiftId).orElse(null);
        }
        if (shift == null) {
            shift = shiftSessionRepository.findByVehicleIdAndStatus(vehicle.getId(), ShiftStatus.ACTIVE).orElse(null);
        }

        String driverId = root.path("driver_id").asText(null);
        Driver driver = null;
        if (driverId != null && !driverId.isBlank()) {
            driver = driverRepository.findByIdAndIsDeletedFalse(driverId).orElse(null);
        }
        if (driver == null && shift != null) {
            driver = shift.getDriver();
        }

        // Parse Alert Type & Severity
        String alertTypeStr = root.path("alert_type").asText("DROWSINESS");
        SafetyAlertType alertType;
        try {
            alertType = SafetyAlertType.valueOf(alertTypeStr.toUpperCase());
        } catch (Exception e) {
            alertType = SafetyAlertType.DROWSINESS;
        }

        String severityStr = root.path("severity").asText("MEDIUM");
        IncidentSeverity severity;
        try {
            severity = IncidentSeverity.valueOf(severityStr.toUpperCase());
        } catch (Exception e) {
            severity = IncidentSeverity.MEDIUM;
        }

        LocalDateTime eventTime = parseTimestamp(root.path("timestamp"));

        // Telemetry
        JsonNode telemetryNode = root.path("telemetry");
        Double lat = telemetryNode.has("latitude") ? telemetryNode.get("latitude").asDouble() : null;
        Double lng = telemetryNode.has("longitude") ? telemetryNode.get("longitude").asDouble() : null;
        Double speedKmh = telemetryNode.has("speed_kmh") ? telemetryNode.get("speed_kmh").asDouble() : null;

        // Biometrics
        JsonNode bioNode = root.path("biometrics");
        Float ear = bioNode.has("ear") ? (float) bioNode.get("ear").asDouble() : null;
        Float mar = bioNode.has("mar") ? (float) bioNode.get("mar").asDouble() : null;
        Float pitch = bioNode.has("pitch_deg") ? (float) bioNode.get("pitch_deg").asDouble() : null;
        Float yaw = bioNode.has("yaw_deg") ? (float) bioNode.get("yaw_deg").asDouble() : null;
        Float perclos = bioNode.has("perclos_60s") ? (float) bioNode.get("perclos_60s").asDouble() : null;

        // Evidence
        JsonNode evidenceNode = root.path("evidence");
        String snapshotUrl = evidenceNode.has("snapshot_url") ? evidenceNode.get("snapshot_url").asText(null) : null;

        SafetyAlertLog alertLog = SafetyAlertLog.builder()
                .shift(shift)
                .vehicle(vehicle)
                .driver(driver)
                .alertType(alertType)
                .severity(severity)
                .timestamp(eventTime)
                .latitude(lat)
                .longitude(lng)
                .speedKmh(speedKmh)
                .earValue(ear)
                .marValue(mar)
                .pitchValue(pitch)
                .yawValue(yaw)
                .perclosValue(perclos)
                .snapshotUrl(snapshotUrl)
                .isSynced(true)
                .build();

        alertLog = safetyAlertLogRepository.save(alertLog);
        log.info("🚨 [Alert Saved] ID={}, Vehicle={}, Type={}, Severity={}",
                alertLog.getId(), vehicle.getPlateNumber(), alertType, severity);

        // Nếu mức độ là HIGH hoặc CRITICAL, tự động đưa vào hàng đợi HITL (Human-in-the-Loop)
        if (severity == IncidentSeverity.HIGH || severity == IncidentSeverity.CRITICAL) {
            HitlReviewCase reviewCase = HitlReviewCase.builder()
                    .alert(alertLog)
                    .status(HitlStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            hitlReviewCaseRepository.save(reviewCase);
            log.info("🔔 [HITL Auto-Created] Tạo ca kiểm duyệt PENDING: caseId={}, alertId={}",
                    reviewCase.getId(), alertLog.getId());
        }

        // Phát sóng sự kiện SSE xuống Web Dashboard
        sseBroadcasterService.broadcast("alert", root);
    }

    private void handleTelemetryMessage(JsonNode root) {
        try {
            if (root instanceof ObjectNode objectNode) {
                String vehicleId = root.path("vehicle_id").asText(null);
                if (vehicleId != null && !vehicleId.isBlank()) {
                    CachedVehicleInfo cached = vehicleCache.get(vehicleId);
                    if (cached != null && cached.isValid()) {
                        if (cached.plateNumber() != null) objectNode.put("plate_number", cached.plateNumber());
                        if (cached.driverName() != null) objectNode.put("driver_name", cached.driverName());
                    } else {
                        Optional<Vehicle> vehicleOpt = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId);
                        if (vehicleOpt.isEmpty()) {
                            vehicleOpt = vehicleRepository.findByPlateNumberAndIsDeletedFalse(vehicleId);
                        }
                        if (vehicleOpt.isPresent()) {
                            Vehicle v = vehicleOpt.get();
                            String plate = v.getPlateNumber();
                            objectNode.put("plate_number", plate);

                            String driverName = null;
                            Optional<Driver> driverOpt = driverRepository.findByVehicleIdAndIsDeletedFalse(v.getId());
                            if (driverOpt.isPresent() && driverOpt.get().getStaff() != null) {
                                Staff s = driverOpt.get().getStaff();
                                driverName = s.getLastName() + " " + s.getFirstName();
                                objectNode.put("driver_name", driverName);
                            }
                            vehicleCache.put(vehicleId, new CachedVehicleInfo(plate, driverName, System.currentTimeMillis()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi enrich telemetry: {}", e.getMessage());
        }

        // Phát sóng tức thời tọa độ GPS xe tới các Client Dashboard
        sseBroadcasterService.broadcast("gps", root);

        // Kiểm tra Geofence Điểm đen tai nạn (bán kính 200m)
        try {
            String vehicleId = root.path("vehicle_id").asText(null);
            Double lat = root.has("latitude") ? root.get("latitude").asDouble() : null;
            Double lng = root.has("longitude") ? root.get("longitude").asDouble() : null;
            Double speed = root.has("speed_kmh") ? root.get("speed_kmh").asDouble() : 0.0;
            if (vehicleId != null && lat != null && lng != null) {
                geofenceService.checkBlackspotGeofence(vehicleId, lat, lng, speed);
            }
        } catch (Exception e) {
            log.warn("Lỗi kiểm tra Geofence: {}", e.getMessage());
        }
    }

    private void handleHeartbeatMessage(JsonNode root) {
        try {
            String deviceId = root.path("device_id").asText("unknown_device");
            String vehicleId = root.path("vehicle_id").asText(null);
            Vehicle vehicle = null;
            if (vehicleId != null && !vehicleId.isBlank()) {
                vehicle = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId).orElse(null);
            }

            LocalDateTime timestamp = parseTimestamp(root.path("timestamp"));

            JsonNode healthNode = root.path("health");
            Integer batteryPct = healthNode.has("battery_pct") ? healthNode.get("battery_pct").asInt() : null;
            Float cpuTemp = healthNode.has("cpu_temperature") ? (float) healthNode.get("cpu_temperature").asDouble() : null;
            Float cameraFps = healthNode.has("camera_fps") ? (float) healthNode.get("camera_fps").asDouble() : null;

            DeviceHeartbeat heartbeat = DeviceHeartbeat.builder()
                    .deviceId(deviceId)
                    .vehicle(vehicle)
                    .batteryPct(batteryPct)
                    .cpuTemperature(cpuTemp)
                    .cameraFps(cameraFps)
                    .timestamp(timestamp)
                    .build();

            deviceHeartbeatRepository.save(heartbeat);

            // Phát sóng SSE
            sseBroadcasterService.broadcast("heartbeat", root);
        } catch (Exception e) {
            log.warn("Lỗi lưu DeviceHeartbeat: {}", e.getMessage());
        }
    }

    private LocalDateTime parseTimestamp(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return LocalDateTime.now();
        }
        if (node.isNumber()) {
            long ts = node.asLong();
            if (ts < 10_000_000_000L) {
                // Epoch seconds
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(ts), ZoneId.systemDefault());
            } else {
                // Epoch millis
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
            }
        }
        try {
            return LocalDateTime.parse(node.asText());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
