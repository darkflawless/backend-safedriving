package com.safedriving.service.impl;

import com.safedriving.dto.request.DriverRequest;
import com.safedriving.dto.response.AddressResponse;
import com.safedriving.dto.response.DriverResponse;
import com.safedriving.dto.response.DriverVehicleInfo;
import com.safedriving.dto.response.StaffAccountInfo;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Address;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Vehicle;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.StaffRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final StaffRepository staffRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {
        log.info("Lấy danh sách tất cả tài xế đang quản lý");
        return driverRepository.findByIsDeletedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(String id) {
        log.info("Tra cứu hồ sơ chi tiết tài xế với ID: {}", id);
        Driver driver = findDriverOrThrow(id);
        return toResponse(driver);
    }

    @Override
    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        String staffId = request.resolveStaffId();
        if (staffId == null || staffId.isBlank()) {
            throw new BadRequestException("Hồ sơ nhân sự (staff) là bắt buộc khi tạo tài xế");
        }

        log.info("Tạo mới hồ sơ tài xế cho nhân viên ID: {}", staffId);

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhân sự với ID: " + staffId));

        if (driverRepository.existsByStaffId(staffId)) {
            throw new BadRequestException("Nhân viên này đã được tạo hồ sơ tài xế trước đó");
        }

        Vehicle vehicle = null;
        String vehicleId = request.resolveVehicleId();
        if (vehicleId != null) {
            vehicle = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + vehicleId));
        }



        Driver driver = Driver.builder()
                .hireDate(request.getHireDate())
                .urlImage(request.getUrlImage() != null ? request.getUrlImage().trim() : null)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .staff(staff)
                .vehicle(vehicle)
                .isDeleted(false)
                .build();

        Driver saved = driverRepository.save(driver);
        log.info("Tạo hồ sơ tài xế thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DriverResponse updateDriver(String id, DriverRequest request) {
        log.info("Cập nhật hồ sơ tài xế với ID: {}", id);
        Driver driver = findDriverOrThrow(id);

        String staffId = request.resolveStaffId();
        if (staffId != null && !staffId.isBlank()) {
            if (!driver.getStaff().getId().equals(staffId)) {
                Staff staff = staffRepository.findById(staffId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhân sự với ID: " + staffId));

                if (driverRepository.existsByStaffIdAndIdNot(staffId, id)) {
                    throw new BadRequestException("Nhân viên này đã được tạo hồ sơ tài xế khác");
                }
                driver.setStaff(staff);
            }
        }

        String vehicleId = request.resolveVehicleId();
        if (vehicleId != null && !vehicleId.isBlank()) {
            Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + vehicleId));
            driver.setVehicle(vehicle);
        } else if (request.getVehicle() != null || request.getVehicleId() != null) {
            driver.setVehicle(null);
        }



        if (request.getHireDate() != null) {
            driver.setHireDate(request.getHireDate());
        }
        if (request.getUrlImage() != null) {
            driver.setUrlImage(request.getUrlImage().trim());
        }
        if (request.getIsActive() != null) {
            driver.setIsActive(request.getIsActive());
        }

        Driver updated = driverRepository.save(driver);
        log.info("Cập nhật hồ sơ tài xế thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDriver(String id) {
        log.info("Xóa hồ sơ tài xế với ID: {}", id);
        Driver driver = findDriverOrThrow(id);
        driver.setIsDeleted(true);
        driverRepository.save(driver);
        log.info("Đã xóa mềm hồ sơ tài xế với ID: {}", id);
    }

    private Driver findDriverOrThrow(String id) {
        return driverRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ tài xế với ID: " + id));
    }

    private DriverResponse toResponse(Driver driver) {
        StaffResponse staffResponse = null;
        if (driver.getStaff() != null) {
            Staff staff = driver.getStaff();
            AddressResponse addressResponse = null;
            if (staff.getAddress() != null) {
                Address addr = staff.getAddress();
                addressResponse = AddressResponse.builder()
                        .id(addr.getId())
                        .exactAddress(addr.getExactAddress())
                        .commune(addr.getCommune())
                        .province(addr.getProvince())
                        .lat(addr.getLat())
                        .lng(addr.getLng())
                        .build();
            }

            StaffAccountInfo accountInfo = null;
            if (staff.getAccount() != null) {
                Account acc = staff.getAccount();
                accountInfo = StaffAccountInfo.builder()
                        .id(acc.getId())
                        .username(acc.getUsername())
                        .role(acc.getRole())
                        .status(acc.getStatus())
                        .build();
            }

            staffResponse = StaffResponse.builder()
                    .id(staff.getId())
                    .firstName(staff.getFirstName())
                    .lastName(staff.getLastName())
                    .fullName(staff.getFirstName() + " " + staff.getLastName())
                    .dateOfBirth(staff.getDateOfBirth())
                    .gender(staff.getGender())
                    .email(staff.getEmail())
                    .phone(staff.getPhone())
                    .address(addressResponse)
                    .account(accountInfo)
                    .createdAt(staff.getCreatedAt())
                    .build();
        }

        DriverVehicleInfo vehicleInfo = null;
        if (driver.getVehicle() != null) {
            Vehicle veh = driver.getVehicle();
            vehicleInfo = DriverVehicleInfo.builder()
                    .id(veh.getId())
                    .plateNumber(veh.getPlateNumber())
                    .vin(veh.getVin())
                    .capacity(veh.getCapacity())
                    .status(veh.getStatus())
                    .build();
        }



        return DriverResponse.builder()
                .id(driver.getId())
                .hireDate(driver.getHireDate())
                .urlImage(driver.getUrlImage())
                .isActive(driver.getIsActive())
                .staff(staffResponse)
                .vehicle(vehicleInfo)
                .build();
    }
}
