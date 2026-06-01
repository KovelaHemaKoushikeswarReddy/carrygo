package com.cts.mrfp.carrygo.repository;

import com.cts.mrfp.carrygo.dto.RatingsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cts.mrfp.carrygo.model.Ratings;

import java.util.List;

// JPA queries for porter ratings.
public interface RatingsRepository extends JpaRepository<Ratings, Integer> {

    // Returns the rating list already mapped into DTO form so the service doesn't
    // have to convert each row by hand.
    @Query("SELECT new com.cts.mrfp.carrygo.dto.RatingsDTO(" +
           "r.ratingId, r.delivery.deliveryId, r.sender.userId, r.commuter.userId, " +
           "r.rating, r.comment, r.createdAt) " +
           "FROM Ratings r WHERE r.commuter.userId = :commuterId")
    List<RatingsDTO> findDTOsByCommuterId(@Param("commuterId") Integer commuterId);

    // Average star rating for one porter. Null if they have no ratings yet.
    @Query(value = "SELECT AVG(rating) FROM ratings WHERE commuter_id = :commuterId", nativeQuery = true)
    Double findAvgRatingByCommuterId(@Param("commuterId") Integer commuterId);

    // True if this delivery has already been rated — prevents duplicate ratings.
    boolean existsByDelivery_DeliveryId(Integer deliveryId);
}