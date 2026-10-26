package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.entity.AccidentBlackspot;
import com.safedriving.repository.AccidentBlackspotRepository;
import com.safedriving.service.GeofenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        AccidentBlackspot saved = accidentBlackspotRepository.save(blackspot);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm điểm đen tai nạn thành công", saved));
    }
}
