package com.safedriving.repository;

import com.safedriving.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {

    List<Staff> findByIsDeletedFalse();

    Optional<Staff> findByIdAndIsDeletedFalse(String id);

    boolean existsByAccountId(String accountId);

    boolean existsByAccountIdAndIsDeletedFalse(String accountId);

    boolean existsByAccountIdAndIdNot(String accountId, String id);

    boolean existsByAccountIdAndIdNotAndIsDeletedFalse(String accountId, String id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, String id);

    Optional<Staff> findByAccountId(String accountId);

    Optional<Staff> findByAccountIdAndIsDeletedFalse(String accountId);
}
