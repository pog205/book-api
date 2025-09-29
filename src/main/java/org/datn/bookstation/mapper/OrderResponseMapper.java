package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.OrderDetailResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.OrderDetail;
import org.datn.bookstation.entity.OrderVoucher;
import org.datn.bookstation.utils.OrderStatusUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderResponseMapper {
    
    public OrderResponse toResponse(Order order) {
        if (order == null) return null;
        
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCode(order.getCode());
        response.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        response.setUserEmail(order.getUser() != null ? order.getUser().getEmail() : null);
        response.setUserName(order.getUser() != null ? order.getUser().getFullName() : null);
        response.setStaffId(order.getStaff() != null ? order.getStaff().getId() : null);
        response.setStaffName(order.getStaff() != null ? order.getStaff().getFullName() : null);
        response.setAddressId(order.getAddress() != null ? order.getAddress().getId() : null);
        response.setAddressDetail(order.getAddress() != null ? order.getAddress().getAddressDetail() : null);
        
        // ✅ FIX: Ưu tiên lấy thông tin từ Order cho counter sales, fallback về Address cho online orders
        if (order.getRecipientName() != null) {
            // Counter sales - lấy từ Order
            response.setRecipientName(order.getRecipientName());
            response.setPhoneNumber(order.getPhoneNumber());
        } else if (order.getAddress() != null) {
            // Online orders - lấy từ Address
            response.setRecipientName(order.getAddress().getRecipientName());
            response.setPhoneNumber(order.getAddress().getPhoneNumber());
        } else {
            // Fallback
            response.setRecipientName(null);
            response.setPhoneNumber(null);
        }
        response.setOrderDate(order.getOrderDate());
        
        // Financial fields - FIXED: Add missing financial mappings
        response.setSubtotal(order.getSubtotal());
        response.setShippingFee(order.getShippingFee());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setDiscountShipping(order.getDiscountShipping());
        response.setVoucherDiscountAmount(order.getDiscountAmount().add(order.getDiscountShipping())); // ✅ THÊM: Tổng discount voucher
        response.setTotalAmount(order.getTotalAmount());
        response.setRegularVoucherCount(order.getRegularVoucherCount());
        response.setShippingVoucherCount(order.getShippingVoucherCount());
        
        response.setStatus(order.getStatus());
        response.setOrderStatus(order.getOrderStatus());
        response.setOrderStatusDisplay(OrderStatusUtil.getStatusDisplayName(order.getOrderStatus()));
        response.setOrderType(order.getOrderType());
        response.setPaymentMethod(order.getPaymentMethod()); // ✅ THÊM MỚI
        response.setNotes(order.getNotes());
        response.setCancelReason(order.getCancelReason());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setCreatedBy(order.getCreatedBy());
        response.setUpdatedBy(order.getUpdatedBy());
        
        // ✅ THÊM: Thông tin trạng thái có thể chuyển
        response.setAvailableTransitions(OrderStatusUtil.getAvailableTransitions(order.getOrderStatus()));
        
        return response;
    }
    
    public OrderResponse toResponseWithDetails(Order order, List<OrderDetail> orderDetails, List<OrderVoucher> orderVouchers) {
        OrderResponse response = toResponse(order);
        if (response == null) return null;
        
        // Map order details
        if (orderDetails != null) {
            List<OrderDetailResponse> detailResponses = orderDetails.stream()
                .map(this::toOrderDetailResponse)
                .collect(Collectors.toList());
            response.setOrderDetails(detailResponses);
        }
        
        // Map vouchers
        if (orderVouchers != null) {
            List<VoucherResponse> voucherResponses = orderVouchers.stream()
                .map(this::toVoucherResponse)
                .collect(Collectors.toList());
            response.setVouchers(voucherResponses);
        }
        
        return response;
    }
    
    private OrderDetailResponse toOrderDetailResponse(OrderDetail detail) {
        if (detail == null) return null;
        
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderId(detail.getOrder() != null ? detail.getOrder().getId() : null);
        response.setBookId(detail.getBook() != null ? detail.getBook().getId() : null);
        response.setBookName(detail.getBook() != null ? detail.getBook().getBookName() : null);
        response.setBookCode(detail.getBook() != null ? detail.getBook().getBookCode() : null);
        
        // ✅ THÊM: Giá gốc của sách (luôn luôn là book.price)
        response.setOriginalPrice(detail.getBook() != null ? detail.getBook().getPrice() : null);
        
        // Flash sale information
        response.setFlashSaleItemId(detail.getFlashSaleItem() != null ? detail.getFlashSaleItem().getId() : null);
        response.setFlashSalePrice(detail.getFlashSaleItem() != null ? detail.getFlashSaleItem().getDiscountPrice() : null);
        response.setFlashSaleStock(detail.getFlashSaleItem() != null ? detail.getFlashSaleItem().getStockQuantity() : null);
        response.setIsFlashSale(detail.getFlashSaleItem() != null);
        
        // Stock information
        response.setAvailableStock(detail.getBook() != null ? detail.getBook().getStockQuantity() : null);
        
        // ✅ FIX: Book image - check both images field and coverImageUrl
        if (detail.getBook() != null) {
            String bookImage = null;
            
            // First, try to get from images field (comma-separated)
            if (detail.getBook().getImages() != null && !detail.getBook().getImages().trim().isEmpty()) {
                String[] imageUrls = detail.getBook().getImages().split(",");
                if (imageUrls.length > 0 && !imageUrls[0].trim().isEmpty()) {
                    bookImage = imageUrls[0].trim(); // Use the first image
                }
            }
            
            // Fallback to coverImageUrl if images field is empty
            if (bookImage == null && detail.getBook().getCoverImageUrl() != null) {
                bookImage = detail.getBook().getCoverImageUrl();
            }
            
            response.setBookImageUrl(bookImage);
        }
        
        response.setQuantity(detail.getQuantity());
        response.setUnitPrice(detail.getUnitPrice());
        response.setVoucherDiscountAmount(detail.getVoucherDiscountAmount()); // ✅ THÊM: Map voucher discount amount
        response.setTotalPrice(detail.getUnitPrice().multiply(java.math.BigDecimal.valueOf(detail.getQuantity())));
        response.setCreatedAt(detail.getCreatedAt());
        response.setUpdatedAt(detail.getUpdatedAt());
        response.setCreatedBy(detail.getCreatedBy());
        response.setUpdatedBy(detail.getUpdatedBy());
        response.setStatus(detail.getStatus());
        
        return response;
    }
    
    private VoucherResponse toVoucherResponse(OrderVoucher orderVoucher) {
        if (orderVoucher == null || orderVoucher.getVoucher() == null) return null;
        
        VoucherResponse response = new VoucherResponse();
        response.setId(orderVoucher.getVoucher().getId());
        response.setCode(orderVoucher.getVoucher().getCode());
        response.setName(orderVoucher.getVoucher().getName());
        response.setDescription(orderVoucher.getVoucher().getDescription());
        response.setVoucherCategory(orderVoucher.getVoucher().getVoucherCategory());
        response.setDiscountType(orderVoucher.getVoucher().getDiscountType());
        response.setDiscountPercentage(orderVoucher.getVoucher().getDiscountPercentage());
        response.setDiscountAmount(orderVoucher.getVoucher().getDiscountAmount());
        response.setStartTime(orderVoucher.getVoucher().getStartTime());
        response.setEndTime(orderVoucher.getVoucher().getEndTime());
        response.setMinOrderValue(orderVoucher.getVoucher().getMinOrderValue());
        response.setMaxDiscountValue(orderVoucher.getVoucher().getMaxDiscountValue());
        response.setUsageLimit(orderVoucher.getVoucher().getUsageLimit());
        response.setUsedCount(orderVoucher.getVoucher().getUsedCount());
        response.setUsageLimitPerUser(orderVoucher.getVoucher().getUsageLimitPerUser());
        response.setStatus(orderVoucher.getVoucher().getStatus());
        response.setCreatedAt(orderVoucher.getVoucher().getCreatedAt());
        response.setUpdatedAt(orderVoucher.getVoucher().getUpdatedAt());
        response.setCreatedBy(orderVoucher.getVoucher().getCreatedBy());
        response.setUpdatedBy(orderVoucher.getVoucher().getUpdatedBy());
        
        return response;
    }
}
