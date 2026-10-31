package com.safedriving.service.impl;

import com.safedriving.dto.request.AssignmentRequest;
import com.safedriving.dto.response.AssignmentResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Assignment;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Trip;
import com.safedriving.entity.enums.TripStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.repository.AssignmentRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.TripRepository;
import com.safedriving.service.AssignmentService;
import com.safedriving.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final AccountRepository accountRepository;

    @Override
    public List<AssignmentResponse> getAll() {
        return assignmentRepository.findAllByIsDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentResponse getById(String id) {
        Assignment entity = assignmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ID: " + id));
        return mapToResponse(entity);
    }

    @Override
    public List<AssignmentResponse> getByDriverId(String driverId) {
        return assignmentRepository.findByDriverIdAndIsDeletedFalse(driverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> getTodayByDriverId(String driverId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<Assignment> assignments = assignmentRepository.findByDriverIdAndIsDeletedFalse(driverId);

        return assignments.stream()
                .filter(a -> isAssignmentOnDate(a, targetDate))
                .sorted((a1, a2) -> {
                    LocalDateTime t1 = getSortTime(a1);
                    LocalDateTime t2 = getSortTime(a2);
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t1.compareTo(t2);
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> getTodayAssignmentsForCurrentDriver(CustomUserDetails userDetails, LocalDate date) {
        String accountId = userDetails.getAccount().getId();
        Driver driver = driverRepository.findByStaffAccountIdAndIsDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ tài xế liên kết với tài khoản này"));
        return getTodayByDriverId(driver.getId(), date);
    }

    private boolean isAssignmentOnDate(Assignment assignment, LocalDate targetDate) {
        Trip trip = assignment.getTrip();
        if (trip != null) {
            // Nếu chuyến đang chạy (IN_PROGRESS), luôn ưu tiên hiển thị trong danh sách hôm nay
            if (trip.getStatus() == TripStatus.IN_PROGRESS) {
                return true;
            }
            if (trip.getPlannedStartTime() != null) {
                return trip.getPlannedStartTime().toLocalDate().isEqual(targetDate);
            }
            if (trip.getStartTime() != null) {
                return trip.getStartTime().toLocalDate().isEqual(targetDate);
            }
        }
        if (assignment.getAssignmentAt() != null) {
            return assignment.getAssignmentAt().toLocalDate().isEqual(targetDate);
        }
        return false;
    }

    private LocalDateTime getSortTime(Assignment assignment) {
        Trip trip = assignment.getTrip();
        if (trip != null) {
            if (trip.getPlannedStartTime() != null) return trip.getPlannedStartTime();
            if (trip.getStartTime() != null) return trip.getStartTime();
        }
        return assignment.getAssignmentAt();
    }


    @Override
    @Transactional
    public AssignmentResponse create(AssignmentRequest request) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + request.getTripId()));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế ID: " + request.getDriverId()));

        // Kiểm tra tài xế đã được gán phương tiện trong hồ sơ chưa
        if (driver.getVehicle() == null) {
            throw new BadRequestException("Tài xế này chưa được gán phương tiện! Vui lòng gán xe cho tài xế trước khi phân công chuyến đi.");
        }

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ID: " + request.getAccountId()));

        Assignment assignment = Assignment.builder()
                .trip(trip)
                .driver(driver)
                .account(account)
                .assignmentAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        // Chuyển trạng thái chuyến đi sang ASSIGNED và gán tài xế + phương tiện trực tiếp vào chuyến đi
        trip.setStatus(TripStatus.ASSIGNED);
        trip.setDriver(driver);
        trip.setVehicle(driver.getVehicle());
        tripRepository.save(trip);

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public AssignmentResponse update(String id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ID: " + id));

        Trip trip = tripRepository.findByIdAndIsDeletedFalse(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi ID: " + request.getTripId()));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài xế ID: " + request.getDriverId()));

        // Kiểm tra tài xế đã được gán phương tiện trong hồ sơ chưa
        if (driver.getVehicle() == null) {
            throw new BadRequestException("Tài xế này chưa được gán phương tiện! Vui lòng gán xe cho tài xế trước khi phân công chuyến đi.");
        }

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản ID: " + request.getAccountId()));

        Trip oldTrip = assignment.getTrip();
        if (oldTrip != null && !oldTrip.getId().equals(trip.getId())) {
            oldTrip.setStatus(TripStatus.PLANNED);
            oldTrip.setDriver(null);
            oldTrip.setVehicle(null);
            tripRepository.save(oldTrip);
        }

        assignment.setTrip(trip);
        assignment.setDriver(driver);
        assignment.setAccount(account);

        trip.setStatus(TripStatus.ASSIGNED);
        trip.setDriver(driver);
        trip.setVehicle(driver.getVehicle());
        tripRepository.save(trip);

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Assignment assignment = assignmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ID: " + id));
        assignment.setIsDeleted(true);
        assignmentRepository.save(assignment);

        Trip trip = assignment.getTrip();
        if (trip != null) {
            trip.setStatus(TripStatus.PLANNED);
            trip.setDriver(null);
            trip.setVehicle(null);
            tripRepository.save(trip);
        }
    }

    private AssignmentResponse mapToResponse(Assignment entity) {
        Driver driver = entity.getDriver();
        Trip trip = entity.getTrip();

        String driverName = null;
        if (driver != null && driver.getStaff() != null) {
            Staff staff = driver.getStaff();
            String lastName = staff.getLastName() != null ? staff.getLastName() : "";
            String firstName = staff.getFirstName() != null ? staff.getFirstName() : "";
            driverName = (lastName + " " + firstName).trim();
        }

        String vehiclePlate = null;
        String vehicleId = null;
        if (trip != null && trip.getVehicle() != null) {
            vehiclePlate = trip.getVehicle().getPlateNumber();
            vehicleId = trip.getVehicle().getId();
        } else if (driver != null && driver.getVehicle() != null) {
            vehiclePlate = driver.getVehicle().getPlateNumber();
            vehicleId = driver.getVehicle().getId();
        }

        String routeCode = null;
        String routeName = null;
        if (trip != null && trip.getRoute() != null) {
            routeCode = trip.getRoute().getCode();
            routeName = trip.getRoute().getRouteName();
        }

        return AssignmentResponse.builder()
                .id(entity.getId())
                .tripId(trip != null ? trip.getId() : null)
                .driverId(driver != null ? driver.getId() : null)
                .accountId(entity.getAccount() != null ? entity.getAccount().getId() : null)
                .assignmentAt(entity.getAssignmentAt())
                .driverName(driverName)
                .vehiclePlate(vehiclePlate)
                .vehicleId(vehicleId)
                .routeCode(routeCode)
                .routeName(routeName)
                .startLocation(trip != null ? trip.getStartLocation() : null)
                .endLocation(trip != null ? trip.getEndLocation() : null)
                .distanceKm(trip != null ? trip.getDistanceKm() : null)
                .status(trip != null && trip.getStatus() != null ? trip.getStatus().name() : null)
                .plannedStartTime(trip != null ? trip.getPlannedStartTime() : null)
                .plannedEndTime(trip != null ? trip.getPlannedEndTime() : null)
                .startTime(trip != null ? trip.getStartTime() : null)
                .endTime(trip != null ? trip.getEndTime() : null)
                .note(trip != null ? trip.getNote() : null)
                .build();
    }
}
