package com.safedriving.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.entity.AccidentBlackspot;
import com.safedriving.entity.Driver;
import com.safedriving.entity.SafetyAlertLog;
import com.safedriving.entity.ShiftSession;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.enums.IncidentSeverity;
import com.safedriving.entity.enums.SafetyAlertType;
import com.safedriving.entity.enums.ShiftStatus;
import com.safedriving.repository.AccidentBlackspotRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.SafetyAlertLogRepository;
import com.safedriving.repository.ShiftSessionRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.GeofenceService;
import com.safedriving.service.MqttPublisherService;
import com.safedriving.service.SseBroadcasterService;
import com.safedriving.util.HaversineUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceServiceImpl implements GeofenceService {

    private final AccidentBlackspotRepository accidentBlackspotRepository;
    private final SafetyAlertLogRepository safetyAlertLogRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final ShiftSessionRepository shiftSessionRepository;
    private final SseBroadcasterService sseBroadcasterService;
    private final MqttPublisherService mqttPublisherService;
    private final ObjectMapper objectMapper;

    // Cooldown 45 giây cho mỗi cặp (vehicleId, blackspotId) để tránh spam cảnh báo liên tục khi xe di chuyển trong vùng
    private final Map<String, Long> alertCooldownMap = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 45_000L;

    @Override
    public List<AccidentBlackspot> getActiveBlackspots() {
        return accidentBlackspotRepository.findByIsActiveTrueAndIsDeletedFalse();
    }

    @Override
    @Transactional
    public void checkBlackspotGeofence(String vehicleId, double currentLat, double currentLng, Double speedKmh) {
        if (vehicleId == null || vehicleId.isBlank()) {
            return;
        }

        List<AccidentBlackspot> blackspots = getActiveBlackspots();
        if (blackspots.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();

        for (AccidentBlackspot spot : blackspots) {
            double distanceMeters = HaversineUtil.calculateDistanceMeters(
                    currentLat, currentLng,
                    spot.getLatitude(), spot.getLongitude()
            );

            double radius = spot.getRadiusMeters() != null ? spot.getRadiusMeters() : 200.0;

            if (distanceMeters <= radius) {
                String cooldownKey = vehicleId + "_" + spot.getId();
                Long lastAlertTime = alertCooldownMap.get(cooldownKey);

                if (lastAlertTime == null || (now - lastAlertTime) > COOLDOWN_MS) {
                    alertCooldownMap.put(cooldownKey, now);
                    triggerBlackspotAlert(vehicleId, spot, distanceMeters, currentLat, currentLng, speedKmh);
                }
            }
        }
    }

    private void triggerBlackspotAlert(String vehicleId, AccidentBlackspot spot, double distanceMeters,
                                       double currentLat, double currentLng, Double speedKmh) {
        log.warn("🚨 [GEOFENCE TRIGGER] Xe {} đã tiến vào vùng nguy hiểm: {} (Khoảng cách: {}m <= Bán kính: {}m)",
                vehicleId, spot.getName(), Math.round(distanceMeters), Math.round(spot.getRadiusMeters()));

        // Tìm thông tin xe và ca lái
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId);
        if (vehicleOpt.isEmpty()) {
            vehicleOpt = vehicleRepository.findByPlateNumberAndIsDeletedFalse(vehicleId);
        }
        Vehicle vehicle = vehicleOpt.orElse(null);

        Driver driver = null;
        ShiftSession shift = null;

        if (vehicle != null) {
            Optional<ShiftSession> shiftOpt = shiftSessionRepository.findByVehicleIdAndStatus(
                    vehicle.getId(), ShiftStatus.ACTIVE);
            if (shiftOpt.isPresent()) {
                shift = shiftOpt.get();
                driver = shift.getDriver();
            } else {
                Optional<Driver> driverOpt = driverRepository.findByVehicleIdAndIsDeletedFalse(vehicle.getId());
                driver = driverOpt.orElse(null);
            }
        }

        // 1. Lưu bản ghi SafetyAlertLog loại BLACKSPOT_WARNING
        if (vehicle != null) {
            SafetyAlertLog alertLog = SafetyAlertLog.builder()
                    .shift(shift)
                    .vehicle(vehicle)
                    .driver(driver)
                    .alertType(SafetyAlertType.BLACKSPOT_WARNING)
                    .severity(IncidentSeverity.HIGH)
                    .timestamp(LocalDateTime.now())
                    .latitude(currentLat)
                    .longitude(currentLng)
                    .speedKmh(speedKmh)
                    .isSynced(true)
                    .build();
            safetyAlertLogRepository.save(alertLog);
        }

        // 2. Tạo Payload cảnh báo chuẩn
        Map<String, Object> alertPayload = new HashMap<>();
        alertPayload.put("event", "BLACKSPOT_WARNING");
        alertPayload.put("vehicle_id", vehicle != null ? vehicle.getId() : vehicleId);
        alertPayload.put("plate_number", vehicle != null ? vehicle.getPlateNumber() : vehicleId);
        alertPayload.put("driver_name", driver != null && driver.getStaff() != null
                ? driver.getStaff().getLastName() + " " + driver.getStaff().getFirstName()
                : "Tài xế");
        alertPayload.put("blackspot_id", spot.getId());
        alertPayload.put("blackspot_name", spot.getName());
        alertPayload.put("hazard_type", spot.getHazardType().name());
        alertPayload.put("distance_meters", Math.round(distanceMeters));
        alertPayload.put("radius_meters", spot.getRadiusMeters());
        alertPayload.put("latitude", currentLat);
        alertPayload.put("longitude", currentLng);
        alertPayload.put("speed_kmh", speedKmh);
        alertPayload.put("description", spot.getDescription());
        alertPayload.put("feedback_count", spot.getFeedbackCount());
        alertPayload.put("message", String.format("CẢNH BÁO: Sắp tới điểm đen tai nạn '%s' (cách %dm)! Chú ý giảm tốc độ.",
                spot.getName(), Math.round(distanceMeters)));
        alertPayload.put("timestamp", System.currentTimeMillis());

        try {
            String jsonPayload = objectMapper.writeValueAsString(alertPayload);

            // 3. Bắn SSE Realtime xuống Web Dashboard
            sseBroadcasterService.broadcast("alert", alertPayload);
            sseBroadcasterService.broadcast("geofence_warning", alertPayload);

            // 4. Bắn MQTT thông báo xuống Điện thoại Android (AppDetect)
            String targetTopic = String.format("driverguard/warnings/%s", vehicle != null ? vehicle.getId() : vehicleId);
            mqttPublisherService.publish(targetTopic, jsonPayload);
            mqttPublisherService.publish("driverguard/warnings/all", jsonPayload);

            log.info("📢 Đã phát cảnh báo Geofence thành công tới SSE và MQTT topic [{}]", targetTopic);

        } catch (Exception e) {
            log.error("❌ Lỗi khi đóng gói JSON cảnh báo geofence: {}", e.getMessage(), e);
        }
    }
}
