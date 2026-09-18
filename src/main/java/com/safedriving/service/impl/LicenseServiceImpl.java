package com.safedriving.service.impl;

import com.safedriving.dto.request.LicenseRequest;
import com.safedriving.dto.response.LicenseClassResponse;
import com.safedriving.dto.response.LicenseResponse;
import com.safedriving.entity.Driver;
import com.safedriving.entity.License;
import com.safedriving.entity.LicenseClass;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.LicenseClassRepository;
import com.safedriving.repository.LicenseRepository;
import com.safedriving.service.LicenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseServiceImpl implements LicenseService {

    private final LicenseRepository licenseRepository;
    private final DriverRepository driverRepository;
    private final LicenseClassRepository licenseClassRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LicenseResponse> getAllLicenses(String driverId) {
        if (driverId != null && !driverId.isBlank()) {
            log.info("Lấy danh sách giấy phép lái xe của tài xế ID: {}", driverId);
            return licenseRepository.findByDriverIdAndIsDeletedFalse(driverId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        log.info("Lấy danh sách tất cả giấy phép lái xe");
        return licenseRepository.findByIsDeletedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LicenseResponse getLicenseById(String id) {
        log.info("Tra cứu giấy phép lái xe với ID: {}", id);
        License license = findLicenseOrThrow(id);
        return toResponse(license);
    }

    @Override
    @Transactional
    public LicenseResponse createLicense(LicenseRequest request) {
        log.info("Tạo mới giấy phép lái xe số: {}", request.getLicenseNo());

        if (request.getDriverId() == null || request.getDriverId().isBlank()) {
            throw new BadRequestException("ID tài xế là bắt buộc");
        }
        Driver driver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế với ID: " + request.getDriverId()));

        Short licenseClassId = request.resolveLicenseClassId();
        if (licenseClassId == null) {
            throw new BadRequestException("Hạng giấy phép lái xe là bắt buộc");
        }
        LicenseClass licenseClass = licenseClassRepository.findById(licenseClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hạng bằng lái với ID: " + licenseClassId));

        if (licenseRepository.existsByLicenseNoAndIsDeletedFalse(request.getLicenseNo())) {
            throw new BadRequestException("Số GPLX này đã được đăng ký trong hệ thống");
        }

        License license = License.builder()
                .licenseNo(request.getLicenseNo().trim())
                .issueDate(request.getIssueDate())
                .licenseExpiry(request.getLicenseExpiry())
                .placeOfIssue(request.getPlaceOfIssue().trim())
                .driver(driver)
                .licenseClass(licenseClass)
                .isDeleted(false)
                .build();

        License saved = licenseRepository.save(license);
        log.info("Tạo GPLX thành công với ID: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public LicenseResponse updateLicense(String id, LicenseRequest request) {
        log.info("Cập nhật giấy phép lái xe với ID: {}", id);
        License license = findLicenseOrThrow(id);

        if (request.getLicenseNo() != null && !request.getLicenseNo().isBlank()) {
            String newNo = request.getLicenseNo().trim();
            if (!license.getLicenseNo().equals(newNo) && licenseRepository.existsByLicenseNoAndIdNotAndIsDeletedFalse(newNo, id)) {
                throw new BadRequestException("Số GPLX này đã được đăng ký cho một hồ sơ khác");
            }
            license.setLicenseNo(newNo);
        }

        if (request.getIssueDate() != null) {
            license.setIssueDate(request.getIssueDate());
        }
        if (request.getLicenseExpiry() != null) {
            license.setLicenseExpiry(request.getLicenseExpiry());
        }
        if (request.getPlaceOfIssue() != null && !request.getPlaceOfIssue().isBlank()) {
            license.setPlaceOfIssue(request.getPlaceOfIssue().trim());
        }

        if (request.getDriverId() != null && !request.getDriverId().isBlank() && !license.getDriver().getId().equals(request.getDriverId())) {
            Driver driver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế với ID: " + request.getDriverId()));
            license.setDriver(driver);
        }

        Short licenseClassId = request.resolveLicenseClassId();
        if (licenseClassId != null && !license.getLicenseClass().getId().equals(licenseClassId)) {
            LicenseClass licenseClass = licenseClassRepository.findById(licenseClassId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hạng bằng lái với ID: " + licenseClassId));
            license.setLicenseClass(licenseClass);
        }

        License updated = licenseRepository.save(license);
        log.info("Cập nhật GPLX thành công cho ID: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteLicense(String id) {
        log.info("Xóa mềm giấy phép lái xe với ID: {}", id);
        License license = findLicenseOrThrow(id);
        license.setIsDeleted(true);
        licenseRepository.save(license);
        log.info("Đã xóa mềm GPLX với ID: {}", id);
    }

    private License findLicenseOrThrow(String id) {
        return licenseRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giấy phép lái xe với ID: " + id));
    }

    private LicenseResponse toResponse(License license) {
        LicenseClassResponse classResponse = null;
        if (license.getLicenseClass() != null) {
            LicenseClass lc = license.getLicenseClass();
            classResponse = LicenseClassResponse.builder()
                    .id(lc.getId())
                    .code(lc.getCode())
                    .name(lc.getName())
                    .capacity(lc.getCapacity())
                    .build();
        }

        String driverName = null;
        if (license.getDriver() != null && license.getDriver().getStaff() != null) {
            driverName = license.getDriver().getStaff().getFirstName() + " " + license.getDriver().getStaff().getLastName();
        }

        return LicenseResponse.builder()
                .id(license.getId())
                .licenseNo(license.getLicenseNo())
                .issueDate(license.getIssueDate())
                .licenseExpiry(license.getLicenseExpiry())
                .placeOfIssue(license.getPlaceOfIssue())
                .licenseClass(classResponse)
                .driverId(license.getDriver() != null ? license.getDriver().getId() : null)
                .driverName(driverName)
                .build();
    }
}
