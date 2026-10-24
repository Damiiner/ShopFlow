package com.shopflow.service;

import com.shopflow.domain.*;
import com.shopflow.dto.auth.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.PasswordResetTokenRepository;
import com.shopflow.repository.RefreshTokenRepository;
import com.shopflow.repository.UserRepository;
import com.shopflow.security.JwtService;
import com.shopflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${shopflow.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (req.getRole() == Role.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Cannot self-register as admin");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (req.getRole() == Role.SELLER && (req.getShopName() == null || req.getShopName().isBlank())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Shop name required for seller");
        }

        var user = User.builder()
                .email(req.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .role(req.getRole())
                .active(true)
                .build();
        user = userRepository.save(user);

        if (req.getRole() == Role.SELLER) {
            var profile = SellerProfile.builder()
                    .user(user)
                    .shopName(req.getShopName())
                    .description(req.getShopDescription() != null ? req.getShopDescription() : "")
                    .logoUrl(req.getLogoUrl())
                    .build();
            user.setSellerProfile(profile);
            userRepository.save(user);
        }

        var principal = new UserPrincipal(userRepository.findById(user.getId()).orElseThrow());
        return issueTokens(principal);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        var user = userRepository.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!user.isActive()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Account disabled");
        }
        return issueTokens(new UserPrincipal(user));
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        var stored = refreshTokenRepository.findByTokenAndRevokedFalse(req.getRefreshToken())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        var user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(new UserPrincipal(user));
    }

    @Transactional
    public void logout(RefreshRequest req) {
        refreshTokenRepository.findByTokenAndRevokedFalse(req.getRefreshToken())
                .ifPresent(t -> {
                    t.setRevoked(true);
                    refreshTokenRepository.save(t);
                });
    }

    private TokenResponse issueTokens(UserPrincipal principal) {
        var access = jwtService.generateAccessToken(principal);
        var refresh = jwtService.generateRefreshToken(principal);
        var claims = jwtService.parse(refresh);
        var exp = claims.getExpiration().toInstant();
        var rt = RefreshToken.builder()
                .token(refresh)
                .user(userRepository.getReferenceById(principal.getId()))
                .expiresAt(exp)
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
        return TokenResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresInMs(accessExpirationMs)
                .build();
    }

    @Transactional
    public java.util.Optional<String> forgotPassword(ForgotPasswordRequest req) {
        var user = userRepository.findByEmail(req.getEmail().toLowerCase().trim());
        if (user.isEmpty()) {
            return java.util.Optional.empty();
        }
        var token = UUID.randomUUID().toString();
        var prt = PasswordResetToken.builder()
                .token(token)
                .user(user.get())
                .expiresAt(Instant.now().plusSeconds(3600))
                .used(false)
                .build();
        passwordResetTokenRepository.save(prt);
        return java.util.Optional.of(token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        var prt = passwordResetTokenRepository.findByTokenAndUsedFalse(req.getToken())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Invalid or used token"));
        if (prt.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token expired");
        }
        var user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
    }
}
