package com.shopflow.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenResponse {
    String accessToken;
    String refreshToken;
    String tokenType;
    long expiresInMs;
}
