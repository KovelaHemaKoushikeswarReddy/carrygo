package com.cts.mrfp.carrygo.service;

import com.cts.mrfp.carrygo.dto.RatingsDTO;
import com.cts.mrfp.carrygo.model.Deliveries;
import com.cts.mrfp.carrygo.model.Ratings;
import com.cts.mrfp.carrygo.model.Users;
import com.cts.mrfp.carrygo.repository.RatingsRepository;
import com.cts.mrfp.carrygo.repository.UsersRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// Handles porter ratings — saving a new rating and keeping the porter's average up to date.
@Service
public class RatingsService {

    @Autowired
    private RatingsRepository ratingsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @PersistenceContext
    private EntityManager em;

    // Saves a rating row tied to a delivery, the sender, and the porter.
    // We use em.getReference() instead of findById() so JPA doesn't run an
    // extra SELECT just to confirm the foreign-key rows exist.
    @Transactional
    public Ratings addRating(RatingsDTO dto) {
        Deliveries delivery = em.getReference(Deliveries.class, dto.getDeliveryId());
        Users sender        = em.getReference(Users.class,      dto.getSenderId());
        Users commuter      = em.getReference(Users.class,      dto.getCommuterId());

        Ratings rating = new Ratings();
        rating.setDelivery(delivery);
        rating.setSender(sender);
        rating.setCommuter(commuter);
        rating.setRating(dto.getRating());
        rating.setComment(dto.getComment());
        rating.setCreatedAt(LocalDateTime.now());

        return ratingsRepository.save(rating);
    }

    // Recalculates a porter's running average and writes it back on the users table
    // so we don't have to recompute on every read.
    @Transactional
    public void updateCommuterAvgRating(Integer commuterId) {
        Double avg = ratingsRepository.findAvgRatingByCommuterId(commuterId);
        if (avg == null) return;
        double rounded = Math.round(avg * 10.0) / 10.0;
        usersRepository.updateAvgRating(commuterId, rounded);
    }

    // Returns the porter's current average, rounded to 1 decimal.
    public Double getAvgRating(Integer commuterId) {
        Double avg = ratingsRepository.findAvgRatingByCommuterId(commuterId);
        if (avg == null) return null;
        return Math.round(avg * 10.0) / 10.0;
    }

    public List<RatingsDTO> getRatingsByCommuter(Integer commuterId) {
        return ratingsRepository.findDTOsByCommuterId(commuterId);
    }

    // Used by the frontend to decide whether to show the "Rate" button.
    public boolean isDeliveryRated(Integer deliveryId) {
        return ratingsRepository.existsByDelivery_DeliveryId(deliveryId);
    }
}
