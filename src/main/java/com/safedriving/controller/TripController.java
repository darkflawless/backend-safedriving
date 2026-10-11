package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.TripGpsTrackRequest;
import com.safedriving.dto.request.TripRequest;
import com.safedriving.dto.response.TripGpsTrackResponse;
import com.safedriving.dto.response.TripResponse;
import com.safedriving.entity.enums.TripStatus;
import com.safedriving.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/trips", "/api/v1/trips"})
@RequiredArgsConstructor
@Tag(name = "Trip Management", description = "APIs Quản lý Chuyến đi thực tế, bắt đầu/kết thúc chuyến và lưu trữ vệt tọa độ GPS")
public class TripController {

    private final TripService tripService;

    @GetMapping
    @Operation(summary = "Lấy danh sách chuyến đi", description = "Lấy toàn bộ danh sách chuyến đi hoặc lọc theo trạng thái, tài xế, phương tiện")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getAllTrips(
            @RequestParam(required = false) TripStatus status,
            @RequestParam(required = false) String driverId,
            @RequestParam(required = false) String vehicleId) {
        List<TripResponse> trips = tripService.getAllTrips(status, driverId, vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chuyến đi thành công", trips));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết chuyến đi", description = "Tra cứu thông tin chi tiết một chuyến đi theo ID")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(@PathVariable String id) {
        TripResponse trip = tripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết chuyến đi thành công", trip));
    }

    @PostMapping
    @Operation(summary = "Tạo chuyến đi mới", description = "Điều phối viên/Admin tạo chuyến đi với điểm đón, điểm đến, xe và tài xế")
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(@Valid @RequestBody TripRequest request) {
        TripResponse created = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo chuyến đi thành công", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật chuyến đi", description = "Cập nhật thông tin điểm đi, điểm đến hoặc xe/tài xế")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @PathVariable String id,
            @Valid @RequestBody TripRequest request) {
        TripResponse updated = tripService.updateTrip(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chuyến đi thành công", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa chuyến đi", description = "Xóa mềm một chuyến đi")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(@PathVariable String id) {
        tripService.deleteTrip(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chuyến đi thành công", null));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Bắt đầu chuyến đi", description = "Tài xế bấm bắt đầu chuyến trên App Android: đổi trạng thái sang IN_PROGRESS, ghi nhận startTime, chuyển xe sang IN_SERVICE")
    public ResponseEntity<ApiResponse<TripResponse>> startTrip(@PathVariable String id) {
        TripResponse response = tripService.startTrip(id);
        return ResponseEntity.ok(ApiResponse.success("Bắt đầu chuyến đi thành công", response));
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "Kết thúc chuyến đi", description = "Tài xế bấm kết thúc chuyến trên App Android: đổi trạng thái sang COMPLETED, ghi nhận endTime, giải phóng xe sang AVAILABLE")
    public ResponseEntity<ApiResponse<TripResponse>> endTrip(
            @PathVariable String id,
            @RequestParam(required = false) Double distanceKm) {
        TripResponse response = tripService.endTrip(id, distanceKm);
        return ResponseEntity.ok(ApiResponse.success("Kết thúc chuyến đi thành công", response));
    }

    @PostMapping("/{id}/auto-dispatch")
    @Operation(summary = "Tự động nổ đơn phân công", description = "Hệ thống tự động quét tìm xe đang rảnh (AVAILABLE) và tài xế phụ trách để gán vào chuyến đi, đồng thời tự động lưu vết Assignment")
    public ResponseEntity<ApiResponse<TripResponse>> autoDispatch(@PathVariable String id) {
        TripResponse response = tripService.autoDispatchTrip(id);
        return ResponseEntity.ok(ApiResponse.success("Tự động nổ đơn chuyến đi thành công", response));
    }

    @GetMapping("/active/driver/{driverId}")
    @Operation(summary = "Lấy chuyến đi hiện tại của tài xế", description = "Mobile App gọi để tự động mở chuyến đi đang chạy hoặc chuyến sắp tới được phân công")
    public ResponseEntity<ApiResponse<TripResponse>> getActiveTripByDriver(@PathVariable String driverId) {
        TripResponse trip = tripService.getActiveTripByDriver(driverId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chuyến đi hiện tại thành công", trip));
    }

    @PostMapping("/{id}/tracks")
    @Operation(summary = "Ghi nhận tọa độ GPS của chuyến đi", description = "Mobile App hoặc thiết bị GPS định kỳ gửi tọa độ để lưu vệt đường và cộng dồn số km")
    public ResponseEntity<ApiResponse<TripGpsTrackResponse>> recordGpsTrack(
            @PathVariable String id,
            @Valid @RequestBody TripGpsTrackRequest request) {
        TripGpsTrackResponse track = tripService.recordGpsTrack(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ghi nhận tọa độ GPS thành công", track));
    }

    @GetMapping("/{id}/tracks")
    @Operation(summary = "Lấy vệt tọa độ GPS của chuyến đi", description = "Phục vụ vẽ lại lộ trình xe đã di chuyển từ điểm A đến điểm B trên bản đồ Web")
    public ResponseEntity<ApiResponse<List<TripGpsTrackResponse>>> getGpsTracks(@PathVariable String id) {
        List<TripGpsTrackResponse> tracks = tripService.getGpsTracks(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy lộ trình tọa độ thành công", tracks));
    }
}
