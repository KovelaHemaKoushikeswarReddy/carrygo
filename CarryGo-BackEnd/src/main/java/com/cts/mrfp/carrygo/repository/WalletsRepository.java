package com.cts.mrfp.carrygo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.mrfp.carrygo.model.Wallets;
import java.util.Optional;

// JPA queries for the wallets table.
public interface WalletsRepository extends JpaRepository<Wallets, Integer> {
    // Each user has exactly one wallet — look it up by user ID.
    Optional<Wallets> findByUserUserId(Integer userId);
}