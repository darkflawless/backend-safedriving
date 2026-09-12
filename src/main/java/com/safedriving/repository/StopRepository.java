package com.safedriving.repository;

import com.safedriving.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, String> {

    List<Stop> findByAddressId(String addressId);
}
