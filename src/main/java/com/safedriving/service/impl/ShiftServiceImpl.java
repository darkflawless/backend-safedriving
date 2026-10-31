package com.safedriving.service.impl;

import com.safedriving.dto.request.ShiftEndRequest;
import com.safedriving.dto.request.ShiftStartRequest;
import com.safedriving.dto.response.MqttConfigResponse;
import com.safedriving.dto.response.ShiftResponse;
import com.safedriving.dto.response.VehicleBasicResponse;
import com.safedriving.entity.Driver;
import com.safedriving.entity.ShiftSession;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.enums.ShiftStatus;
import com.safedriving.entity.enums.VehicleStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.ShiftSessionRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.security.CustomUserDetails;
import com.safedriving.service.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftSessionRepository shiftSessionRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    @Override
    @Transactional
    public ShiftResponse startShift(CustomUserDetails userDetails, ShiftStartRequest request) {
        String accountId = userDetails.getAccount().getId();
        Driver driver = driverRepository.findByStaffAccountIdAndIsDeletedFalse(accountId)
                .orElseThrow(() -> new BadRequestException("Tài khoản chưa được liên kết với hồ sơ tài xế. Vui lòng liên hệ Quản lý."));

        Vehicle vehicle = driver.getVehicle();
        if (vehicle == null) {
            throw new BadRequestException("Tài xế chưa được phân công phương tiện. Vui lòng liên hệ Điều phối viên.");
        }

        // Kiểm tra tài xế đã có ca lái nào đang chạy không
        Optional<ShiftSession> existingDriverShift = shiftSessionRepository.findByDriverIdAndStatus(driver.getId(), ShiftStatus.ACTIVE);
        if (existingDriverShift.isPresent()) {
            log.info("Tài xế {} đã có ca lái đang hoạt động: {}", driver.getId(), existingDriverShift.get().getId());
            return toShiftResponse(existingDriverShift.get(), vehicle);
        }

        // Kiểm tra xe có đang được lái bởi tài xế khác trong ca ACTIVE không
        Optional<ShiftSession> existingVehicleShift = shiftSessionRepository.findByVehicleIdAndStatus(vehicle.getId(), ShiftStatus.ACTIVE);
        if (existingVehicleShift.isPresent()) {
            throw new BadRequestException("Phương tiện " + vehicle.getPlateNumber() + " hiện đang được sử dụng trong một ca lái khác chưa kết thúc.");
        }

        // Cập nhật trạng thái xe thành IN_USE
        vehicle.setStatus(VehicleStatus.IN_USE);
        vehicleRepository.save(vehicle);

        // Tạo ShiftSession
        ShiftSession session = ShiftSession.builder()
                .driver(driver)
                .vehicle(vehicle)
                .startTime(LocalDateTime.now())
                .status(ShiftStatus.ACTIVE)
                .startOdometer(request != null ? request.getStartOdometer() : vehicle.getOdometerKm())
                .startBattery(request != null ? request.getStartBattery() : null)
                .build();

        session = shiftSessionRepository.save(session);
        log.info("✅ Bắt đầu ca lái mới thành công: shiftId={}, driverId={}, vehicleId={}",
                session.getId(), driver.getId(), vehicle.getId());

        return toShiftResponse(session, vehicle);
    }

    @Override
    @Transactional
    public ShiftResponse endShift(CustomUserDetails userDetails, ShiftEndRequest request) {
        ShiftSession session = shiftSessionRepository.findById(request.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca lái với ID: " + request.getShiftId()));

        if (session.getStatus() == ShiftStatus.COMPLETED) {
            return toShiftResponse(session, session.getVehicle());
        }

        session.setStatus(ShiftStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        session.setEndOdometer(request.getEndOdometer());
        session.setEndBattery(request.getEndBattery());
        shiftSessionRepository.save(session);

        // Giải phóng xe về trạng thái AVAILABLE
        Vehicle vehicle = session.getVehicle();
        if (vehicle != null) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            if (request.getEndOdometer() != null) {
                vehicle.setOdometerKm(request.getEndOdometer());
            }
            vehicleRepository.save(vehicle);
        }

        log.info("🏁 Đã kết thúc ca lái thành công: shiftId={}, odometer={}", session.getId(), request.getEndOdometer());
        return toShiftResponse(session, vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getActiveShift(CustomUserDetails userDetails) {
        String accountId = userDetails.getAccount().getId();
        Driver driver = driverRepository.findByStaffAccountIdAndIsDeletedFalse(accountId)
                .orElseThrow(() -> new BadRequestException("Tài khoản chưa được liên kết với hồ sơ tài xế."));

        ShiftSession session = shiftSessionRepository.findByDriverIdAndStatus(driver.getId(), ShiftStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Tài xế hiện không có ca lái nào đang hoạt động."));

        return toShiftResponse(session, session.getVehicle());
    }

    private ShiftResponse toShiftResponse(ShiftSession session, Vehicle vehicle) {
        String vehicleId = vehicle != null ? vehicle.getId() : "";
        String sessionId = session.getId();
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4);

        String vIdShort = vehicleId.length() >= 8 ? vehicleId.substring(0, 8) : vehicleId;
        String sIdShort = sessionId.length() >= 8 ? sessionId.substring(0, 8) : sessionId;
        String clientId = String.format("DRIVERGUARD_%s_%s_%s", vIdShort, sIdShort, randomSuffix);

        String deviceId = (vehicle != null && vehicle.getDevice() != null && vehicle.getDevice().getSerialNumber() != null)
                ? vehicle.getDevice().getSerialNumber()
                : vehicleId;

        MqttConfigResponse mqttConfig = MqttConfigResponse.builder()
                .brokerEndpoint(brokerUrl)
                .clientId(clientId)
                .telemetryTopic(String.format("driverguard/telemetry/%s/gps", vehicleId))
                .alertTopic(String.format("driverguard/alerts/%s", vehicleId))
                .heartbeatTopic(String.format("driverguard/heartbeat/%s", deviceId))
                .build();

        VehicleBasicResponse vehicleDto = null;
        if (vehicle != null) {
            String model = (vehicle.getVehicleType() != null) ? vehicle.getVehicleType().getName() : "Standard";
            vehicleDto = VehicleBasicResponse.builder()
                    .id(vehicle.getId())
                    .plateNumber(vehicle.getPlateNumber())
                    .model(model)
                    .status(vehicle.getStatus() != null ? vehicle.getStatus().name() : null)
                    .build();
        }

        return ShiftResponse.builder()
                .shiftId(session.getId())
                .driverId(session.getDriver() != null ? session.getDriver().getId() : null)
                .vehicle(vehicleDto)
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus())
                .startOdometer(session.getStartOdometer())
                .endOdometer(session.getEndOdometer())
                .mqttConfig(mqttConfig)
                .build();
    }
}
