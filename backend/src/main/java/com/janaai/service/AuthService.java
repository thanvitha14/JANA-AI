package com.janaai.service;

import com.janaai.dto.auth.AuthResponse;
import com.janaai.dto.auth.LoginRequest;
import com.janaai.dto.auth.RefreshTokenRequest;
import com.janaai.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, String ipAddress);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
}
