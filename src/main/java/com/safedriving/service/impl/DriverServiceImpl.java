package com.safedriving.service.impl;

import com.safedriving.dto.request.DriverRequest;
import com.safedriving.dto.response.DriverResponse;
import com.safedriving.dto.response.DriverVehicleInfo;
import com.safedriving.dto.response.StaffAccountInfo;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Vehicle;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.StaffRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.dto.request.DriverCreateRequest;
import com.safedriving.entity.enums.AccountRole;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.repository.AccountRepository;
import com.safedriving.service.FaceRecognitionService;
import com.safedriving.service.MinioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.safedriving.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final StaffRepository staffRepository;
    private final VehicleRepository vehicleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final MinioService minioService;
    private final FaceRecognitionService faceRecognitionService;

    @Override
    @Transactional
    public DriverResponse createDriver(DriverCreateRequest request, MultipartFile avatar) {
        log.info("Xử lý tạo hồ sơ tài xế trọn gói cho username: {}", request.getUsername());

        // 1. Kiểm tra tính duy nhất
        String username = request.getUsername().trim();
        if (accountRepository.existsByUsername(username)) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        String phone = request.getPhone().trim();
        if (staffRepository.existsByPhone(phone)) {
            throw new BadRequestException("Số điện thoại đã được sử dụng bởi một nhân sự khác");
        }

        String email = request.getEmail() != null && !request.getEmail().isBlank()
                ? request.getEmail().trim()
                : null;
        if (email != null && staffRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã được sử dụng bởi một nhân sự khác");
        }

        // 2. Tạo Account (Role: DRIVER, Status: ACTIVE)
        Account account = Account.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AccountRole.DRIVER)
                .status(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build();
        Account savedAccount = accountRepository.save(account);

        // 3. Tạo Staff gắn với Account
        Staff staff = Staff.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(phone)
                .email(email)
                .exactAddress(request.getExactAddress() != null ? request.getExactAddress().trim() : null)
                .commune(request.getCommune() != null ? request.getCommune().trim() : null)
                .province(request.getProvince() != null ? request.getProvince().trim() : null)
                .account(savedAccount)
                .createdAt(LocalDateTime.now())
                .build();
        Staff savedStaff = staffRepository.save(staff);

        // 4. Tra cứu Vehicle (nếu có)
        Vehicle vehicle = null;
        if (request.getVehicleId() != null && !request.getVehicleId().isBlank()) {
            String vehicleId = request.getVehicleId().trim();
            vehicle = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + vehicleId));
        }

        // 5. Xác định URL ảnh
        String finalImageUrl = request.getUrlImage() != null && !request.getUrlImage().isBlank()
                ? request.getUrlImage().trim()
                : null;

        if (avatar != null && !avatar.isEmpty()) {
            finalImageUrl = minioService.uploadFile(avatar, "avatars");
        }

        // 6. Tạo Driver
        LocalDate hireDate = request.getHireDate() != null ? request.getHireDate() : LocalDate.now();
        Driver driver = Driver.builder()
                .hireDate(hireDate)
                .urlImage(finalImageUrl)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .staff(savedStaff)
                .vehicle(vehicle)
                .isDeleted(false)
                .build();
        Driver savedDriver = driverRepository.save(driver);

        // 7. Đồng bộ khuôn mặt sang FastAPI (InsightFace) nếu có gửi file ảnh
        if (avatar != null && !avatar.isEmpty()) {
            faceRecognitionService.enrollDriver(savedDriver.getId(), avatar, true);
        }

        log.info("Tạo thành công hồ sơ tài xế trọn gói: ID={}, Username={}, StaffID={}",
                savedDriver.getId(), savedAccount.getUsername(), savedStaff.getId());

        return toResponse(savedDriver);
    }

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
    @Transactional(readOnly = true)
    public DriverResponse getMyDriverProfile(String accountId) {
        log.info("Tra cứu hồ sơ tài xế hiện tại theo Account ID: {}", accountId);
        Driver driver = driverRepository.findByStaffAccountIdAndIsDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ tài xế liên kết với tài khoản này"));
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
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + vehicleId));
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
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhân sự với ID: " + staffId));

                if (driverRepository.existsByStaffIdAndIdNot(staffId, id)) {
                    throw new BadRequestException("Nhân viên này đã được tạo hồ sơ tài xế khác");
                }
                driver.setStaff(staff);
            }
        }

        String vehicleId = request.resolveVehicleId();
        if (vehicleId != null && !vehicleId.isBlank()) {
            Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(vehicleId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + vehicleId));
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

        // 1. Xóa mềm Driver và gỡ phương tiện
        driver.setIsDeleted(true);
        driver.setIsActive(false);
        driver.setVehicle(null);
        driverRepository.save(driver);

        // 2. Vô hiệu hóa tài khoản Account liên kết
        if (driver.getStaff() != null && driver.getStaff().getAccount() != null) {
            Account account = driver.getStaff().getAccount();
            account.setStatus(AccountStatus.INACTIVE);
            account.setIsDeleted(true);
            accountRepository.save(account);
            log.info("Đã vô hiệu hóa tài khoản liên kết với tài xế: {}", account.getUsername());
        }

        // 3. Xóa dữ liệu khuôn mặt trong file .pkl và RAM của FastAPI AI
        faceRecognitionService.deleteDriver(id);

        log.info("Đã xóa hoàn tất hồ sơ tài xế với ID: {} trên cả Database và AI Server", id);
    }

    private Driver findDriverOrThrow(String id) {
        return driverRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ tài xế với ID: " + id));
    }

    private DriverResponse toResponse(Driver driver) {
        StaffResponse staffResponse = null;
        if (driver.getStaff() != null) {
            Staff staff = driver.getStaff();
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
                    .exactAddress(staff.getExactAddress())
                    .commune(staff.getCommune())
                    .province(staff.getProvince())
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
