package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.request.OrderDetailRefundRequest;
import org.datn.bookstation.dto.request.RefundRequestDto;
import org.datn.bookstation.dto.request.AdminRefundDecisionDto;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.OrderDetailResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.RevenueStatsResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.entity.RefundRequest.RefundStatus;
import org.datn.bookstation.entity.RefundRequest.RefundType;
import org.datn.bookstation.exception.BusinessException;
import org.datn.bookstation.mapper.OrderResponseMapper;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.OrderService;
import org.datn.bookstation.service.PointManagementService;
import org.datn.bookstation.service.VoucherCalculationService;
import org.datn.bookstation.service.FlashSaleService;
import org.datn.bookstation.service.OrderStatisticsService;
import org.datn.bookstation.utils.RefundReasonUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderVoucherRepository orderVoucherRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final AddressRepository addressRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final RefundItemRepository refundItemRepository;
    private final VoucherRepository voucherRepository;
    private final PointManagementService pointManagementService;
    private final OrderResponseMapper orderResponseMapper;
    private final VoucherCalculationService voucherCalculationService;
    private final FlashSaleService flashSaleService;
    private final OrderStatisticsService orderStatisticsService;

    @Override
    public Optional<Integer> findIdByCode(String code) {
        return orderRepository.findIdByCode(code);
    }

    @Override
    public PaginationResponse<OrderResponse> getAllWithPagination(int page, int size, String code,
            Integer userId, OrderStatus orderStatus, String orderType, Long startDate, Long endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (code != null && !code.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")),
                        "%" + code.toLowerCase() + "%"));
            }

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            if (orderStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderStatus"), orderStatus));
            }

            if (orderType != null && !orderType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("orderType")),
                        "%" + orderType.toLowerCase() + "%"));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(orderResponseMapper::toResponse)
                .toList();

        return PaginationResponse.<OrderResponse>builder()
                .content(orderResponses)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order getById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + id));
    }

    @Override
    public OrderResponse getByIdWithDetails(Integer id) {
        Order order = getById(id);

        // Lấy order details với đầy đủ thông tin
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id);

        // Lấy order vouchers
        List<OrderVoucher> orderVouchers = orderVoucherRepository.findByOrderId(id);

        // Sử dụng mapper với details
        OrderResponse response = orderResponseMapper.toResponseWithDetails(order, orderDetails, orderVouchers);

        //  THÊM MỚI: Set thông tin hoàn trả
        setRefundInfoToOrderResponse(response, order);

        return response;
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> create(OrderRequest request) {
        //  MODIFIED: Validate user - allow null for counter sales
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(
                            () -> new BusinessException("Không tìm thấy người dùng với ID: " + request.getUserId()));
        } else if (!"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("User ID là bắt buộc cho đơn hàng online");
        }

        // Validate order type - CHỈ CHO PHÉP "ONLINE" và "COUNTER"
        if (!"ONLINE".equalsIgnoreCase(request.getOrderType()) &&
                !"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("Kiểu đơn hàng chỉ được phép là 'ONLINE' hoặc 'COUNTER'");
        }

        //  MODIFIED: Validate address - allow null for counter sales
        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(
                            () -> new BusinessException("Không tìm thấy địa chỉ với ID: " + request.getAddressId()));
        } else if (!"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("Address ID là bắt buộc cho đơn hàng online");
        }

        //  BACKEND TỰ TÍNH TOÁN SUBTOTAL từ orderDetails - KHÔNG TIN FRONTEND
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        for (var detailRequest : request.getOrderDetails()) {
            BigDecimal itemTotal = detailRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(detailRequest.getQuantity()));
            calculatedSubtotal = calculatedSubtotal.add(itemTotal);
        }

        //  TỰ TÍNH VOUCHER DISCOUNT (nếu có)
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountShipping = BigDecimal.ZERO;

        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            // Tạo order tạm để tính voucher
            Order tempOrder = new Order();
            tempOrder.setSubtotal(calculatedSubtotal);
            tempOrder.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);

            try {
                VoucherCalculationService.VoucherCalculationResult voucherResult = voucherCalculationService
                        .calculateVoucherDiscount(tempOrder, request.getVoucherIds(), request.getUserId());
                discountAmount = voucherResult.getTotalProductDiscount();
                discountShipping = voucherResult.getTotalShippingDiscount();
            } catch (Exception e) {
                throw new BusinessException("Lỗi tính toán voucher: " + e.getMessage());
            }
        }

        //  TỰ TÍNH TOTAL AMOUNT - KHÔNG TIN FRONTEND
        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal calculatedTotalAmount = calculatedSubtotal.add(shippingFee).subtract(discountAmount)
                .subtract(discountShipping);
        calculatedTotalAmount = calculatedTotalAmount.max(BigDecimal.ZERO); // Không âm

        // Create order
        Order order = new Order();
        order.setCode(generateOrderCode());
        order.setUser(user);
        order.setAddress(address);
        order.setOrderType(request.getOrderType().toUpperCase());
        order.setPaymentMethod(request.getPaymentMethod()); // THÊM MỚI
        
        // AUTO-SET CONFIRMED STATUS FOR VNPAY PAYMENTS
        String paymentMethod = request.getPaymentMethod();
        if ("VNPay".equals(paymentMethod) || "VNPAY".equalsIgnoreCase(paymentMethod)) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            log.info("Auto-setting order status to CONFIRMED for VNPAY payment: {}", order.getCode());
        } else {
            order.setOrderStatus(request.getOrderStatus());
        }
        order.setOrderDate(System.currentTimeMillis());
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setDiscountShipping(discountShipping);
        order.setSubtotal(calculatedSubtotal); //  Dùng giá trị backend tính
        order.setTotalAmount(calculatedTotalAmount); //  Dùng giá trị backend tính
        order.setNotes(request.getNotes());

        //  THÊM: Set thông tin người nhận cho đơn hàng tại quầy
        if ("COUNTER".equalsIgnoreCase(request.getOrderType())) {
            order.setRecipientName(request.getRecipientName());
            order.setPhoneNumber(request.getPhoneNumber());
        }

        //  FIX: Set createdBy properly for counter sales
        if (user != null) {
            order.setCreatedBy(user.getId()); // Online order - use customer ID
        } else if (request.getStaffId() != null) {
            order.setCreatedBy(request.getStaffId()); // Counter sales - use staff ID
        } else {
            throw new BusinessException("Phải có staffId cho đơn hàng counter sales");
        }

        if (request.getStaffId() != null) {
            User staff = userRepository.findById(request.getStaffId())
                    .orElseThrow(
                            () -> new BusinessException("Không tìm thấy nhân viên với ID: " + request.getStaffId()));
            order.setStaff(staff);
        }

        order = orderRepository.save(order);

        // Create order details
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (var detailRequest : request.getOrderDetails()) {
            Book book = bookRepository.findById(detailRequest.getBookId())
                    .orElseThrow(
                            () -> new BusinessException("Không tìm thấy sách với ID: " + detailRequest.getBookId()));

            //  ENHANCED: Xử lý logic flash sale và mixed purchase
            FlashSaleItem flashSaleItem = null;
            int quantityToOrder = detailRequest.getQuantity();

            if (detailRequest.getFlashSaleItemId() != null) {
                // Trường hợp 1: Frontend đã chỉ định flash sale item
                flashSaleItem = flashSaleItemRepository.findById(detailRequest.getFlashSaleItemId())
                        .orElseThrow(() -> new BusinessException(
                                "Không tìm thấy flash sale item với ID: " + detailRequest.getFlashSaleItemId()));

                //  Validate flash sale purchase limit per user
                if (!flashSaleService.canUserPurchaseMore(flashSaleItem.getId().longValue(), request.getUserId(),
                        quantityToOrder)) {
                    int currentPurchased = flashSaleService.getUserPurchasedQuantity(flashSaleItem.getId().longValue(),
                            request.getUserId());
                    int maxAllowed = flashSaleItem.getMaxPurchasePerUser();
                    throw new BusinessException("Bạn đã mua " + currentPurchased + "/" + maxAllowed +
                            " sản phẩm flash sale này. Không thể mua thêm " + quantityToOrder + " sản phẩm.");
                }

                // Validate flash sale stock
                if (flashSaleItem.getStockQuantity() < quantityToOrder) {
                    throw new BusinessException("Không đủ số lượng flash sale cho sản phẩm: " + book.getBookName() +
                            " (Flash sale còn: " + flashSaleItem.getStockQuantity() + ", Yêu cầu: " + quantityToOrder
                            + ")");
                }

                // Trừ flash sale stock
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() - quantityToOrder);
                flashSaleItemRepository.save(flashSaleItem);

                // Trừ book stock (vì sách flash sale cũng tính vào tổng stock)
                book.setStockQuantity(book.getStockQuantity() - quantityToOrder);
                bookRepository.save(book);

            } else {
                // Trường hợp 2: Không chỉ định flash sale - kiểm tra tự động
                // Tìm flash sale active cho book này
                Optional<FlashSaleItem> activeFlashSaleOpt = flashSaleItemRepository
                        .findActiveFlashSalesByBookId(book.getId().longValue(), System.currentTimeMillis())
                        .stream()
                        .findFirst();

                if (activeFlashSaleOpt.isPresent()) {
                    FlashSaleItem activeFlashSale = activeFlashSaleOpt.get();
                    int flashSaleStock = activeFlashSale.getStockQuantity();

                    if (flashSaleStock >= quantityToOrder) {
                        //  ENHANCED: Validate flash sale purchase limit với hai loại thông báo
                        if (!flashSaleService.canUserPurchaseMore(activeFlashSale.getId().longValue(),
                                request.getUserId(), quantityToOrder)) {
                            int currentPurchased = flashSaleService
                                    .getUserPurchasedQuantity(activeFlashSale.getId().longValue(), request.getUserId());
                            int maxAllowed = activeFlashSale.getMaxPurchasePerUser();

                            //  LOẠI 1: Đã đạt giới hạn tối đa
                            if (currentPurchased >= maxAllowed) {
                                throw new BusinessException("Bạn đã mua đủ " + maxAllowed + " sản phẩm flash sale '" +
                                        book.getBookName() + "' cho phép. Không thể đặt hàng thêm.");
                            }

                            //  LOẠI 2: Chưa đạt giới hạn nhưng đặt quá số lượng cho phép
                            int remainingAllowed = maxAllowed - currentPurchased;
                            if (quantityToOrder > remainingAllowed) {
                                throw new BusinessException("Bạn đã mua " + currentPurchased
                                        + " sản phẩm, chỉ được mua thêm tối đa " +
                                        remainingAllowed + " sản phẩm flash sale '" + book.getBookName() + "'.");
                            }

                            //  LOẠI 3: Thông báo chung
                            throw new BusinessException(
                                    "Bạn chỉ được mua tối đa " + maxAllowed + " sản phẩm flash sale '" +
                                            book.getBookName() + "'.");
                        }

                        // Đủ flash sale stock - dùng toàn bộ flash sale
                        flashSaleItem = activeFlashSale;
                        flashSaleItem.setStockQuantity(flashSaleStock - quantityToOrder);
                        flashSaleItemRepository.save(flashSaleItem);

                        // Cập nhật unit price về flash sale price
                        detailRequest.setUnitPrice(activeFlashSale.getDiscountPrice());

                        log.info(" Auto-applied flash sale for book {}: {} items at price {}",
                                book.getId(), quantityToOrder, activeFlashSale.getDiscountPrice());
                    } else if (flashSaleStock > 0) {
                        // Không đủ flash sale stock - KHÔNG hỗ trợ mixed purchase trong
                        // OrderServiceImpl
                        // Để tránh phức tạp, báo lỗi để frontend xử lý
                        throw new BusinessException("Flash sale chỉ còn " + flashSaleStock + " sản phẩm. " +
                                "Vui lòng đặt " + flashSaleStock + " sản phẩm flash sale trong đơn riêng.");
                    }
                    // Nếu flashSaleStock = 0, không áp dụng flash sale
                }

                // Validate và trừ book stock thông thường
                if (book.getStockQuantity() < quantityToOrder) {
                    throw new BusinessException("Không đủ số lượng tồn kho cho sản phẩm: " + book.getBookName() +
                            " (Tồn kho: " + book.getStockQuantity() + ", Yêu cầu: " + quantityToOrder + ")");
                }

                book.setStockQuantity(book.getStockQuantity() - quantityToOrder);
                bookRepository.save(book);
            }

            // Tạo OrderDetail
            OrderDetail orderDetail = new OrderDetail();
            OrderDetailId orderDetailId = new OrderDetailId();
            orderDetailId.setOrderId(order.getId());
            orderDetailId.setBookId(book.getId());
            orderDetail.setId(orderDetailId);
            orderDetail.setOrder(order);
            orderDetail.setBook(book);
            orderDetail.setFlashSaleItem(flashSaleItem); // null nếu không phải flash sale
            orderDetail.setQuantity(quantityToOrder);
            orderDetail.setUnitPrice(detailRequest.getUnitPrice());

            //  FIX: Set createdBy properly for counter sales
            if (order.getUser() != null) {
                orderDetail.setCreatedBy(order.getUser().getId()); // Online order
            } else {
                orderDetail.setCreatedBy(order.getCreatedBy()); // Counter sales - use same as order
            }

            orderDetails.add(orderDetail);
        }

        orderDetailRepository.saveAll(orderDetails);

        //  CẬP NHẬT VOUCHER USAGE VÀ LƯU ORDERV OUCHER ENTITIES (nếu có sử dụng
        // voucher)
        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            try {
                // 1. Update voucher usage counts
                voucherCalculationService.updateVoucherUsage(request.getVoucherIds(), request.getUserId());

                // 2.  FIX: Save OrderVoucher entities để vouchers hiển thị trong API responses
                saveOrderVouchers(order, request.getVoucherIds(), calculatedSubtotal, shippingFee);

                // 3. Update voucher count trong order theo số lượng discount đã tính
                order.setRegularVoucherCount(discountAmount.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
                order.setShippingVoucherCount(discountShipping.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
                orderRepository.save(order);

            } catch (Exception e) {
                log.warn("Không thể cập nhật voucher usage: {}", e.getMessage());
            }
        }

        OrderResponse response = orderResponseMapper.toResponse(order);
        return new ApiResponse<>(HttpStatus.CREATED.value(), "Tạo đơn hàng thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> update(OrderRequest request, Integer id) {
        Order existingOrder = getById(id);

        // Only allow updates for PENDING orders
        if (existingOrder.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể cập nhật đơn hàng ở trạng thái PENDING");
        }

        // Validate order type
        if (!"ONLINE".equalsIgnoreCase(request.getOrderType()) &&
                !"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("Kiểu đơn hàng chỉ được phép là 'ONLINE' hoặc 'COUNTER'");
        }

        // Update order fields
        existingOrder.setOrderType(request.getOrderType().toUpperCase());
        existingOrder.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        existingOrder.setSubtotal(request.getSubtotal() != null ? request.getSubtotal() : BigDecimal.ZERO);
        existingOrder.setTotalAmount(request.getTotalAmount());
        existingOrder.setNotes(request.getNotes());
        existingOrder.setUpdatedBy(request.getUserId());

        Order updatedOrder = orderRepository.save(existingOrder);
        OrderResponse response = orderResponseMapper.toResponse(updatedOrder);

        return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật đơn hàng thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updateStatus(Integer id, OrderStatus newStatus, Integer staffId) {
        Order order = getById(id);
        OrderStatus oldStatus = order.getOrderStatus();

        // Update status
        order.setOrderStatus(newStatus);

        if (staffId != null) {
            User staff = userRepository.findById(staffId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên với ID: " + staffId));
            order.setStaff(staff);
            order.setUpdatedBy(staffId);
        }

        // Handle business logic based on status change
        handleStatusChangeBusinessLogic(order, oldStatus, newStatus);

        Order updatedOrder = orderRepository.save(order);
        OrderResponse response = orderResponseMapper.toResponse(updatedOrder);

        return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật trạng thái đơn hàng thành công", response);
    }

    @Override
    public void delete(Integer id) {
        Order order = getById(id);

        // Only allow deletion of PENDING or CANCELED orders
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CANCELED) {
            throw new BusinessException("Chỉ có thể xóa đơn hàng ở trạng thái PENDING hoặc CANCELED");
        }

        orderRepository.delete(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(Integer userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(order -> {
                    //  SỬA: Lấy chi tiết đầy đủ như API getByIdWithDetails
                    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                    List<OrderVoucher> orderVouchers = orderVoucherRepository.findByOrderId(order.getId());

                    OrderResponse response = orderResponseMapper.toResponseWithDetails(order, orderDetails,
                            orderVouchers);
                    setRefundInfoToOrderResponse(response, order);

                    return response;
                })
                .toList();
    }

    @Override
    public PaginationResponse<OrderResponse> getOrdersByUserWithPagination(Integer userId, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với ID: " + userId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Order> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"),
                userId);

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(order -> {
                    //  SỬA: Lấy chi tiết đầy đủ như API getByIdWithDetails
                    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                    List<OrderVoucher> orderVouchers = orderVoucherRepository.findByOrderId(order.getId());

                    OrderResponse response = orderResponseMapper.toResponseWithDetails(order, orderDetails,
                            orderVouchers);
                    setRefundInfoToOrderResponse(response, order);

                    return response;
                })
                .toList();

        return PaginationResponse.<OrderResponse>builder()
                .content(orderResponses)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatusOrderByCreatedAtDesc(status);
        return orders.stream()
                .map(orderResponseMapper::toResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getProcessingOrdersByBookId(Integer bookId) {
        // Sử dụng trạng thái processing từ BookProcessingQuantityService
        List<OrderStatus> processingStatuses = List.of(
                OrderStatus.PENDING, // Chờ xử lý
                OrderStatus.CONFIRMED, // Đã xác nhận
                OrderStatus.SHIPPED, // Đang giao hàng
                OrderStatus.DELIVERY_FAILED, // Giao hàng thất bại
                OrderStatus.REDELIVERING, // Đang giao lại
                OrderStatus.RETURNING_TO_WAREHOUSE, // Đang trả về kho
                OrderStatus.REFUND_REQUESTED, // Yêu cầu hoàn trả
                OrderStatus.AWAITING_GOODS_RETURN, // Đang chờ lấy hàng hoàn trả
                OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER, // Đã nhận hàng hoàn trả từ khách
                OrderStatus.GOODS_RETURNED_TO_WAREHOUSE, // Hàng đã về kho
                OrderStatus.REFUNDING // Đang hoàn tiền
        );

        List<Order> processingOrders = orderDetailRepository.findProcessingOrdersByBookId(bookId, processingStatuses);
        return processingOrders.stream()
                .map(orderResponseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> cancelOrder(Integer id, String reason, Integer userId) {
        Order order = getById(id);

        // Validate that order can be canceled
        if (order.getOrderStatus() == OrderStatus.DELIVERED ||
                order.getOrderStatus() == OrderStatus.CANCELED ||
                order.getOrderStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }

        // Validate user authorization
        if (userId != null && !order.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền hủy đơn hàng này");
        }

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELED);
        order.setCancelReason(reason);
        order.setUpdatedBy(userId);

        // Handle cancellation business logic
        handleCancellationBusinessLogic(order, oldStatus);

        Order canceledOrder = orderRepository.save(order);
        OrderResponse response = orderResponseMapper.toResponse(canceledOrder);

        return new ApiResponse<>(HttpStatus.OK.value(), "Hủy đơn hàng thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> partialRefund(Integer orderId, Integer userId, String reason,
            List<OrderDetailRefundRequest> refundDetails) {

        Order order = getById(orderId);

        // Validate order status - Cho phép REFUNDING từ admin approval process
        if (order.getOrderStatus() != OrderStatus.DELIVERED &&
                order.getOrderStatus() != OrderStatus.REFUNDING) {
            throw new BusinessException("Chỉ có thể hoàn trả đơn hàng đã giao hoặc đã được phê duyệt hoàn trả");
        }

        // Validate user authorization
        if (userId != null && !order.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền hoàn trả đơn hàng này");
        }

        // Calculate refund amount and update order details
        BigDecimal totalRefundAmount = handlePartialRefundBusinessLogic(order, refundDetails, reason);

        // Update order status
        order.setOrderStatus(OrderStatus.PARTIALLY_REFUNDED);
        order.setCancelReason(reason);
        order.setUpdatedBy(userId);

        // Deduct points if customer earned points from this order
        pointManagementService.deductPointsFromPartialRefund(totalRefundAmount, order, order.getUser());

        Order refundedOrder = orderRepository.save(order);
        OrderResponse response = orderResponseMapper.toResponse(refundedOrder);

        return new ApiResponse<>(HttpStatus.OK.value(), "Hoàn trả một phần đơn hàng thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> fullRefund(Integer orderId, Integer userId, String reason) {

        Order order = getById(orderId);

        // Validate order status - Cho phép REFUNDING từ admin approval process
        if (order.getOrderStatus() != OrderStatus.DELIVERED &&
                order.getOrderStatus() != OrderStatus.SHIPPED &&
                order.getOrderStatus() != OrderStatus.REFUNDING) {
            throw new BusinessException(
                    "Chỉ có thể hoàn trả đơn hàng đã giao, đang vận chuyển hoặc đã được phê duyệt hoàn trả");
        }

        // Validate user authorization
        if (userId != null && !order.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền hoàn trả đơn hàng này");
        }

        // Handle full refund business logic
        handleFullRefundBusinessLogic(order, reason);

        // Update order status
        order.setOrderStatus(OrderStatus.REFUNDED);
        order.setCancelReason(reason);
        order.setUpdatedBy(userId);

        // Deduct all points earned from this order
        pointManagementService.deductPointsFromCancelledOrder(order, order.getUser());

        Order refundedOrder = orderRepository.save(order);
        OrderResponse response = orderResponseMapper.toResponse(refundedOrder);

        return new ApiResponse<>(HttpStatus.OK.value(), "Hoàn trả toàn bộ đơn hàng thành công", response);
    }

    // ================== PRIVATE HELPER METHODS ==================

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }

    private void handleStatusChangeBusinessLogic(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        User user = order.getUser();

        switch (newStatus) {
            case CONFIRMED:
                if (oldStatus == OrderStatus.PENDING) {
                    log.info("Order {} confirmed", order.getCode());
                }
                break;

            case SHIPPED:
                if (oldStatus == OrderStatus.CONFIRMED) {
                    log.info("Order {} shipped", order.getCode());
                }
                break;

            case DELIVERED:
                // CHÍNH THỨC CỘNG SỐ LƯỢNG ĐÃ BÁN KHI GIAO THÀNH CÔNG
                handleDeliveredBusinessLogic(order);

                //  Award points khi đơn hàng DELIVERED (không chỉ từ SHIPPED)
                // Đảm bảo chỉ tích điểm 1 lần
                pointManagementService.earnPointsFromOrder(order, user);
                log.info("Order {} delivered successfully, sold count updated, points awarded", order.getCode());
                break;

            case DELIVERY_FAILED:
                //  KHÔI PHỤC STOCK KHI GIAO HÀNG THẤT BẠI
                handleDeliveryFailedBusinessLogic(order, oldStatus);
                log.info("Order {} delivery failed, stock restored", order.getCode());
                break;

            case CANCELED:
                handleCancellationBusinessLogic(order, oldStatus);
                break;

            default:
                log.info("Order {} status changed from {} to {}", order.getCode(), oldStatus, newStatus);
        }
    }

    private void handleCancellationBusinessLogic(Order order, OrderStatus oldStatus) {
        //  Restore stock for canceled orders - cả book thông thường và flash sale
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            if (detail.getFlashSaleItem() != null) {
                //  CHỈ restore flash sale stock (không cần trừ sold count)
                FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() + detail.getQuantity());
                // KHÔNG cần trừ sold count vì khi tạo đơn chưa cộng
                flashSaleItemRepository.save(flashSaleItem);
            } else {
                //  CHỈ restore book stock (không cần trừ sold count)
                Book book = detail.getBook();
                book.setStockQuantity(book.getStockQuantity() + detail.getQuantity());
                // KHÔNG cần trừ sold count vì khi tạo đơn chưa cộng
                bookRepository.save(book);
            }
        }

        // Restore voucher usage if applicable
        if (order.getRegularVoucherCount() > 0 || order.getShippingVoucherCount() > 0) {
            // Would need voucher restoration logic here
            log.info("Order {} canceled, voucher usage should be restored", order.getCode());
        }

        log.info("Order {} canceled, stock restored", order.getCode());
    }

    /**
     *  LOGIC NGHIỆP VỤ KHI ĐƠN HÀNG ĐƯỢC GIAO THÀNH CÔNG
     * - CHÍNH THỨC cộng số lượng đã bán cho cả Book và FlashSaleItem
     * - Chỉ gọi khi chuyển sang DELIVERED
     */
    private void handleDeliveredBusinessLogic(Order order) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            if (detail.getFlashSaleItem() != null) {
                //  Cộng sold count cho flash sale item
                FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                int currentSoldCount = flashSaleItem.getSoldCount() != null ? flashSaleItem.getSoldCount() : 0;
                flashSaleItem.setSoldCount(currentSoldCount + detail.getQuantity());
                flashSaleItemRepository.save(flashSaleItem);

                log.info("FlashSaleItem {} sold count updated: +{} = {}",
                        flashSaleItem.getId(), detail.getQuantity(), flashSaleItem.getSoldCount());
            }

            //  Cộng sold count cho book (cả flash sale và regular)
            Book book = detail.getBook();
            int currentBookSoldCount = book.getSoldCount() != null ? book.getSoldCount() : 0;
            book.setSoldCount(currentBookSoldCount + detail.getQuantity());
            bookRepository.save(book);

            log.info("Book {} sold count updated: +{} = {}",
                    book.getId(), detail.getQuantity(), book.getSoldCount());
        }

        log.info("Order {} delivered successfully, all sold counts updated", order.getCode());
    }

    /**
     *  LOGIC NGHIỆP VỤ KHI GIAO HÀNG THẤT BẠI
     * - Khôi phục stock về số lượng ban đầu (vì khi tạo đơn đã trừ stock)
     * - KHÔNG cần trừ sold count (vì khi tạo đơn chưa cộng sold count)
     */
    private void handleDeliveryFailedBusinessLogic(Order order, OrderStatus oldStatus) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            if (detail.getFlashSaleItem() != null) {
                //  CHỈ restore flash sale stock (không cần trừ sold count)
                FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() + detail.getQuantity());
                // KHÔNG cần trừ sold count vì khi tạo đơn chưa cộng
                flashSaleItemRepository.save(flashSaleItem);

                log.info("FlashSaleItem {} stock restored: +{}",
                        flashSaleItem.getId(), detail.getQuantity());
            }

            //  CHỈ restore book stock (không cần trừ sold count)
            Book book = detail.getBook();
            book.setStockQuantity(book.getStockQuantity() + detail.getQuantity());
            // KHÔNG cần trừ sold count vì khi tạo đơn chưa cộng
            bookRepository.save(book);

            log.info("Book {} stock restored: +{}",
                    book.getId(), detail.getQuantity());
        }

        log.info("Order {} delivery failed, stock restored", order.getCode());
    }

    private BigDecimal handlePartialRefundBusinessLogic(Order order, List<OrderDetailRefundRequest> refundDetails,
            String reason) {
        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        for (OrderDetailRefundRequest refundDetail : refundDetails) {
            // Find order detail by orderId and bookId
            OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndBookId(order.getId(),
                    refundDetail.getBookId());
            if (orderDetail == null) {
                throw new BusinessException(
                        "Không tìm thấy chi tiết đơn hàng cho sách ID: " + refundDetail.getBookId());
            }

            // Validate refund quantity
            if (refundDetail.getRefundQuantity() > orderDetail.getQuantity()) {
                throw new BusinessException("Số lượng hoàn trả vượt quá số lượng đã mua");
            }

            // Calculate refund amount for this detail
            BigDecimal unitRefundAmount = orderDetail.getUnitPrice();
            BigDecimal detailRefundAmount = unitRefundAmount
                    .multiply(BigDecimal.valueOf(refundDetail.getRefundQuantity()));
            totalRefundAmount = totalRefundAmount.add(detailRefundAmount);

            //  KHÔNG cộng stock ở đây nữa - chỉ khi admin đổi trạng thái về
            // GOODS_RETURNED_TO_WAREHOUSE
            log.info("Partial refund calculated for book {}: quantity={}, amount={}",
                    refundDetail.getBookId(), refundDetail.getRefundQuantity(), detailRefundAmount);

            // Update order detail quantity
            orderDetail.setQuantity(orderDetail.getQuantity() - refundDetail.getRefundQuantity());
            orderDetailRepository.save(orderDetail);
        }

        return totalRefundAmount;
    }

    private void handleFullRefundBusinessLogic(Order order, String reason) {
        //  KHÔNG cộng stock ở đây nữa - chỉ khi admin đổi trạng thái về
        // GOODS_RETURNED_TO_WAREHOUSE
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

        // Tính toán tổng số lượng để log
        int totalQuantity = orderDetails.stream().mapToInt(OrderDetail::getQuantity).sum();
        log.info(
                "Full refund processed for order {}: {} items. Stock will be restored when admin changes status to GOODS_RETURNED_TO_WAREHOUSE",
                order.getCode(), totalQuantity);

        // Restore voucher usage if applicable
        if (order.getRegularVoucherCount() > 0 || order.getShippingVoucherCount() > 0) {
            // Would need voucher restoration logic here
            log.info("Order {} fully refunded, voucher usage should be restored", order.getCode());
        }

        log.info("Order {} fully refunded, stock will be restored separately", order.getCode());
    }

    /**
     *  THÊM MỚI: Khách hàng gửi yêu cầu hoàn trả
     *  FIXED: Tạo RefundRequest record trong database
     */
    @Override
    @Transactional
    public ApiResponse<OrderResponse> requestRefund(Integer orderId, RefundRequestDto refundRequest) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + orderId));

            // Kiểm tra quyền truy cập
            if (!order.getUser().getId().equals(refundRequest.getUserId().intValue())) {
                throw new BusinessException("Bạn không có quyền hoàn trả đơn hàng này");
            }

            // Kiểm tra trạng thái đơn hàng
            if (order.getOrderStatus() != OrderStatus.DELIVERED) {
                throw new BusinessException("Chỉ có thể yêu cầu hoàn trả đơn hàng đã giao thành công");
            }

            //  SOLUTION: Tạo RefundRequest record thông qua RefundService
            RefundRequest newRefundRequest = new RefundRequest();
            newRefundRequest.setOrder(order);
            newRefundRequest.setUser(order.getUser());
            newRefundRequest.setReason(refundRequest.getReason());
            newRefundRequest.setRefundType(RefundType.PARTIAL); // Default, có thể điều chỉnh theo logic
            newRefundRequest.setStatus(RefundStatus.PENDING);
            newRefundRequest.setCreatedAt(System.currentTimeMillis());

            // Set evidence images/videos if provided
            if (refundRequest.getEvidenceImages() != null) {
                newRefundRequest.setEvidenceImages(new ArrayList<>(refundRequest.getEvidenceImages()));
            }
            if (refundRequest.getEvidenceVideos() != null) {
                newRefundRequest.setEvidenceVideos(new ArrayList<>(refundRequest.getEvidenceVideos()));
            }

            // Save RefundRequest first
            RefundRequest savedRefundRequest = refundRequestRepository.save(newRefundRequest);

            //  Tạo RefundItem records cho từng sản phẩm
            if (refundRequest.getRefundDetails() != null) {
                List<RefundItem> refundItems = new ArrayList<>();
                for (OrderDetailRefundRequest detail : refundRequest.getRefundDetails()) {
                    OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndBookId(orderId, detail.getBookId());
                    if (orderDetail == null) {
                        throw new BusinessException("Không tìm thấy sản phẩm trong đơn hàng");
                    }

                    RefundItem refundItem = new RefundItem();
                    refundItem.setRefundRequest(savedRefundRequest);
                    refundItem.setBook(orderDetail.getBook());
                    refundItem.setRefundQuantity(detail.getRefundQuantity());
                    refundItem.setUnitPrice(orderDetail.getUnitPrice());
                    refundItem.setTotalAmount(
                            orderDetail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getRefundQuantity())));
                    refundItem.setCreatedAt(System.currentTimeMillis());

                    refundItems.add(refundItem);
                }
                refundItemRepository.saveAll(refundItems);
            }

            // Cập nhật trạng thái đơn hàng
            order.setOrderStatus(OrderStatus.REFUND_REQUESTED);
            order.setCancelReason(refundRequest.getReason());
            order.setUpdatedBy(refundRequest.getUserId().intValue());
            orderRepository.save(order);

            log.info("Customer {} requested refund for order {} - RefundRequest ID: {}",
                    refundRequest.getUserId(), order.getCode(), savedRefundRequest.getId());

            OrderResponse response = orderResponseMapper.toResponse(order);
            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Yêu cầu hoàn trả đã được gửi thành công. Admin sẽ xem xét và phản hồi sớm nhất.",
                    response);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error requesting refund for order {}: {}", orderId, e.getMessage(), e);
            throw new BusinessException("Có lỗi xảy ra khi gửi yêu cầu hoàn trả: " + e.getMessage());
        }
    }

    /**
     *  THÊM MỚI: Admin chấp nhận yêu cầu hoàn trả
     */
    @Override
    @Transactional
    public ApiResponse<OrderResponse> approveRefundRequest(AdminRefundDecisionDto decision) {
        try {
            Order order = orderRepository.findById(decision.getOrderId().intValue())
                    .orElseThrow(
                            () -> new BusinessException("Không tìm thấy đơn hàng với ID: " + decision.getOrderId()));

            // Kiểm tra trạng thái đơn hàng
            if (order.getOrderStatus() != OrderStatus.REFUND_REQUESTED) {
                throw new BusinessException("Đơn hàng không ở trạng thái chờ xem xét hoàn trả");
            }

            //  FIX: Admin chấp nhận -> CHỈ chuyển sang REFUNDING (không auto REFUNDED)
            // Admin sẽ manual chuyển sau khi đã xử lý đầy đủ
            order.setOrderStatus(OrderStatus.REFUNDING);
            order.setUpdatedBy(decision.getAdminId().intValue());
            orderRepository.save(order);

            // Thực hiện logic hoàn trả toàn bộ
            handleFullRefundBusinessLogic(order, decision.getAdminNotes());

            // Trừ điểm khách hàng
            if (order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                // TODO: Implement deductPointsForRefund method in PointManagementService
                log.info("Should deduct points for user {} amount {}", order.getUser().getId(), order.getTotalAmount());
            }

            //  REMOVED: Không tự động chuyển thành REFUNDED nữa
            // Admin sẽ sử dụng Order Status Transition API để chuyển thành:
            // REFUNDING → GOODS_RECEIVED_FROM_CUSTOMER → GOODS_RETURNED_TO_WAREHOUSE →
            // REFUNDED

            log.info(
                    " Admin {} approved refund for order {} - Status: REFUNDING (admin must manually transition to complete)",
                    decision.getAdminId(), order.getCode());

            OrderResponse response = orderResponseMapper.toResponse(order);
            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Yêu cầu hoàn trả đã được chấp nhận. Admin cần chuyển trạng thái đơn hàng để hoàn thành quy trình.",
                    response);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error approving refund for order {}: {}", decision.getOrderId(), e.getMessage(), e);
            throw new BusinessException("Có lỗi xảy ra khi chấp nhận hoàn trả: " + e.getMessage());
        }
    }

    /**
     *  THÊM MỚI: Admin từ chối yêu cầu hoàn trả
     */
    @Override
    @Transactional
    public ApiResponse<OrderResponse> rejectRefundRequest(AdminRefundDecisionDto decision) {
        try {
            Order order = orderRepository.findById(decision.getOrderId().intValue())
                    .orElseThrow(
                            () -> new BusinessException("Không tìm thấy đơn hàng với ID: " + decision.getOrderId()));

            // Kiểm tra trạng thái đơn hàng
            if (order.getOrderStatus() != OrderStatus.REFUND_REQUESTED) {
                throw new BusinessException("Đơn hàng không ở trạng thái chờ xem xét hoàn trả");
            }

            // Admin từ chối -> chuyển về DELIVERED
            order.setOrderStatus(OrderStatus.DELIVERED);
            order.setCancelReason(decision.getAdminNotes()); // Lưu lý do từ chối
            order.setUpdatedBy(decision.getAdminId().intValue());
            orderRepository.save(order);

            log.info("Admin {} rejected refund for order {}", decision.getAdminId(), order.getCode());

            OrderResponse response = orderResponseMapper.toResponse(order);
            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Yêu cầu hoàn trả đã bị từ chối",
                    response);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error rejecting refund for order {}: {}", decision.getOrderId(), e.getMessage(), e);
            throw new BusinessException("Có lỗi xảy ra khi từ chối hoàn trả: " + e.getMessage());
        }
    }

    @Override
    public OrderResponse getOrderDetailById(Integer id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
            return null;
        // Lấy danh sách sản phẩm và voucher của đơn hàng
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id);
        List<org.datn.bookstation.entity.OrderVoucher> orderVouchers = orderVoucherRepository.findByOrderId(id);
        // Map sang DTO
        OrderResponse response = orderResponseMapper.toResponseWithDetails(order, orderDetails, orderVouchers);

        //  THÊM MỚI: Set thông tin hoàn trả
        setRefundInfoToOrderResponse(response, order);

        return response;
    }

    /**
     *  THÊM MỚI: Set thông tin hoàn trả cho OrderResponse
     */
    private void setRefundInfoToOrderResponse(OrderResponse orderResponse, Order order) {
        // Kiểm tra trạng thái hoàn trả
        if (order.getOrderStatus() == OrderStatus.PARTIALLY_REFUNDED) {
            orderResponse.setRefundType("PARTIAL");
        } else if (order.getOrderStatus() == OrderStatus.REFUNDED) {
            orderResponse.setRefundType("FULL");
        }

        // Lấy thông tin hoàn trả từ RefundRequest entity
        List<RefundRequest> refundRequests = refundRequestRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());

        // Lọc những request đã approved
        List<RefundRequest> approvedRefunds = refundRequests.stream()
                .filter(r -> r.getStatus() == RefundStatus.APPROVED || r.getStatus() == RefundStatus.COMPLETED)
                .toList();

        if (!approvedRefunds.isEmpty()) {
            RefundRequest latestRefund = approvedRefunds.get(approvedRefunds.size() - 1);
            orderResponse.setTotalRefundedAmount(latestRefund.getTotalRefundAmount());
            orderResponse.setRefundReason(latestRefund.getReason());
            orderResponse.setRefundReasonDisplay(RefundReasonUtil.getReasonDisplayName(latestRefund.getReason())); // ✅
                                                                                                                   // THÊM
            orderResponse.setRefundDate(latestRefund.getApprovedAt());
            if (latestRefund.getApprovedBy() != null) {
                orderResponse.setRefundedByStaff(latestRefund.getApprovedBy().getId());
                orderResponse.setRefundedByStaffName(latestRefund.getApprovedBy().getFullName());
            }
        }

        //  Set thông tin hoàn trả cho từng order detail
        if (orderResponse.getOrderDetails() != null) {
            for (OrderDetailResponse detail : orderResponse.getOrderDetails()) {
                setRefundInfoToOrderDetail(detail, order.getId());
            }
        }
    }

    /**
     *  SỬA: Set thông tin hoàn trả cho OrderDetailResponse
     */
    private void setRefundInfoToOrderDetail(OrderDetailResponse detail, Integer orderId) {
        // Lấy tất cả RefundItem cho sản phẩm này trong đơn hàng
        List<RefundItem> refundItems = refundItemRepository.findByOrderIdAndBookId(orderId, detail.getBookId());

        if (!refundItems.isEmpty()) {
            // Tính tổng số lượng và số tiền đã hoàn
            int totalRefundedQuantity = refundItems.stream()
                    .mapToInt(RefundItem::getRefundQuantity)
                    .sum();

            BigDecimal totalRefundedAmount = refundItems.stream()
                    .map(RefundItem::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            detail.setRefundedQuantity(totalRefundedQuantity);
            detail.setRefundedAmount(totalRefundedAmount);

            //  SỬA: Lấy lý do từ RefundRequest (dropdown user chọn), không phải từ
            // RefundItem
            RefundItem latestItem = refundItems.get(refundItems.size() - 1);
            RefundRequest refundRequest = latestItem.getRefundRequest();

            // Lý do chính từ dropdown user chọn
            detail.setRefundReason(refundRequest.getReason());
            detail.setRefundReasonDisplay(RefundReasonUtil.getReasonDisplayName(refundRequest.getReason())); // ✅ THÊM
            detail.setRefundDate(refundRequest.getCreatedAt());

            //  THÊM MỚI: Set trạng thái hoàn trả của sản phẩm
            detail.setRefundStatus(refundRequest.getStatus().name());
            detail.setRefundStatusDisplay(getRefundStatusDisplay(refundRequest.getStatus()));

        } else {
            detail.setRefundedQuantity(0);
            detail.setRefundedAmount(BigDecimal.ZERO);
            detail.setRefundStatus("NONE");
            detail.setRefundStatusDisplay("Không hoàn trả");
        }
    }

    /**
     *  THÊM MỚI: Helper method để convert trạng thái hoàn trả sang display name
     */
    private String getRefundStatusDisplay(RefundStatus status) {
        switch (status) {
            case PENDING:
                return "Chờ phê duyệt";
            case APPROVED:
                return "Đã phê duyệt";
            case REJECTED:
                return "Đã từ chối";
            case COMPLETED:
                return "Hoàn thành";
            default:
                return "Không xác định";
        }
    }

    // ========= Common date helpers =========
    private static final long MIN_WEEK_DAYS = 7L;
    private static final long MIN_MONTH_DAYS = 28L;
    private static final long MIN_YEAR_DAYS = 365L;

    private long toStartOfDayMillis(LocalDate d) {
        return d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private long toEndOfDayMillis(LocalDate d) {
        return d.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private long daysInclusive(LocalDate s, LocalDate e) {
        return java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
    }

    // ========= Public API =========
    @Override
    public ApiResponse<List<RevenueStatsResponse>> getRevenueStats(
            String type, Integer year, Integer month, String startDate, String endDate) {

        if (type == null)
            return new ApiResponse<>(400, "type không được null", null);

        switch (type.toLowerCase()) {
            case "day":
                return getRevenueStatsByDay(startDate, endDate);
            case "week":
                return getRevenueStatsByWeek(startDate, endDate);
            case "month":
                return getRevenueStatsByMonth(year, startDate, endDate);
            case "year":
                return getRevenueStatsByYear(startDate, endDate);
            default:
                return new ApiResponse<>(400, "Loại thống kê không hợp lệ", null);
        }
    }

    // ========= Extracted handlers =========

    private ApiResponse<List<RevenueStatsResponse>> getRevenueStatsByDay(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return new ApiResponse<>(400, "Cần truyền startDate và endDate (yyyy-MM-dd) cho type=day", null);
        }

        LocalDate s, e;
        try {
            s = LocalDate.parse(startDate);
            e = LocalDate.parse(endDate);
        } catch (Exception ex) {
            return new ApiResponse<>(400, "Định dạng ngày không hợp lệ (yyyy-MM-dd)", null);
        }
        if (s.isAfter(e))
            return new ApiResponse<>(400, "startDate phải <= endDate", null);

        long startMillis = toStartOfDayMillis(s);
        long endMillis = toEndOfDayMillis(e);

        // Query chỉ trả về các ngày có doanh thu
        List<Object[]> raw = orderRepository.findDailyRevenueByDateRange(startMillis, endMillis);

        // Map day -> revenue
        java.util.Map<LocalDate, BigDecimal> revenueByDay = new java.util.HashMap<>();
        for (Object[] row : raw) {
            String dayKey = String.valueOf(row[0]); // "YYYY-MM-DD"
            BigDecimal revenue = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
            try {
                LocalDate d = LocalDate.parse(dayKey);
                revenueByDay.put(d, revenue);
            } catch (Exception ignored) {
            }
        }

        // Điền đầy đủ từng ngày trong khoảng, ngày nào không có thì revenue = 0
        List<RevenueStatsResponse> result = new ArrayList<>();
        for (LocalDate d = s; !d.isAfter(e); d = d.plusDays(1)) {
            BigDecimal revenue = revenueByDay.getOrDefault(d, BigDecimal.ZERO);
            RevenueStatsResponse item = new RevenueStatsResponse(d.getYear(), d.getMonthValue(), null, revenue);
            item.setDay(d.toString()); // "2025-08-01"
            result.add(item);
        }

        return new ApiResponse<>(200, "Thành công", result);
    }

    private ApiResponse<List<RevenueStatsResponse>> getRevenueStatsByWeek(String startDate, String endDate) {
        // Có truyền khoảng ngày → validate >= 7 ngày và group theo tuần
        if (startDate != null || endDate != null) {
            if (startDate == null || endDate == null)
                return new ApiResponse<>(400, "Cần truyền đủ startDate và endDate (yyyy-MM-dd)", null);

            LocalDate s, e;
            try {
                s = LocalDate.parse(startDate);
                e = LocalDate.parse(endDate);
            } catch (Exception ex) {
                return new ApiResponse<>(400, "Định dạng ngày không hợp lệ (yyyy-MM-dd)", null);
            }
            if (s.isAfter(e))
                return new ApiResponse<>(400, "startDate phải <= endDate", null);
            if (daysInclusive(s, e) < MIN_WEEK_DAYS)
                return new ApiResponse<>(400, "Khoảng ngày phải đủ ít nhất 7 ngày cho thống kê theo tuần", null);

            long startMillis = toStartOfDayMillis(s);
            long endMillis = toEndOfDayMillis(e);
            List<Object[]> raw = orderRepository.findAllWeeklyRevenueByDateRange(startMillis, endMillis);
            List<RevenueStatsResponse> result = new ArrayList<>();
            for (Object[] row : raw) {
                String weekPeriod = (String) row[0]; // "YYYY-Wxx"
                BigDecimal revenue = (BigDecimal) row[1];
                Integer weekNum = null;
                if (weekPeriod != null && weekPeriod.contains("-W")) {
                    try {
                        weekNum = Integer.parseInt(weekPeriod.split("-W")[1]);
                    } catch (Exception ignored) {
                    }
                }
                result.add(new RevenueStatsResponse(LocalDate.now().getYear(), null, weekNum, revenue));
            }
            return new ApiResponse<>(200, "Thành công", result);
        }

        // Không truyền → 5 tuần gần nhất
        int numWeeks = 5;
        LocalDate now = LocalDate.now();
        LocalDate startOfTargetWeek = now.with(java.time.DayOfWeek.MONDAY).minusWeeks(numWeeks - 1);
        LocalDate endOfThisWeek = now.with(java.time.DayOfWeek.SUNDAY);

        long startMillis = toStartOfDayMillis(startOfTargetWeek);
        long endMillis = toEndOfDayMillis(endOfThisWeek);

        List<Object[]> raw = orderRepository.findAllWeeklyRevenueByDateRange(startMillis, endMillis);
        List<RevenueStatsResponse> result = new ArrayList<>();
        for (Object[] row : raw) {
            String weekPeriod = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            Integer weekNum = null;
            if (weekPeriod != null && weekPeriod.contains("-W")) {
                try {
                    weekNum = Integer.parseInt(weekPeriod.split("-W")[1]);
                } catch (Exception ignored) {
                }
            }
            result.add(new RevenueStatsResponse(now.getYear(), null, weekNum, revenue));
        }
        return new ApiResponse<>(200, "Thành công", result);
    }

    private ApiResponse<List<RevenueStatsResponse>> getRevenueStatsByMonth(Integer year, String startDate,
            String endDate) {
        // Có truyền khoảng ngày → validate >= 28 ngày và group theo tháng
        if (startDate != null && endDate != null) {
            LocalDate s, e;
            try {
                s = LocalDate.parse(startDate);
                e = LocalDate.parse(endDate);
            } catch (Exception ex) {
                return new ApiResponse<>(400, "Định dạng ngày không hợp lệ (yyyy-MM-dd)", null);
            }
            if (s.isAfter(e))
                return new ApiResponse<>(400, "startDate phải <= endDate", null);
            if (daysInclusive(s, e) < MIN_MONTH_DAYS)
                return new ApiResponse<>(400, "Khoảng ngày phải đủ ít nhất 28 ngày cho thống kê theo tháng", null);

            long startMillis = toStartOfDayMillis(s);
            long endMillis = toEndOfDayMillis(e);

            List<Object[]> raw = orderRepository.findAllMonthlyRevenueByDateRange(startMillis, endMillis);

            // Map month_key -> revenue
            DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");
            java.util.Map<YearMonth, BigDecimal> revenueByMonth = new java.util.HashMap<>();
            for (Object[] row : raw) {
                String monthKey = (String) row[0]; // "YYYY-MM"
                BigDecimal revenue = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
                try {
                    YearMonth ym = YearMonth.parse(monthKey, ymFmt);
                    revenueByMonth.put(ym, revenue);
                } catch (Exception ignored) {
                }
            }

            // Điền đủ từng tháng trong khoảng [s..e] (theo first day của tháng)
            YearMonth startYM = YearMonth.from(s);
            YearMonth endYM = YearMonth.from(e);
            List<RevenueStatsResponse> result = new ArrayList<>();
            for (YearMonth ym = startYM; !ym.isAfter(endYM); ym = ym.plusMonths(1)) {
                BigDecimal revenue = revenueByMonth.getOrDefault(ym, BigDecimal.ZERO);
                result.add(new RevenueStatsResponse(ym.getYear(), ym.getMonthValue(), null, revenue));
            }
            return new ApiResponse<>(200, "Thành công", result);
        }

        // Không truyền → 12 tháng của năm chỉ định (mặc định năm hiện tại)
        if (year == null)
            year = LocalDate.now().getYear();
        List<RevenueStatsResponse> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            LocalDateTime start = LocalDateTime.of(year, m, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);
            long startMillis = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endMillis = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<Object[]> raw = orderRepository.findAllMonthlyRevenueByDateRange(startMillis, endMillis);

            //  SỬA: Thay index 2 thành index 1 (vì chỉ có 2 cột)
            BigDecimal revenue = raw.isEmpty() ? BigDecimal.ZERO : (BigDecimal) rowValue(raw.get(0), 1);
            result.add(new RevenueStatsResponse(year, m, null, revenue));
        }
        return new ApiResponse<>(200, "Thành công", result);
    }

    private ApiResponse<List<RevenueStatsResponse>> getRevenueStatsByYear(String startDate, String endDate) {
        // Có truyền khoảng ngày → validate >= 365 ngày, group theo năm và FILL 0 cho
        // năm thiếu
        if (startDate != null && endDate != null) {
            LocalDate s, e;
            try {
                s = LocalDate.parse(startDate);
                e = LocalDate.parse(endDate);
            } catch (Exception ex) {
                return new ApiResponse<>(400, "Định dạng ngày không hợp lệ (yyyy-MM-dd)", null);
            }
            if (s.isAfter(e))
                return new ApiResponse<>(400, "startDate phải <= endDate", null);
            if (daysInclusive(s, e) < MIN_YEAR_DAYS)
                return new ApiResponse<>(400, "Khoảng ngày phải đủ ít nhất 365 ngày cho thống kê theo năm", null);

            long startMillis = toStartOfDayMillis(s);
            long endMillis = toEndOfDayMillis(e);

            List<Object[]> raw = orderRepository.findYearlyRevenueByDateRange(startMillis, endMillis);

            // Map year -> revenue
            java.util.Map<Integer, BigDecimal> revenueByYear = new java.util.HashMap<>();
            for (Object[] row : raw) {
                String yearKey = String.valueOf(row[0]); // "YYYY"
                BigDecimal revenue = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
                try {
                    Integer yy = Integer.parseInt(yearKey);
                    revenueByYear.put(yy, revenue);
                } catch (Exception ignored) {
                }
            }

            // Fill đủ từng năm trong khoảng [s..e], năm không có đơn => revenue = 0
            List<RevenueStatsResponse> result = new ArrayList<>();
            for (int y = s.getYear(); y <= e.getYear(); y++) {
                BigDecimal revenue = revenueByYear.getOrDefault(y, BigDecimal.ZERO);
                result.add(new RevenueStatsResponse(y, null, null, revenue));
            }
            return new ApiResponse<>(200, "Thành công", result);
        }

        // Không truyền → 3 năm gần nhất (đã có fill 0 theo từng năm)
        int currentYear = LocalDate.now().getYear();
        List<RevenueStatsResponse> result = new ArrayList<>();
        for (int y = currentYear - 2; y <= currentYear; y++) {
            LocalDateTime start = LocalDateTime.of(y, 1, 1, 0, 0);
            LocalDateTime end = start.plusYears(1).minusSeconds(1);
            long startMillis = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endMillis = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<Object[]> raw = orderRepository.findYearlyRevenueByDateRange(startMillis, endMillis);
            BigDecimal revenue = raw.isEmpty() ? BigDecimal.ZERO : (BigDecimal) rowValue(raw.get(0), 1);

            result.add(new RevenueStatsResponse(y, null, null, revenue));
        }
        return new ApiResponse<>(200, "Thành công", result);
    }

    @Override
    public ApiResponse<List<RevenueStatsResponse>> getMonthlySoldQuantity() {
        int year = LocalDate.now().getYear(); // Lấy năm hiện tại
        List<RevenueStatsResponse> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            LocalDateTime start = LocalDateTime.of(year, m, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);
            long startMillis = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endMillis = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<Object[]> raw = orderRepository.getMonthlySoldQuantity(startMillis, endMillis);
            Long sold = raw.isEmpty() ? 0L : ((Number) raw.get(0)[2]).longValue();
            result.add(new RevenueStatsResponse(year, m, null, BigDecimal.valueOf(sold)));
        }
        return new ApiResponse<>(200, "Thành công", result);
    }

    @Override
    public ApiResponse<Long> getTotalDeliveredOrders() {
        Long total = orderRepository.countDeliveredOrders();
        return new ApiResponse<>(200, "Thành công", total);
    }

    /**
     *  ENHANCED: Save OrderVoucher entities để vouchers hiển thị trong API
     * responses
     */
    private void saveOrderVouchers(Order order, List<Integer> voucherIds, BigDecimal orderSubtotal,
            BigDecimal shippingFee) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            return;
        }

        try {
            // Get vouchers from database
            List<Voucher> vouchers = voucherRepository.findAllById(voucherIds);

            for (Voucher voucher : vouchers) {
                // Calculate actual discount applied for this voucher
                BigDecimal discountApplied = voucherCalculationService.calculateSingleVoucherDiscount(voucher,
                        orderSubtotal, shippingFee);

                // Create OrderVoucher entity
                OrderVoucher orderVoucher = new OrderVoucher();

                // Set composite ID
                OrderVoucherId id = new OrderVoucherId();
                id.setOrderId(order.getId());
                id.setVoucherId(voucher.getId());
                orderVoucher.setId(id);

                // Set relationships
                orderVoucher.setOrder(order);
                orderVoucher.setVoucher(voucher);

                // Set voucher information (will be auto-set in @PrePersist)
                orderVoucher.setVoucherCategory(voucher.getVoucherCategory());
                orderVoucher.setDiscountType(voucher.getDiscountType());

                // Set discount applied amount
                orderVoucher.setDiscountApplied(discountApplied);

                // appliedAt will be set in @PrePersist

                // Save OrderVoucher
                orderVoucherRepository.save(orderVoucher);

                log.info(" Saved OrderVoucher: orderId={}, voucherId={}, discountApplied={}",
                        order.getId(), voucher.getId(), discountApplied);
            }

        } catch (Exception e) {
            log.error(" Failed to save OrderVoucher entities for order {}: {}", order.getId(), e.getMessage(), e);
            // Không throw exception để không làm fail việc tạo order
        }
    }

    // helper để an toàn kiểu Number -> BigDecimal
    private Object rowValue(Object[] row, int idx) {
        return row[idx];
    }

    // ================================================================
    // ORDER STATISTICS APIs IMPLEMENTATION - 2-TIER ARCHITECTURE
    // ================================================================

    /**
     *  API THỐNG KÊ TỔNG QUAN ĐỚN HÀNG - TIER 1 (Summary)
     * Tương tự BookServiceImpl.getBookStatisticsSummary() nhưng cho Order
     */
    @Override
    public ApiResponse<Map<String, Object>> getOrderStatisticsSummary(String period, Long fromDate, Long toDate) {
        try {
            log.info(" Getting order statistics summary - period: {}, fromDate: {}, toDate: {}", period, fromDate, toDate);
            
            List<Map<String, Object>> summaryData = new ArrayList<>();
            Long startTime, endTime;
            String finalPeriodType;
            
            // 1. Xử lý logic period và time range (copy từ BookServiceImpl)
            OrderPeriodCalculationResult periodResult = calculateOrderPeriodAndTimeRange(period, fromDate, toDate);
            startTime = periodResult.getStartTime();
            endTime = periodResult.getEndTime();
            finalPeriodType = periodResult.getFinalPeriodType();
            
            // 2. Validate khoảng thời gian tối đa cho từng period type
            String validationError = validateOrderDateRangeForPeriod(finalPeriodType, startTime, endTime);
            if (validationError != null) {
                log.warn(" Date range validation failed: {}", validationError);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("data", new ArrayList<>());
                errorData.put("totalOrdersSum", 0);
                errorData.put("netRevenueSum", 0.0);
                errorData.put("averageAOV", 0.0);
                errorData.put("completionRate", 0.0);
                return new ApiResponse<>(400, validationError, errorData);
            }
            
            log.info(" Final period: {}, timeRange: {} to {}", finalPeriodType, 
                    new java.util.Date(startTime), new java.util.Date(endTime));
            
            // 3. Query dữ liệu từ database
            List<Object[]> rawData = orderRepository.findOrderStatisticsSummaryByDateRange(startTime, endTime);
            
            // 4. Convert raw data thành Map và FIXED: Recalculate netRevenue using TRUE logic
            Map<String, Map<String, Object>> dataMap = new HashMap<>();
            for (Object[] row : rawData) {
                String date = row[0].toString(); // Date string từ DB
                Integer totalOrders = ((Number) row[1]).intValue();
                Integer completedOrders = ((Number) row[2]).intValue();
                Integer canceledOrders = ((Number) row[3]).intValue();
                Integer refundedOrders = ((Number) row[4]).intValue();
                // Bỏ qua netRevenue từ query (row[5]) vì nó tính sai
                
                // ✅ RECALCULATE: Tính lại netRevenue chính xác cho ngày này
                LocalDate dateLocal = LocalDate.parse(date);
                Long dayStart = dateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                Long dayEnd = dateLocal.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                
                // Sử dụng cùng logic với Overview API
                BigDecimal correctNetRevenue = orderStatisticsService.calculateNetRevenueForPeriod(dayStart, dayEnd);
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("totalOrders", totalOrders);
                dayData.put("completedOrders", completedOrders);
                dayData.put("canceledOrders", canceledOrders);
                dayData.put("refundedOrders", refundedOrders);
                dayData.put("netRevenue", correctNetRevenue); // ✅ FIXED: Sử dụng giá trị chính xác
                // AOV = Average Order Value = Net Revenue / Total Orders
                BigDecimal aov = totalOrders > 0 ? correctNetRevenue.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                dayData.put("aov", aov);
                dataMap.put(date, dayData);
                
                log.debug("📊 Fixed netRevenue for {}: {} (was: {})", date, correctNetRevenue, 
                    row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO);
            }
            
            // 5. Generate full date range với 0 cho ngày không có data
            switch (finalPeriodType) {
                case "daily":
                    summaryData = generateOrderDailySummary(startTime, endTime, dataMap);
                    break;
                case "weekly":
                    summaryData = generateOrderWeeklySummary(startTime, endTime, dataMap);
                    break;
                case "monthly":
                    summaryData = generateOrderMonthlySummary(startTime, endTime, dataMap);
                    break;
                case "quarterly":
                    summaryData = generateOrderQuarterlySummary(startTime, endTime, dataMap);
                    break;
                case "yearly":
                    summaryData = generateOrderYearlySummary(startTime, endTime, dataMap);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported period type: " + finalPeriodType);
            }
            
            //  Calculate summary totals and add to response 
            Map<String, Object> responseWithSummary = calculateOrderSummaryTotals(summaryData);
            
            log.info(" Generated {} data points with summary totals for period: {} (final: {})", summaryData.size(), period, finalPeriodType);
            return new ApiResponse<>(200, "Lấy thống kê tổng quan đơn hàng thành công", responseWithSummary);
            
        } catch (Exception e) {
            log.error(" Error getting order statistics summary", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("data", new ArrayList<>());
            errorData.put("totalOrdersSum", 0);
            errorData.put("netRevenueSum", 0.0);
            errorData.put("averageAOV", 0.0);
            errorData.put("completionRate", 0.0);
            return new ApiResponse<>(500, "Lỗi khi lấy thống kê tổng quan đơn hàng", errorData);
        }
    }
    
    /**
     *  Calculate summary totals from data list
     * Returns Map with "data" array and summary totals
     */
    private Map<String, Object> calculateOrderSummaryTotals(List<Map<String, Object>> summaryData) {
        Map<String, Object> result = new HashMap<>();
        result.put("data", summaryData);
        
        if (summaryData.isEmpty()) {
            // Empty data case
            result.put("totalOrdersSum", 0);
            result.put("netRevenueSum", 0.00);
            result.put("averageAOV", 0.00);
            result.put("completionRate", 0.00);
            return result;
        }
        
        // Calculate totals
        int totalOrdersSum = 0;
        int completedOrdersSum = 0; 
        int canceledOrdersSum = 0;
        int refundedOrdersSum = 0;
        BigDecimal totalRevenueSum = BigDecimal.ZERO;
        
        for (Map<String, Object> record : summaryData) {
            totalOrdersSum += (Integer) record.getOrDefault("totalOrders", 0);
            completedOrdersSum += (Integer) record.getOrDefault("completedOrders", 0);
            canceledOrdersSum += (Integer) record.getOrDefault("canceledOrders", 0);
            refundedOrdersSum += (Integer) record.getOrDefault("refundedOrders", 0);
            BigDecimal revenue = (BigDecimal) record.getOrDefault("netRevenue", BigDecimal.ZERO);
            totalRevenueSum = totalRevenueSum.add(revenue);
        }
        
        // Calculate averages and rates
        BigDecimal averageAOV = totalOrdersSum > 0 ? 
            totalRevenueSum.divide(new BigDecimal(totalOrdersSum), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
            
        BigDecimal completionRate = totalOrdersSum > 0 ? 
            new BigDecimal(completedOrdersSum).multiply(new BigDecimal("100")).divide(new BigDecimal(totalOrdersSum), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;
        
        // Add summary fields
        result.put("totalOrdersSum", totalOrdersSum);
        result.put("completedOrdersSum", completedOrdersSum);
        result.put("canceledOrdersSum", canceledOrdersSum);  
        result.put("refundedOrdersSum", refundedOrdersSum);
        result.put("netRevenueSum", totalRevenueSum);
        result.put("averageAOV", averageAOV);
        result.put("completionRate", completionRate);
        
        return result;
    }

    /**
     *  API THỐNG KÊ CHI TIẾT ĐỚN HÀNG - TIER 2 (Details)  
     * Tương tự BookServiceImpl.getBookStatisticsDetails() nhưng cho Order
     */
    @Override
    public ApiResponse<List<Map<String, Object>>> getOrderStatisticsDetails(String period, Long date, Integer limit) {
        try {
            log.info("Getting order statistics details - period: {}, date: {}, limit: {}", period, date, limit);
            
            // Parse timestamp và tính toán khoảng thời gian cụ thể
            OrderTimeRangeInfo timeRange;
            
            if ("week".equalsIgnoreCase(period) || "weekly".equalsIgnoreCase(period)) {
                // Sử dụng logic giống BookServiceImpl cho week calculation
                LocalDate inputDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate weekStart = inputDate.with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);
                
                long weekStartMs = weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long weekEndMs = weekEnd.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                
                log.info(" Week calculation - Input: {} -> Week: {} to {} ({}ms to {}ms)", 
                        inputDate, weekStart, weekEnd, weekStartMs, weekEndMs);
                
                timeRange = new OrderTimeRangeInfo(weekStartMs, weekEndMs);
            } else {
                // Other periods use existing logic
                timeRange = calculateOrderTimeRangeFromTimestamp(period, date);
            }
            
            log.info(" Calculated time range: {} to {} for period: {}", 
                    Instant.ofEpochMilli(timeRange.getStartTime()).toString(), 
                    Instant.ofEpochMilli(timeRange.getEndTime()).toString(), period);
            
            // Query chi tiết đơn hàng trong khoảng thời gian đó
            List<Object[]> orderData = orderRepository.findOrderDetailsByDateRange(
                    timeRange.getStartTime(), timeRange.getEndTime(), limit != null ? limit : 10);
            
            log.info(" Found {} orders in time range", orderData.size());
            
            // Build response với thông tin chi tiết
            List<Map<String, Object>> detailsData = buildOrderDetailsResponse(orderData);
            
            String message = String.format("Order details retrieved successfully for %s on %s", period, date);
            return new ApiResponse<>(200, message, detailsData);
            
        } catch (Exception e) {
            log.error(" Error getting order statistics details", e);
            return new ApiResponse<>(500, "Lỗi khi lấy chi tiết thống kê đơn hàng", new ArrayList<>());
        }
    }

    // ================================================================
    // HELPER CLASSES VÀ METHODS CHO ORDER STATISTICS
    // ================================================================

    /**
     * Tương tự BookServiceImpl.PeriodCalculationResult
     */
    private static class OrderPeriodCalculationResult {
        private final long startTime;
        private final long endTime;
        private final String finalPeriodType;
        
        public OrderPeriodCalculationResult(long startTime, long endTime, String finalPeriodType) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.finalPeriodType = finalPeriodType;
        }
        
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public String getFinalPeriodType() { return finalPeriodType; }
    }
    
    /**
     * Tương tự BookServiceImpl.TimeRangeInfo
     */
    private static class OrderTimeRangeInfo {
        private final long startTime;
        private final long endTime;
        
        public OrderTimeRangeInfo(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
    }

    /**
     * Calculate period và time range cho Order (copy từ BookServiceImpl)
     */
    /**
     *  CORE: Tính toán period và time range với logic đúng (COPY từ BookServiceImpl)
     * Logic:
     * - Nếu không có fromDate/toDate → dùng default period ranges
     * - Nếu có fromDate/toDate → kiểm tra validation và return exact range
     */
    private OrderPeriodCalculationResult calculateOrderPeriodAndTimeRange(String period, Long fromDate, Long toDate) {
        long currentTime = System.currentTimeMillis();
        
        // Case 1: Không có fromDate/toDate → dùng default ranges
        if (fromDate == null || toDate == null) {
            return calculateOrderDefaultPeriodRange(period, currentTime);
        }
        
        // Case 2: Có fromDate/toDate → return exact range (validation sẽ check sau)
        return calculateOrderCustomPeriodRange(period, fromDate, toDate);
    }
    
    /**
     * Tính toán default period ranges khi không có fromDate/toDate (COPY từ BookServiceImpl)
     */
    private OrderPeriodCalculationResult calculateOrderDefaultPeriodRange(String period, long currentTime) {
        switch (period.toLowerCase()) {
            case "day":
                // 30 ngày trước
                return new OrderPeriodCalculationResult(
                    currentTime - (30L * 24 * 60 * 60 * 1000), 
                    currentTime, 
                    "daily"
                );
            case "week":
                // 3 tuần trước (21 ngày)
                return new OrderPeriodCalculationResult(
                    currentTime - (21L * 24 * 60 * 60 * 1000), 
                    currentTime, 
                    "weekly"
                );
            case "month":
                // 3 tháng trước (~90 ngày)
                return new OrderPeriodCalculationResult(
                    currentTime - (90L * 24 * 60 * 60 * 1000), 
                    currentTime, 
                    "monthly"
                );
            case "quarter":
                // 3 quý trước (~270 ngày)
                return new OrderPeriodCalculationResult(
                    currentTime - (270L * 24 * 60 * 60 * 1000), 
                    currentTime, 
                    "quarterly"
                );
            case "year":
                // 1 năm trước
                return new OrderPeriodCalculationResult(
                    currentTime - (365L * 24 * 60 * 60 * 1000), 
                    currentTime, 
                    "yearly"
                );
            default:
                // Default: 30 ngày
                return new OrderPeriodCalculationResult(
                    currentTime - (30L * 24 * 60 * 60 * 1000), 
                    currentTime, 
                    "daily"
                );
        }
    }
    
    /**
     *  STRICT VALIDATION: Return exact period range (COPY từ BookServiceImpl)
     * - Validation sẽ được thực hiện sau method này
     */
    private OrderPeriodCalculationResult calculateOrderCustomPeriodRange(String period, Long fromDate, Long toDate) {
        long duration = toDate - fromDate;
        long daysDuration = duration / (24 * 60 * 60 * 1000L);
        
        log.info(" Order Custom period analysis: {} with {} days duration", period, daysDuration);
        log.info(" USING FULL RANGE: {} to {} (NO DATA CUTTING)", new java.util.Date(fromDate), new java.util.Date(toDate));
        
        // KHÔNG auto-downgrade, chỉ return period như user request
        // Validation sẽ được thực hiện ở validateOrderDateRangeForPeriod method
        switch (period.toLowerCase()) {
            case "year":
                log.info(" Using FULL yearly range: {} days (validation will check minimum requirements)", daysDuration);
                return new OrderPeriodCalculationResult(fromDate, toDate, "yearly");
                
            case "quarter":
                log.info(" Using FULL quarterly range: {} days (validation will check minimum requirements)", daysDuration);
                return new OrderPeriodCalculationResult(fromDate, toDate, "quarterly");
                
            case "month":
                log.info(" Using FULL monthly range: {} days (validation will check minimum requirements)", daysDuration);
                return new OrderPeriodCalculationResult(fromDate, toDate, "monthly");
                
            case "week":
                log.info(" Using FULL weekly range: {} days (validation will check minimum requirements)", daysDuration);
                return new OrderPeriodCalculationResult(fromDate, toDate, "weekly");
                
            case "day":
            default:
                log.info(" Using FULL daily range: {} days (validation will check minimum requirements)", daysDuration);
                return new OrderPeriodCalculationResult(fromDate, toDate, "daily");
        }
    }
    
    /**
     * Validate date range cho Order (copy từ BookServiceImpl)
     */
    /**
     *  VALIDATE DATE RANGE FOR PERIOD TYPES (COPIED FROM BookServiceImpl)
     * Kiểm tra khoảng thời gian có hợp lệ cho từng period type không
     * - Giới hạn giống hệt Book APIs để đảm bảo consistency
     */
    private String validateOrderDateRangeForPeriod(String periodType, long startTime, long endTime) {
        long durationMillis = endTime - startTime;
        long durationDays = durationMillis / (24 * 60 * 60 * 1000L);
        long durationYears = durationDays / 365L;
        
        switch (periodType.toLowerCase()) {
            case "daily":
                // Minimum: ít nhất 1 ngày
                if (durationDays < 1) {
                    return "Khoảng thời gian quá nhỏ cho chế độ ngày (tối thiểu 1 ngày). Khoảng thời gian hiện tại: " + durationDays + " ngày.";
                }
                // Maximum: tối đa 90 ngày
                if (durationDays > 90) {
                    return "Khoảng thời gian quá lớn cho chế độ ngày (tối đa 90 ngày). Khoảng thời gian hiện tại: " + durationDays + " ngày.";
                }
                break;
            
            case "weekly":
                // Minimum: ít nhất 7 ngày (1 tuần)
                if (durationDays < 7) {
                    return "Khoảng thời gian quá nhỏ cho chế độ tuần (tối thiểu 7 ngày). Khoảng thời gian hiện tại: " + durationDays + " ngày.";
                }
                // Maximum: tối đa 2 năm
                if (durationYears > 2) {
                    return "Khoảng thời gian quá lớn cho chế độ tuần (tối đa 2 năm). Khoảng thời gian hiện tại: " + durationYears + " năm.";
                }
                break;
            
            case "monthly":
                // Minimum: ít nhất 28 ngày (1 tháng)
                if (durationDays < 28) {
                    return "Khoảng thời gian quá nhỏ cho chế độ tháng (tối thiểu 28 ngày). Khoảng thời gian hiện tại: " + durationDays + " ngày.";
                }
                // Maximum: tối đa 5 năm
                if (durationYears > 5) {
                    return "Khoảng thời gian quá lớn cho chế độ tháng (tối đa 5 năm). Khoảng thời gian hiện tại: " + durationYears + " năm.";
                }
                break;
            
            case "quarterly":
                // Minimum: ít nhất 90 ngày (1 quý)
                if (durationDays < 90) {
                    return "Khoảng thời gian quá nhỏ cho chế độ quý (tối thiểu 90 ngày). Khoảng thời gian hiện tại: " + durationDays + " ngày.";
                }
                // Maximum: tối đa 5 năm
                if (durationYears > 5) {
                    return "Khoảng thời gian quá lớn cho chế độ quý (tối đa 5 năm). Khoảng thời gian hiện tại: " + durationYears + " năm.";
                }
                break;
            
            case "yearly":
                // Minimum: ít nhất 365 ngày (1 năm)
                if (durationDays < 365) {
                    return "Khoảng thời gian quá nhỏ cho chế độ năm (tối thiểu 365 ngày). Khoảng thời gian hiện tại: " + durationDays + " ngày.";
                }
                // Maximum: tối đa 25 năm
                if (durationYears > 25) {
                    return "Khoảng thời gian quá lớn cho chế độ năm (tối đa 25 năm). Khoảng thời gian hiện tại: " + durationYears + " năm.";
                }
                break;
        }
        
        return null; // Valid
    }
    
    /**
     * Calculate time range từ timestamp cho Order
     */
    private OrderTimeRangeInfo calculateOrderTimeRangeFromTimestamp(String period, Long date) {
        LocalDate inputDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();
        
        switch (period.toLowerCase()) {
            case "day":
            case "daily":
                long dayStart = inputDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long dayEnd = inputDate.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                return new OrderTimeRangeInfo(dayStart, dayEnd);
                
            case "month":
            case "monthly":
                LocalDate monthStart = inputDate.withDayOfMonth(1);
                LocalDate monthEnd = inputDate.withDayOfMonth(inputDate.lengthOfMonth());
                long monthStartMs = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long monthEndMs = monthEnd.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                return new OrderTimeRangeInfo(monthStartMs, monthEndMs);
                
            case "quarter":
            case "quarterly":
                // ✅ FIX: Add quarter calculation
                LocalDate quarterStart = getQuarterStart(inputDate);
                LocalDate quarterEnd = getQuarterEnd(quarterStart);
                long quarterStartMs = quarterStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long quarterEndMs = quarterEnd.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                return new OrderTimeRangeInfo(quarterStartMs, quarterEndMs);
                
            case "year":
            case "yearly":
                LocalDate yearStart = inputDate.withDayOfYear(1);
                LocalDate yearEnd = inputDate.withDayOfYear(inputDate.lengthOfYear());
                long yearStartMs = yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long yearEndMs = yearEnd.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                return new OrderTimeRangeInfo(yearStartMs, yearEndMs);
                
            default:
                // Default to day
                long defaultStart = inputDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long defaultEnd = inputDate.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                return new OrderTimeRangeInfo(defaultStart, defaultEnd);
        }
    }
    
    /**
     * Build response cho Order details
     */
    private List<Map<String, Object>> buildOrderDetailsResponse(List<Object[]> orderData) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Object[] row : orderData) {
            Map<String, Object> orderDetail = new HashMap<>();
            
            // Query returns: order_code, customer_name, customer_email, total_amount, order_status, created_at, product_info, net_revenue
            orderDetail.put("orderCode", (String) row[0]);
            orderDetail.put("customerName", (String) row[1]);
            orderDetail.put("customerEmail", (String) row[2]);
            orderDetail.put("totalAmount", row[3]); // Keep for backward compatibility
            orderDetail.put("orderStatus", row[4]);
            orderDetail.put("createdAt", row[5]);
            orderDetail.put("productInfo", row[6]);
            orderDetail.put("netRevenue", row[7]); // ✅ ADD: Net Revenue field
            
            result.add(orderDetail);
        }
        
        return result;
    }
    
    /**
     * Generate daily summary cho Order (tương tự BookServiceImpl)
     */
    private List<Map<String, Object>> generateOrderDailySummary(Long startTime, Long endTime, Map<String, Map<String, Object>> dataMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        LocalDate startDate = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDate();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.toString();
            Map<String, Object> dayDataFromDB = dataMap.getOrDefault(dateStr, new HashMap<>());
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("totalOrders", dayDataFromDB.getOrDefault("totalOrders", 0));
            dayData.put("completedOrders", dayDataFromDB.getOrDefault("completedOrders", 0));
            dayData.put("canceledOrders", dayDataFromDB.getOrDefault("canceledOrders", 0));
            dayData.put("refundedOrders", dayDataFromDB.getOrDefault("refundedOrders", 0));
            dayData.put("netRevenue", dayDataFromDB.getOrDefault("netRevenue", BigDecimal.ZERO));
            dayData.put("aov", dayDataFromDB.getOrDefault("aov", BigDecimal.ZERO));
            dayData.put("period", "daily");
            
            result.add(dayData);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
    
    /**
     * Generate weekly summary cho Order (tương tự BookServiceImpl)
     */
    private List<Map<String, Object>> generateOrderWeeklySummary(Long startTime, Long endTime, Map<String, Map<String, Object>> dataMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        LocalDate startDate = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDate();
        
        // Start from Monday of the week containing startDate
        LocalDate weekStart = startDate.with(java.time.DayOfWeek.MONDAY);
        
        while (!weekStart.isAfter(endDate)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            String weekLabel = weekStart.toString() + " to " + weekEnd.toString();
            
            // Calculate week number of year
            int weekNumber = weekStart.get(WeekFields.ISO.weekOfYear());
            int year = weekStart.getYear();
            
            // Sum all days in this week from dataMap
            int weekTotalOrders = 0;
            int weekCompletedOrders = 0;
            int weekCanceledOrders = 0;
            int weekRefundedOrders = 0;
            BigDecimal weekNetRevenue = BigDecimal.ZERO;
            
            LocalDate currentDay = weekStart;
            LocalDate actualWeekEnd = weekEnd.isAfter(endDate) ? endDate : weekEnd;
            
            while (!currentDay.isAfter(weekEnd) && !currentDay.isAfter(endDate)) {
                String dayStr = currentDay.toString();
                Map<String, Object> dayDataFromDB = dataMap.getOrDefault(dayStr, new HashMap<>());
                weekTotalOrders += (Integer) dayDataFromDB.getOrDefault("totalOrders", 0);
                weekCompletedOrders += (Integer) dayDataFromDB.getOrDefault("completedOrders", 0);
                weekCanceledOrders += (Integer) dayDataFromDB.getOrDefault("canceledOrders", 0);
                weekRefundedOrders += (Integer) dayDataFromDB.getOrDefault("refundedOrders", 0);
                weekNetRevenue = weekNetRevenue.add((BigDecimal) dayDataFromDB.getOrDefault("netRevenue", BigDecimal.ZERO));
                currentDay = currentDay.plusDays(1);
            }
            
            // Calculate week AOV
            BigDecimal weekAov = weekTotalOrders > 0 ? 
                weekNetRevenue.divide(new BigDecimal(weekTotalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            Map<String, Object> weekData = new HashMap<>();
            weekData.put("date", weekStart.toString()); // Use week start as date
            weekData.put("totalOrders", weekTotalOrders);
            weekData.put("completedOrders", weekCompletedOrders);
            weekData.put("canceledOrders", weekCanceledOrders);
            weekData.put("refundedOrders", weekRefundedOrders);
            weekData.put("netRevenue", weekNetRevenue);
            weekData.put("aov", weekAov);
            weekData.put("period", "weekly");
            weekData.put("dateRange", weekLabel);
            weekData.put("weekNumber", weekNumber);
            weekData.put("year", year);
            weekData.put("startDate", weekStart.toString());
            weekData.put("endDate", actualWeekEnd.toString());
            
            result.add(weekData);
            weekStart = weekStart.plusWeeks(1);
        }
        
        return result;
    }
    
    /**
     * Generate monthly summary cho Order (tương tự BookServiceImpl)
     */
    private List<Map<String, Object>> generateOrderMonthlySummary(Long startTime, Long endTime, Map<String, Map<String, Object>> dataMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        LocalDate startDate = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDate();
        
        // Start from first day of the month containing startDate
        LocalDate monthStart = startDate.withDayOfMonth(1);
        
        while (!monthStart.isAfter(endDate)) {
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            String monthLabel = monthStart.getMonth().toString() + " " + monthStart.getYear();
            
            // Calculate month info
            int monthNumber = monthStart.getMonthValue();
            int year = monthStart.getYear();
            String monthName = monthStart.getMonth().getDisplayName(
                java.time.format.TextStyle.FULL, java.util.Locale.forLanguageTag("vi-VN"));
            
            // Sum all days in this month from dataMap
            int monthTotalOrders = 0;
            int monthCompletedOrders = 0;
            int monthCanceledOrders = 0;
            int monthRefundedOrders = 0;
            BigDecimal monthNetRevenue = BigDecimal.ZERO;
            
            LocalDate currentDay = monthStart;
            LocalDate actualMonthEnd = monthEnd.isAfter(endDate) ? endDate : monthEnd;
            
            while (!currentDay.isAfter(monthEnd) && !currentDay.isAfter(endDate)) {
                String dayStr = currentDay.toString();
                Map<String, Object> dayDataFromDB = dataMap.getOrDefault(dayStr, new HashMap<>());
                monthTotalOrders += (Integer) dayDataFromDB.getOrDefault("totalOrders", 0);
                monthCompletedOrders += (Integer) dayDataFromDB.getOrDefault("completedOrders", 0);
                monthCanceledOrders += (Integer) dayDataFromDB.getOrDefault("canceledOrders", 0);
                monthRefundedOrders += (Integer) dayDataFromDB.getOrDefault("refundedOrders", 0);
                monthNetRevenue = monthNetRevenue.add((BigDecimal) dayDataFromDB.getOrDefault("netRevenue", BigDecimal.ZERO));
                currentDay = currentDay.plusDays(1);
            }
            
            // Calculate month AOV
            BigDecimal monthAov = monthTotalOrders > 0 ? 
                monthNetRevenue.divide(new BigDecimal(monthTotalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("date", monthStart.toString()); // Use month start as date
            monthData.put("totalOrders", monthTotalOrders);
            monthData.put("completedOrders", monthCompletedOrders);
            monthData.put("canceledOrders", monthCanceledOrders);
            monthData.put("refundedOrders", monthRefundedOrders);
            monthData.put("netRevenue", monthNetRevenue);
            monthData.put("aov", monthAov);
            monthData.put("period", "monthly");
            monthData.put("dateRange", monthLabel);
            monthData.put("monthNumber", monthNumber);
            monthData.put("monthName", monthName);
            monthData.put("year", year);
            monthData.put("startDate", monthStart.toString());
            monthData.put("endDate", actualMonthEnd.toString());
            
            result.add(monthData);
            monthStart = monthStart.plusMonths(1);
        }
        
        return result;
    }
    
    /**
     * Generate quarterly summary cho Order (tương tự BookServiceImpl)
     */
    private List<Map<String, Object>> generateOrderQuarterlySummary(Long startTime, Long endTime, Map<String, Map<String, Object>> dataMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        LocalDate startDate = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDate();
        
        // Start from first day of the quarter containing startDate
        LocalDate quarterStart = getQuarterStart(startDate);
        
        while (!quarterStart.isAfter(endDate)) {
            LocalDate quarterEnd = getQuarterEnd(quarterStart);
            int quarterNumber = getQuarterNumber(quarterStart);
            int year = quarterStart.getYear();
            String quarterLabel = "Quý " + quarterNumber + " năm " + year;
            
            // Sum all days in this quarter from dataMap
            int quarterTotalOrders = 0;
            int quarterCompletedOrders = 0;
            int quarterCanceledOrders = 0;
            int quarterRefundedOrders = 0;
            BigDecimal quarterNetRevenue = BigDecimal.ZERO;
            
            LocalDate currentDay = quarterStart;
            LocalDate actualQuarterEnd = quarterEnd.isAfter(endDate) ? endDate : quarterEnd;
            
            while (!currentDay.isAfter(quarterEnd) && !currentDay.isAfter(endDate)) {
                String dayStr = currentDay.toString();
                Map<String, Object> dayDataFromDB = dataMap.getOrDefault(dayStr, new HashMap<>());
                quarterTotalOrders += (Integer) dayDataFromDB.getOrDefault("totalOrders", 0);
                quarterCompletedOrders += (Integer) dayDataFromDB.getOrDefault("completedOrders", 0);
                quarterCanceledOrders += (Integer) dayDataFromDB.getOrDefault("canceledOrders", 0);
                quarterRefundedOrders += (Integer) dayDataFromDB.getOrDefault("refundedOrders", 0);
                quarterNetRevenue = quarterNetRevenue.add((BigDecimal) dayDataFromDB.getOrDefault("netRevenue", BigDecimal.ZERO));
                currentDay = currentDay.plusDays(1);
            }
            
            // Calculate quarter AOV
            BigDecimal quarterAov = quarterTotalOrders > 0 ? 
                quarterNetRevenue.divide(new BigDecimal(quarterTotalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            Map<String, Object> quarterData = new HashMap<>();
            quarterData.put("date", quarterStart.toString()); // Use quarter start as date
            quarterData.put("totalOrders", quarterTotalOrders);
            quarterData.put("completedOrders", quarterCompletedOrders);
            quarterData.put("canceledOrders", quarterCanceledOrders);
            quarterData.put("refundedOrders", quarterRefundedOrders);
            quarterData.put("netRevenue", quarterNetRevenue);
            quarterData.put("aov", quarterAov);
            quarterData.put("period", "quarterly");
            quarterData.put("dateRange", quarterLabel);
            quarterData.put("quarter", quarterNumber);
            quarterData.put("year", year);
            quarterData.put("startDate", quarterStart.toString());
            quarterData.put("endDate", actualQuarterEnd.toString());
            
            result.add(quarterData);
            quarterStart = quarterStart.plusMonths(3);
        }
        
        return result;
    }
    
    /**
     * Generate yearly summary cho Order (tương tự BookServiceImpl)
     */
    private List<Map<String, Object>> generateOrderYearlySummary(Long startTime, Long endTime, Map<String, Map<String, Object>> dataMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        LocalDate startDate = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDate();
        
        // Start from January 1st of the year containing startDate
        LocalDate yearStart = startDate.withDayOfYear(1);
        
        while (!yearStart.isAfter(endDate)) {
            LocalDate yearEnd = yearStart.withDayOfYear(yearStart.lengthOfYear());
            String yearLabel = "Year " + yearStart.getYear();
            
            // Sum all days in this year from dataMap
            int yearTotalOrders = 0;
            int yearCompletedOrders = 0;
            int yearCanceledOrders = 0;
            int yearRefundedOrders = 0;
            BigDecimal yearNetRevenue = BigDecimal.ZERO;
            
            LocalDate currentDay = yearStart;
            LocalDate actualYearEnd = yearEnd.isAfter(endDate) ? endDate : yearEnd;
            
            while (!currentDay.isAfter(yearEnd) && !currentDay.isAfter(endDate)) {
                String dayStr = currentDay.toString();
                Map<String, Object> dayDataFromDB = dataMap.getOrDefault(dayStr, new HashMap<>());
                yearTotalOrders += (Integer) dayDataFromDB.getOrDefault("totalOrders", 0);
                yearCompletedOrders += (Integer) dayDataFromDB.getOrDefault("completedOrders", 0);
                yearCanceledOrders += (Integer) dayDataFromDB.getOrDefault("canceledOrders", 0);
                yearRefundedOrders += (Integer) dayDataFromDB.getOrDefault("refundedOrders", 0);
                yearNetRevenue = yearNetRevenue.add((BigDecimal) dayDataFromDB.getOrDefault("netRevenue", BigDecimal.ZERO));
                currentDay = currentDay.plusDays(1);
            }
            
            // Calculate year AOV
            BigDecimal yearAov = yearTotalOrders > 0 ? 
                yearNetRevenue.divide(new BigDecimal(yearTotalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            Map<String, Object> yearData = new HashMap<>();
            yearData.put("date", yearStart.toString()); // Use year start as date
            yearData.put("totalOrders", yearTotalOrders);
            yearData.put("completedOrders", yearCompletedOrders);
            yearData.put("canceledOrders", yearCanceledOrders);
            yearData.put("refundedOrders", yearRefundedOrders);
            yearData.put("netRevenue", yearNetRevenue);
            yearData.put("aov", yearAov);
            yearData.put("period", "yearly");
            yearData.put("dateRange", yearLabel);
            yearData.put("year", yearStart.getYear());
            yearData.put("startDate", yearStart.toString());
            yearData.put("endDate", actualYearEnd.toString());
            
            result.add(yearData);
            yearStart = yearStart.plusYears(1);
        }
        
        return result;
    }
    
    // ============================================================================
    // QUARTER HELPER METHODS (Copy from BookServiceImpl)
    // ============================================================================
    
    private LocalDate getQuarterStart(LocalDate date) {
        int month = date.getMonthValue();
        if (month <= 3) {
            return date.withMonth(1).withDayOfMonth(1); // Q1: Jan 1
        } else if (month <= 6) {
            return date.withMonth(4).withDayOfMonth(1); // Q2: Apr 1
        } else if (month <= 9) {
            return date.withMonth(7).withDayOfMonth(1); // Q3: Jul 1
        } else {
            return date.withMonth(10).withDayOfMonth(1); // Q4: Oct 1
        }
    }
    
    private LocalDate getQuarterEnd(LocalDate quarterStart) {
        return quarterStart.plusMonths(3).minusDays(1);
    }
    
    private int getQuarterNumber(LocalDate date) {
        int month = date.getMonthValue();
        return (month - 1) / 3 + 1;
    }
}
