package com.cts.mrfp.carrygo.service;

import com.cts.mrfp.carrygo.model.Deliveries;
import com.cts.mrfp.carrygo.repository.DeliveriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledDeliveryDispatcher {

    @Autowired private DeliveriesRepository deliveriesRepo;
    @Autowired private DeliveriesService deliveriesService;

    // Runs every minute — finds scheduled deliveries whose time has arrived and dispatches them
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void dispatchDueDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        List<Deliveries> pending = deliveriesRepo.findPendingScheduledNotDispatched();
        for (Deliveries delivery : pending) {
            LocalDateTime scheduledAt = deliveriesService.buildScheduledDateTime(delivery);
            if (!scheduledAt.isAfter(now)) {
                deliveriesService.dispatchDelivery(delivery);
            }
        }
    }
}
