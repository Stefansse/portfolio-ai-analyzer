package com.example.userservice.web;

import com.example.userservice.jwt.JwtUtils;
import com.example.userservice.model.dto.*;
import com.example.userservice.model.exceptions.UserNotFoundException;
import com.example.userservice.service.AddressClientService;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final AddressClientService addressClientService;

    // -----------------------------
    // REGISTER
    // -----------------------------
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO dto) {
        return ResponseEntity.ok(userService.register(dto));
    }

    // -----------------------------
    // LOGIN WITH EMAIL VERIFICATION CHECK
    // -----------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO dto) {
        try {
            // 1️⃣ Fetch user info first
            UserResponseDTO user = userService.getByEmail(dto.getEmail());

            // 2️⃣ Check if email is verified
            if (!user.isEmailVerified()) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Please verify your email before logging in."));
            }

            // 3️⃣ Authenticate password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );

            // 4️⃣ Generate JWT token
            String token = jwtUtils.generateToken(dto.getEmail());

            // 5️⃣ Return token + user info
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", user.getId(),
                    "email", user.getEmail()
            ));

        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
    }

    // -----------------------------
    // VERIFY EMAIL CODE
    // -----------------------------
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeDTO dto) {
        try {
            UserResponseDTO verifiedUser = userService.verifyEmailCode(dto.getEmail(), dto.getCode());
            return ResponseEntity.ok(verifiedUser);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(400).body(ex.getMessage());
        }
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String email = jwtUtils.getEmailFromJwt(token); // extract email from JWT
            UserResponseDTO user = userService.getByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }

    @PostMapping("/{id}/address")
    public ResponseEntity<UserResponseDTO> addAddress(@PathVariable Long id,
                                                      @RequestBody AddressRequestDTO dto) {

        // 1️⃣ Create address in AddressService
        AddressResponseDTO address = addressClientService.createAddress(dto);

        // 2️⃣ Update user with addressId
        UserResponseDTO user = userService.updateAddress(id, address.getId());

        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/with-address")
    public ResponseEntity<Map<String, Object>> getUserWithAddress(@PathVariable Long id) {
        UserResponseDTO user = userService.getById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);

        if (user.getAddressId() != null) {
            AddressResponseDTO address = addressClientService.getAddress(user.getAddressId());
            response.put("address", address);
        }

        return ResponseEntity.ok(response);
    }
}
