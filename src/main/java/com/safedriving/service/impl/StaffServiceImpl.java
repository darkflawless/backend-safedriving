package com.safedriving.service.impl;

import com.safedriving.dto.request.StaffRequest;
import com.safedriving.dto.response.StaffAccountInfo;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Staff;
import com.safedriving.entity.enums.AccountStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.StaffRepository;
import com.safedriving.service.FaceRecognitionService;
import com.safedriving.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final DriverRepository driverRepository;
    private final FaceRecognitionService faceRecognitionService;

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        log.info("Lấy danh sách tất cả cán bộ, nhân viên chưa bị xóa");
        return staffRepository.findByIsDeletedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(String id) {
        log.info("Tra cứu thông tin hồ sơ nhân sự với ID: {}", id);
        Staff staff = findStaffOrThrow(id);
        return toResponse(staff);
    }

    @Override
    @Transactional
    public StaffResponse createStaff(StaffRequest request) {
        log.info("Tạo mới hồ sơ nhân sự: {} {}", request.getFirstName(), request.getLastName());

        Account account = null;
        String accountId = request.resolveAccountId();
        if (accountId != null) {
            account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + accountId));

            if (staffRepository.existsByAccountId(accountId)) {
                throw new BadRequestException("Tài khoản này đã được liên kết với một hồ sơ nhân sự khác");
            }
        }

        Staff staff = Staff.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .phone(request.getPhone().trim())
                .exactAddress(request.getExactAddress() != null ? request.getExactAddress().trim() : null)
                .commune(request.getCommune() != null ? request.getCommune().trim() : null)
                .province(request.getProvince() != null ? request.getProvince().trim() : null)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isDeleted(false)
                .account(account)
                .createdAt(LocalDateTime.now())
                .build();

        Staff saved = staffRepository.save(staff);
        log.info("Tạo hồ sơ nhân sự thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(String id, StaffRequest request) {
        log.info("Cập nhật hồ sơ nhân sự với ID: {}", id);
        // Cho phép tìm cả Staff kể cả đã bị xóa mềm để có thể khôi phục
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhân sự với ID: " + id));

        Account account = null;
        String accountId = request.resolveAccountId();
        if (accountId != null) {
            account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + accountId));

            if (staffRepository.existsByAccountIdAndIdNot(accountId, id)) {
                throw new BadRequestException("Tài khoản này đã được liên kết với một hồ sơ nhân sự khác");
            }
        }

        staff.setFirstName(request.getFirstName().trim());
        staff.setLastName(request.getLastName().trim());
        staff.setDateOfBirth(request.getDateOfBirth());
        staff.setGender(request.getGender());
        staff.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        staff.setPhone(request.getPhone().trim());
        staff.setExactAddress(request.getExactAddress() != null ? request.getExactAddress().trim() : null);
        staff.setCommune(request.getCommune() != null ? request.getCommune().trim() : null);
        staff.setProvince(request.getProvince() != null ? request.getProvince().trim() : null);

        if (accountId != null) {
            staff.setAccount(account);
        }

        // Xử lý đồng bộ trạng thái isActive / isDeleted
        if (request.getIsActive() != null) {
            boolean active = request.getIsActive();
            staff.setIsActive(active);

            if (active) {
                // Khôi phục staff
                staff.setIsDeleted(false);
            }

            // Đồng bộ sang Account
            Account currentAccount = staff.getAccount();
            if (currentAccount != null) {
                currentAccount.setStatus(active ? AccountStatus.ACTIVE : AccountStatus.INACTIVE);
                if (active) {
                    currentAccount.setIsDeleted(false);
                }
                accountRepository.save(currentAccount);
            }

            // Đồng bộ sang Driver nếu nhân sự này là Driver
            driverRepository.findByStaffId(staff.getId()).ifPresent(driver -> {
                driver.setIsActive(active);
                if (active) {
                    driver.setIsDeleted(false);
                }
                driverRepository.save(driver);
            });
        }

        Staff updated = staffRepository.save(staff);
        log.info("Cập nhật hồ sơ nhân sự thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStaff(String id) {
        log.info("Xóa mềm hồ sơ nhân sự với ID: {}", id);
        Staff staff = findStaffOrThrow(id);

        // 1. Xóa mềm Staff
        staff.setIsDeleted(true);
        staff.setIsActive(false);
        staffRepository.save(staff);

        // 2. Vô hiệu hóa và xóa mềm Account liên kết để chặn hoàn toàn việc đăng nhập
        if (staff.getAccount() != null) {
            Account account = staff.getAccount();
            account.setIsDeleted(true);
            account.setStatus(AccountStatus.INACTIVE);
            accountRepository.save(account);
            log.info("Đã xóa mềm và chuyển INACTIVE cho Account liên kết ID: {}", account.getId());
        }

        // 3. Nếu nhân sự này có hồ sơ tài xế (Driver), tiến hành xóa mềm Driver, gỡ xe và xóa face data
        driverRepository.findByStaffId(id).ifPresent(driver -> {
            driver.setIsDeleted(true);
            driver.setIsActive(false);
            driver.setVehicle(null);
            driverRepository.save(driver);
            faceRecognitionService.deleteDriver(driver.getId());
            log.info("Đã đồng bộ xóa mềm hồ sơ tài xế ID: {} gắn liền với Staff ID: {}", driver.getId(), id);
        });

        log.info("Đã xóa mềm hoàn tất hồ sơ nhân sự với ID: {}", id);
    }

    private Staff findStaffOrThrow(String id) {
        return staffRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhân sự với ID: " + id));
    }

    private StaffResponse toResponse(Staff staff) {
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

        return StaffResponse.builder()
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
                .isActive(staff.getIsActive())
                .account(accountInfo)
                .createdAt(staff.getCreatedAt())
                .build();
    }
}
