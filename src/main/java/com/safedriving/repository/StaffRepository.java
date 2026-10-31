package com.safedriving.repository;

import com.safedriving.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {

    boolean existsByAccountId(String accountId);

    boolean existsByAccountIdAndIdNot(String accountId, String id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, String id);

    Optional<Staff> findByAccountId(String accountId);
}
