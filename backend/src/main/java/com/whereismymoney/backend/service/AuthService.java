package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.AuthRequest;
import com.whereismymoney.backend.dto.AuthResponse;
import com.whereismymoney.backend.dto.LoginRequest;

public interface AuthService {

    AuthResponse signup(AuthRequest request);

    AuthResponse login(LoginRequest request);
}