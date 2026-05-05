package com.cts.mrfp.carrygo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cts.mrfp.carrygo.model.Deliveries;

public interface DeliveriesRepository extends JpaRepository<Deliveries, Integer> {
    List<Deliveries> findBySenderUserId(Integer userId);
    List<Deliveries> findByCommuterUserId(Integer userId);
    List<Deliveries> findByStatus(String status);

    // Scheduled deliveries that are PENDING and have not yet been dispatched to porters (totalPool is null).
    // JOIN FETCH sender to avoid LazyInitializationException when the scheduler accesses sender outside a session.
    @Query("SELECT d FROM Deliveries d LEFT JOIN FETCH d.sender WHERE d.status = 'PENDING' AND d.preferredDate IS NOT NULL AND d.totalPool IS NULL")
    List<Deliveries> findPendingScheduledNotDispatched();
}