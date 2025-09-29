package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.RegisterRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.RegisterResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.enums.RoleName;
import org.datn.bookstation.mapper.AuthMapper;
import org.datn.bookstation.repository.RoleRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import java.util.Optional;
import org.datn.bookstation.dto.request.LoginRequest;
import org.datn.bookstation.dto.request.ForgotPasswordRequest;
import org.datn.bookstation.dto.response.LoginResponse;
import org.datn.bookstation.configuration.JwtUtil;
import org.datn.bookstation.util.EmailUtil;
import org.datn.bookstation.dto.request.ResetPasswordRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.datn.bookstation.dto.response.TokenValidationResponse;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;
    private final EmailUtil emailUtil;


    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {
        // Check email đã tồn tại chưa
        Optional<User> existUserOpt = userRepository.findByEmail(request.getEmail());
        if (existUserOpt.isPresent()) {
            User user = existUserOpt.get();
            if(user.getEmailVerified() == 0) {
                return new ApiResponse<>(400, "Email đã được đăng ký nhưng chưa được xác nhận. Vui lòng kiểm tra email để xác nhận tài khoản.", null);
            }
            return new ApiResponse<>(400, "Tài khoản với email này đã tồn tại trên hệ thống.", null);
        }

        // Tạo user mới
        User user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByRoleName(RoleName.CUSTOMER).orElse(null));
        user.setStatus((byte) 1);
        user.setEmailVerified((byte) 0); // Email chưa được xác nhận
        userRepository.save(user);

        // Gửi email xác nhận
        try {
            String verificationToken = jwtUtil.generateVerificationToken(user);
            
            // Lấy origin từ header request
            HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String origin = servletRequest.getHeader("Origin");
            if (origin == null || origin.isEmpty()) {
                origin = servletRequest.getHeader("Referer");
            }
            if (origin == null || origin.isEmpty()) {
                origin = "http://localhost:5173"; // fallback
            }
            if (origin.endsWith("/")) {
                origin = origin.substring(0, origin.length() - 1);
            }

            String verificationLink = origin + "/verify-email?token=" + verificationToken;

            String html = "<p>Xin chào " + user.getFullName() + ",</p>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản tại BookStation. Vui lòng nhấn vào link bên dưới để xác nhận email của bạn:</p>"
                    + "<p><a href='" + verificationLink + "'>Xác nhận email</a></p>"
                    + "<br/><p>Nếu bạn không thực hiện đăng ký, hãy bỏ qua email này.</p>";
            
            emailUtil.sendHtmlEmail(user.getEmail(), "Xác nhận email - BookStation", html);
        } catch (Exception e) {
            // Nếu gửi email thất bại, vẫn tạo user nhưng thông báo
            return new ApiResponse<>(201, "Đăng ký thành công! Vui lòng kiểm tra email để xác nhận tài khoản.", authMapper.toRegisterResponse(user));
        }

        return new ApiResponse<>(201, "Đăng ký thành công! Vui lòng kiểm tra email để xác nhận tài khoản.", authMapper.toRegisterResponse(user));
    }

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(401, "Sai email hoặc mật khẩu!", null);
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ApiResponse<>(401, "Sai email hoặc mật khẩu!", null);
        }
        
        // Kiểm tra tài khoản có bị vô hiệu hóa không
        if (user.getStatus() != null && user.getStatus() == 0) {
            return new ApiResponse<>(403, "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ admin để được hỗ trợ.", null);
        }
        
        // Kiểm tra email đã được xác nhận chưa
        // Chỉ kiểm tra với user mới (có trường emailVerified)
        if (user.getEmailVerified() != null && user.getEmailVerified() != 1) {
            return new ApiResponse<>(403, "Vui lòng xác nhận email trước khi đăng nhập!", null);
        }
        
        String token = jwtUtil.generateToken(user);
        LoginResponse res = new LoginResponse(token, authMapper.toRegisterResponse(user));
        return new ApiResponse<>(200, "Đăng nhập thành công!", res);
    }

    @Override
    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest request) {
        // Kiểm tra email tồn tại
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Email không tồn tại", null);
        }

        User user = userOpt.get();
        // Sinh reset token
        String resetToken = jwtUtil.generateResetToken(user);

        // Lấy origin (URL FE) từ header request (Origin hoặc Referer)
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String origin = servletRequest.getHeader("Origin");
        if (origin == null || origin.isEmpty()) {
            origin = servletRequest.getHeader("Referer");
        }
        if (origin == null || origin.isEmpty()) {
            origin = "http://localhost:5173"; // fallback
        }
        // loại bỏ dấu "/" cuối nếu có
        if (origin.endsWith("/")) {
            origin = origin.substring(0, origin.length() - 1);
        }

        String resetLink = origin + "/reset-password?token=" + resetToken;

        String html = "<p>Xin chào " + user.getFullName() + ",</p>"
                + "<p>Bạn vừa yêu cầu khôi phục mật khẩu. Nhấn vào link bên dưới để đặt lại mật khẩu (hiệu lực 15 phút):</p>"
                + "<p><a href='" + resetLink + "'>Khôi phục mật khẩu</a></p>"
                + "<br/><p>Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>";
        try {
            emailUtil.sendHtmlEmail(user.getEmail(), "Khôi phục mật khẩu", html);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi gửi email: " + e.getMessage(), null);
        }

        return new ApiResponse<>(200, "Đã gửi link khôi phục mật khẩu tới email của bạn", null);
    }

    @Override
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        // Validate token & type
        if (!jwtUtil.validateToken(request.getToken()) || !jwtUtil.isResetToken(request.getToken())) {
            return new ApiResponse<>(400, "Token không hợp lệ hoặc đã hết hạn, vui lòng thử lại!", null);
        }

        Integer userId = jwtUtil.extractUserId(request.getToken());
        if (userId == null) {
            return new ApiResponse<>(400, "Token không chứa thông tin user", null);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Người dùng không tồn tại", null);
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new ApiResponse<>(200, "Khôi phục mật khẩu thành công", null);
    }

    @Override
    public ApiResponse<Void> verifyEmail(String token) {
        // Validate token & type
        if (!jwtUtil.validateToken(token) || !jwtUtil.isVerificationToken(token)) {
            return new ApiResponse<>(400, "Token không hợp lệ hoặc đã hết hạn, vui lòng thử lại!", null);
        }

        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return new ApiResponse<>(400, "Token không chứa thông tin user", null);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Người dùng không tồn tại", null);
        }

        User user = userOpt.get();
        if (user.getEmailVerified() == 1) {
            return new ApiResponse<>(400, "Email đã được xác nhận trước đó", null);
        }

        user.setEmailVerified((byte) 1);
        userRepository.save(user);

        return new ApiResponse<>(200, "Xác nhận email thành công! Bạn có thể đăng nhập ngay bây giờ.", null);
    }

    @Override
    public ApiResponse<TokenValidationResponse> validateToken(String token) {
        // 1. Validate JWT token format
        if (!jwtUtil.validateToken(token)) {
            return new ApiResponse<>(401, "Token không hợp lệ", null);
        }

        // 2. Extract userId từ token
        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return new ApiResponse<>(401, "Token không chứa thông tin user", null);
        }

        // 3. Query database để lấy thông tin user HIỆN TẠI
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Người dùng không tồn tại", null);
        }

        User currentUser = userOpt.get();

        // 4. Kiểm tra status
        if (currentUser.getStatus() != null && currentUser.getStatus() == 0) {
            return new ApiResponse<>(403, "Tài khoản đã bị vô hiệu hóa", null);
        }

        // 5. Kiểm tra email verification (chỉ với user mới)
        if (currentUser.getEmailVerified() != null && currentUser.getEmailVerified() != 1) {
            return new ApiResponse<>(403, "Email chưa được xác nhận", null);
        }

        // 6. So sánh với JWT để phát hiện thay đổi
        try {
            // Sử dụng các method public có sẵn
            String jwtRole = jwtUtil.extractRole(token);
            Byte jwtStatus = jwtUtil.extractStatus(token);
            Byte jwtEmailVerified = jwtUtil.extractEmailVerified(token);
            
            // So sánh role
            if (!currentUser.getRole().getRoleName().name().equals(jwtRole)) {
                return new ApiResponse<>(401, "Vai trò đã thay đổi từ " + jwtRole + " thành " + currentUser.getRole().getRoleName().name() + ", vui lòng đăng nhập lại", null);
            }
            
            // So sánh status
            if (jwtStatus != null && !jwtStatus.equals(currentUser.getStatus())) {
                if (currentUser.getStatus() == 0) {
                    return new ApiResponse<>(401, "Tài khoản đã bị vô hiệu hóa, vui lòng đăng nhập lại", null);
                } else {
                    return new ApiResponse<>(401, "Trạng thái tài khoản đã thay đổi, vui lòng đăng nhập lại", null);
                }
            }
            
            // So sánh emailVerified
            if (jwtEmailVerified != null && !jwtEmailVerified.equals(currentUser.getEmailVerified())) {
                if (currentUser.getEmailVerified() == 1) {
                    return new ApiResponse<>(401, "Email đã được xác nhận, vui lòng đăng nhập lại", null);
                } else {
                    return new ApiResponse<>(401, "Trạng thái xác nhận email đã thay đổi, vui lòng đăng nhập lại", null);
                }
            }
            
        } catch (Exception e) {
            // Nếu không parse được JWT, coi như có thay đổi
            return new ApiResponse<>(401, "Token không hợp lệ, vui lòng đăng nhập lại", null);
        }

        // 7. Tạo response với thông tin mới nhất từ database (không có thay đổi)
        TokenValidationResponse response = new TokenValidationResponse();
        response.setValid(true);
        response.setUserId(currentUser.getId());
        response.setEmail(currentUser.getEmail());
        response.setFullName(currentUser.getFullName());
        response.setRole(currentUser.getRole().getRoleName().name());
        response.setStatus(currentUser.getStatus());
        response.setEmailVerified(currentUser.getEmailVerified());
        response.setPhoneNumber(currentUser.getPhoneNumber());

        return new ApiResponse<>(200, "Token hợp lệ", response);
    }
}
