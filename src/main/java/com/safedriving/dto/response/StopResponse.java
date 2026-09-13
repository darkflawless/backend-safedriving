package com.safedriving.dto.response;

import com.safedriving.entity.enums.StopType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopResponse {

    private String id;
    private String nameStop;
    private AddressResponse address;
    private StopType type;
}
