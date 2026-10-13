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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

        // Chuyển trạng thái chuyến đi sang ASSIGNED
        trip.setStatus(TripStatus.ASSIGNED);
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

        assignment.setTrip(trip);
        assignment.setDriver(driver);
        assignment.setAccount(account);

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Assignment assignment = assignmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công ID: " + id));
        assignment.setIsDeleted(true);
        assignmentRepository.save(assignment);
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
        if (driver != null && driver.getVehicle() != null) {
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
                .build();
    }
}
