package com.safedriving.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safedriving.entity.AccidentBlackspot;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Trip;
import com.safedriving.entity.TripGpsTrack;
import com.safedriving.entity.Vehicle;
import com.safedriving.repository.AccidentBlackspotRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.TripGpsTrackRepository;
import com.safedriving.repository.TripRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.GeofenceService;
import com.safedriving.service.GpsSimulationService;
import com.safedriving.service.MqttPublisherService;
import com.safedriving.service.SseBroadcasterService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpsSimulationServiceImpl implements GpsSimulationService {

    private final ObjectMapper objectMapper;
    private final GeofenceService geofenceService;
    private final SseBroadcasterService sseBroadcasterService;
    private final MqttPublisherService mqttPublisherService;
    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripGpsTrackRepository tripGpsTrackRepository;
    private final AccidentBlackspotRepository accidentBlackspotRepository;

    // Danh sách tọa độ chuẩn [lat, lng] (đã đảo từ [lng, lat] của GeoJSON sang Leaflet format)
    private final List<double[]> waypoints = new ArrayList<>();
    private double totalDistanceMeters = 9446.7;

    // Trạng thái mô phỏng
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private String activeTripId = "trip-e2e-001";
    private String activeVehicleId = "58591121-1660-44c0-9ca6-94eeff2b3d5b";
    private String activePlateNumber = "30F-13579";
    private String activeDriverName = "Nguyễn Văn A (App Mobile)";

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> simulationTask;

    @PostConstruct
    public void initRouteCoordinates() {
        try {
            ClassPathResource resource = new ClassPathResource("simulation/hn_route_e2e.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    JsonNode root = objectMapper.readTree(is);
                    JsonNode coords = root.path("geometry").path("coordinates");
                    if (coords.isArray()) {
                        waypoints.clear();
                        for (JsonNode pt : coords) {
                            double lng = pt.get(0).asDouble();
                            double lat = pt.get(1).asDouble();
                            // Lưu theo định dạng Leaflet chuẩn: [lat, lng]
                            waypoints.add(new double[]{lat, lng});
                        }
                    }
                    if (root.has("distance")) {
                        totalDistanceMeters = root.get("distance").asDouble();
                    }
                    log.info("🚗 [GpsSimulation] Nạp thành công {} điểm tọa độ tuyến đường E2E Hà Nội ({} km)",
                            waypoints.size(), String.format("%.2f", totalDistanceMeters / 1000.0));
                }
            } else {
                log.warn("⚠️ Không tìm thấy file simulation/hn_route_e2e.json trong resources");
            }
        } catch (Exception e) {
            log.error("❌ Lỗi nạp tọa độ mô phỏng OSRM: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        stopSimulation();
    }

    @Override
    public Map<String, Object> getRouteData() {
        Map<String, Object> res = new HashMap<>();
        res.put("route_id", "route-hn-01");
        res.put("route_code", "HN-E2E-01");
        res.put("route_name", "Tuyến Thử Nghiệm: Bến xe Mỹ Đình -> Đống Đa -> Hoàn Kiếm");
        res.put("distance_km", Math.round(totalDistanceMeters / 100.0) / 10.0);
        res.put("total_waypoints", waypoints.size());
        res.put("waypoints", waypoints); // [ [lat, lng], ... ]

        List<AccidentBlackspot> blackspots = geofenceService.getActiveBlackspots();
        res.put("blackspots", blackspots);
        res.put("is_running", isRunning.get());
        res.put("current_index", currentIndex.get());
        return res;
    }

    @Override
    public synchronized Map<String, Object> startSimulation(String tripId, Double speedMultiplier, Integer startIndex) {
        if (waypoints.isEmpty()) {
            initRouteCoordinates();
        }

        if (tripId != null && !tripId.isBlank()) {
            activeTripId = tripId;
        }

        if (startIndex != null && startIndex >= 0 && startIndex < waypoints.size()) {
            currentIndex.set(startIndex);
        }

        // Lấy thông tin chuyến đi và xe
        Optional<Trip> tripOpt = tripRepository.findById(activeTripId);
        if (tripOpt.isPresent()) {
            Trip t = tripOpt.get();
            if (t.getVehicle() != null) {
                activeVehicleId = t.getVehicle().getId();
                activePlateNumber = t.getVehicle().getPlateNumber();
            }
            if (t.getDriver() != null && t.getDriver().getStaff() != null) {
                activeDriverName = t.getDriver().getStaff().getLastName() + " " + t.getDriver().getStaff().getFirstName();
            }
        }

        double multiplier = (speedMultiplier != null && speedMultiplier > 0) ? speedMultiplier : 2.0;
        int intervalMs = (int) Math.max(200, Math.round(1000.0 / multiplier));

        if (isRunning.get()) {
            Map<String, Object> res = getStatus();
            res.put("message", "Mô phỏng đang chạy sẵn");
            return res;
        }

        isRunning.set(true);
        if (currentIndex.get() >= waypoints.size() - 1) {
            currentIndex.set(0);
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "GpsSimulationThread");
            t.setDaemon(true);
            return t;
        });

        simulationTask = scheduler.scheduleAtFixedRate(this::tickSimulation, 0, intervalMs, TimeUnit.MILLISECONDS);
        log.info("▶️ [GpsSimulation] BẮT ĐẦU mô phỏng GPS xe {} trên tuyến E2E (Interval: {}ms, Bắt đầu từ idx: {})",
                activePlateNumber, intervalMs, currentIndex.get());

        Map<String, Object> res = getStatus();
        res.put("message", "Đã khởi chạy mô phỏng GPS thành công");
        return res;
    }

    @Override
    public synchronized Map<String, Object> stopSimulation() {
        if (isRunning.get()) {
            isRunning.set(false);
            if (simulationTask != null) {
                simulationTask.cancel(false);
            }
            if (scheduler != null) {
                scheduler.shutdown();
            }
            log.info("⏹️ [GpsSimulation] ĐÃ DỪNG mô phỏng tại điểm idx: {}/{}", currentIndex.get(), waypoints.size());
        }
        Map<String, Object> res = getStatus();
        res.put("message", "Đã dừng mô phỏng GPS");
        return res;
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> res = new HashMap<>();
        res.put("is_running", isRunning.get());
        res.put("current_index", currentIndex.get());
        res.put("total_points", waypoints.size());
        res.put("trip_id", activeTripId);
        res.put("vehicle_id", activeVehicleId);
        res.put("plate_number", activePlateNumber);
        res.put("driver_name", activeDriverName);

        if (!waypoints.isEmpty()) {
            int idx = Math.min(currentIndex.get(), waypoints.size() - 1);
            double[] currentPt = waypoints.get(idx);
            res.put("current_lat", currentPt[0]);
            res.put("current_lng", currentPt[1]);
        }
        return res;
    }

    private void tickSimulation() {
        if (!isRunning.get() || waypoints.isEmpty()) {
            return;
        }

        int idx = currentIndex.get();
        if (idx >= waypoints.size()) {
            log.info("🏁 [GpsSimulation] Xe đã đến đích (Nhà Hát Lớn Hà Nội). Kết thúc hành trình!");
            stopSimulation();
            return;
        }

        double[] pt = waypoints.get(idx);
        double lat = pt[0];
        double lng = pt[1];
        double speedKmh = 35.0 + (idx % 10); // Tốc độ dao động thực tế 35 - 45 km/h

        // 1. Lưu vết TripGpsTrack vào DB (mỗi 3 nhịp lưu 1 lần để tối ưu dung lượng)
        if (idx % 3 == 0) {
            try {
                tripRepository.findById(activeTripId).ifPresent(trip -> {
                    TripGpsTrack track = TripGpsTrack.builder()
                            .trip(trip)
                            .latitude(lat)
                            .longitude(lng)
                            .speedKmh(speedKmh)
                            .recordedAt(LocalDateTime.now())
                            .build();
                    tripGpsTrackRepository.save(track);
                });
            } catch (Exception e) {
                log.debug("Lỗi lưu trip track: {}", e.getMessage());
            }
        }

        // 2. Tạo Payload Telemetry Realtime
        Map<String, Object> telemetryPayload = new HashMap<>();
        telemetryPayload.put("vehicle_id", activeVehicleId);
        telemetryPayload.put("plate_number", activePlateNumber);
        telemetryPayload.put("driver_name", activeDriverName);
        telemetryPayload.put("latitude", lat);
        telemetryPayload.put("longitude", lng);
        telemetryPayload.put("speed_kmh", speedKmh);
        telemetryPayload.put("step_index", idx);
        telemetryPayload.put("total_steps", waypoints.size());
        telemetryPayload.put("timestamp", System.currentTimeMillis());

        // 3. Phát sóng SSE xuống Web Dashboard (sự kiện 'gps')
        sseBroadcasterService.broadcast("gps", telemetryPayload);

        // 4. Phát MQTT xuống Broker EMQX
        try {
            String jsonPayload = objectMapper.writeValueAsString(telemetryPayload);
            mqttPublisherService.publish(String.format("driverguard/telemetry/%s", activeVehicleId), jsonPayload);
        } catch (Exception ignored) {}

        // 5. Kích hoạt kiểm tra Geofence Điểm đen tai nạn 200m
        geofenceService.checkBlackspotGeofence(activeVehicleId, lat, lng, speedKmh);

        // Tăng index cho nhịp tiếp theo
        currentIndex.incrementAndGet();
    }
}
