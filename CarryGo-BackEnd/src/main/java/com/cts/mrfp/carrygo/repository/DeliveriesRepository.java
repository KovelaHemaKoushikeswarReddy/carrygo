package com.cts.mrfp.carrygo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.mrfp.carrygo.model.Deliveries;

// JPA queries for the deliveries table.
// Method names follow Spring Data conventions so the SQL is generated automatically.
public interface DeliveriesRepository extends JpaRepository<Deliveries, Integer> {

    // All deliveries created by a specific user (their "sent parcels" history).
    List<Deliveries> findBySenderUserId(Integer userId);

    // All deliveries assigned to a specific porter.
    List<Deliveries> findByCommuterUserId(Integer userId);

    // All deliveries with a given status — used to fetch "PENDING" requests for porters.
    List<Deliveries> findByStatus(String status);
}