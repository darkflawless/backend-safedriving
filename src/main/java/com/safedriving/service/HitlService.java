package com.safedriving.service;

import com.safedriving.dto.request.HitlReviewDecisionRequest;
import com.safedriving.dto.response.HitlCaseResponse;
import com.safedriving.entity.enums.HitlStatus;
import com.safedriving.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HitlService {

    Page<HitlCaseResponse> getQueue(HitlStatus status, Pageable pageable);

    HitlCaseResponse getCaseById(String id);

    HitlCaseResponse reviewCase(String id, CustomUserDetails reviewer, HitlReviewDecisionRequest request);
}
