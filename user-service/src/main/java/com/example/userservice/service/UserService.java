package com.example.userservice.service;

import com.example.userservice.config.EmailService;
import com.example.userservice.model.User;
import com.example.userservice.model.dto.AddressResponseDTO;
import com.example.userservice.model.dto.UserRegisterDTO;
import com.example.userservice.model.dto.UserResponseDTO;
import com.example.userservice.model.enumeration.Role;
import com.example.userservice.model.exceptions.UserAlreadyExistsException;
import com.example.userservice.model.exceptions.UserNotFoundException;
import com.example.userservice.model.mapper.UserMapper;
import com.example.userservice.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService; // Inject your email service
    private final AddressClientService addressClientService;

    // -----------------------------
    // NORMAL REGISTRATION WITH EMAIL CODE
    // -----------------------------
    public UserResponseDTO register(UserRegisterDTO dto) {
        // 1️⃣ Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail());
        }

        // 2️⃣ Build the user entity (without addressId for now)
        User.UserBuilder userBuilder = User.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .createdAt(LocalDateTime.now())
                .isOauth2User(false)
                .role(Role.USER)
                .emailVerified(false)
                .verificationCode(generateVerificationCode());

        // 3️⃣ If optional address is provided, create it via AddressService
        if (dto.getAddress() != null) {
            try {
                AddressResponseDTO createdAddress = addressClientService.createAddress(dto.getAddress());
                userBuilder.addressId(createdAddress.getId()); // link address
            } catch (Exception e) {
                // Log the error but don’t fail registration
                System.err.println("Failed to create address: " + e.getMessage());
            }
        }

        // 4️⃣ Save the user
        User user = userRepository.save(userBuilder.build());

        // 5️⃣ Send verification email
        emailService.sendEmail(user.getEmail(),
                "Your verification code is: " + user.getVerificationCode());

        // 6️⃣ Return DTO
        return userMapper.toDTO(user);
    }

    // -----------------------------
    // VERIFY EMAIL CODE
    // -----------------------------
    @Transactional
    public UserResponseDTO verifyEmailCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.isEmailVerified()) {
            return userMapper.toDTO(user);
        }

        if (user.getVerificationCode().equals(code)) {
            user.setEmailVerified(true);
            user.setVerificationCode(null); // clear code
            user = userRepository.save(user); // save and reload
            return userMapper.toDTO(user);
        } else {
            throw new RuntimeException("Invalid verification code");
        }
    }

    // -----------------------------
    // GET USER BY EMAIL
    // -----------------------------
    public UserResponseDTO getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toDTO(user);
    }

    // -----------------------------
    // OAUTH2 REGISTRATION
    // -----------------------------
    @Transactional
    public UserResponseDTO registerOAuthUser(String email, String fullName) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Generate a random password to satisfy NOT NULL constraint
            String randomPassword = UUID.randomUUID().toString();

            user = User.builder()
                    .email(email)
                    .fullName(fullName)
                    .password(passwordEncoder.encode(randomPassword))
                    .createdAt(LocalDateTime.now())
                    .isOauth2User(true)
                    .role(Role.USER)
                    .emailVerified(true) // OAuth2 users are auto-verified
                    .build();

            userRepository.save(user);
            System.out.println("✅ OAuth2 user saved: " + email);
        } else {
            System.out.println("🔹 OAuth2 user already exists: " + email);
        }

        return userMapper.toDTO(user);
    }

    // -----------------------------
    // HELPER: GENERATE 6-DIGIT CODE
    // -----------------------------
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public UserResponseDTO updateAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAddressId(addressId);
        return userMapper.toDTO(userRepository.save(user));
    }

    public UserResponseDTO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDTO(user);
    }
}
