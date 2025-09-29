package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.math.BigDecimal;

// Lombok annotations only for inner classes
public class CheckoutSessionRequest {
    @NotNull(message = "Danh sách sản phẩm không được để trống")
    @Size(min = 1, message = "Phải có ít nhất 1 sản phẩm")
    private List<BookQuantity> items;
    
    // Address information
    private Integer addressId;
    
    // Shipping information
    private String shippingMethod;
    private BigDecimal shippingFee; // Phí vận chuyển
    private Long estimatedDeliveryFrom;
    private Long estimatedDeliveryTo;
    
    // Payment information
    private String paymentMethod;
    
    // Voucher information
    private List<Integer> selectedVoucherIds;
    
    // Additional notes
    private String notes;

    // Getters and setters
    public List<BookQuantity> getItems() {
        return items;
    }

    public void setItems(List<BookQuantity> items) {
        this.items = items;
    }
    
    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }
    
    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }
    
    public Long getEstimatedDeliveryFrom() {
        return estimatedDeliveryFrom;
    }

    public void setEstimatedDeliveryFrom(Long estimatedDeliveryFrom) {
        this.estimatedDeliveryFrom = estimatedDeliveryFrom;
    }
    
    public Long getEstimatedDeliveryTo() {
        return estimatedDeliveryTo;
    }

    public void setEstimatedDeliveryTo(Long estimatedDeliveryTo) {
        this.estimatedDeliveryTo = estimatedDeliveryTo;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public List<Integer> getSelectedVoucherIds() {
        return selectedVoucherIds;
    }

    public void setSelectedVoucherIds(List<Integer> selectedVoucherIds) {
        this.selectedVoucherIds = selectedVoucherIds;
    }
    
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public static class BookQuantity {
        @NotNull(message = "Book ID không được để trống")
        private Integer bookId;

        @NotNull(message = "Số lượng không được để trống")
        private Integer quantity;

        public Integer getBookId() {
            return bookId;
        }

        public void setBookId(Integer bookId) {
            this.bookId = bookId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
    }
