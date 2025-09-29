package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.OrderVoucher;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.repository.OrderVoucherRepository;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.service.VoucherManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class VoucherManagementServiceImpl implements VoucherManagementService {
    
    private final VoucherRepository voucherRepository;
    private final OrderVoucherRepository orderVoucherRepository;
    
    @Override
    public void useVouchersForOrder(Order order, List<Voucher> vouchers) {
        if (vouchers == null || vouchers.isEmpty()) {
            return;
        }
        
        for (Voucher voucher : vouchers) {
            // Tăng used_count
            voucher.setUsedCount((voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) + 1);
            voucher.setUpdatedAt(System.currentTimeMillis());
            voucherRepository.save(voucher);
            
            log.info("Used voucher {} for order {}", voucher.getCode(), order.getCode());
        }
    }
    
    @Override
    public void refundVouchersFromCancelledOrder(Order order) {
        //  THEO YÊU CẦU: Khi hủy đơn KHÔNG hoàn lại voucher
        // Method này chỉ ghi log để theo dõi, không thực hiện hoàn voucher
        if (order == null) {
            return;
        }
        
        List<OrderVoucher> orderVouchers = orderVoucherRepository.findAll().stream()
            .filter(ov -> ov.getOrder() != null && ov.getOrder().getId().equals(order.getId()))
            .toList();
        
        for (OrderVoucher orderVoucher : orderVouchers) {
            Voucher voucher = orderVoucher.getVoucher();
            if (voucher != null) {
                // KHÔNG hoàn voucher khi hủy đơn
                log.info("Order {} was cancelled but voucher {} will NOT be refunded (as per business rule)", 
                         order.getCode(), voucher.getCode());
            }
        }
    }
    
    @Override
    public void refundVouchersFromReturnedOrder(Order order) {
        //  HOÀN VOUCHER KHI TRẢ HÀNG
        log.info("Bắt đầu hoàn voucher cho đơn hàng trả {} ", order.getCode());
        
        if (order == null) {
            return;
        }
        
        List<OrderVoucher> orderVouchers = orderVoucherRepository.findAll().stream()
            .filter(ov -> ov.getOrder() != null && ov.getOrder().getId().equals(order.getId()))
            .toList();
        
        for (OrderVoucher orderVoucher : orderVouchers) {
            Voucher voucher = orderVoucher.getVoucher();
            if (voucher != null) {
                // Giảm số lượng đã sử dụng
                if (voucher.getUsedCount() != null && voucher.getUsedCount() > 0) {
                    voucher.setUsedCount(voucher.getUsedCount() - 1);
                    voucher.setUpdatedAt(System.currentTimeMillis());
                    voucherRepository.save(voucher);
                    
                    log.info(" Đã hoàn voucher {} cho đơn hàng trả {}", voucher.getCode(), order.getCode());
                }
            }
        }
    }
    
    @Override
    public String validateVoucherUsage(Voucher voucher, Integer userId) {
        if (voucher == null) {
            return "Voucher không tồn tại";
        }
        
        // Kiểm tra status
        if (voucher.getStatus() == null || voucher.getStatus() != 1) {
            return "Voucher đã bị vô hiệu hóa";
        }
        
        // Kiểm tra thời gian
        long now = System.currentTimeMillis();
        if (voucher.getStartTime() != null && now < voucher.getStartTime()) {
            return "Voucher chưa đến thời gian sử dụng";
        }
        
        if (voucher.getEndTime() != null && now > voucher.getEndTime()) {
            return "Voucher đã hết hạn";
        }
        
        // Kiểm tra số lượng
        if (isVoucherOutOfStock(voucher)) {
            return "Voucher đã hết số lượng sử dụng";
        }
        
        // Kiểm tra giới hạn per user
        if (hasUserExceededVoucherLimit(voucher, userId)) {
            return String.format("Bạn đã sử dụng tối đa %d lần voucher này", 
                                voucher.getUsageLimitPerUser());
        }
        
        return null; // Hợp lệ
    }
    
    @Override
    public boolean isVoucherExpired(Voucher voucher) {
        if (voucher == null || voucher.getEndTime() == null) {
            return true;
        }
        return System.currentTimeMillis() > voucher.getEndTime();
    }
    
    @Override
    public boolean isVoucherOutOfStock(Voucher voucher) {
        if (voucher == null || voucher.getUsageLimit() == null) {
            return false; // Không giới hạn số lượng
        }
        
        int usedCount = voucher.getUsedCount() != null ? voucher.getUsedCount() : 0;
        return usedCount >= voucher.getUsageLimit();
    }
    
    @Override
    public boolean hasUserExceededVoucherLimit(Voucher voucher, Integer userId) {
        if (voucher == null || userId == null || voucher.getUsageLimitPerUser() == null) {
            return false;
        }
        
        // Đếm số lần user đã sử dụng voucher này
        long userUsageCount = orderVoucherRepository.findAll().stream()
            .filter(ov -> ov.getVoucher() != null && ov.getVoucher().getId().equals(voucher.getId()))
            .filter(ov -> ov.getOrder() != null && ov.getOrder().getUser() != null && 
                         ov.getOrder().getUser().getId().equals(userId))
            .count();
        
        return userUsageCount >= voucher.getUsageLimitPerUser();
    }
}
