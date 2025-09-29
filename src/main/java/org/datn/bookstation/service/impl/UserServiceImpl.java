package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.UserRequest;
import org.datn.bookstation.dto.request.UserRetail;
import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserResponse;
import org.datn.bookstation.dto.response.TopSpenderResponse;
import org.datn.bookstation.entity.Role;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.mapper.UserMapper;
import org.datn.bookstation.repository.RoleRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.UserService;
import org.datn.bookstation.specification.UserRankSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public PaginationResponse<UserResponse> getAllWithPagination(int page, int size, String fullName, String email,
            String phoneNumber, Integer roleId, String status,Integer userId) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Tạo specification để filter
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        
        // Filter theo tên
        if (fullName != null && !fullName.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%"));
        }
        
        // Filter theo email
        if (email != null && !email.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        
        // Filter theo số điện thoại
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(root.get("phoneNumber"), "%" + phoneNumber + "%"));
        }
        
        // Filter theo role
        if (roleId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("role").get("id"), roleId));
        }
        
        // Filter theo status
        if (status != null && !status.trim().isEmpty()) {
            Byte statusByte = "1".equals(status) ? (byte) 1 : (byte) 0;
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("status"), statusByte));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.notEqual(root.get("id"), userId)  // ≠ userId
            );
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<UserResponse> content = userPage.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        System.out.println(content+"content");
        return PaginationResponse.<UserResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    @Override
    public Optional<UserResponse> getUserResponseById(Integer id) {
        return userRepository.findById(id).map(this::toResponse);
    }

    @Override
    public ApiResponse<UserResponse> add(UserRequest req) {
        // Validate các trường bắt buộc
        if (req.getFull_name() == null || req.getFull_name().trim().isEmpty()) {
            return new ApiResponse<>(400, "Họ tên không được để trống", null);
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            return new ApiResponse<>(400, "Email không được để trống", null);
        }
        if (req.getRole_id() == null) {
            return new ApiResponse<>(400, "Vai trò không được để trống", null);
        }
        
        // Validate email trùng
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return new ApiResponse<>(400, "Email đã tồn tại", null);
        }
        
        // Validate role tồn tại
        if (roleRepository.findById(req.getRole_id()).isEmpty()) {
            return new ApiResponse<>(400, "Vai trò không tồn tại", null);
        }
        
        User user = new User();
        user.setFullName(req.getFull_name().trim());
        user.setEmail(req.getEmail().trim());
        user.setPhoneNumber(req.getPhone_number() != null ? req.getPhone_number().trim() : null);
        user.setStatus(parseStatus(req.getStatus()));
        // Set password mặc định cho user được tạo bởi admin
        user.setPassword(passwordEncoder.encode("123456")); // Password mặc định
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        user.setTotalSpent(req.getTotal_spent() != null ? req.getTotal_spent() : BigDecimal.ZERO);
        user.setTotalPoint(req.getTotal_point() != null ? req.getTotal_point() : 0);
        // Set role
        user.setRole(roleRepository.findById(req.getRole_id()).get());
        
        User saved = userRepository.save(user);
        return new ApiResponse<>(201, "Tạo mới thành công", toResponse(saved));
    }

    @Override
    public ApiResponse<UserResponse> update(UserRequest req, Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Không tìm thấy", null);
        }
        User user = userOpt.get();
        // Nếu đổi email, check trùng
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())
                && userRepository.findByEmail(req.getEmail()).isPresent()) {
            return new ApiResponse<>(400, "Email đã tồn tại", null);
        }
        user.setFullName(req.getFull_name());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhone_number());
        user.setStatus(Byte.parseByte(req.getStatus()));
        user.setUpdatedAt(System.currentTimeMillis());
        user.setTotalSpent(req.getTotal_spent() != null ? req.getTotal_spent() : user.getTotalSpent());
        user.setTotalPoint(req.getTotal_point() != null ? req.getTotal_point() : user.getTotalPoint());
        // Set role nếu có
        if (req.getRole_id() != null) {
            user.setRole(roleRepository.findById(req.getRole_id()).orElse(user.getRole()));
        }
        User saved = userRepository.save(user);
        return new ApiResponse<>(200, "Cập nhật thành công", toResponse(saved));
    }

    @Override
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public ApiResponse<UserResponse> toggleStatus(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Không tìm thấy", null);
        }
        User user = userOpt.get();
        user.setStatus(user.getStatus() != null && user.getStatus() == 1 ? (byte) 0 : (byte) 1);
        user.setUpdatedAt(System.currentTimeMillis());
        User saved = userRepository.save(user);
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công", toResponse(saved));
    }

    @Override
    public ApiResponse<User> getUserByEmail(String email) {
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công", userRepository.findByEmail(email).get());

    }

    @Override
    public ApiResponse<User> updateClient(User user, Integer id) {
        // Kiểm tra id có tồn tại không
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(404, "Không tìm thấy người dùng với id = " + id, null);
        }

        // Lấy user từ DB
        User userById = optionalUser.get();

        // Validate dữ liệu đầu vào
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return new ApiResponse<>(400, "Họ tên không được để trống", null);
        }

        if (user.getPhoneNumber() == null || !user.getPhoneNumber().matches("^(0\\d{9})$")) {
            return new ApiResponse<>(400, "Số điện thoại không hợp lệ (phải có 10 số và bắt đầu bằng 0)", null);
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return new ApiResponse<>(400, "Email không được để trống", null);
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return new ApiResponse<>(400, "Email không đúng định dạng", null);
        }
        // Cập nhật thông tin
        userById.setUpdatedAt(System.currentTimeMillis());
        userById.setFullName(user.getFullName().trim());
        userById.setEmail(user.getEmail().trim());
        userById.setPhoneNumber(user.getPhoneNumber().trim());


        User userUpdate = userRepository.save(userById);

        return new ApiResponse<>(200, "Cập nhật thông tin thành công", userUpdate);
    }


    @Override
    public ApiResponse<List<UserRoleRequest>> getUserPOS(String text) {
        Specification<User> userSpecification = UserRankSpecification.filterBy(text);
        List<User> users = userRepository.findAll(userSpecification);

        return new ApiResponse<>(200, "Lấy danh sách user thành công", userMapper.userMapper(users));
    }

    @Override
    public ApiResponse<User> addRetail(UserRetail req) {
        System.out.println(req);
        User user = new User();
        try {
            if (userRepository.getByPhoneNumber(req.getPhoneNumber()) == null) {
                Role role = roleRepository.findById(3).get();
                user.setIsRetail((byte)1);
                user.setFullName(req.getFullName());
                user.setPhoneNumber(req.getPhoneNumber());
                user.setRole(role);
                System.out.println(user);
                User userSave = userRepository.save(user);
                return new ApiResponse<>(200, "Thêm khách vãng lai thành công", userSave);
            } else {
                return new ApiResponse<>(400, "Số điện thoại đã tồn tại ", null);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ApiResponse<>(400,"thêm that bai",null);
        }
    }

    @Override
    public List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getStatus() != null && u.getStatus() == 1)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> searchCustomersForDropdown(String search) {
        // Lấy tất cả user có role CUSTOMER và status = 1 (active)
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getStatus() != null && u.getStatus() == 1) // Active users only
                .filter(u -> u.getRole() != null && "CUSTOMER".equals(u.getRole().getRoleName().name())) // Only customers
                .collect(Collectors.toList());

        // Nếu không có search term hoặc rỗng, trả về tất cả customers
        if (search == null || search.trim().isEmpty()) {
            return customers;
        }

        // Lọc theo tên hoặc email (case insensitive)
        String searchLower = search.trim().toLowerCase();
        return customers.stream()
                .filter(u -> (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchLower)) ||
                           (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
    }

    @Override
    public ApiResponse<List<TopSpenderResponse>> getTopSpenders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<TopSpenderResponse> result = userRepository.findTopSpenders();
        return new ApiResponse<>(200, "Thành công", result);
    }

    @Override
    public ApiResponse<Long> getTotalUsers() {
        long total = userRepository.count();
        long totalActive = userRepository.countActiveUsers();
        // Nếu bạn chỉ muốn trả về tổng user đang hoạt động:
        return new ApiResponse<>(200, "Thành công", totalActive);
        // Nếu muốn trả về tổng tất cả user, dùng: return new ApiResponse<>(200, "Thành
        // công", total);
    }

    // Helper chuyển status String -> Byte
    private Byte parseStatus(String status) {
        if (status == null)
            return 1;
        if (status.equals("1"))
            return 1;
        if (status.equals("0"))
            return 0;
        try {
            return Byte.valueOf(status);
        } catch (Exception e) {
            return 1;
        }
    }

    private Byte parseStatus(String status, Byte defaultStatus) {
        if (status == null)
            return defaultStatus != null ? defaultStatus : 1;
        return parseStatus(status);
    }

    private UserResponse toResponse(User u) {
        UserResponse res = new UserResponse();
        res.setUser_id(u.getId());
        res.setFull_name(u.getFullName());
        res.setEmail(u.getEmail());
        res.setPhone_number(u.getPhoneNumber());
        res.setRole_id(u.getRole() != null ? u.getRole().getId() : null);
        res.setRole_name(u.getRole() != null ? u.getRole().getRoleName().name() : null); // Thêm tên vai trò
        res.setStatus(u.getStatus());
        res.setEmail_verified(u.getEmailVerified());
        res.setCreated_at(formatTime(u.getCreatedAt()));
        res.setUpdated_at(formatTime(u.getUpdatedAt()));
        res.setTotal_spent(u.getTotalSpent() != null ? u.getTotalSpent() : BigDecimal.ZERO);
        res.setTotal_point(u.getTotalPoint() != null ? u.getTotalPoint() : 0);
        res.setIsRetail(u.getIsRetail() != null ? u.getIsRetail():0);
        return res;
    }

    private String formatTime(Long millis) {
        if (millis == null)
            return null;
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     *  THÊM MỚI: Tìm kiếm khách hàng theo tên hoặc email
     */
    @Override
    public List<UserResponse> searchCustomers(String search) {
        if (search == null || search.trim().isEmpty()) {
            return List.of();
        }

        String searchTerm = "%" + search.toLowerCase().trim() + "%";
        List<User> users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm,
                searchTerm);

        return users.stream()
                .filter(user -> user.getStatus() != null && user.getStatus() == 1) // Chỉ lấy user active
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
