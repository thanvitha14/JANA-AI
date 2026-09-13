package com.janaai.service.impl;

import com.janaai.dto.auth.AuthResponse;
import com.janaai.dto.auth.LoginRequest;
import com.janaai.dto.auth.RefreshTokenRequest;
import com.janaai.dto.auth.RegisterRequest;
import com.janaai.entity.RefreshToken;
import com.janaai.entity.Role;
import com.janaai.entity.User;
import com.janaai.exception.AccountLockedException;
import com.janaai.exception.DuplicateResourceException;
import com.janaai.exception.InvalidCredentialsException;
import com.janaai.exception.TokenRefreshException;
import com.janaai.repository.RefreshTokenRepository;
import com.janaai.repository.RoleRepository;
import com.janaai.repository.UserRepository;
import com.janaai.security.JwtService;
import com.janaai.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("An account with this phone number already exists");
        }

        Role role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.getRole()));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .phoneNumber(blankToNull(request.getPhoneNumber()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .city(request.getCity())
                .state(request.getState())
                .preferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en")
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .failedLoginCount(0)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} with role {}", user.getEmail(), role.getName());

        return buildAuthResponse(user, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException(
                    "Account temporarily locked due to multiple failed login attempts. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new InvalidCredentialsException("This account has been deactivated. Contact support.");
        }

        user.setFailedLoginCount(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String refreshTokenValue = createRefreshToken(user, ipAddress);
        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(user, refreshTokenValue);
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("Account locked due to repeated failed logins: {}", user.getEmail());
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found. Please log in again."));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new TokenRefreshException("Refresh token expired or revoked. Please log in again.");
        }

        User user = storedToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user, UUID.fromString(user.getId()), user.getRole().getName());
        return AuthResponse.builder()
                .id(UUID.fromString(user.getId()))
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .accessToken(newAccessToken)
                .refreshToken(storedToken.getToken())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private String createRefreshToken(User user, String ipAddress) {
        String tokenValue = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .revoked(false)
                .ipAddress(ipAddress)
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private AuthResponse buildAuthResponse(User user, String existingRefreshToken) {
        String accessToken = jwtService.generateAccessToken(user, UUID.fromString(user.getId()), user.getRole().getName());
        String refreshToken = existingRefreshToken != null ? existingRefreshToken : createRefreshToken(user, null);

        return AuthResponse.builder()
                .id(UUID.fromString(user.getId()))
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .build();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
