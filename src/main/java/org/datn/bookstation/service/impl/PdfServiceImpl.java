package org.datn.bookstation.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.datn.bookstation.dto.response.PdfResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.repository.OrderRepository;
import org.datn.bookstation.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

// ✅ Thêm import này
import java.util.Base64;

@Service
public class PdfServiceImpl implements PdfService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public byte[] generateInvoicePdf(Integer orderId) throws Exception {
        PdfResponse invoiceData = getInvoiceData(orderId);
        return generateInvoicePdf(invoiceData);
    }

    @Override
    public byte[] generateInvoicePdf(PdfResponse invoiceData) throws Exception {
        Context context = new Context();

        // Set locale UTF-8
        context.setLocale(java.util.Locale.forLanguageTag("vi-VN"));

        // Thêm logo Base64
        String logoBase64 = getLogoAsBase64();
        context.setVariable("logoBase64", logoBase64);

        // Set variables
        context.setVariable("orderCode", invoiceData.getOrderCode());
        context.setVariable("orderDate", invoiceData.getOrderDate());
        context.setVariable("orderType", invoiceData.getOrderType());
        context.setVariable("paymentMethod", invoiceData.getPaymentMethod());
        context.setVariable("orderStatus", invoiceData.getOrderStatus());
        context.setVariable("notes", invoiceData.getNotes());

        context.setVariable("customerName", invoiceData.getCustomerName());
        context.setVariable("customerPhone", invoiceData.getCustomerPhone());
        context.setVariable("customerEmail", invoiceData.getCustomerEmail());
        context.setVariable("deliveryAddress", invoiceData.getDeliveryAddress());
        context.setVariable("staffName", invoiceData.getStaffName());

        context.setVariable("orderItems", invoiceData.getOrderItems());
        context.setVariable("subtotal", invoiceData.getSubtotal());
        context.setVariable("totalDiscountAmount", invoiceData.getTotalDiscountAmount());
        context.setVariable("shippingFee", invoiceData.getShippingFee());
        context.setVariable("shippingDiscount", invoiceData.getShippingDiscount());
        context.setVariable("totalAmount", invoiceData.getTotalAmount());
        context.setVariable("appliedVouchers", invoiceData.getAppliedVouchers());

        String html = templateEngine.process("invoice", context);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();

        // ✅ Load font DejaVu Sans đúng cách
        try {
            // Cách 1: Load từ resources
            InputStream fontStream = this.getClass().getResourceAsStream("/fonts/DejaVuSans.ttf");
            if (fontStream != null) {
                builder.useFont(() -> this.getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"), "DejaVu Sans");
                System.out.println("✅ DejaVu Sans font loaded from resources");
            } else {
                System.out.println("❌ Font not found in /fonts/DejaVuSans.ttf");
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading DejaVu font: " + e.getMessage());
        }

        // ✅ Cách 2: Load từ file system (backup)
        try {
            java.io.File fontFile = new java.io.File("src/main/resources/fonts/DejaVuSans.ttf");
            if (fontFile.exists()) {
                builder.useFont(fontFile, "DejaVu Sans");
                System.out.println("✅ DejaVu Sans font loaded from file system");
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading font from file system: " + e.getMessage());
        }

        builder.withHtmlContent(html, null);
        builder.toStream(os);
        builder.run();

        return os.toByteArray();
    }

    private String getLogoAsBase64() {
        try {
            // Đọc logo từ resources
            InputStream logoStream = this.getClass().getResourceAsStream("/static/images/bookstation-logo.png");
            if (logoStream == null) {
                // Fallback: tạo logo text nếu không tìm thấy file
                return "";
            }

            byte[] logoBytes = logoStream.readAllBytes();
            String base64Logo = Base64.getEncoder().encodeToString(logoBytes);
            return "data:image/png;base64," + base64Logo;

        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Trả về empty nếu có lỗi
        }
    }

    @Override
    public PdfResponse getInvoiceData(Integer orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
        }

        Order order = orderOpt.get();
        PdfResponse response = new PdfResponse();

        // Thông tin đơn hàng
        response.setOrderCode(order.getCode());
        response.setOrderDate(new Date(order.getCreatedAt()));
        response.setOrderType(order.getOrderType());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setOrderStatus(order.getOrderStatus().toString());
        response.setNotes(order.getNotes());

        // Thông tin khách hàng
        if (order.getUser() != null) {
            response.setCustomerName(order.getUser().getFullName());
            response.setCustomerPhone(order.getUser().getPhoneNumber());
            response.setCustomerEmail(order.getUser().getEmail());
        } else {
            response.setCustomerName(order.getRecipientName());
            response.setCustomerPhone(order.getPhoneNumber());
        }

        // Địa chỉ giao hàng
        if (order.getAddress() != null) {
            response.setDeliveryAddress(order.getAddress().getAddressDetail());
        }

        // Thông tin nhân viên
        if (order.getStaff() != null) {
            response.setStaffName(order.getStaff().getFullName());
        }

        // Chi tiết sản phẩm
        List<PdfResponse.OrderItemDto> orderItems = order.getOrderDetails().stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList());
        response.setOrderItems(orderItems);

        // Thông tin tài chính
        response.setSubtotal(order.getSubtotal());
        response.setTotalDiscountAmount(order.getDiscountAmount());
        response.setShippingFee(order.getShippingFee());
        response.setShippingDiscount(order.getDiscountShipping());
        response.setTotalAmount(order.getTotalAmount());

        // Voucher đã áp dụng
        List<PdfResponse.AppliedVoucherDto> appliedVouchers = order.getVouchers().stream()
                .map(this::convertToAppliedVoucherDto)
                .collect(Collectors.toList());
        response.setAppliedVouchers(appliedVouchers);

        return response;
    }

    private PdfResponse.OrderItemDto convertToOrderItemDto(OrderDetail orderDetail) {
        PdfResponse.OrderItemDto dto = new PdfResponse.OrderItemDto();
        Book book = orderDetail.getBook();

        dto.setBookName(book.getBookName());
        dto.setBookCode(book.getBookCode());
        dto.setQuantity(orderDetail.getQuantity());
        dto.setUnitPrice(orderDetail.getUnitPrice());
        dto.setVoucherDiscountAmount(orderDetail.getVoucherDiscountAmount());

        // Tính giá gốc và giảm giá sách
        BigDecimal originalPrice = book.getPrice();
        BigDecimal effectivePrice = book.getEffectivePrice();
        dto.setOriginalPrice(originalPrice);

        // Kiểm tra flash sale
        if (orderDetail.getFlashSaleItem() != null) {
            dto.setIsFlashSale(true);
            dto.setFlashSalePrice(orderDetail.getUnitPrice());
            dto.setItemDiscountAmount(originalPrice.subtract(orderDetail.getUnitPrice()));
        } else {
            dto.setIsFlashSale(false);
            dto.setItemDiscountAmount(originalPrice.subtract(effectivePrice));
        }

        // Thành tiền = (số lượng * đơn giá) - giảm giá voucher
        BigDecimal totalAmount = orderDetail.getUnitPrice()
                .multiply(BigDecimal.valueOf(orderDetail.getQuantity()))
                .subtract(orderDetail.getVoucherDiscountAmount());
        dto.setTotalAmount(totalAmount);

        return dto;
    }

    private PdfResponse.AppliedVoucherDto convertToAppliedVoucherDto(Voucher voucher) {
        PdfResponse.AppliedVoucherDto dto = new PdfResponse.AppliedVoucherDto();
        dto.setCode(voucher.getCode());
        dto.setName(voucher.getName());
        dto.setType(voucher.getVoucherCategory().toString());

        // Tính số tiền đã giảm (cần logic tính toán phức tạp hơn)
        // Tạm thời set 0, bạn có thể implement logic tính toán chi tiết
        dto.setDiscountAmount(BigDecimal.ZERO);

        return dto;
    }
}
