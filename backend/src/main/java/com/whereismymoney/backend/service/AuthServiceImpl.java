package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.AuthRequest;
import com.whereismymoney.backend.dto.AuthResponse;
import com.whereismymoney.backend.dto.LoginRequest;
import com.whereismymoney.backend.entity.User;
import com.whereismymoney.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse signup(AuthRequest request) {

        logger.info(
                "Signup attempt for email={}",
                request.email());

        if (userRepository.existsByEmail(request.email())) {

            logger.warn(
                    "Signup failed: email already registered, email={}",
                    request.email());

            throw new RuntimeException(
                    "Email already registered");
        }

        User user = new User();

        user.setEmail(request.email());

        user.setFullName(
                request.fullName());

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.password()));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        logger.info(
                "Signup successful: userId={}, email={}",
                savedUser.getId(),
                savedUser.getEmail());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        logger.info(
                "Login attempt for email={}",
                request.email());

        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() -> {

                    logger.warn(
                            "Login failed: user not found, email={}",
                            request.email());

                    return new RuntimeException(
                            "Invalid credentials");
                });

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash());

        if (!passwordMatches) {

            logger.warn(
                    "Login failed: invalid password, userId={}",
                    user.getId());

            throw new RuntimeException(
                    "Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        logger.info(
                "Login successful: userId={}, email={}",
                user.getId(),
                user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail());
    }
}