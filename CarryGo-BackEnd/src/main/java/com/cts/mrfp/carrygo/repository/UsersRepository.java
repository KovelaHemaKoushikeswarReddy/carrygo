package com.cts.mrfp.carrygo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cts.mrfp.carrygo.model.Users;

// Spring Data JPA generates the implementation of these methods automatically
// based on their names. We just declare the queries we need.
public interface UsersRepository extends JpaRepository<Users, Integer> {

    // Used by login to verify credentials.
    Optional<Users> findByEmailAndPassword(String email, String password);
    Optional<Users> findByPhoneAndPassword(String phone, String password);

    Optional<Users> findByEmail(String email);

    // Returns all porters currently marked as online — used when broadcasting ride requests.
    List<Users> findByIsOnlineTrue();

    // Custom UPDATE to refresh a porter's cached average rating after a new rating is saved.
    @Modifying
    @Query(value = "UPDATE users SET avg_rating = :avgRating WHERE user_id = :userId", nativeQuery = true)
    void updateAvgRating(@Param("userId") Integer userId, @Param("avgRating") Double avgRating);
}
