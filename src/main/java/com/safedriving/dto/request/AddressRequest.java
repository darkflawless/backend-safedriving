package com.safedriving.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String exactAddress;

    private String commune;

    private String province;

    private BigDecimal lat;

    private BigDecimal lng;
}
