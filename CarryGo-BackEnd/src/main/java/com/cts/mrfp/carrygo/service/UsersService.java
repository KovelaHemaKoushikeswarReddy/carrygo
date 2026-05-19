package com.cts.mrfp.carrygo.service;

import com.cts.mrfp.carrygo.dto.UsersDTO;
import com.cts.mrfp.carrygo.dto.CommuterRegistrationRequest;
import com.cts.mrfp.carrygo.model.Users;
import com.cts.mrfp.carrygo.model.Wallets;
import com.cts.mrfp.carrygo.repository.UsersRepository;
import com.cts.mrfp.carrygo.repository.WalletsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
public class UsersService {
    private final UsersRepository usersRepository;

    @Autowired
    private WalletsRepository walletsRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    // "commuter" is just the UI label for a porter — treat them as the same role everywhere.
    private static String normalizeRole(String role) {
        if (role == null) return "user";
        String r = role.trim().toLowerCase();
        return r.equals("commuter") ? "porter" : r;
    }

    // Registration
    public Users register(Users users) {
        if (users.getPassword() == null || users.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        users.setEmail(users.getEmail().trim());
        users.setPassword(users.getPassword().trim());
        users.setRole(normalizeRole(users.getRole()));
        Users saved = usersRepository.save(users);

        // Auto-create wallet with 0 balance for every new user
        Wallets wallet = new Wallets();
        wallet.setUser(saved);
        wallet.setBalance(0f);
        wallet.setLastUpdated(LocalDateTime.now());
        walletsRepository.save(wallet);

        return saved;
    }

    // Login with email + password + role
    public Optional<Users> login(String email, String password, String role) {
        String normalizedEmail = email.trim();
        String normalizedPassword = password.trim();
        String normalizedRole = normalizeRole(role);

        Optional<Users> user = usersRepository.findByEmailAndPassword(normalizedEmail, normalizedPassword);

        // Validate role: supports comma-separated roles e.g. "user,porter".
        // Each stored token is normalized too so legacy "commuter" rows still match.
        return user.filter(u -> u.getRole() != null &&
                Arrays.stream(u.getRole().split(","))
                      .map(UsersService::normalizeRole)
                      .anyMatch(r -> r.equals(normalizedRole)));
    }

    // Retrieve user by ID
    public Optional<Users> getUserById(Integer userId) {
        return usersRepository.findById(userId);
    }

    // Retrieve user by email
    public Optional<Users> getUserByEmail(String email) {
        return usersRepository.findByEmail(email.trim());
    }

    // Update user online/offline status
    public Optional<Users> updateUserStatus(Integer userId, Boolean isOnline) {
        Optional<Users> user = usersRepository.findById(userId);
        if (user.isPresent()) {
            Users u = user.get();
            u.setIsOnline(isOnline);
            usersRepository.save(u);
        }
        return user;
    }

    // Register existing user as a commuter (adds "porter" role, stores vehicle/licence info)
    public Optional<Users> registerAsCommuter(Integer userId, CommuterRegistrationRequest req) {
        Optional<Users> existing = usersRepository.findById(userId);
        if (existing.isPresent()) {
            Users u = existing.get();
            // Add porter role if not already present
            String currentRole = u.getRole() != null ? u.getRole().trim() : "user";
            boolean alreadyPorter = Arrays.stream(currentRole.split(","))
                    .map(String::trim)
                    .anyMatch(r -> r.equalsIgnoreCase("porter"));
            if (!alreadyPorter) {
                u.setRole(currentRole + ",porter");
            }
            if (req.getVehicleType()   != null) u.setVehicleType(req.getVehicleType());
            if (req.getVehicleNumber() != null) u.setVehicleNumber(req.getVehicleNumber());
            if (req.getVehicleModel()  != null) u.setVehicleModel(req.getVehicleModel());
            if (req.getLicenceNumber() != null) u.setLicenceNumber(req.getLicenceNumber());
            if (req.getLicenceExpiry() != null) {
                u.setLicenceExpiry(LocalDate.parse(req.getLicenceExpiry())); // "YYYY-MM-DD"
            }
            usersRepository.save(u);
            return Optional.of(u);
        }
        return Optional.empty();
    }

    // Update user KYC / profile fields (patch-style, only non-null fields)
    public Optional<Users> updateUserProfile(Integer userId, UsersDTO dto) {
        Optional<Users> existing = usersRepository.findById(userId);
        if (existing.isPresent()) {
            Users u = existing.get();
            if (dto.getName()               != null) u.setName(dto.getName());
            if (dto.getPhone()              != null) u.setPhone(dto.getPhone());
            if (dto.getVehicleType()        != null) u.setVehicleType(dto.getVehicleType());
            if (dto.getVehicleNumber()      != null) u.setVehicleNumber(dto.getVehicleNumber());
            if (dto.getVehicleModel()       != null) u.setVehicleModel(dto.getVehicleModel());
            if (dto.getLicenceNumber()      != null) u.setLicenceNumber(dto.getLicenceNumber());
            if (dto.getLicenceExpiry()      != null) u.setLicenceExpiry(dto.getLicenceExpiry());
            // KYC personal
            if (dto.getGender()             != null) u.setGender(dto.getGender());
            if (dto.getDateOfBirth()        != null) u.setDateOfBirth(dto.getDateOfBirth());
            if (dto.getIdType()             != null) u.setIdType(dto.getIdType());
            if (dto.getIdNumber()           != null) u.setIdNumber(dto.getIdNumber());
            // KYC address
            if (dto.getHouseNo()            != null) u.setHouseNo(dto.getHouseNo());
            if (dto.getStreet()             != null) u.setStreet(dto.getStreet());
            if (dto.getCity()               != null) u.setCity(dto.getCity());
            if (dto.getState()              != null) u.setState(dto.getState());
            if (dto.getPinCode()            != null) u.setPinCode(dto.getPinCode());
            // KYC bank
            if (dto.getBankAccountHolder()  != null) u.setBankAccountHolder(dto.getBankAccountHolder());
            if (dto.getBankAccountNumber()  != null) u.setBankAccountNumber(dto.getBankAccountNumber());
            if (dto.getBankIfscCode()       != null) u.setBankIfscCode(dto.getBankIfscCode());
            if (dto.getBankName()           != null) u.setBankName(dto.getBankName());
            // KYC status & images
            if (dto.getKycStatus()          != null) u.setKycStatus(dto.getKycStatus());
            if (dto.getIdFrontImage()       != null) u.setIdFrontImage(dto.getIdFrontImage());
            if (dto.getIdBackImage()        != null) u.setIdBackImage(dto.getIdBackImage());
            usersRepository.save(u);
            return Optional.of(u);
        }
        return Optional.empty();
    }
}
