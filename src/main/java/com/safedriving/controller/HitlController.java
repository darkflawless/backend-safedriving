package com.safedriving.controller;

import com.safedriving.dto.common.ApiResponse;
import com.safedriving.dto.request.HitlReviewDecisionRequest;
import com.safedriving.dto.response.HitlCaseResponse;
import com.safedriving.entity.enums.HitlStatus;
import com.safedriving.security.CustomUserDetails;
import com.safedriving.service.HitlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/hitl", "/api/v1/hitl"})
@RequiredArgsConstructor
@Tag(name = "HITL Review Center", description = "APIs Cổng kiểm duyệt vi phạm Human-in-the-Loop (Duyệt vi phạm AI mức độ HIGH/CRITICAL)")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class HitlController {

    private final HitlService hitlService;

    @GetMapping("/queue")
    @Operation(summary = "Lấy hàng đợi vi phạm cần duyệt (HITL Queue)",
            description = "Trả về danh sách vi phạm đang ở trạng thái PENDING (hoặc APPROVED, REJECTED) kèm ảnh bằng chứng và sinh trắc học.")
    public ResponseEntity<ApiResponse<Page<HitlCaseResponse>>> getQueue(
            @RequestParam(required = false, defaultValue = "PENDING") HitlStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<HitlCaseResponse> responses = hitlService.getQueue(
                status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy hàng đợi vi phạm thành công", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một ca duyệt vi phạm",
            description = "Tra cứu đầy đủ thông tin ca duyệt theo ID.")
    public ResponseEntity<ApiResponse<HitlCaseResponse>> getCaseById(@PathVariable String id) {
        HitlCaseResponse response = hitlService.getCaseById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết ca vi phạm thành công", response));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Gửi quyết định kiểm duyệt vi phạm (APPROVED / REJECTED)",
            description = "Xác nhận vi phạm hoặc bác bỏ sai số AI kèm ghi chú của người giám sát.")
    public ResponseEntity<ApiResponse<HitlCaseResponse>> reviewCase(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails reviewer,
            @Valid @RequestBody HitlReviewDecisionRequest request) {
        HitlCaseResponse response = hitlService.reviewCase(id, reviewer, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật quyết định kiểm duyệt thành công", response));
    }
}
