package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.dto.response.VoucherStatsResponse;
import org.datn.bookstation.dto.response.VoucherDropdownResponse;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.UserVoucher;
import org.datn.bookstation.entity.UserRank;
import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.repository.UserRankRepository;
import org.datn.bookstation.repository.UserVoucherRepository;
import org.datn.bookstation.repository.RankRepository;
import org.datn.bookstation.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private org.datn.bookstation.service.VoucherCalculationService voucherCalculationService;

    @Override
    public java.util.List<org.datn.bookstation.dto.response.AvailableVoucherResponse> getAvailableVouchersForUser(
            Integer userId) {
        PaginationResponse<VoucherResponse> allVouchers = getAllWithPagination(0, 1000, null, null, null, null, (byte) 1);
        long currentTime = System.currentTimeMillis();
        return allVouchers.getContent().stream()
                .filter(voucher -> {
                    boolean isTimeValid = voucher.getStartTime() <= currentTime && currentTime <= voucher.getEndTime();
                    boolean canUserUse = voucherCalculationService.canUserUseVoucher(userId, voucher.getId());
                    boolean hasUsageLimit = voucher.getUsageLimit() == null
                            || voucher.getUsedCount() < voucher.getUsageLimit();
                    return isTimeValid && canUserUse && hasUsageLimit;
                })
                .map(voucher -> {
                    org.datn.bookstation.dto.response.AvailableVoucherResponse dto = new org.datn.bookstation.dto.response.AvailableVoucherResponse();
                    dto.setId(voucher.getId());
                    dto.setCode(voucher.getCode());
                    dto.setName(voucher.getName());
                    dto.setDescription(voucher.getDescription());
                    dto.setCategoryVi(voucher.getVoucherCategory() == null ? ""
                            : (voucher.getVoucherCategory().name().equals("NORMAL") ? "Giảm giá sản phẩm"
                                    : "Giảm giá vận chuyển"));
                    dto.setDiscountTypeVi(voucher.getDiscountType() == null ? ""
                            : (voucher.getDiscountType().name().equals("PERCENTAGE") ? "Giảm theo phần trăm"
                                    : "Giảm số tiền cố định"));
                    dto.setDiscountValue(voucher.getDiscountPercentage() != null ? voucher.getDiscountPercentage()
                            : voucher.getDiscountAmount());
                    dto.setMinOrderValue(voucher.getMinOrderValue());
                    dto.setMaxDiscountValue(voucher.getMaxDiscountValue());
                    dto.setStartTime(voucher.getStartTime());
                    dto.setEndTime(voucher.getEndTime());
                    dto.setUsageLimit(voucher.getUsageLimit());
                    dto.setUsedCount(voucher.getUsedCount());
                    dto.setUsageLimitPerUser(voucher.getUsageLimitPerUser());
                    dto.setRemainingUses(voucher.getUsageLimit() == null ? null
                            : voucher.getUsageLimit() - (voucher.getUsedCount() == null ? 0 : voucher.getUsedCount()));
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm");
                    dto.setExpireDate(
                            voucher.getEndTime() != null
                                    ? java.time.LocalDateTime
                                            .ofInstant(java.time.Instant.ofEpochMilli(voucher.getEndTime()),
                                                    java.time.ZoneId.systemDefault())
                                            .format(formatter)
                                    : "");
                    dto.setDiscountInfo((voucher.getDiscountPercentage() != null
                            ? ("Giảm " + voucher.getDiscountPercentage().stripTrailingZeros().toPlainString() + "%")
                            : ("Giảm " + formatVnMoney(voucher.getDiscountAmount()) + "đ")) + " cho đơn từ "
                            + formatVnMoney(voucher.getMinOrderValue()) + "đ");
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private static String formatVnMoney(java.math.BigDecimal money) {
        if (money == null)
            return "0";
        java.text.DecimalFormat df = new java.text.DecimalFormat("###,###");
        return df.format(money.setScale(0, java.math.RoundingMode.DOWN));
    }

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRankRepository userRankRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private RankRepository rankRepository;

    @Override
    public PaginationResponse<VoucherResponse> getAllWithPagination(
            int page, int size, String code, String name, String voucherCategory, String discountType, Byte status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Voucher> spec = Specification.where(null);

        if (code != null && !code.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
        }
        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (voucherCategory != null && !voucherCategory.isEmpty()) {
            try {
                org.datn.bookstation.entity.enums.VoucherCategory category = 
                    org.datn.bookstation.entity.enums.VoucherCategory.valueOf(voucherCategory.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("voucherCategory"), category));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Danh mục voucher không hợp lệ: " + voucherCategory + 
                    ". Các giá trị hợp lệ: NORMAL, SHIPPING");
            }
        }
        
        if (discountType != null && !discountType.isEmpty()) {
            try {
                org.datn.bookstation.entity.enums.DiscountType type = 
                    org.datn.bookstation.entity.enums.DiscountType.valueOf(discountType.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("discountType"), type));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Loại giảm giá không hợp lệ: " + discountType + 
                    ". Các giá trị hợp lệ: PERCENTAGE, FIXED_AMOUNT");
            }
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Voucher> voucherPage = voucherRepository.findAll(spec, pageable);

        List<VoucherResponse> responses = voucherPage.getContent().stream().map(voucher -> {
            VoucherResponse dto = new VoucherResponse();
            dto.setId(voucher.getId());
            dto.setCode(voucher.getCode());
            dto.setName(voucher.getName());
            dto.setDescription(voucher.getDescription());
            dto.setVoucherCategory(voucher.getVoucherCategory());
            dto.setDiscountType(voucher.getDiscountType());
            dto.setDiscountPercentage(voucher.getDiscountPercentage());
            dto.setDiscountAmount(voucher.getDiscountAmount());
            dto.setStartTime(voucher.getStartTime());
            dto.setEndTime(voucher.getEndTime());
            dto.setMinOrderValue(voucher.getMinOrderValue());
            dto.setMaxDiscountValue(voucher.getMaxDiscountValue());
            dto.setUsageLimit(voucher.getUsageLimit());
            dto.setUsedCount(voucher.getUsedCount());
            dto.setUsageLimitPerUser(voucher.getUsageLimitPerUser());
            dto.setStatus(voucher.getStatus());
            dto.setCreatedAt(voucher.getCreatedAt());
            dto.setUpdatedAt(voucher.getUpdatedAt());
            dto.setCreatedBy(voucher.getCreatedBy());
            dto.setUpdatedBy(voucher.getUpdatedBy());
            return dto;
        }).collect(Collectors.toList());

        return new PaginationResponse<>(
                responses,
                voucherPage.getNumber(),
                voucherPage.getSize(),
                voucherPage.getTotalElements(),
                voucherPage.getTotalPages());
    }

    @Override
    public void addVoucher(VoucherRepuest request) {
        // Validate input
        validateVoucherRequest(request, false);
        
        // Validate code unique
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã voucher '" + request.getCode() + "' đã tồn tại, vui lòng chọn mã khác");
        }
        
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode());
        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setVoucherCategory(request.getVoucherCategory());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setDiscountAmount(request.getDiscountAmount());
        voucher.setStartTime(request.getStartTime());
        voucher.setEndTime(request.getEndTime());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscountValue(
                request.getMaxDiscountValue() != null ? request.getMaxDiscountValue() : BigDecimal.ZERO);
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setUsedCount(0);
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser());
        voucher.setStatus((byte) 1);
        voucher.setCreatedBy(request.getCreatedBy());
        voucher.setUpdatedBy(request.getUpdatedBy());
        
        voucherRepository.save(voucher);
    }

    @Override
    public void editVoucher(VoucherRepuest request) {
        // Validate input
        validateVoucherRequest(request, true);
        
        Voucher voucher = voucherRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher với ID: " + request.getId()));
        
        // Validate code unique (trừ voucher hiện tại)
        if (voucherRepository.existsByCode(request.getCode())) {
            Voucher existingVoucher = voucherRepository.findByCode(request.getCode()).orElse(null);
            if (existingVoucher != null && !existingVoucher.getId().equals(request.getId())) {
                throw new RuntimeException("Mã voucher '" + request.getCode() + "' đã tồn tại, vui lòng chọn mã khác");
            }
        }
        
        voucher.setCode(request.getCode());
        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setVoucherCategory(request.getVoucherCategory());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setDiscountAmount(request.getDiscountAmount());
        voucher.setStartTime(request.getStartTime());
        voucher.setEndTime(request.getEndTime());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscountValue(request.getMaxDiscountValue());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setUsedCount(request.getUsedCount());
        voucher.setStatus((byte) 1); // Khi edit thì luôn để trạng thái là hoạt động
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser());
        voucher.setUpdatedBy(request.getUpdatedBy());
        
        voucherRepository.save(voucher);
    }

    @Override
    public void updateStatus(Integer id, byte status, String updatedBy) {
        // Validate input
        if (id == null) {
            throw new RuntimeException("ID voucher không được để trống");
        }
        
        if (status != 0 && status != 1) {
            throw new RuntimeException("Trạng thái voucher chỉ được là 0 (không hoạt động) hoặc 1 (hoạt động)");
        }
        
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new RuntimeException("Người cập nhật không được để trống");
        }
        
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher với ID: " + id));
        
        voucher.setStatus(status);
        voucher.setUpdatedBy(updatedBy);
        voucherRepository.save(voucher);
    }

    @Override
    public void deleteVoucher(Integer id) {
        // Validate input
        if (id == null) {
            throw new RuntimeException("ID voucher không được để trống");
        }
        
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher với ID: " + id));
        
        // Kiểm tra có thể xóa được không (nếu voucher đã được sử dụng)
        if (voucher.getUsedCount() != null && voucher.getUsedCount() > 0) {
            throw new RuntimeException("Không thể xóa voucher đã được sử dụng " + voucher.getUsedCount() + " lần. Vui lòng ẩn voucher thay vì xóa");
        }
        
        voucherRepository.delete(voucher);
    }

    @Override
    public List<VoucherResponse> searchVouchersForCounterSales(String query, int limit) {
        Specification<Voucher> spec = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.conjunction();

        // Active vouchers only
        spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), (byte) 1));

        //  NEW: Exclude shipping vouchers for counter sales
        spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("voucherCategory"),
                org.datn.bookstation.entity.enums.VoucherCategory.SHIPPING));

        // Search by code or name
        if (query != null && !query.trim().isEmpty()) {
            String searchQuery = "%" + query.trim().toLowerCase() + "%";
            spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), searchQuery),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchQuery)));
        }

        // Current time validation
        long currentTime = System.currentTimeMillis();
        spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), currentTime),
                criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), currentTime)));

        // Has usage limit
        spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("usageLimit")),
                criteriaBuilder.lessThan(root.get("usedCount"), root.get("usageLimit"))));

        Pageable pageable = PageRequest.of(0, limit);
        Page<Voucher> vouchers = voucherRepository.findAll(spec, pageable);

        return vouchers.getContent().stream()
                .map(this::toVoucherResponse)
                .collect(Collectors.toList());
    }

    private VoucherResponse toVoucherResponse(Voucher voucher) {
        VoucherResponse response = new VoucherResponse();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setName(voucher.getName());
        response.setDescription(voucher.getDescription());
        response.setVoucherCategory(voucher.getVoucherCategory());
        response.setDiscountType(voucher.getDiscountType());
        response.setDiscountPercentage(voucher.getDiscountPercentage());
        response.setDiscountAmount(voucher.getDiscountAmount());
        response.setMinOrderValue(voucher.getMinOrderValue());
        response.setMaxDiscountValue(voucher.getMaxDiscountValue());
        response.setStartTime(voucher.getStartTime());
        response.setEndTime(voucher.getEndTime());
        response.setUsageLimit(voucher.getUsageLimit());
        response.setUsedCount(voucher.getUsedCount());
        response.setStatus(voucher.getStatus());
        return response;
    }

    @Override
    public VoucherStatsResponse getVoucherStats() {
        Long currentTime = System.currentTimeMillis();

        // Tổng số voucher
        Long totalVouchers = voucherRepository.countTotalVouchers();

        //  SỬA: Voucher chưa được sử dụng thay vì voucher đang hoạt động
        Long unusedVouchers = voucherRepository.countUnusedVouchers();

        // Tổng lượt sử dụng (giữ nguyên)
        Long totalUsageCount = voucherRepository.sumTotalUsageCount();

        // Voucher phổ biến nhất
        List<String> mostPopularCodes = voucherRepository.findMostPopularVoucherCode();
        String mostPopularVoucher = mostPopularCodes.isEmpty() ? "N/A" : mostPopularCodes.get(0);

        return new VoucherStatsResponse(
                totalVouchers != null ? totalVouchers : 0L,
                unusedVouchers != null ? unusedVouchers : 0L,  // ← SỬA: dùng unusedVouchers
                totalUsageCount != null ? totalUsageCount : 0L,
                mostPopularVoucher);
    }

    @Override
    public List<VoucherDropdownResponse> getVoucherDropdown(String search) {
        Specification<Voucher> spec = Specification.where(
            (root, query, cb) -> cb.equal(root.get("status"), (byte) 1)
        );

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("code")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
                )
            );
        }

        List<Voucher> vouchers = voucherRepository.findAll(spec);

        return vouchers.stream()
                .map(this::toVoucherDropdownResponse)
                .collect(Collectors.toList());
    }

    private VoucherDropdownResponse toVoucherDropdownResponse(Voucher voucher) {
        VoucherDropdownResponse response = new VoucherDropdownResponse();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setName(voucher.getName());
        response.setDescription(voucher.getDescription());
        response.setVoucherCategory(voucher.getVoucherCategory());
        response.setDiscountType(voucher.getDiscountType());
        response.setDiscountPercentage(voucher.getDiscountPercentage());
        response.setDiscountAmount(voucher.getDiscountAmount());
        response.setStartTime(voucher.getStartTime());
        response.setEndTime(voucher.getEndTime());
        response.setMinOrderValue(voucher.getMinOrderValue());
        response.setMaxDiscountValue(voucher.getMaxDiscountValue());
        response.setUsageLimit(voucher.getUsageLimit());
        response.setUsedCount(voucher.getUsedCount());
        response.setUsageLimitPerUser(voucher.getUsageLimitPerUser());
        response.setStatus(voucher.getStatus());
        response.setCreatedBy(voucher.getCreatedBy());
        response.setUpdatedBy(voucher.getUpdatedBy());
        return response;
    }

    /**
     * Validate voucher request cho cả add và edit
     */
    private void validateVoucherRequest(VoucherRepuest request, boolean isEdit) {
        // 1. Input Validation
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new RuntimeException("Mã voucher không được để trống");
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên voucher không được để trống");
        }
        
        if (request.getStartTime() == null) {
            throw new RuntimeException("Thời gian bắt đầu không được để trống");
        }
        
        if (request.getEndTime() == null) {
            throw new RuntimeException("Thời gian kết thúc không được để trống");
        }
        
        if (request.getCreatedBy() == null || request.getCreatedBy().trim().isEmpty()) {
            throw new RuntimeException("Người tạo voucher không được để trống");
        }
        
        // 2. Business Logic Validation
        // Thời gian hợp lệ: startTime < endTime
        if (request.getStartTime() >= request.getEndTime()) {
            throw new RuntimeException("Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc");
        }
        
        // Giá trị đơn hàng tối thiểu >= 0 và <= 10 triệu
        if (request.getMinOrderValue() != null) {
            if (request.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Giá trị đơn hàng tối thiểu không được nhỏ hơn 0");
            }
            if (request.getMinOrderValue().compareTo(new BigDecimal("10000000")) > 0) {
                throw new RuntimeException("Giá trị đơn hàng tối thiểu không được vượt quá 10 triệu đồng");
            }
        }
        
        // Giá trị giảm tối đa <= 10 triệu
        if (request.getMaxDiscountValue() != null) {
            if (request.getMaxDiscountValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Giá trị giảm tối đa không được âm");
            }
            if (request.getMaxDiscountValue().compareTo(new BigDecimal("10000000")) > 0) {
                throw new RuntimeException("Giá trị giảm tối đa không được vượt quá 10 triệu đồng");
            }
        }
        
        // Giới hạn sử dụng > 0
        if (request.getUsageLimit() != null && request.getUsageLimit() <= 0) {
            throw new RuntimeException("Giới hạn sử dụng phải lớn hơn 0");
        }
        
        if (request.getUsageLimitPerUser() != null && request.getUsageLimitPerUser() <= 0) {
            throw new RuntimeException("Giới hạn sử dụng trên mỗi user phải lớn hơn 0");
        }
        
        // 3. Discount Logic Validation
        if (request.getDiscountType() == null) {
            throw new RuntimeException("Loại giảm giá không được để trống");
        }
        
        if (request.getVoucherCategory() == null) {
            throw new RuntimeException("Danh mục voucher không được để trống");
        }
        
        if (request.getDiscountType() == org.datn.bookstation.entity.enums.DiscountType.PERCENTAGE) {
            // Nếu là PERCENTAGE thì discountAmount phải null
            if (request.getDiscountAmount() != null) {
                throw new RuntimeException("Voucher giảm theo phần trăm không được có giá trị giảm cố định");
            }
            
            // Kiểm tra discountPercentage
            if (request.getDiscountPercentage() == null) {
                throw new RuntimeException("Phần trăm giảm giá không được để trống");
            }
            
            if (request.getDiscountPercentage().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Phần trăm giảm giá phải lớn hơn 0");
            }
            
            if (request.getDiscountPercentage().compareTo(new BigDecimal("100")) > 0) {
                throw new RuntimeException("Phần trăm giảm giá không được vượt quá 100%");
            }
            
        } else if (request.getDiscountType() == org.datn.bookstation.entity.enums.DiscountType.FIXED_AMOUNT) {
            // Nếu là FIXED_AMOUNT thì discountPercentage phải null
            if (request.getDiscountPercentage() != null) {
                throw new RuntimeException("Voucher giảm giá cố định không được có phần trăm giảm");
            }
            
            // Kiểm tra discountAmount
            if (request.getDiscountAmount() == null) {
                throw new RuntimeException("Giá trị giảm cố định không được để trống");
            }
            
            if (request.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Giá trị giảm cố định phải lớn hơn 0");
            }
            
            if (request.getDiscountAmount().compareTo(new BigDecimal("10000000")) > 0) {
                throw new RuntimeException("Giá trị giảm cố định không được vượt quá 10 triệu đồng");
            }
        }
        
        // 4. Edit Validation (chỉ khi edit)
        if (isEdit) {
            if (request.getId() == null) {
                throw new RuntimeException("ID voucher không được để trống khi cập nhật");
            }
        }
    }

    @Override
    public ApiResponse<String> distributeVouchersToSilverRank(Integer voucherId) {
        try {
            int[] result = distributeVouchersByRank(voucherId, "BẠC");
            int distributedCount = result[0];
            int skippedCount = result[1];
            int totalCount = result[2];
            
            String message;
            if (distributedCount == 0) {
                message = "Tất cả " + totalCount + " người dùng hạng BẠC đã có voucher này";
            } else if (skippedCount == 0) {
                message = "Đã phát voucher cho " + distributedCount + " người dùng hạng BẠC";
            } else {
                message = "Đã phát voucher cho " + distributedCount + " người dùng hạng BẠC, " + skippedCount + " người bị bỏ qua (đã có voucher)";
            }
            
            return new ApiResponse<>(200, message, "Thành công");
        } catch (Exception e) {
            return new ApiResponse<>(400, "Lỗi khi phát voucher: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> distributeVouchersToGoldRank(Integer voucherId) {
        try {
            int[] result = distributeVouchersByRank(voucherId, "VÀNG");
            int distributedCount = result[0];
            int skippedCount = result[1];
            int totalCount = result[2];
            
            String message;
            if (distributedCount == 0) {
                message = "Tất cả " + totalCount + " người dùng hạng VÀNG đã có voucher này";
            } else if (skippedCount == 0) {
                message = "Đã phát voucher cho " + distributedCount + " người dùng hạng VÀNG";
            } else {
                message = "Đã phát voucher cho " + distributedCount + " người dùng hạng VÀNG, " + skippedCount + " người bị bỏ qua (đã có voucher)";
            }
            
            return new ApiResponse<>(200, message, "Thành công");
        } catch (Exception e) {
            return new ApiResponse<>(400, "Lỗi khi phát voucher: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> distributeVouchersToDiamondRank(Integer voucherId) {
        try {
            int[] result = distributeVouchersByRank(voucherId, "KIM CƯƠNG");
            int distributedCount = result[0];
            int skippedCount = result[1];
            int totalCount = result[2];
            
            String message;
            if (distributedCount == 0) {
                message = "Tất cả " + totalCount + " người dùng hạng KIM CƯƠNG đã có voucher này";
            } else if (skippedCount == 0) {
                message = "Đã phát voucher cho " + distributedCount + " người dùng hạng KIM CƯƠNG";
            } else {
                message = "Đã phát voucher cho " + distributedCount + " người dùng hạng KIM CƯƠNG, " + skippedCount + " người bị bỏ qua (đã có voucher)";
            }
            
            return new ApiResponse<>(200, message, "Thành công");
        } catch (Exception e) {
            return new ApiResponse<>(400, "Lỗi khi phát voucher: " + e.getMessage(), null);
        }
    }

    private int[] distributeVouchersByRank(Integer voucherId, String rankName) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        Rank rank = rankRepository.findByRankName(rankName)
                .orElseThrow(() -> new RuntimeException("Hạng không tồn tại"));

        List<UserRank> userRanks = userRankRepository.findByRankId(rank.getId());
        
        if (userRanks.isEmpty()) {
            throw new RuntimeException("Không có user nào thuộc hạng " + rankName);
        }

        List<UserRank> eligibleUsers = userRanks.stream()
                .filter(userRank -> userRank.getUser().getRole() != null && 
                        userRank.getUser().getRole().getRoleName() == org.datn.bookstation.entity.enums.RoleName.CUSTOMER)
                .collect(java.util.stream.Collectors.toList());

        if (eligibleUsers.isEmpty()) {
            throw new RuntimeException("Không có user CUSTOMER nào thuộc hạng " + rankName);
        }

        int totalUsers = eligibleUsers.size();
        int skippedUsers = 0;
        List<UserVoucher> userVouchers = new java.util.ArrayList<>();

        for (UserRank userRank : eligibleUsers) {
            if (userVoucherRepository.existsByUser_IdAndVoucher_Id(userRank.getUser().getId(), voucherId)) {
                skippedUsers++;
            } else {
                UserVoucher uv = new UserVoucher();
                uv.setUser(userRank.getUser());
                uv.setVoucher(voucher);
                uv.setUsedCount(0);
                userVouchers.add(uv);
            }
        }

        if (!userVouchers.isEmpty()) {
            userVoucherRepository.saveAll(userVouchers);
        }

        return new int[]{userVouchers.size(), skippedUsers, totalUsers};
    }
}
