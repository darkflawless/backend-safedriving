package com.safedriving.service.impl;

import com.safedriving.dto.request.ViolationRequest;
import com.safedriving.dto.response.ViolationResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Incident;
import com.safedriving.entity.Violation;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.IncidentRepository;
import com.safedriving.repository.ViolationRepository;
import com.safedriving.service.ViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViolationServiceImpl implements ViolationService {

    private final ViolationRepository violationRepository;
    private final DriverRepository driverRepository;
    private final IncidentRepository incidentRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ViolationResponse> getAllViolations(String driverId) {
        if (driverId != null && !driverId.isBlank()) {
            log.info("Lấy danh sách biên bản vi phạm của tài xế ID: {}", driverId);
            return violationRepository.findByDriverIdOrderByTimeViolationDesc(driverId.trim()).stream()
                    .map(this::toResponse)
                    .toList();
        }
        log.info("Lấy tất cả biên bản vi phạm trong hệ thống");
        return violationRepository.findAllByOrderByTimeViolationDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ViolationResponse getViolationById(String id) {
        log.info("Tra cứu biên bản vi phạm với ID: {}", id);
        Violation violation = findViolationOrThrow(id);
        return toResponse(violation);
    }

    @Override
    @Transactional
    public ViolationResponse createViolation(ViolationRequest request) {
        log.info("Lập biên bản vi phạm mới cho tài xế ID: {}", request.getDriverId());

        Driver driver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế với ID: " + request.getDriverId()));

        Incident incident = incidentRepository.findById(request.getIncidentId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự cố với ID: " + request.getIncidentId()));

        Account account = accountRepository.findById(request.getAccountId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản người lập biên bản với ID: " + request.getAccountId()));

        Violation violation = Violation.builder()
                .driver(driver)
                .incident(incident)
                .account(account)
                .timeViolation(request.getTimeViolation() != null ? request.getTimeViolation() : LocalDateTime.now())
                .type(request.getType())
                .penalty(request.getPenalty())
                .note(request.getNote())
                .build();

        Violation saved = violationRepository.save(violation);
        log.info("Lập biên bản vi phạm thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ViolationResponse updateViolation(String id, ViolationRequest request) {
        log.info("Cập nhật biên bản vi phạm với ID: {}", id);
        Violation violation = findViolationOrThrow(id);

        if (!violation.getDriver().getId().equals(request.getDriverId().trim())) {
            Driver driver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế với ID: " + request.getDriverId()));
            violation.setDriver(driver);
        }

        if (!violation.getIncident().getId().equals(request.getIncidentId().trim())) {
            Incident incident = incidentRepository.findById(request.getIncidentId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự cố với ID: " + request.getIncidentId()));
            violation.setIncident(incident);
        }

        if (!violation.getAccount().getId().equals(request.getAccountId().trim())) {
            Account account = accountRepository.findById(request.getAccountId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản người lập biên bản với ID: " + request.getAccountId()));
            violation.setAccount(account);
        }

        if (request.getTimeViolation() != null) {
            violation.setTimeViolation(request.getTimeViolation());
        }
        violation.setType(request.getType());
        violation.setPenalty(request.getPenalty());
        violation.setNote(request.getNote());

        Violation updated = violationRepository.save(violation);
        log.info("Cập nhật biên bản vi phạm thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteViolation(String id) {
        log.info("Xóa biên bản vi phạm với ID: {}", id);
        Violation violation = findViolationOrThrow(id);
        violationRepository.delete(violation);
        log.info("Đã xóa biên bản vi phạm với ID: {}", id);
    }

    private Violation findViolationOrThrow(String id) {
        return violationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biên bản vi phạm với ID: " + id));
    }

    private ViolationResponse toResponse(Violation violation) {
        return ViolationResponse.builder()
                .id(violation.getId())
                .driverId(violation.getDriver() != null ? violation.getDriver().getId() : null)
                .incidentId(violation.getIncident() != null ? violation.getIncident().getId() : null)
                .accountId(violation.getAccount() != null ? violation.getAccount().getId() : null)
                .timeViolation(violation.getTimeViolation())
                .type(violation.getType())
                .penalty(violation.getPenalty())
                .note(violation.getNote())
                .build();
    }
}
