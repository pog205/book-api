package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.UserRankRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserRankResponse;
import org.datn.bookstation.dto.response.UserRankSimpleResponse;
import org.datn.bookstation.entity.UserRank;
import org.datn.bookstation.service.UserRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@AllArgsConstructor
@RequestMapping("/api/user-ranks")
public class UserRankController {
    private final UserRankService userRankService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<UserRankResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer rankId,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String rankName) {
        PaginationResponse<UserRankResponse> data = userRankService.getAllWithPagination(page, size, userId, rankId, status, userEmail, rankName);
        ApiResponse<PaginationResponse<UserRankResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRank>> getById(@PathVariable Integer id) {
        UserRank userRank = userRankService.getById(id);
        if (userRank == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", userRank));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserRankSimpleResponse>> addUserRank(@RequestBody UserRankRequest request) {
        ApiResponse<UserRank> response = userRankService.add(request);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, response.getMessage(), null));
        }
        if (response.getStatus() == 409) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, response.getMessage(), null));
        }
        UserRank userRank = response.getData();
        UserRankSimpleResponse simpleResponse = new UserRankSimpleResponse(
            userRank.getId(),
            userRank.getUser() != null ? userRank.getUser().getEmail() : null,
            userRank.getUser() != null ? userRank.getUser().getFullName() : null,
            userRank.getStatus(),
            userRank.getCreatedAt(),
            userRank.getUpdatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo mới thành công", simpleResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserRank>> update(@PathVariable Integer id, @RequestBody UserRankRequest request) {
        ApiResponse<UserRank> response = userRankService.update(request, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", response.getData()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userRankService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rank/{rankId}")
    public ResponseEntity<ApiResponse<PaginationResponse<UserRankSimpleResponse>>> getByRankIdWithFilter(
            @PathVariable Integer rankId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String userName) {
        PaginationResponse<UserRankSimpleResponse> data = userRankService.getByRankIdWithFilter(page, size, rankId, email, userName);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", data));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserRankSimpleResponse>> toggleUserRankStatus(@PathVariable Integer id) {
        ApiResponse<UserRank> response = userRankService.toggleStatus(id);
        // Kiểm tra lỗi 404 (không tìm thấy UserRank)
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        // Kiểm tra lỗi 409 (conflict - user đã có rank hoạt động khác)
        if (response.getStatus() == 409) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, response.getMessage(), null));
        }
        // Kiểm tra data null (trường hợp bất thường)
        if (response.getData() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
        UserRank userRank = response.getData();
        UserRankSimpleResponse simpleResponse = new UserRankSimpleResponse(
            userRank.getId(),
            userRank.getUser() != null ? userRank.getUser().getEmail() : null,
            userRank.getUser() != null ? userRank.getUser().getFullName() : null,
            userRank.getStatus(),
            userRank.getCreatedAt(),
            userRank.getUpdatedAt()
        );
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", simpleResponse));
    }
}
