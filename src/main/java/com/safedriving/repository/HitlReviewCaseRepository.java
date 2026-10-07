package com.safedriving.repository;

import com.safedriving.entity.HitlReviewCase;
import com.safedriving.entity.enums.HitlStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HitlReviewCaseRepository extends JpaRepository<HitlReviewCase, String> {

    List<HitlReviewCase> findByStatusOrderByCreatedAtDesc(HitlStatus status);

    Page<HitlReviewCase> findByStatus(HitlStatus status, Pageable pageable);

    Optional<HitlReviewCase> findByAlertId(String alertId);
}
