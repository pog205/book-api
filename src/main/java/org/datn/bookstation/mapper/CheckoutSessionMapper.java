package org.datn.bookstation.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.request.CheckoutSessionRequest;
import org.datn.bookstation.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckoutSessionMapper {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckoutSessionMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CheckoutSession toEntity(Integer userId, CheckoutSessionRequest request) {
        CheckoutSession session = new CheckoutSession();
        // Set user
        User user = new User();
        user.setId(userId);
        session.setUser(user);
        // Convert and set checkout items
        try {
            session.setCheckoutItems(objectMapper.writeValueAsString(request.getItems()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể convert checkout items", e);
        }
        // Set timestamps
        session.setCreatedAt(System.currentTimeMillis());
        session.setUpdatedAt(System.currentTimeMillis());
        // Set expiry (1 day from now)
        session.setExpiresAt(System.currentTimeMillis() + (24 * 60 * 60 * 1000L));
        // Set status (default active)
        session.setStatus((byte) 1);
        // Set shipping fee if provided
        if (request.getShippingFee() != null) {
            session.setShippingFee(request.getShippingFee());
        }
        session.setCreatedBy(userId);
        return session;
    }

    public void updateEntity(CheckoutSession existingSession, CheckoutSessionRequest request) {
        // Update danh sách sản phẩm
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            try {
                existingSession.setCheckoutItems(objectMapper.writeValueAsString(request.getItems()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Không thể convert checkout items", e);
            }
        }
        
        // Update address if provided
        if (request.getAddressId() != null) {
            Address address = new Address();
            address.setId(request.getAddressId());
            existingSession.setAddress(address);
        }
        
        // Update shipping method
        if (request.getShippingMethod() != null) {
            existingSession.setShippingMethod(request.getShippingMethod());
        }

        // Update shipping fee
        if (request.getShippingFee() != null) {
            existingSession.setShippingFee(request.getShippingFee());
        }
        
        // Update estimated delivery times
        if (request.getEstimatedDeliveryFrom() != null) {
            existingSession.setEstimatedDeliveryFrom(request.getEstimatedDeliveryFrom());
        }
        if (request.getEstimatedDeliveryTo() != null) {
            existingSession.setEstimatedDeliveryTo(request.getEstimatedDeliveryTo());
        }
        
        // Update payment method
        if (request.getPaymentMethod() != null) {
            existingSession.setPaymentMethod(request.getPaymentMethod());
        }
        
        // Update voucher selection
        if (request.getSelectedVoucherIds() != null) {
            try {
                existingSession.setSelectedVoucherIds(objectMapper.writeValueAsString(request.getSelectedVoucherIds()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Không thể convert voucher IDs", e);
            }
        }
        
        // Update notes
        if (request.getNotes() != null) {
            existingSession.setNotes(request.getNotes());
        }
        
        existingSession.setUpdatedAt(System.currentTimeMillis());
    }

    public List<Integer> parseVoucherIds(String voucherIdsJson) {
        if (voucherIdsJson == null || voucherIdsJson.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(voucherIdsJson, new TypeReference<List<Integer>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public List<CheckoutSessionRequest.BookQuantity> parseCheckoutItems(String checkoutItemsJson) {
        if (checkoutItemsJson == null || checkoutItemsJson.trim().isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(checkoutItemsJson, new TypeReference<List<CheckoutSessionRequest.BookQuantity>>() {});
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to parse checkout items JSON: {}", checkoutItemsJson, e);
            return List.of();
        }
    }
}