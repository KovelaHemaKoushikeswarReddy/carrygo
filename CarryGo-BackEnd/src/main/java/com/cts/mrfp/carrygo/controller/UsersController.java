package com.cts.mrfp.carrygo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.mrfp.carrygo.model.Users;
import com.cts.mrfp.carrygo.dto.UsersDTO;
import com.cts.mrfp.carrygo.dto.CommuterRegistrationRequest;
import com.cts.mrfp.carrygo.service.UsersService;
import com.cts.mrfp.carrygo.util.DTOConverter;

// REST endpoints for everything related to users: signup, login, profile updates,
// going online/offline, and upgrading a regular user into a porter/commuter.
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UsersController {
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    // POST /api/users/register — create a new user account.
    @PostMapping("/register")
    public ResponseEntity<UsersDTO> register(@RequestBody UsersDTO userDTO) {
        Users user = DTOConverter.convertDTOToUsers(userDTO);
        Users saved = usersService.register(user);
        return ResponseEntity.ok(DTOConverter.convertUsersToDTO(saved));
    }

    // POST /api/users/login — verify email/password and return the user if it matches.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");
        String role = loginData.get("role");

        return usersService.login(email, password, role)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(DTOConverter.convertUsersToDTO(user)))
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }

    // GET /api/users/{id} — fetch a user by their numeric ID.
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return usersService.getUserById(id)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(DTOConverter.convertUsersToDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/users/email/{email} — fetch a user by email (used by the porter profile screen).
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return usersService.getUserByEmail(email)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(DTOConverter.convertUsersToDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /api/users/{userId}/status — porter toggles online/offline so they receive ride requests.
    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Integer userId, @RequestBody Map<String, Boolean> statusData) {
        Boolean isOnline = statusData.get("is_online");
        if (isOnline == null) {
            return ResponseEntity.badRequest().body("is_online field is required");
        }
        return usersService.updateUserStatus(userId, isOnline)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(DTOConverter.convertUsersToDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /api/users/{userId} — update profile fields (KYC info, vehicle details, etc.).
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Integer userId, @RequestBody UsersDTO profileDTO) {
        return usersService.updateUserProfile(userId, profileDTO)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(DTOConverter.convertUsersToDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/users/{userId}/register-commuter — turn an existing user into a porter
    // by adding the porter role and saving their vehicle / licence info.
    @PostMapping("/{userId}/register-commuter")
    public ResponseEntity<?> registerAsCommuter(@PathVariable Integer userId,
                                                @RequestBody CommuterRegistrationRequest req) {
        return usersService.registerAsCommuter(userId, req)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(DTOConverter.convertUsersToDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
