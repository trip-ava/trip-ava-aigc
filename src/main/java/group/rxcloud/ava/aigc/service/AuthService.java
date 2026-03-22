package group.rxcloud.ava.aigc.service;

import group.rxcloud.ava.aigc.dto.auth.*;
import group.rxcloud.ava.aigc.entity.User;
import group.rxcloud.ava.aigc.repository.UserRepository;
import group.rxcloud.ava.aigc.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication service for user registration, login, and token management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Username already taken")
                    .build();
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Email already registered")
                    .build();
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername())
                .role(User.UserRole.USER)
                .active(true)
                .build();

        userRepository.save(user);

        log.info("User registered successfully: {}", request.getUsername());

        // Generate tokens
        String accessToken = tokenProvider.generateToken(user.getUsername());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .success(true)
                .message("User registered successfully")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime(accessToken))
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Authenticate user and generate tokens
     */
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            // Update last login time
            userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
            });

            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            log.info("User logged in successfully: {}", request.getUsername());

            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTime(accessToken))
                    .user(mapToUserInfo(user))
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid username or password")
                    .build();
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid refresh token")
                    .build();
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String newAccessToken = tokenProvider.generateToken(username);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        return AuthResponse.builder()
                .success(true)
                .message("Token refreshed successfully")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime(newAccessToken))
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Get current user info
     */
    public AuthResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        return AuthResponse.builder()
                .success(true)
                .message("User found")
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Map User entity to UserInfo DTO
     */
    private AuthResponse.UserInfo mapToUserInfo(User user) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();
    }
}
