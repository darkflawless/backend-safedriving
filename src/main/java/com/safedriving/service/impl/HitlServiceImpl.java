package com.safedriving.service.impl;

import com.safedriving.dto.request.HitlReviewDecisionRequest;
import com.safedriving.dto.response.HitlCaseResponse;
import com.safedriving.dto.response.SafetyAlertDetailResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Driver;
import com.safedriving.entity.HitlReviewCase;
import com.safedriving.entity.SafetyAlertLog;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.enums.HitlStatus;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.HitlReviewCaseRepository;
import com.safedriving.security.CustomUserDetails;
import com.safedriving.service.HitlService;
import com.safedriving.service.SseBroadcasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HitlServiceImpl implements HitlService {

    private final HitlReviewCaseRepository hitlReviewCaseRepository;
    private final SseBroadcasterService sseBroadcasterService;

    @Override
    @Transactional(readOnly = true)
    public Page<HitlCaseResponse> getQueue(HitlStatus status, Pageable pageable) {
        HitlStatus queryStatus = (status != null) ? status : HitlStatus.PENDING;
        Page<HitlReviewCase> cases = hitlReviewCaseRepository.findByStatus(queryStatus, pageable);
        return cases.map(this::toHitlCaseResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public HitlCaseResponse getCaseById(String id) {
        HitlReviewCase reviewCase = hitlReviewCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca duyệt với ID: " + id));
        return toHitlCaseResponse(reviewCase);
    }

    @Override
    @Transactional
    public HitlCaseResponse reviewCase(String id, CustomUserDetails reviewer, HitlReviewDecisionRequest request) {
        HitlReviewCase reviewCase = hitlReviewCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca duyệt với ID: " + id));

        Account reviewerAccount = (reviewer != null) ? reviewer.getAccount() : null;

        reviewCase.setStatus(request.getDecision());
        reviewCase.setReviewer(reviewerAccount);
        reviewCase.setReviewNotes(request.getNotes());
        reviewCase.setReviewedAt(LocalDateTime.now());

        reviewCase = hitlReviewCaseRepository.save(reviewCase);
        log.info("✅ Quyết định HITL thành công: caseId={}, decision={}", id, request.getDecision());

        HitlCaseResponse response = toHitlCaseResponse(reviewCase);

        // Phát sóng sự kiện duyệt xong tới Web Dashboard qua SSE
        sseBroadcasterService.broadcast("hitl_review", response);

        return response;
    }

    private HitlCaseResponse toHitlCaseResponse(HitlReviewCase reviewCase) {
        SafetyAlertDetailResponse alertDto = null;
        SafetyAlertLog alert = reviewCase.getAlert();
        if (alert != null) {
            Vehicle vehicle = alert.getVehicle();
            Driver driver = alert.getDriver();
            String driverName = null;
            if (driver != null && driver.getStaff() != null) {
                Staff staff = driver.getStaff();
                driverName = staff.getLastName() + " " + staff.getFirstName();
            }

            alertDto = SafetyAlertDetailResponse.builder()
                    .id(alert.getId())
                    .vehicleId(vehicle != null ? vehicle.getId() : null)
                    .plateNumber(vehicle != null ? vehicle.getPlateNumber() : null)
                    .driverId(driver != null ? driver.getId() : null)
                    .driverName(driverName)
                    .alertType(alert.getAlertType())
                    .severity(alert.getSeverity())
                    .timestamp(alert.getTimestamp())
                    .latitude(alert.getLatitude())
                    .longitude(alert.getLongitude())
                    .speedKmh(alert.getSpeedKmh())
                    .earValue(alert.getEarValue())
                    .marValue(alert.getMarValue())
                    .pitchValue(alert.getPitchValue())
                    .yawValue(alert.getYawValue())
                    .perclosValue(alert.getPerclosValue())
                    .snapshotUrl(alert.getSnapshotUrl())
                    .build();
        }

        return HitlCaseResponse.builder()
                .id(reviewCase.getId())
                .alertId(alert != null ? alert.getId() : null)
                .status(reviewCase.getStatus())
                .reviewerId(reviewCase.getReviewer() != null ? reviewCase.getReviewer().getId() : null)
                .reviewerUsername(reviewCase.getReviewer() != null ? reviewCase.getReviewer().getUsername() : null)
                .reviewNotes(reviewCase.getReviewNotes())
                .reviewedAt(reviewCase.getReviewedAt())
                .createdAt(reviewCase.getCreatedAt())
                .alert(alertDto)
                .build();
    }
}
