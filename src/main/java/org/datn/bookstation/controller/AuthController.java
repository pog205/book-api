package org.datn.bookstation.controller;

import org.datn.bookstation.dto.request.RegisterRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.RegisterResponse;
import org.datn.bookstation.dto.request.LoginRequest;
import org.datn.bookstation.dto.request.ForgotPasswordRequest;
import org.datn.bookstation.dto.request.ResetPasswordRequest;
import org.datn.bookstation.dto.response.LoginResponse;
import org.datn.bookstation.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.datn.bookstation.dto.response.TokenValidationResponse;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // @PostMapping("/login")

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }

    @PostMapping("/validate-token")
    public ApiResponse<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String token) {
        // Loại bỏ "Bearer " prefix nếu có
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return authService.validateToken(token);
    }

    
}
