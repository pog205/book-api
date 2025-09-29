package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.CounterSaleRequest;
import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.request.OrderDetailRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CounterSaleResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.exception.BusinessException;
import org.datn.bookstation.mapper.OrderResponseMapper;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.CounterSaleService;
import org.datn.bookstation.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ Implementation cho Counter Sale Service
 * Xử lý bán hàng tại quầy với đầy đủ nghiệp vụ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CounterSaleServiceImpl implements CounterSaleService {
    
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final UserRepository userRepository;
    private final OrderResponseMapper orderResponseMapper;
    
    @Override
    public ApiResponse<CounterSaleResponse> createCounterSale(CounterSaleRequest request) {
        try {
            log.info("🛒 Creating counter sale for customer: {}, staff: {}", 
                request.getCustomerName(), request.getStaffId());
            
            // 1. Validate request
            log.debug("Step 1: Validating counter sale request");
            validateCounterSaleRequest(request);
            log.debug("Step 1: Request validation passed");
            
            // 2. Validate staff
            log.debug("Step 2: Validating staff with ID: {}", request.getStaffId());
            User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên với ID: " + request.getStaffId()));
            log.debug("Step 2: Staff validation passed, found: {}", staff.getFullName());
            
            // 3. Validate customer (nếu có userId)
            log.debug("Step 3: Validating customer. UserId: {}", request.getUserId());
            User customer = null;
            if (request.getUserId() != null) {
                customer = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy khách hàng với ID: " + request.getUserId()));
                log.debug("Step 3: Customer validation passed, found: {}", customer.getFullName());
            } else {
                log.debug("Step 3: Walk-in customer, no userId validation needed");
            }
            
            // 4. Validate sản phẩm và tính giá
            log.debug("Step 4: Performing counter sale calculation");
            CounterSaleResponse calculation = performCounterSaleCalculation(request);
            if (calculation == null) {
                log.error("Step 4: Calculation failed - returned null");
                return new ApiResponse<>(400, "Lỗi tính toán đơn hàng", null);
            }
            log.debug("Step 4: Calculation completed successfully");
            
            // 5. Tạo OrderRequest từ CounterSaleRequest
            log.debug("Step 5: Building OrderRequest from CounterSaleRequest");
            OrderRequest orderRequest = buildOrderRequestFromCounterSale(request, customer);
            log.debug("Step 5: OrderRequest built successfully");
            
            // 6. Tạo đơn hàng thông qua OrderService
            log.debug("Step 6: Creating order through OrderService");
            log.debug("Step 6: OrderRequest details - userId: {}, staffId: {}, orderType: {}, totalAmount: {}", 
                orderRequest.getUserId(), orderRequest.getStaffId(), orderRequest.getOrderType(), orderRequest.getTotalAmount());
            ApiResponse<OrderResponse> orderResponse = orderService.create(orderRequest);
            log.debug("Step 6: OrderService.create() returned status: {}", orderResponse.getStatus());
            if (orderResponse.getStatus() != 200 && orderResponse.getStatus() != 201) {
                log.error("Step 6: Order creation failed with status: {}, message: {}", 
                    orderResponse.getStatus(), orderResponse.getMessage());
                return new ApiResponse<>(orderResponse.getStatus(), orderResponse.getMessage(), null);
            }
            log.debug("Step 6: Order created successfully with ID: {}", orderResponse.getData().getId());
            
            // 7. Cập nhật trạng thái đơn hàng thành DELIVERED (đã thanh toán tại quầy)
            log.debug("Step 7: Retrieving order for status update");
            Order order = orderRepository.findById(orderResponse.getData().getId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng vừa tạo"));
            log.debug("Step 7: Found order: {}", order.getCode());
            
            // Set staff cho đơn hàng
            log.debug("Step 7: Setting staff and saving order");
            order.setStaff(staff);
            order.setUpdatedBy(staff.getId());
            orderRepository.save(order);
            log.debug("Step 7: Order saved with staff assignment");
            
            // Chuyển trạng thái từ PENDING sang DELIVERED (sẽ trigger business logic tự động)
            log.debug("Step 7: Updating order status to DELIVERED");
            ApiResponse<OrderResponse> statusUpdateResponse = orderService.updateStatus(order.getId(), OrderStatus.DELIVERED, staff.getId());
            log.debug("Step 7: Status update returned: {}", statusUpdateResponse.getStatus());
            if (statusUpdateResponse.getStatus() != 200) {
                log.error("Step 7: Status update failed: {}", statusUpdateResponse.getMessage());
                throw new BusinessException("Lỗi cập nhật trạng thái đơn hàng: " + statusUpdateResponse.getMessage());
            }
            log.debug("Step 7: Status update completed successfully");
            
            // 8. Lấy order đã được cập nhật và tạo response
            log.debug("Step 8: Building counter sale response");
            Order updatedOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng đã cập nhật"));
            CounterSaleResponse response = buildCounterSaleResponse(updatedOrder, calculation);
            log.debug("Step 8: Response built successfully");
            
            log.info("✅ Counter sale created successfully: orderCode={}, totalAmount={}", 
                updatedOrder.getCode(), updatedOrder.getTotalAmount());
            
            return new ApiResponse<>(200, "Tạo đơn hàng tại quầy thành công", response);
            
        } catch (BusinessException e) {
            log.error("❌ Business error creating counter sale: {}", e.getMessage());
            return new ApiResponse<>(400, "Lỗi nghiệp vụ: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("❌ Unexpected error creating counter sale: {}", e.getMessage(), e);
            // ✅ ENHANCED: Return detailed error message for debugging
            String detailMessage = e.getMessage();
            if (e.getCause() != null) {
                detailMessage += " | Cause: " + e.getCause().getMessage();
            }
            return new ApiResponse<>(500, "Lỗi hệ thống: " + detailMessage, null);
        }
    }
    
    @Override
    public ApiResponse<CounterSaleResponse> calculateCounterSale(CounterSaleRequest request) {
        try {
            log.info("🧮 Calculating counter sale for {} items", request.getOrderDetails().size());
            
            // Validate request
            validateCounterSaleRequest(request);
            
            // Thực hiện tính toán
            CounterSaleResponse calculation = performCounterSaleCalculation(request);
            if (calculation == null) {
                return new ApiResponse<>(400, "Lỗi tính toán đơn hàng", null);
            }
            
            return new ApiResponse<>(200, "Tính toán thành công", calculation);
            
        } catch (BusinessException e) {
            log.error("❌ Business error calculating counter sale: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            log.error("❌ Unexpected error calculating counter sale: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Lỗi hệ thống khi tính toán", null);
        }
    }
    
    @Override
    public ApiResponse<OrderResponse> getCounterSaleDetails(Integer orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + orderId));
            
            // Kiểm tra có phải đơn hàng tại quầy không
            if (!"COUNTER".equals(order.getOrderType())) {
                return new ApiResponse<>(400, "Đây không phải đơn hàng tại quầy", null);
            }
            
            OrderResponse response = orderResponseMapper.toResponse(order);
            return new ApiResponse<>(200, "Lấy thông tin đơn hàng thành công", response);
            
        } catch (BusinessException e) {
            log.error("❌ Business error getting counter sale details: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            log.error("❌ Unexpected error getting counter sale details: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Lỗi hệ thống khi lấy thông tin đơn hàng", null);
        }
    }
    
    @Override
    public ApiResponse<OrderResponse> cancelCounterSale(Integer orderId, Integer staffId, String reason) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + orderId));
            
            // Kiểm tra có phải đơn hàng tại quầy không
            if (!"COUNTER".equals(order.getOrderType())) {
                return new ApiResponse<>(400, "Đây không phải đơn hàng tại quầy", null);
            }
            
            // Kiểm tra thời gian hủy (chỉ được hủy trong 24h)
            long currentTime = System.currentTimeMillis();
            long orderTime = order.getOrderDate();
            long timeDiff = currentTime - orderTime;
            long twentyFourHours = 24 * 60 * 60 * 1000L;
            
            if (timeDiff > twentyFourHours) {
                return new ApiResponse<>(400, "Chỉ có thể hủy đơn hàng tại quầy trong vòng 24 giờ", null);
            }
            
            // Kiểm tra trạng thái có thể hủy
            if (order.getOrderStatus() == OrderStatus.CANCELED || 
                order.getOrderStatus() == OrderStatus.REFUNDED) {
                return new ApiResponse<>(400, "Đơn hàng đã được hủy hoặc hoàn trước đó", null);
            }
            
            // Hủy đơn hàng thông qua OrderService
            ApiResponse<OrderResponse> cancelResponse = orderService.cancelOrder(orderId, reason, staffId);
            
            if (cancelResponse.getStatus() == 200) {
                log.info("✅ Counter sale cancelled successfully: orderCode={}, reason={}", 
                    order.getCode(), reason);
            }
            
            return cancelResponse;
            
        } catch (BusinessException e) {
            log.error("❌ Business error cancelling counter sale: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            log.error("❌ Unexpected error cancelling counter sale: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Lỗi hệ thống khi hủy đơn hàng", null);
        }
    }
    
    // ================= PRIVATE HELPER METHODS =================
    
    private void validateCounterSaleRequest(CounterSaleRequest request) {
        if (request.getOrderDetails() == null || request.getOrderDetails().isEmpty()) {
            throw new BusinessException("Danh sách sản phẩm không được để trống");
        }
        
        if (request.getStaffId() == null) {
            throw new BusinessException("Staff ID không được để trống");
        }
        

        

        // Validate từng sản phẩm
        for (OrderDetailRequest detail : request.getOrderDetails()) {
            if (detail.getBookId() == null) {
                throw new BusinessException("Book ID không được để trống");
            }
            if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                throw new BusinessException("Số lượng phải lớn hơn 0");
            }
            if (detail.getUnitPrice() == null || detail.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Đơn giá phải lớn hơn 0");
            }
        }
    }
    
    private CounterSaleResponse performCounterSaleCalculation(CounterSaleRequest request) {
        CounterSaleResponse response = new CounterSaleResponse();
        List<CounterSaleResponse.CounterSaleItemResponse> items = new ArrayList<>();
        
        BigDecimal subtotal = BigDecimal.ZERO;
        
        // Tính từng item
        for (OrderDetailRequest detail : request.getOrderDetails()) {
            Book book = bookRepository.findById(detail.getBookId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy sách với ID: " + detail.getBookId()));
            
            // Kiểm tra tồn kho
            if (book.getStockQuantity() < detail.getQuantity()) {
                throw new BusinessException("Sách '" + book.getBookName() + "' không đủ tồn kho. Còn lại: " + book.getStockQuantity());
            }
            
            // ✅ BACKEND TÍNH GIÁ: Không tin frontend, tính giá hoàn toàn từ backend
            BigDecimal actualUnitPrice = book.getPrice(); // Giá gốc từ database
            FlashSaleItem flashSaleItem = null;
            
            // Kiểm tra flash sale nếu có
            if (detail.getFlashSaleItemId() != null) {
                flashSaleItem = flashSaleItemRepository.findById(detail.getFlashSaleItemId())
                    .orElse(null);
                
                if (flashSaleItem != null) {
                    // Validate flash sale
                    if (flashSaleItem.getStockQuantity() < detail.getQuantity()) {
                        throw new BusinessException("Flash sale item '" + book.getBookName() + "' không đủ tồn kho. Còn lại: " + flashSaleItem.getStockQuantity());
                    }
                    
                    // Áp dụng giá flash sale
                    actualUnitPrice = flashSaleItem.getDiscountPrice();
                }
            }
            
            // ✅ VALIDATION: So sánh giá frontend với backend (optional - để warning)
            if (detail.getUnitPrice() != null && detail.getUnitPrice().compareTo(actualUnitPrice) != 0) {
                log.warn("⚠️ Price mismatch for book ID {}: Frontend={}, Backend={}", 
                    detail.getBookId(), detail.getUnitPrice(), actualUnitPrice);
            }
            
            // Tạo item response với giá BACKEND tính toán
            CounterSaleResponse.CounterSaleItemResponse itemResponse = new CounterSaleResponse.CounterSaleItemResponse();
            itemResponse.setBookId(book.getId());
            itemResponse.setBookName(book.getBookName());
            itemResponse.setBookCode(book.getBookCode());
            itemResponse.setQuantity(detail.getQuantity());
            itemResponse.setUnitPrice(actualUnitPrice); // ✅ Giá backend tính
            itemResponse.setTotalPrice(actualUnitPrice.multiply(BigDecimal.valueOf(detail.getQuantity())));
            itemResponse.setOriginalPrice(book.getPrice());
            
            if (flashSaleItem != null) {
                itemResponse.setFlashSale(true);
                itemResponse.setFlashSaleItemId(flashSaleItem.getId());
                BigDecimal saved = book.getPrice().subtract(actualUnitPrice).multiply(BigDecimal.valueOf(detail.getQuantity()));
                itemResponse.setSavedAmount(saved);
            } else {
                itemResponse.setFlashSale(false);
                itemResponse.setSavedAmount(BigDecimal.ZERO);
            }
            
            items.add(itemResponse);
            subtotal = subtotal.add(itemResponse.getTotalPrice());
        }
        
        response.setItems(items);
        response.setSubtotal(subtotal);
        response.setDiscountAmount(BigDecimal.ZERO); // TODO: Apply vouchers if needed
        response.setTotalAmount(subtotal.subtract(response.getDiscountAmount()));
        
        return response;
    }
    
    private OrderRequest buildOrderRequestFromCounterSale(CounterSaleRequest counterRequest, User customer) {
        OrderRequest orderRequest = new OrderRequest();
        
        // User info
        if (customer != null) {
            orderRequest.setUserId(customer.getId());
        } else {
            // ✅ SPECIAL CASE: Counter sales can have null userId for walk-in customers
            orderRequest.setUserId(null);
        }
        
        // Không cần địa chỉ cho counter sale  
        orderRequest.setAddressId(null);
        
        // ✅ THÊM: Set thông tin khách hàng cho đơn hàng tại quầy
        if (customer != null) {
            // Nếu có tài khoản thì lấy từ user
            orderRequest.setRecipientName(customer.getFullName());
            orderRequest.setPhoneNumber(customer.getPhoneNumber());
        } else {
            // Nếu là khách vãng lai thì lấy từ request
            orderRequest.setRecipientName(counterRequest.getCustomerName());
            orderRequest.setPhoneNumber(counterRequest.getCustomerPhone());
        }
        
        // Order details
        List<OrderDetailRequest> orderDetails = counterRequest.getOrderDetails().stream()
            .map(detail -> {
                OrderDetailRequest orderDetail = new OrderDetailRequest();
                orderDetail.setBookId(detail.getBookId());
                orderDetail.setQuantity(detail.getQuantity());
                orderDetail.setUnitPrice(detail.getUnitPrice());
                orderDetail.setFlashSaleItemId(detail.getFlashSaleItemId());
                // Set frontend prices for validation - use unitPrice as fallback
                orderDetail.setFrontendPrice(detail.getUnitPrice() != null ? detail.getUnitPrice() : BigDecimal.ZERO);
                orderDetail.setFrontendFlashSalePrice(detail.getUnitPrice());
                orderDetail.setFrontendFlashSaleId(detail.getFlashSaleItemId());
                return orderDetail;
            })
            .collect(Collectors.toList());
        
        orderRequest.setOrderDetails(orderDetails);
        
        // Vouchers
        orderRequest.setVoucherIds(counterRequest.getVoucherIds());
        
        // Financial info
        orderRequest.setSubtotal(counterRequest.getSubtotal());
        orderRequest.setShippingFee(BigDecimal.ZERO); // No shipping for counter sales
        orderRequest.setTotalAmount(counterRequest.getTotalAmount());
            orderRequest.setPaymentMethod(counterRequest.getPaymentMethod()); // Hoặc lấy từ counterRequest nếu có

        // Order type and notes
        orderRequest.setOrderType("COUNTER");
        orderRequest.setNotes(counterRequest.getNotes());
        
        // ✅ FIX: Set staffId for counter sales  
        orderRequest.setStaffId(counterRequest.getStaffId());
        
        return orderRequest;
    }
    
    private CounterSaleResponse buildCounterSaleResponse(Order order, CounterSaleResponse calculation) {
        CounterSaleResponse response = new CounterSaleResponse();
        
        response.setOrderId(order.getId());
        response.setOrderCode(order.getCode());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setOrderType(order.getOrderType());
        
        // Customer info
        if (order.getUser() != null) {
            response.setUserId(order.getUser().getId());
            response.setCustomerName(order.getUser().getFullName());
            response.setCustomerPhone(order.getUser().getPhoneNumber());
        } else {
            response.setUserId(null);
            // ✅ FIX: Get customer name and phone from Order fields for walk-in customers
            response.setCustomerName(order.getRecipientName());
            response.setCustomerPhone(order.getPhoneNumber());
        }
        
        // Financial info
        response.setSubtotal(order.getSubtotal());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod("CASH"); // Default for counter sales
        
        // Staff info
        if (order.getStaff() != null) {
            response.setStaffId(order.getStaff().getId());
            response.setStaffName(order.getStaff().getFullName());
        }
        
        // Other info
        response.setOrderDate(order.getOrderDate());
        response.setNotes(order.getNotes());
        
        // Items from calculation
        response.setItems(calculation.getItems());
        response.setAppliedVouchers(new ArrayList<>()); // TODO: Map vouchers
        
        return response;
    }
}
