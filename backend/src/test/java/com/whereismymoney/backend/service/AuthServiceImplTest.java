package com.whereismymoney.backend.service;

import com.whereismymoney.backend.dto.AuthRequest;
import com.whereismymoney.backend.dto.AuthResponse;
import com.whereismymoney.backend.dto.LoginRequest;
import com.whereismymoney.backend.entity.User;
import com.whereismymoney.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setup() {

        user = new User();

        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPasswordHash("encoded-password");
    }

    @Test
    void signupShouldCreateUser() {

        AuthRequest request = new AuthRequest(
                "test@example.com",
                "Password@123",
                "Test User");

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User savedUser = invocation.getArgument(0);

                    return savedUser;
                });

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("jwt-token");

        AuthResponse response = authService.signup(request);

        assertEquals(
                "test@example.com",
                response.email());

        assertEquals(
                "jwt-token",
                response.token());

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void signupShouldRejectDuplicateEmail() {

        AuthRequest request = new AuthRequest(
                "test@example.com",
                "Password@123",
                "Test User");

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> authService.signup(request));

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {

        LoginRequest request = new LoginRequest(
                "test@example.com",
                "Password@123");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())).thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals(
                "jwt-token",
                response.token());

        assertEquals(
                "test@example.com",
                response.email());
    }

    @Test
    void loginShouldRejectWrongPassword() {

        LoginRequest request = new LoginRequest(
                "test@example.com",
                "WrongPassword");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> authService.login(request));

        verify(jwtService, never())
                .generateToken(any(User.class));
    }

    @Test
    void loginShouldRejectUnknownUser() {

        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "Password@123");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> authService.login(request));

        verify(jwtService, never())
                .generateToken(any(User.class));
    }
}