package com.cts.mrfp.carrygo.util;

import com.cts.mrfp.carrygo.dto.*;
import com.cts.mrfp.carrygo.model.Deliveries;
import com.cts.mrfp.carrygo.model.Notifications;
import com.cts.mrfp.carrygo.model.Ratings;
import com.cts.mrfp.carrygo.model.Transactions;
import com.cts.mrfp.carrygo.model.Users;
import com.cts.mrfp.carrygo.model.Wallets;

// Helpers that copy data between database entity classes and the DTO classes we send over HTTP.
// Doing this conversion by hand keeps the API response shape stable even if the entity changes.
public class DTOConverter {

    // ── Users ────────────────────────────────────────────────────────────────
    public static UsersDTO convertUsersToDTO(Users user) {
        if (user == null) {
            return null;
        }
        UsersDTO dto = new UsersDTO(
            user.getUserId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole(),
            user.getAuthProvider(),
            user.getThemePreference(),
            user.getLicenceNumber(),
            user.getLicenceExpiry(),
            user.getVehicleType(),
            user.getVehicleNumber(),
            user.getVehicleModel(),
            user.getIsOnline()
        );
        dto.setAvgRating(user.getAvgRating());
        return dto;
    }

    public static Users convertDTOToUsers(UsersDTO dto) {
        if (dto == null) {
            return null;
        }
        Users user = new Users(
            dto.getUserId(),
            dto.getName(),
            dto.getEmail(),
            dto.getPhone(),
            dto.getPassword(),
            dto.getRole(),
            dto.getAuthProvider(),
            dto.getThemePreference(),
            dto.getLicenceNumber(),
            dto.getLicenceExpiry(),
            dto.getVehicleType(),
            dto.getVehicleNumber(),
            dto.getVehicleModel()
        );
        user.setIsOnline(dto.getIsOnline());
        return user;
    }

    // ── Deliveries ───────────────────────────────────────────────────────────
    public static DeliveriesDTO convertDeliveriesToDTO(Deliveries delivery) {
        if (delivery == null) {
            return null;
        }
        Integer senderId   = delivery.getSender()   != null ? delivery.getSender().getUserId()   : null;
        Integer commuterId = delivery.getCommuter() != null ? delivery.getCommuter().getUserId() : null;

        DeliveriesDTO dto = new DeliveriesDTO(
            delivery.getDeliveryId(),
            senderId,
            commuterId,
            delivery.getPickupAddress(),
            delivery.getPickupLat(),
            delivery.getPickupLng(),
            delivery.getPickupContact(),
            delivery.getPickupPhone(),
            delivery.getDropAddress(),
            delivery.getDropLat(),
            delivery.getDropLng(),
            delivery.getReceiverName(),
            delivery.getReceiverPhone(),
            delivery.getPackageType(),
            delivery.getWeightKg(),
            delivery.getPackageSize(),
            delivery.getSpecialInstructions(),
            delivery.getDeliveryType(),
            delivery.getPreferredDate(),
            delivery.getPreferredTime(),
            delivery.getFlexibleMatching(),
            delivery.getDistanceKm(),
            delivery.getBasePrice(),
            delivery.getDistanceCost(),
            delivery.getServiceFee(),
            delivery.getTotalAmount(),
            delivery.getStatus(),
            delivery.getCreatedAt()
        );
        // Flatten the porter's basic info into the DTO so the user dashboard
        // doesn't have to make a second call to fetch their name / phone / vehicle.
        if (delivery.getCommuter() != null) {
            dto.setCommuterName(delivery.getCommuter().getName());
            dto.setCommuterPhone(delivery.getCommuter().getPhone());
            dto.setCommuterVehicle(delivery.getCommuter().getVehicleType());
        }

        // Surge pricing details, OTP, and the "searching for driver" counters.
        dto.setOtp(delivery.getOtp());
        dto.setSurgeMultiplier(delivery.getSurgeMultiplier());
        dto.setSurgeLabel(delivery.getSurgeLabel());
        dto.setZoneSurcharge(delivery.getZoneSurcharge());
        dto.setTimeFare(delivery.getTimeFare());
        dto.setVehicleType(delivery.getVehicleType());
        dto.setTotalPool(delivery.getTotalPool());
        dto.setTotalNotified(delivery.getTotalNotified());
        dto.setTotalRejected(delivery.getTotalRejected());

        return dto;
    }

    // ── Wallets ──────────────────────────────────────────────────────────────
    public static WalletsDTO convertWalletsToDTO(Wallets wallet) {
        if (wallet == null) {
            return null;
        }
        Integer userId = wallet.getUser() != null ? wallet.getUser().getUserId() : null;
        
        return new WalletsDTO(
            wallet.getWalletId(),
            userId,
            wallet.getBalance(),
            wallet.getLastUpdated()
        );
    }

    // ── Transactions ─────────────────────────────────────────────────────────
    public static TransactionsDTO convertTransactionsToDTO(Transactions transaction) {
        if (transaction == null) {
            return null;
        }
        Integer walletId = transaction.getWallet() != null ? transaction.getWallet().getWalletId() : null;
        Integer deliveryId = transaction.getDelivery() != null ? transaction.getDelivery().getDeliveryId() : null;
        
        return new TransactionsDTO(
            transaction.getTransactionId(),
            walletId,
            deliveryId,
            transaction.getType(),
            transaction.getAmount(),
            transaction.getStatus(),
            transaction.getCreatedAt()
        );
    }

    // ── Ratings ──────────────────────────────────────────────────────────────
    public static RatingsDTO convertRatingsToDTO(Ratings rating) {
        if (rating == null) {
            return null;
        }
        Integer deliveryId = rating.getDelivery() != null ? rating.getDelivery().getDeliveryId() : null;
        Integer senderId = rating.getSender() != null ? rating.getSender().getUserId() : null;
        Integer commuterId = rating.getCommuter() != null ? rating.getCommuter().getUserId() : null;
        
        return new RatingsDTO(
            rating.getRatingId(),
            deliveryId,
            senderId,
            commuterId,
            rating.getRating(),
            rating.getComment(),
            rating.getCreatedAt()
        );
    }

    // ── Notifications ────────────────────────────────────────────────────────
    public static NotificationsDTO convertNotificationsToDTO(Notifications notification) {
        if (notification == null) {
            return null;
        }
        Integer userId = notification.getUser() != null ? notification.getUser().getUserId() : null;
        
        return new NotificationsDTO(
            notification.getNotificationId(),
            userId,
            notification.getType(),
            notification.getMessage(),
            notification.getIsRead(),
            notification.getCreatedAt()
        );
    }
}
