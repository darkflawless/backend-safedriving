package com.safedriving.service.impl;

import com.safedriving.dto.request.TripGpsTrackRequest;
import com.safedriving.dto.request.TripRequest;
import com.safedriving.dto.response.TripGpsTrackResponse;
import com.safedriving.dto.response.TripResponse;
import com.safedriving.entity.Account;
import com.safedriving.entity.Assignment;
import com.safedriving.entity.Driver;
import com.safedriving.entity.Route;
import com.safedriving.entity.Staff;
import com.safedriving.entity.Trip;
import com.safedriving.entity.TripGpsTrack;
import com.safedriving.entity.Vehicle;
import com.safedriving.entity.enums.TripStatus;
import com.safedriving.entity.enums.VehicleStatus;
import com.safedriving.exception.BadRequestException;
import com.safedriving.exception.ResourceNotFoundException;
import com.safedriving.repository.AccountRepository;
import com.safedriving.repository.AssignmentRepository;
import com.safedriving.repository.DriverRepository;
import com.safedriving.repository.IncidentRepository;
import com.safedriving.repository.RouteRepository;
import com.safedriving.repository.SomnolenceRecordRepository;
import com.safedriving.repository.TripGpsTrackRepository;
import com.safedriving.repository.TripRepository;
import com.safedriving.repository.VehicleRepository;
import com.safedriving.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripGpsTrackRepository tripGpsTrackRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final SomnolenceRecordRepository somnolenceRecordRepository;
    private final IncidentRepository incidentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAllTrips(TripStatus status, String driverId, String vehicleId) {
        List<Trip> trips;
        if (status != null) {
            trips = tripRepository.findByStatusAndIsDeletedFalseOrderByStartTimeDesc(status);
        } else if (driverId != null && !driverId.isBlank()) {
            trips = tripRepository.findByDriverIdAndIsDeletedFalseOrderByStartTimeDesc(driverId);
        } else if (vehicleId != null && !vehicleId.isBlank()) {
            trips = tripRepository.findByVehicleIdAndIsDeletedFalseOrderByStartTimeDesc(vehicleId);
        } else {
            trips = tripRepository.findAllByIsDeletedFalseOrderByStartTimeDesc();
        }

        if (trips == null) {
            return List.of();
        }

        return trips.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(String id) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + id));
        return mapToResponse(trip);
    }

    @Override
    @Transactional
    public TripResponse createTrip(TripRequest request) {
        Vehicle vehicle = null;
        if (request.getVehicleId() != null && !request.getVehicleId().isBlank()) {
            vehicle = vehicleRepository.findByIdAndIsDeletedFalse(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy phương tiện với ID: " + request.getVehicleId()));
        }

        Driver driver = null;
        if (request.getDriverId() != null && !request.getDriverId().isBlank()) {
            driver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy tài xế với ID: " + request.getDriverId()));
        }

        Route route = null;
        if (request.getRouteId() != null && !request.getRouteId().isBlank()) {
            route = routeRepository.findById(request.getRouteId()).orElse(null);
        }

        TripStatus status = request.getStatus() != null ? request.getStatus() : TripStatus.PLANNED;

        Trip trip = Trip.builder()
                .startLocation(request.getStartLocation())
                .endLocation(request.getEndLocation())
                .vehicle(vehicle)
                .driver(driver)
                .route(route)
                .status(status)
                .distanceKm(request.getDistanceKm() != null && request.getDistanceKm() > 0
                        ? request.getDistanceKm()
                        : (route != null && route.getDistanceKm() != null ? route.getDistanceKm() : 0.0))
                .plannedStartTime(
                        request.getPlannedStartTime() != null ? request.getPlannedStartTime() : LocalDateTime.now())
                .plannedEndTime(request.getPlannedEndTime())
                .note(request.getNote())
                .isDeleted(false)
                .build();

        Trip saved = tripRepository.save(trip);

        if (driver != null) {
            Assignment assignment = Assignment.builder()
                    .trip(saved)
                    .driver(driver)
                    .assignmentAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            assignmentRepository.save(assignment);
        }

        log.info("Đã tạo chuyến đi mới: id={}, start={}, end={}", saved.getId(), saved.getStartLocation(),
                saved.getEndLocation());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TripResponse updateTrip(String id, TripRequest request) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + id));

        trip.setStartLocation(request.getStartLocation());
        trip.setEndLocation(request.getEndLocation());

        if (request.getVehicleId() != null) {
            if (request.getVehicleId().isBlank()) {
                trip.setVehicle(null);
            } else {
                Vehicle vehicle = vehicleRepository.findByIdAndIsDeletedFalse(request.getVehicleId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy phương tiện với ID: " + request.getVehicleId()));
                trip.setVehicle(vehicle);
            }
        }

        if (request.getRouteId() != null) {
            if (request.getRouteId().isBlank()) {
                trip.setRoute(null);
            } else {
                Route route = routeRepository.findById(request.getRouteId()).orElse(null);
                trip.setRoute(route);
            }
        }

        if (request.getDriverId() != null && !request.getDriverId().isBlank()) {
            Driver driver = driverRepository.findByIdAndIsDeletedFalse(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy tài xế với ID: " + request.getDriverId()));
            trip.setDriver(driver);

            Assignment assignment = Assignment.builder()
                    .trip(trip)
                    .driver(driver)
                    .assignmentAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            assignmentRepository.save(assignment);
        }

        if (request.getStatus() != null) {
            trip.setStatus(request.getStatus());
        }

        if (request.getPlannedStartTime() != null) {
            trip.setPlannedStartTime(request.getPlannedStartTime());
        }
        if (request.getPlannedEndTime() != null) {
            trip.setPlannedEndTime(request.getPlannedEndTime());
        }
        if (request.getDistanceKm() != null && request.getDistanceKm() > 0) {
            trip.setDistanceKm(request.getDistanceKm());
        } else if (trip.getRoute() != null && trip.getRoute().getDistanceKm() != null) {
            trip.setDistanceKm(trip.getRoute().getDistanceKm());
        }
        if (request.getNote() != null) {
            trip.setNote(request.getNote());
        }

        Trip updated = tripRepository.save(trip);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTrip(String id) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + id));
        trip.setIsDeleted(true);
        tripRepository.save(trip);
        log.info("Đã xóa mềm chuyến đi: id={}", id);
    }

    @Override
    @Transactional
    public TripResponse startTrip(String id) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + id));

        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartTime(LocalDateTime.now());

        // Cập nhật trạng thái xe sang IN_SERVICE
        if (trip.getVehicle() != null) {
            Vehicle vehicle = trip.getVehicle();
            vehicle.setStatus(VehicleStatus.IN_SERVICE);
            vehicleRepository.save(vehicle);
        }

        Trip saved = tripRepository.save(trip);
        log.info("Tài xế đã bắt đầu chuyến đi: id={}, startTime={}", saved.getId(), saved.getStartTime());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TripResponse endTrip(String id, Double finalDistanceKm) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + id));

        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndTime(LocalDateTime.now());

        if (finalDistanceKm != null && finalDistanceKm > 0) {
            trip.setDistanceKm(finalDistanceKm);
        }

        // Giải phóng xe về trạng thái AVAILABLE
        if (trip.getVehicle() != null) {
            Vehicle vehicle = trip.getVehicle();
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            if (trip.getDistanceKm() != null && trip.getDistanceKm() > 0) {
                double currentOdo = vehicle.getOdometerKm() != null ? vehicle.getOdometerKm() : 0.0;
                vehicle.setOdometerKm(currentOdo + trip.getDistanceKm());
            }
            vehicleRepository.save(vehicle);
        }

        Trip saved = tripRepository.save(trip);
        log.info("Tài xế đã kết thúc chuyến đi: id={}, endTime={}, distanceKm={}", saved.getId(), saved.getEndTime(),
                saved.getDistanceKm());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TripResponse autoDispatchTrip(String id) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + id));

        if (trip.getStatus() != TripStatus.PLANNED) {
            throw new BadRequestException("Chuyến đi này hiện có trạng thái là '" + trip.getStatus().name()
                    + "'. Chỉ có thể tự động phân công cho chuyến đi đang 'Lên kế hoạch' (PLANNED)!");
        }

        LocalDateTime newStart = trip.getPlannedStartTime();
        LocalDateTime newEnd = trip.getPlannedEndTime();
        if (newStart == null || newEnd == null) {
            throw new BadRequestException("Chuyến đi phải có thời gian xuất phát và kết thúc dự kiến để hệ thống kiểm tra lịch tự động!");
        }
        if (newEnd.isBefore(newStart)) {
            throw new BadRequestException("Thời gian đến nơi dự kiến không thể trước thời gian xuất phát!");
        }

        // Lấy danh sách tài xế hợp lệ: đang hoạt động, chưa bị xóa, đã được gán phương tiện
        List<Driver> activeDrivers = driverRepository.findByIsDeletedFalse().stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsActive()))
                .filter(d -> d.getVehicle() != null && !Boolean.TRUE.equals(d.getVehicle().getIsDeleted()))
                .filter(d -> d.getVehicle().getStatus() != VehicleStatus.MAINTENANCE
                        && d.getVehicle().getStatus() != VehicleStatus.OUT_OF_SERVICE)
                .toList();

        if (activeDrivers.isEmpty()) {
            throw new BadRequestException("Hiện không có tài xế nào đang hoạt động và có phương tiện sẵn sàng trong hệ thống!");
        }

        Driver selectedDriver = null;
        Vehicle selectedVehicle = null;

        // Quét từng tài xế để tìm người rảnh trong khung giờ [newStart, newEnd]
        for (Driver d : activeDrivers) {
            Vehicle v = d.getVehicle();

            // 1. Kiểm tra tài xế có bị trùng lịch chuyến nào không
            boolean driverBusy = hasDriverTimeOverlap(d.getId(), newStart, newEnd, trip.getId());
            if (driverBusy) {
                continue;
            }

            // 2. Kiểm tra phương tiện của tài xế có bị trùng lịch chuyến nào khác không
            boolean vehicleBusy = hasVehicleTimeOverlap(v.getId(), newStart, newEnd, trip.getId());
            if (vehicleBusy) {
                continue;
            }

            selectedDriver = d;
            selectedVehicle = v;
            break;
        }

        if (selectedDriver == null || selectedVehicle == null) {
            throw new BadRequestException("Tất cả tài xế và phương tiện phù hợp đều đã bận hoặc trùng lịch trong khoảng thời gian "
                    + newStart + " - " + newEnd + ". Không thể tự động phân công!");
        }

        trip.setVehicle(selectedVehicle);
        trip.setDriver(selectedDriver);
        trip.setStatus(TripStatus.ASSIGNED);
        Trip saved = tripRepository.save(trip);

        // Tự động ghi nhận lịch sử phân công vào bảng Assignment
        Account operatorAccount = null;
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
                operatorAccount = accountRepository.findByUsernameAndIsDeletedFalse(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {
        }
        if (operatorAccount == null) {
            operatorAccount = accountRepository.findAll().stream()
                    .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                    .findFirst()
                    .orElse(null);
        }

        Assignment assignment = Assignment.builder()
                .trip(saved)
                .driver(selectedDriver)
                .account(operatorAccount)
                .assignmentAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        assignmentRepository.save(assignment);

        log.info("TỰ ĐỘNG PHÂN CÔNG THÀNH CÔNG: Chuyến {} -> Tài xế {} (Xe {})", saved.getId(), selectedDriver.getId(),
                selectedVehicle.getPlateNumber());
        return mapToResponse(saved);
    }

    private boolean hasDriverTimeOverlap(String driverId, LocalDateTime newStart, LocalDateTime newEnd, String excludeTripId) {
        List<Assignment> assignments = assignmentRepository.findByDriverIdAndIsDeletedFalse(driverId);
        for (Assignment a : assignments) {
            Trip t = a.getTrip();
            if (t == null) continue;
            if (excludeTripId != null && t.getId().equals(excludeTripId)) continue;
            if (t.getStatus() == TripStatus.COMPLETED || t.getStatus() == TripStatus.CANCELLED) continue;

            LocalDateTime tStart = t.getPlannedStartTime();
            LocalDateTime tEnd = t.getPlannedEndTime();
            if (tStart == null || tEnd == null) continue;

            // Nếu chuyến đang chạy (IN_PROGRESS), thời gian kết thúc ít nhất là thời điểm hiện tại
            LocalDateTime effectiveEnd = tEnd;
            if (t.getStatus() == TripStatus.IN_PROGRESS && effectiveEnd.isBefore(LocalDateTime.now())) {
                effectiveEnd = LocalDateTime.now();
            }

            if (newStart.isBefore(effectiveEnd) && newEnd.isAfter(tStart)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVehicleTimeOverlap(String vehicleId, LocalDateTime newStart, LocalDateTime newEnd, String excludeTripId) {
        List<Trip> trips = tripRepository.findByVehicleIdAndIsDeletedFalseOrderByStartTimeDesc(vehicleId);
        for (Trip t : trips) {
            if (excludeTripId != null && t.getId().equals(excludeTripId)) continue;
            if (t.getStatus() == TripStatus.COMPLETED || t.getStatus() == TripStatus.CANCELLED || t.getStatus() == TripStatus.PLANNED) continue;

            LocalDateTime tStart = t.getPlannedStartTime();
            LocalDateTime tEnd = t.getPlannedEndTime();
            if (tStart == null || tEnd == null) continue;

            LocalDateTime effectiveEnd = tEnd;
            if (t.getStatus() == TripStatus.IN_PROGRESS && effectiveEnd.isBefore(LocalDateTime.now())) {
                effectiveEnd = LocalDateTime.now();
            }

            if (newStart.isBefore(effectiveEnd) && newEnd.isAfter(tStart)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getActiveTripByDriver(String driverId) {
        Trip trip = tripRepository
                .findFirstByDriverIdAndStatusAndIsDeletedFalseOrderByStartTimeDesc(driverId, TripStatus.IN_PROGRESS)
                .orElseGet(() -> tripRepository
                        .findFirstByDriverIdAndStatusAndIsDeletedFalseOrderByStartTimeDesc(driverId, TripStatus.PLANNED)
                        .orElse(null));

        if (trip == null) {
            return null;
        }
        return mapToResponse(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getMyTripHistory(com.safedriving.security.CustomUserDetails userDetails, TripStatus status) {
        String accountId = userDetails.getAccount().getId();
        Driver driver = driverRepository.findByStaffAccountIdAndIsDeletedFalse(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ tài xế liên kết với tài khoản này"));
        return getTripHistoryByDriver(driver.getId(), status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripHistoryByDriver(String driverId, TripStatus status) {
        List<Trip> trips = tripRepository.findByDriverIdAndIsDeletedFalseOrderByStartTimeDesc(driverId);
        return trips.stream()
                .filter(t -> {
                    // Lịch sử chuyến đi: chỉ lấy các chuyến đã kết thúc (COMPLETED hoặc CANCELLED)
                    if (t.getStatus() != TripStatus.COMPLETED && t.getStatus() != TripStatus.CANCELLED) {
                        return false;
                    }
                    if (status != null) {
                        return t.getStatus() == status;
                    }
                    return true;
                })
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public TripGpsTrackResponse recordGpsTrack(String tripId, TripGpsTrackRequest request) {
        Trip trip = tripRepository.findByIdAndIsDeletedFalse(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + tripId));

        LocalDateTime now = request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now();

        // 1. Lưu điểm GPS waypoint
        TripGpsTrack track = TripGpsTrack.builder()
                .trip(trip)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speedKmh(request.getSpeedKmh())
                .recordedAt(now)
                .build();

        TripGpsTrack savedTrack = tripGpsTrackRepository.save(track);

        // 2. Tính toán cộng dồn quãng đường từ điểm trước đó
        List<TripGpsTrack> existingTracks = tripGpsTrackRepository.findByTripIdOrderByRecordedAtAsc(tripId);
        if (existingTracks.size() >= 2) {
            TripGpsTrack prev = existingTracks.get(existingTracks.size() - 2);
            double distDelta = calculateDistanceKm(prev.getLatitude(), prev.getLongitude(), request.getLatitude(),
                    request.getLongitude());
            // Lọc nhiễu GPS: khoảng cách giữa 2 điểm phải lớn hơn 10m và nhỏ hơn 50km
            if (distDelta > 0.01 && distDelta < 50.0) {
                double currentDistance = trip.getDistanceKm() != null ? trip.getDistanceKm() : 0.0;
                trip.setDistanceKm(Math.round((currentDistance + distDelta) * 100.0) / 100.0);
                tripRepository.save(trip);
            }
        }

        return TripGpsTrackResponse.builder()
                .id(savedTrack.getId())
                .tripId(trip.getId())
                .latitude(savedTrack.getLatitude())
                .longitude(savedTrack.getLongitude())
                .speedKmh(savedTrack.getSpeedKmh())
                .recordedAt(savedTrack.getRecordedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripGpsTrackResponse> getGpsTracks(String tripId) {
        List<TripGpsTrack> tracks = tripGpsTrackRepository.findByTripIdOrderByRecordedAtAsc(tripId);
        if (tracks == null) {
            return List.of();
        }
        return tracks.stream().map(t -> TripGpsTrackResponse.builder()
                .id(t.getId())
                .tripId(tripId)
                .latitude(t.getLatitude())
                .longitude(t.getLongitude())
                .speedKmh(t.getSpeedKmh())
                .recordedAt(t.getRecordedAt())
                .build()).toList();
    }

    private TripResponse mapToResponse(Trip trip) {
        Driver driver = trip.getDriver();
        Vehicle vehicle = trip.getVehicle();

        // Nếu chuyến đi chưa liên kết trực tiếp driver / vehicle nhưng đã có phân công
        // hợp lệ trong hệ thống
        if (driver == null || vehicle == null) {
            List<Assignment> assignments = assignmentRepository.findByTripIdAndIsDeletedFalse(trip.getId());
            if (assignments != null && !assignments.isEmpty()) {
                Assignment latest = assignments.get(assignments.size() - 1);
                if (driver == null && latest.getDriver() != null) {
                    driver = latest.getDriver();
                }
                if (vehicle == null && driver != null && driver.getVehicle() != null) {
                    vehicle = driver.getVehicle();
                }
            }
        }

        String vehiclePlate = vehicle != null ? vehicle.getPlateNumber() : null;
        String vehicleTypeName = (vehicle != null && vehicle.getVehicleType() != null)
                ? vehicle.getVehicleType().getName()
                : null;

        String driverName = null;
        String driverPhone = null;
        if (driver != null && driver.getStaff() != null) {
            Staff staff = driver.getStaff();
            String lastName = staff.getLastName() != null ? staff.getLastName() : "";
            String firstName = staff.getFirstName() != null ? staff.getFirstName() : "";
            driverName = (lastName + " " + firstName).trim();
            driverPhone = staff.getPhone();
        }

        long somnolenceCount = somnolenceRecordRepository.countByTripId(trip.getId());
        long incidentCount = incidentRepository.countByTripId(trip.getId());
        long gpsTrackCount = tripGpsTrackRepository.countByTripId(trip.getId());

        TripStatus status = trip.getStatus();
        List<Assignment> activeAssignments = assignmentRepository.findByTripIdAndIsDeletedFalse(trip.getId());
        boolean hasActiveAssignment = (activeAssignments != null && !activeAssignments.isEmpty());

        // Nếu chuyến đi đang ghi là ASSIGNED nhưng không còn phân công nào còn hiệu lực (do đã bị xoá),
        // tự động giải phóng tài xế, xe và chuyển trạng thái về PLANNED
        if (status == TripStatus.ASSIGNED && !hasActiveAssignment) {
            driver = null;
            vehicle = null;
            vehiclePlate = null;
            vehicleTypeName = null;
            driverName = null;
            driverPhone = null;
            status = TripStatus.PLANNED;
        }

        return TripResponse.builder()
                .id(trip.getId())
                .startLocation(trip.getStartLocation())
                .endLocation(trip.getEndLocation())
                .vehicleId(vehicle != null ? vehicle.getId() : null)
                .vehiclePlateNumber(vehiclePlate)
                .vehicleTypeName(vehicleTypeName)
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driverName)
                .driverPhone(driverPhone)
                .distanceKm(trip.getDistanceKm() != null ? trip.getDistanceKm() : 0.0)
                .status(status)
                .plannedStartTime(trip.getPlannedStartTime())
                .plannedEndTime(trip.getPlannedEndTime())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .note(trip.getNote())
                .routeId(trip.getRoute() != null ? trip.getRoute().getId() : null)
                .routeName(trip.getRoute() != null ? trip.getRoute().getRouteName() : null)
                .somnolenceCount(somnolenceCount)
                .incidentCount(incidentCount)
                .gpsTrackCount(gpsTrackCount)
                .build();
    }

    /**
     * Công thức Haversine tính khoảng cách giữa 2 tọa độ (đơn vị: km)
     */
    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính Trái Đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
