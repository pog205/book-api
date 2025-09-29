package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.LoginRequest;
import org.datn.bookstation.dto.request.RegisterRequest;
import org.datn.bookstation.dto.request.ForgotPasswordRequest;
import org.datn.bookstation.dto.request.ResetPasswordRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.LoginResponse;
import org.datn.bookstation.dto.response.RegisterResponse;
import org.datn.bookstation.dto.response.TokenValidationResponse;

public interface AuthService {
    ApiResponse<RegisterResponse> register(RegisterRequest request);
    ApiResponse<LoginResponse> login(LoginRequest request);

    ApiResponse<Void> forgotPassword(ForgotPasswordRequest request);

    ApiResponse<Void> resetPassword(ResetPasswordRequest request);

    ApiResponse<Void> verifyEmail(String token);

    ApiResponse<TokenValidationResponse> validateToken(String token);
}
