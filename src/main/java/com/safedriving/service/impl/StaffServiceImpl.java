package com.safedriving.service.impl;

import com.safedriving.dto.request.StaffRequest;
import com.safedriving.dto.response.StaffAccountInfo;
import com.safedriving.dto.response.StaffResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Staff;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.StaffRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        log.info("Lấy danh sách tất cả cán bộ, nhân viên");
        return staffRepository.findAll().stream()
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
        Staff staff = findStaffOrThrow(id);

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
        staff.setAccount(account);

        Staff updated = staffRepository.save(staff);
        log.info("Cập nhật hồ sơ nhân sự thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStaff(String id) {
        log.info("Xóa hồ sơ nhân sự với ID: {}", id);
        Staff staff = findStaffOrThrow(id);

        if (driverRepository.existsByStaffId(id)) {
            throw new BadRequestException("Không thể xóa nhân viên vì nhân viên này đang được phân công làm tài xế (Driver)");
        }

        staffRepository.delete(staff);
        log.info("Đã xóa hồ sơ nhân sự với ID: {}", id);
    }

    private Staff findStaffOrThrow(String id) {
        return staffRepository.findById(id)
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
                .account(accountInfo)
                .createdAt(staff.getCreatedAt())
                .build();
    }
}
