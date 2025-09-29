// ...existing code...
package org.datn.bookstation.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.datn.bookstation.dto.request.UserVoucherRequest;
import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserForVoucher;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.dto.response.VoucherStatsResponse;
import org.datn.bookstation.dto.response.VoucherDropdownResponse;
import org.datn.bookstation.dto.response.voucherUserResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.UserVoucher;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.repository.UserVoucherRepository;
import org.datn.bookstation.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Import enum vào đầu file nếu chưa có
import org.datn.bookstation.entity.enums.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.repository.VoucherRepository;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @GetMapping
    public PaginationResponse<VoucherResponse> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String voucherCategory,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) Byte status) {
        return voucherService.getAllWithPagination(page, size, code, name, voucherCategory, discountType, status);
    }

    @GetMapping("/userVoucher/{userId}")
    public List<org.datn.bookstation.dto.response.voucherUserResponse> getVoucherById(@PathVariable Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        List<org.datn.bookstation.dto.response.voucherUserResponse> vouchers = userVoucherRepository
                .findVouchersByUserId(userId);
        if (vouchers == null || vouchers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy voucher cho người dùng với ID: " + userId);
        }
        // Lọc bỏ voucher hết hạn (so sánh đến phút)
        List<org.datn.bookstation.dto.response.voucherUserResponse> validVouchers = vouchers.stream()
                .filter(v -> {
                    if (v.getEndTime() == null)
                        return true;
                    LocalDateTime endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(v.getEndTime()),
                            ZoneId.systemDefault());
                    return endTime.isAfter(now);
                })
                .collect(Collectors.toList());
        if (validVouchers.isEmpty()) {
            throw new RuntimeException("Không có voucher nào còn hạn cho người dùng với ID: " + userId);
        }
        return validVouchers;
    }

    @GetMapping("/new")
    public voucherUserResponse getVoucherByuserId() {
        String code = "WELCOME";
        List<voucherUserResponse> vouchers = userVoucherRepository.findVouchersByVoucherId(code);
        if (vouchers == null || vouchers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy voucher với mã: " + code);
        }
        return vouchers.get(0); // Lấy voucher đầu tiên trong danh sách
    }

    @GetMapping("/new/{userId}")
    public List<voucherUserResponse> getVoucherByUserIdNew(@PathVariable Integer userId) {
        String code = "WELCOME";
        List<voucherUserResponse> vouchers = userVoucherRepository.findVouchersByVoucherUserId(code, userId);
        if (vouchers == null || vouchers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy voucher với mã: " + code);
        }
        return vouchers;
    }

    @GetMapping("/newVoucher/{voucherId}")
    public List<UserForVoucher> getUserByVuocherID(@PathVariable Integer voucherId) {
        List<UserVoucher> userVouchers = userVoucherRepository.findByVoucherId(voucherId);
        if (userVouchers == null || userVouchers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy user nào với voucherId: " + voucherId);
        }
        return userVouchers.stream().map(uv -> {
            UserForVoucher dto = new UserForVoucher();
            dto.setId(uv.getId());
            dto.setUserId(uv.getUser().getId());
            dto.setFullName(uv.getUser().getFullName());
            dto.setVoucherId(uv.getVoucher().getId());
            dto.setVoucherCode(uv.getVoucher().getCode());
            dto.setUsedCount(uv.getUsedCount());
            dto.setCreatedAt(uv.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping("/NewVoucher")
    public void addVoucherForUser(@RequestBody UserVoucherRequest request) {
        UserVoucher userVoucher = new UserVoucher();

        // Tạo đối tượng User và Voucher chỉ với id
        User user = new User();
        user.setId(request.getUserId());
        userVoucher.setUser(user);

        Voucher voucher = new Voucher();
        voucher.setId(request.getVoucherId());
        userVoucher.setVoucher(voucher);

        userVoucher.setUsedCount(0); // hoặc để mặc định cũng được

        userVoucherRepository.save(userVoucher);
    }

    @PostMapping
    public void addVoucher(@RequestBody VoucherRepuest request) {
        voucherService.addVoucher(request);
    }

    @PutMapping
    public void editVoucher(@RequestBody VoucherRepuest request) {
        voucherService.editVoucher(request);
    }

    @PatchMapping("/status")
    public void updateStatus(
            @RequestParam Integer id,
            @RequestParam byte status,
            @RequestParam String updatedBy) {
        voucherService.updateStatus(id, status, updatedBy);
    }

    @DeleteMapping("/{id}")
    public void deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
    }

    /**
     * API lấy danh sách voucher có thể sử dụng cho user (dành cho admin tạo đơn thủ
     * công)
     */
    @GetMapping("/user/{userId}/available")
    public ResponseEntity<ApiResponse<List<org.datn.bookstation.dto.response.AvailableVoucherResponse>>> getAvailableVouchersForUser(
            @PathVariable Integer userId) {
        try {
            List<org.datn.bookstation.dto.response.AvailableVoucherResponse> availableVouchers = voucherService
                    .getAvailableVouchersForUser(userId);
            ApiResponse<List<org.datn.bookstation.dto.response.AvailableVoucherResponse>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách voucher có thể sử dụng thành công",
                    availableVouchers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<org.datn.bookstation.dto.response.AvailableVoucherResponse>> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi lấy danh sách voucher: " + e.getMessage(),
                    null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ✅ API search voucher cho counter sales
     * Tìm kiếm voucher theo mã hoặc tên để staff có thể tra cứu khi khách hàng đưa
     * voucher
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> searchVouchersForCounterSales(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<VoucherResponse> vouchers = voucherService.searchVouchersForCounterSales(query, limit);
            ApiResponse<List<VoucherResponse>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Tìm kiếm voucher thành công",
                    vouchers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<VoucherResponse>> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi tìm kiếm voucher: " + e.getMessage(),
                    null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/voucher-to-all")
    public ResponseEntity<?> addVoucherToAllUsers(@RequestParam("voucherId") Integer voucherId) {
        Optional<Voucher> optionalVoucher = voucherRepository.findById(voucherId);
        if (optionalVoucher.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Voucher không tồn tại với ID: " + voucherId);
        }
        Voucher voucher = optionalVoucher.get();

        List<User> allUsers = userRepository.findAll();

        List<UserVoucher> userVouchers = allUsers.stream()
                .filter(user -> user.getRole() != null && user.getRole().getRoleName() == RoleName.CUSTOMER) // Chỉ lấy
                                                                                                             // user có
                                                                                                             // role
                                                                                                             // CUSTOMER
                .filter(user -> !userVoucherRepository.existsByUser_IdAndVoucher_Id(user.getId(), voucher.getId()))
                .map(user -> {
                    UserVoucher uv = new UserVoucher();
                    uv.setUser(user);
                    uv.setVoucher(voucher);
                    uv.setUsedCount(0);
                    return uv;
                }).collect(Collectors.toList());

        userVoucherRepository.saveAll(userVouchers);

        return ResponseEntity.ok("Đã phát voucher cho " + userVouchers.size() + " người dùng");
    }

    /**
     * API lấy thống kê voucher cho dashboard admin
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<VoucherStatsResponse>> fetchVoucherStats() {
        try {
            VoucherStatsResponse stats = voucherService.getVoucherStats();
            ApiResponse<VoucherStatsResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy thống kê voucher thành công", 
                stats
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<VoucherStatsResponse> response = new ApiResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Lỗi khi lấy thống kê voucher: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API dropdown voucher cho minigame box system
     * Tìm kiếm voucher theo mã hoặc tên và trả về thông tin đầy đủ (trừ createdAt, updatedAt)
     */
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<VoucherDropdownResponse>>> getVoucherDropdown(
            @RequestParam(required = false) String search) {
        try {
            List<VoucherDropdownResponse> vouchers = voucherService.getVoucherDropdown(search);
            ApiResponse<List<VoucherDropdownResponse>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách voucher thành công",
                    vouchers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<VoucherDropdownResponse>> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi lấy danh sách voucher: " + e.getMessage(),
                    null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/distribute/silver")
    public ApiResponse<String> distributeVouchersToSilverRank(
            @RequestParam("voucherId") Integer voucherId) {
        return voucherService.distributeVouchersToSilverRank(voucherId);
    }

    @PostMapping("/distribute/gold")
    public ApiResponse<String> distributeVouchersToGoldRank(
            @RequestParam("voucherId") Integer voucherId) {
        return voucherService.distributeVouchersToGoldRank(voucherId);
    }

    @PostMapping("/distribute/diamond")
    public ApiResponse<String> distributeVouchersToDiamondRank(
            @RequestParam("voucherId") Integer voucherId) {
        return voucherService.distributeVouchersToDiamondRank(voucherId);
    }
}