package com.safedriving.dto.request;

import com.safedriving.entity.enums.StopType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopRequest {

    @NotBlank(message = "Tên điểm dừng không được để trống")
    private String nameStop;

    @NotBlank(message = "ID địa chỉ không được để trống")
    private String addressId;

    private StopType type;
}
