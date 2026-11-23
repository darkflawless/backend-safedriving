package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.entity.AccidentBlackspot;
import com.safedriving.repository.AccidentBlackspotRepository;
import com.safedriving.service.GeofenceService;
import com.safedriving.entity.enums.HazardType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({ "/blackspots", "/api/v1/blackspots" })
@RequiredArgsConstructor
@Tag(name = "Accident Blackspots", description = "APIs Quản lý Điểm đen tai nạn giao thông & Vùng cảnh báo Geofence")
public class AccidentBlackspotController {

    private final GeofenceService geofenceService;
    private final AccidentBlackspotRepository accidentBlackspotRepository;

    @GetMapping
    @Operation(summary = "Lấy danh sách các điểm đen tai nạn đang kích hoạt",
            description = "Trả về danh sách tọa độ, bán kính (mặc định 200m) và thông tin nguy cơ để vẽ lên bản đồ Leaflet")
    public ResponseEntity<ApiResponse<List<AccidentBlackspot>>> getAllActiveBlackspots(
            @RequestParam(required = false) String routeId) {
        List<AccidentBlackspot> list;
        if (routeId != null && !routeId.isBlank()) {
            list = accidentBlackspotRepository.findByRouteIdAndIsActiveTrueAndIsDeletedFalse(routeId);
        } else {
            list = geofenceService.getActiveBlackspots();
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách điểm đen thành công", list));
    }

    @PostMapping
    @Operation(summary = "Thêm điểm đen tai nạn mới", description = "Admin ghim vị trí điểm đen mới với bán kính và lý do phản hồi")
    public ResponseEntity<ApiResponse<AccidentBlackspot>> createBlackspot(@RequestBody AccidentBlackspot blackspot) {
        if (blackspot.getRadiusMeters() == null || blackspot.getRadiusMeters() <= 0) {
            blackspot.setRadiusMeters(200.0);
        }
        if (blackspot.getHazardType() == null) {
            blackspot.setHazardType(HazardType.ACCIDENT_PRONE);
        }
        if (blackspot.getIsActive() == null) {
            blackspot.setIsActive(true);
        }
        if (blackspot.getIsDeleted() == null) {
            blackspot.setIsDeleted(false);
        }
        if (blackspot.getFeedbackCount() == null) {
            blackspot.setFeedbackCount(0);
        }
        if (blackspot.getCreatedAt() == null) {
            blackspot.setCreatedAt(LocalDateTime.now());
        }
        AccidentBlackspot saved = accidentBlackspotRepository.save(blackspot);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm điểm đen tai nạn thành công", saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa điểm đen tai nạn", description = "Gỡ bỏ cờ điểm đen tai nạn khỏi bản đồ và hệ thống cảnh báo Geofence")
    public ResponseEntity<ApiResponse<Void>> deleteBlackspot(@PathVariable String id) {
        Optional<AccidentBlackspot> opt = accidentBlackspotRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "Không tìm thấy điểm đen với ID: " + id));
        }
        AccidentBlackspot spot = opt.get();
        spot.setIsActive(false);
        spot.setIsDeleted(true);
        accidentBlackspotRepository.save(spot);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa điểm đen tai nạn thành công", null));
    }
}
