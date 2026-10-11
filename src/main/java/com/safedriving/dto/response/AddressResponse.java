package com.safedriving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private String id;
    private String exactAddress;
    private String commune;
    private String province;
    private BigDecimal lat;
    private BigDecimal lng;
}
