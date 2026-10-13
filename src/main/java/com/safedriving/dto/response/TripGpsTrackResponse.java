package com.safedriving.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripGpsTrackResponse {

    private String id;

    private String tripId;

    private Double latitude;

    private Double longitude;

    private Double speedKmh;

    private LocalDateTime recordedAt;
}
