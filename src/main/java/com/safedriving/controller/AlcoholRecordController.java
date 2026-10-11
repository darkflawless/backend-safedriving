package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.AlcoholRecordRequest;
import com.safedriving.dto.response.AlcoholRecordResponse;
import com.safedriving.service.AlcoholRecordService;
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
@RequestMapping("/alcohol-records")
@RequiredArgsConstructor
@Tag(name = "Alcohol Records", description = "Quản lý bản ghi kiểm tra nồng độ cồn của tài xế")
public class AlcoholRecordController {

    private final AlcoholRecordService alcoholRecordService;

    @GetMapping
    @Operation(summary = "Lấy danh sách bản ghi nồng độ cồn", description = "Truy xuất danh sách. Có thể lọc theo driverId.")
    public ResponseEntity<ApiResponse<List<AlcoholRecordResponse>>> getAllAlcoholRecords(
            @RequestParam(required = false) String driverId) {
        List<AlcoholRecordResponse> records = alcoholRecordService.getAllAlcoholRecords(driverId);
        return ResponseEntity.ok(ApiResponse.<List<AlcoholRecordResponse>>builder()
                .success(true)
                .data(records)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tra cứu bản ghi nồng độ cồn", description = "Lấy thông tin chi tiết một bản ghi theo ID")
    public ResponseEntity<ApiResponse<AlcoholRecordResponse>> getAlcoholRecordById(@PathVariable Long id) {
        AlcoholRecordResponse record = alcoholRecordService.getAlcoholRecordById(id);
        return ResponseEntity.ok(ApiResponse.<AlcoholRecordResponse>builder()
                .success(true)
                .data(record)
                .build());
    }

    @PostMapping
    @Operation(summary = "Ghi nhận nồng độ cồn", description = "Tạo mới bản ghi nồng độ cồn cho tài xế")
    public ResponseEntity<ApiResponse<AlcoholRecordResponse>> createAlcoholRecord(
            @Valid @RequestBody AlcoholRecordRequest request) {
        AlcoholRecordResponse createdRecord = alcoholRecordService.createAlcoholRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AlcoholRecordResponse>builder()
                        .success(true)
                        .data(createdRecord)
                        .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật bản ghi nồng độ cồn", description = "Cập nhật thông tin bản ghi")
    public ResponseEntity<ApiResponse<AlcoholRecordResponse>> updateAlcoholRecord(
            @PathVariable Long id,
            @RequestBody AlcoholRecordRequest request) {
        AlcoholRecordResponse updatedRecord = alcoholRecordService.updateAlcoholRecord(id, request);
        return ResponseEntity.ok(ApiResponse.<AlcoholRecordResponse>builder()
                .success(true)
                .data(updatedRecord)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa bản ghi nồng độ cồn", description = "Xóa cứng bản ghi khỏi hệ thống")
    public ResponseEntity<ApiResponse<String>> deleteAlcoholRecord(@PathVariable Long id) {
        alcoholRecordService.deleteAlcoholRecord(id);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Xóa bản ghi nồng độ cồn thành công")
                .build());
    }
}
