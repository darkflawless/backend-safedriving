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

        // 1. Tìm các phương tiện đang sẵn sàng (AVAILABLE)
        List<Vehicle> availableVehicles = vehicleRepository.findByStatusAndIsDeletedFalse(VehicleStatus.AVAILABLE);
        if (availableVehicles.isEmpty()) {
            throw new RuntimeException("Hiện không có phương tiện nào đang rảnh (AVAILABLE) để nổ đơn tự động!");
        }

        Vehicle selectedVehicle = null;
        Driver selectedDriver = null;

        // Ưu tiên 1: Tìm xe rảnh ĐÃ CÓ tài xế phụ trách (và tài xế đó đang hoạt động,
        // không bận chuyến)
        for (Vehicle v : availableVehicles) {
            Optional<Driver> driverOpt = driverRepository.findByVehicleIdAndIsDeletedFalse(v.getId());
            if (driverOpt.isPresent()) {
                Driver d = driverOpt.get();
                boolean isActive = Boolean.TRUE.equals(d.getIsActive());
                boolean hasTripInProgress = tripRepository
                        .findFirstByDriverIdAndStatusAndIsDeletedFalseOrderByStartTimeDesc(d.getId(),
                                TripStatus.IN_PROGRESS)
                        .isPresent();
                if (isActive && !hasTripInProgress) {
                    selectedVehicle = v;
                    selectedDriver = d;
                    break;
                }
            }
        }

        // Ưu tiên 2: Nếu không có cặp xe + tài xế cố định rảnh,
        // tìm xe rảnh CHƯA ĐƯỢC GÁN cố định cho ai VÀ tài xế đang hoạt động CHƯA CÓ XE
        // gán cố định
        if (selectedVehicle == null || selectedDriver == null) {
            // Lọc tài xế tự do: isActive = true, vehicle == null, không bận chuyến
            // IN_PROGRESS
            List<Driver> unassignedDrivers = driverRepository.findByIsDeletedFalse().stream()
                    .filter(d -> Boolean.TRUE.equals(d.getIsActive()))
                    .filter(d -> d.getVehicle() == null)
                    .filter(d -> tripRepository
                            .findFirstByDriverIdAndStatusAndIsDeletedFalseOrderByStartTimeDesc(d.getId(),
                                    TripStatus.IN_PROGRESS)
                            .isEmpty())
                    .toList();

            if (!unassignedDrivers.isEmpty()) {
                // Ưu tiên chọn xe rảnh chưa gán tài xế cố định nào
                for (Vehicle v : availableVehicles) {
                    boolean hasAssignedDriver = driverRepository.findByVehicleIdAndIsDeletedFalse(v.getId())
                            .isPresent();
                    if (!hasAssignedDriver) {
                        selectedVehicle = v;
                        break;
                    }
                }
                // Nếu tất cả xe rảnh đều đã được gán tài xế (nhưng tài xế cố định đang bận),
                // dùng tạm xe rảnh đầu tiên
                if (selectedVehicle == null) {
                    selectedVehicle = availableVehicles.get(0);
                }
                selectedDriver = unassignedDrivers.get(0);
            }
        }

        if (selectedVehicle == null || selectedDriver == null) {
            throw new RuntimeException(
                    "Không tìm thấy tài xế phù hợp đang rảnh để nổ đơn tự động! (Yêu cầu tài xế phụ trách xe rảnh hoặc tài xế tự do chưa gán xe)");
        }

        trip.setVehicle(selectedVehicle);
        trip.setDriver(selectedDriver);
        trip.setStatus(TripStatus.ASSIGNED);
        Trip saved = tripRepository.save(trip);

        // 3. Tự động ghi nhận lịch sử phân công vào bảng Assignment
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

        log.info("TỰ ĐỘNG NỔ ĐƠN THÀNH CÔNG: Chuyến {} -> Tài xế {} (Xe {})", saved.getId(), selectedDriver.getId(),
                selectedVehicle.getPlateNumber());
        return mapToResponse(saved);
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
