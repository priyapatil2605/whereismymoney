package com.whereismymoney.backend.controller;

import com.whereismymoney.backend.dto.AuthRequest;
import com.whereismymoney.backend.dto.AuthResponse;
import com.whereismymoney.backend.dto.LoginRequest;
import com.whereismymoney.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(
                authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                authService.login(request));
    }
}