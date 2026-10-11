package com.safedriving.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tham chiếu ID hạng giấy phép lái xe")
public class LicenseClassReference {

    @Schema(description = "ID hạng bằng lái (Short/Number hoặc String có thể parse)", example = "1")
    private Object id;

    public Short getShortId() {
        if (id == null) {
            return null;
        }
        if (id instanceof Number number) {
            return number.shortValue();
        }
        try {
            return Short.parseShort(id.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
