package com.cts.mrfp.carrygo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.mrfp.carrygo.model.Notifications;

// JPA queries for the notifications table.
public interface NotificationsRepository extends JpaRepository<Notifications, Integer> {
    // Notifications for one user, newest first.
    List<Notifications> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
}