package com.cts.mrfp.carrygo.controller;

import com.cts.mrfp.carrygo.model.Deliveries;
import com.cts.mrfp.carrygo.model.Users;
import com.cts.mrfp.carrygo.dto.DeliveriesDTO;
import com.cts.mrfp.carrygo.repository.UsersRepository;
import com.cts.mrfp.carrygo.service.DeliveriesService;
import com.cts.mrfp.carrygo.util.DTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// REST endpoints for the delivery flow:
// creating a request, listing them, accepting, marking arrived, verifying OTP,
// updating status, and rejecting.
@RestController
@RequestMapping("api/deliveries")
@CrossOrigin(origins = "*")
public class DeliveriesController {

    @Autowired private DeliveriesService deliveryService;
    @Autowired private UsersRepository usersRepository;

    // POST /api/deliveries — user creates a new delivery request.
    // Copies fields from the DTO onto an entity, links the sender, then saves it.
    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody DeliveriesDTO deliveryDTO) {
        Deliveries delivery = new Deliveries();

        // Look up the sender user by ID and attach them to the delivery.
        if (deliveryDTO.getSenderId() != null) {
            Users sender = usersRepository.findById(deliveryDTO.getSenderId()).orElse(null);
            if (sender == null) return ResponseEntity.badRequest().body("Sender not found");
            delivery.setSender(sender);
        }

        delivery.setPickupAddress(deliveryDTO.getPickupAddress());
        delivery.setPickupLat(deliveryDTO.getPickupLat());
        delivery.setPickupLng(deliveryDTO.getPickupLng());
        delivery.setPickupContact(deliveryDTO.getPickupContact());
        delivery.setPickupPhone(deliveryDTO.getPickupPhone());
        delivery.setDropAddress(deliveryDTO.getDropAddress());
        delivery.setDropLat(deliveryDTO.getDropLat());
        delivery.setDropLng(deliveryDTO.getDropLng());
        delivery.setReceiverName(deliveryDTO.getReceiverName());
        delivery.setReceiverPhone(deliveryDTO.getReceiverPhone());
        delivery.setPackageType(deliveryDTO.getPackageType());
        delivery.setWeightKg(deliveryDTO.getWeightKg());
        delivery.setPackageSize(deliveryDTO.getPackageSize());
        delivery.setSpecialInstructions(deliveryDTO.getSpecialInstructions());
        delivery.setDeliveryType(deliveryDTO.getDeliveryType());
        delivery.setPreferredDate(deliveryDTO.getPreferredDate());
        delivery.setPreferredTime(deliveryDTO.getPreferredTime());
        delivery.setFlexibleMatching(deliveryDTO.getFlexibleMatching());
        delivery.setDistanceKm(deliveryDTO.getDistanceKm());
        delivery.setBasePrice(deliveryDTO.getBasePrice());
        delivery.setDistanceCost(deliveryDTO.getDistanceCost());
        delivery.setServiceFee(deliveryDTO.getServiceFee());
        delivery.setTotalAmount(deliveryDTO.getTotalAmount());

        Deliveries saved = deliveryService.saveDelivery(delivery);
        return ResponseEntity.ok(DTOConverter.convertDeliveriesToDTO(saved));
    }

    // GET /api/deliveries/user/{userId} — all deliveries a user has sent.
    @GetMapping("/user/{userId}")
    public List<DeliveriesDTO> getUserDeliveries(@PathVariable Integer userId) {
        return deliveryService.getDeliveriesByUser(userId).stream()
                .map(DTOConverter::convertDeliveriesToDTO)
                .collect(Collectors.toList());
    }

    // GET /api/deliveries/available — all PENDING deliveries that porters can browse.
    @GetMapping("/available")
    public List<DeliveriesDTO> getAvailable() {
        return deliveryService.getAllAvailableDeliveries().stream()
                .map(DTOConverter::convertDeliveriesToDTO)
                .collect(Collectors.toList());
    }

    // GET /api/deliveries/commuter/{commuterId} — deliveries assigned to one porter.
    @GetMapping("/commuter/{commuterId}")
    public List<DeliveriesDTO> getCommuterDeliveries(@PathVariable Integer commuterId) {
        return deliveryService.getDeliveriesByCommuter(commuterId).stream()
                .map(DTOConverter::convertDeliveriesToDTO)
                .collect(Collectors.toList());
    }

    // PATCH /api/deliveries/{id}/status?status=... — change status (e.g. PICKED_UP, DELIVERED).
    @PatchMapping("/{id}/status")
    public DeliveriesDTO updateStatus(@PathVariable Integer id, @RequestParam String status) {
        Deliveries delivery = deliveryService.updateStatus(id, status);
        return DTOConverter.convertDeliveriesToDTO(delivery);
    }

    // PATCH /api/deliveries/{id}/accept — porter accepts a pending request.
    // Returns 409 Conflict if another porter took it first.
    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> acceptDelivery(@PathVariable Integer id, @RequestParam Integer commuterId) {
        try {
            Deliveries delivery = deliveryService.acceptDelivery(id, commuterId);
            return ResponseEntity.ok(DTOConverter.convertDeliveriesToDTO(delivery));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // GET /api/deliveries/matching-porters-count — used by the "Find driver" screen
    // to show how many porters are currently online and available.
    @GetMapping("/matching-porters-count")
    public ResponseEntity<Integer> getMatchingPortersCount(
            @RequestParam(required = false) Float pickupLat,
            @RequestParam(required = false) Float pickupLng,
            @RequestParam(required = false) Float dropLat,
            @RequestParam(required = false) Float dropLng) {

        long count = usersRepository.findByIsOnlineTrue().stream()
            .filter(u -> u.getRole() != null && u.getRole().contains("porter"))
            .count();
        return ResponseEntity.ok((int) count);
    }

    // GET /api/deliveries/matched/{porterId} — list of pending deliveries shown
    // to a specific porter on their dashboard.
    @GetMapping("/matched/{porterId}")
    public List<DeliveriesDTO> getMatchedDeliveries(@PathVariable Integer porterId) {
        return deliveryService.getAllAvailableDeliveries().stream()
            .filter(d -> d.getPickupAddress() != null && !d.getPickupAddress().isBlank()
                      && d.getDropAddress()   != null && !d.getDropAddress().isBlank())
            .map(DTOConverter::convertDeliveriesToDTO)
            .collect(Collectors.toList());
    }

    // GET /api/deliveries/{id} — fetch one delivery by its ID.
    @GetMapping("/{id}")
    public DeliveriesDTO getDeliveryById(@PathVariable Integer id) {
        Deliveries delivery = deliveryService.getDeliveryById(id);
        return DTOConverter.convertDeliveriesToDTO(delivery);
    }

    // GET /api/deliveries/user/{userId}/deliveries — same as /user/{userId}, kept for the frontend.
    @GetMapping("/user/{userId}/deliveries")
    public List<DeliveriesDTO> getPersonalizedUserDeliveries(@PathVariable Integer userId) {
        return deliveryService.getDeliveriesByUser(userId).stream()
                .map(DTOConverter::convertDeliveriesToDTO)
                .collect(Collectors.toList());
    }

    // PATCH /api/deliveries/{id}/arrived — porter says they've reached the pickup point.
    // Triggers a notification to the sender along with the OTP to share.
    @PatchMapping("/{id}/arrived")
    public ResponseEntity<?> markArrived(@PathVariable Integer id, @RequestParam Integer commuterId) {
        try {
            Deliveries d = deliveryService.markArrived(id, commuterId);
            return ResponseEntity.ok(DTOConverter.convertDeliveriesToDTO(d));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/deliveries/{id}/verify-otp — porter enters the OTP from the sender.
    // If it matches, the ride starts and status moves to PICKED_UP.
    @PostMapping("/{id}/verify-otp")
    public ResponseEntity<?> verifyOtp(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String entered = body.getOrDefault("enteredOtp", "");
        try {
            Deliveries d = deliveryService.verifyOtp(id, entered);
            return ResponseEntity.ok(Map.of(
                "success",     true,
                "rideStarted", true,
                "delivery",    DTOConverter.convertDeliveriesToDTO(d)
            ));
        } catch (RuntimeException e) {
            String msg = "OTP_MISMATCH".equals(e.getMessage())
                ? "Incorrect OTP — ask the rider to check their app"
                : e.getMessage();
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", msg));
        }
    }

    // PATCH /api/deliveries/{id}/reject — porter rejects a request,
    // or the frontend timer expires before they accept.
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectDelivery(@PathVariable Integer id, @RequestParam Integer commuterId) {
        try {
            deliveryService.rejectDelivery(id, commuterId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false));
        }
    }
}
