package com.safedriving.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi thông tin chi tiết loại xe.
 *
 * @author phanthuy0811
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết loại xe")
public class VehicleTypeResponse {

    @Schema(description = "ID loại xe", example = "1")
    private Short id;

    @Schema(description = "Mã loại xe", example = "BUS29")
    private String code;

    @Schema(description = "Tên loại xe", example = "Xe Khách 29 chỗ")
    private String name;

    @Schema(description = "Sức chứa hành khách", example = "29")
    private Integer capacity;
}
