package com.safedriving.service;

import com.safedriving.dto.request.ShiftEndRequest;
import com.safedriving.dto.request.ShiftStartRequest;
import com.safedriving.dto.response.ShiftResponse;
import com.safedriving.security.CustomUserDetails;

public interface ShiftService {

    ShiftResponse startShift(CustomUserDetails userDetails, ShiftStartRequest request);

    ShiftResponse endShift(CustomUserDetails userDetails, ShiftEndRequest request);

    ShiftResponse getActiveShift(CustomUserDetails userDetails);
}
