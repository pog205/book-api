package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.UserRequest;
import org.datn.bookstation.dto.request.UserRetail;
import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.TopSpenderResponse;
import org.datn.bookstation.dto.response.UserResponse;
import org.datn.bookstation.entity.User;

import java.util.List;

import java.util.List;
import java.util.Optional;

public interface UserService {
    // Chuẩn REST: Phân trang, lọc, trả về PaginationResponse<UserResponse>
    PaginationResponse<UserResponse> getAllWithPagination(int page, int size, String fullName, String email,
                                                          String phoneNumber, Integer roleId, String status, Integer userId);

    // Trả về UserResponse theo id
    Optional<UserResponse> getUserResponseById(Integer id);

    // Thêm mới user
    ApiResponse<UserResponse> add(UserRequest req);

    // Cập nhật user
    ApiResponse<UserResponse> update(UserRequest req, Integer id);

    void deleteById(Integer id);

    // Đổi trạng thái user (ví dụ: ACTIVE <-> INACTIVE)
    ApiResponse<UserResponse> toggleStatus(Integer id);

    /**
     * Trả về danh sách user đang active cho dropdown
     */
    List<User> getActiveUsers();

    /**
     * Tìm kiếm khách hàng (role CUSTOMER) theo tên hoặc email cho dropdown
     *
     * @param search Từ khóa tìm kiếm (có thể là tên hoặc email, null/empty thì trả tất cả)
     * @return Danh sách khách hàng với id, tên, email
     */
    List<User> searchCustomersForDropdown(String search);

    ApiResponse<User> getUserByEmail(String email);

    ApiResponse<User> updateClient(User user, Integer id);

    ApiResponse<List<UserRoleRequest>> getUserPOS(String text);

    ApiResponse<User> addRetail(UserRetail req);

    /**
     * ✅ THÊM MỚI: Tìm kiếm khách hàng theo tên hoặc email
     *
     * @param search Từ khóa tìm kiếm (tên hoặc email)
     * @return Danh sách khách hàng phù hợp
     */
    List<UserResponse> searchCustomers(String search);

    ApiResponse<List<TopSpenderResponse>> getTopSpenders(int limit);

    ApiResponse<Long> getTotalUsers();
}
