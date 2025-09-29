package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.CartResponse;
import org.datn.bookstation.dto.response.CartSummaryResponse;
import org.datn.bookstation.entity.Cart;
import org.springframework.stereotype.Component;

@Component
public class CartResponseMapper {
    
    public CartResponse toResponse(Cart cart) {
        if (cart == null) return null;
        
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        response.setUserEmail(cart.getUser() != null ? cart.getUser().getEmail() : null);
        response.setUserName(cart.getUser() != null ? cart.getUser().getFullName() : null);
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        response.setCreatedBy(cart.getCreatedBy());
        response.setUpdatedBy(cart.getUpdatedBy());
        response.setStatus(cart.getStatus());
        
        return response;
    }
    
    public CartSummaryResponse toSummaryResponse(Cart cart) {
        if (cart == null) return null;
        
        CartSummaryResponse response = new CartSummaryResponse();
        response.setCartId(cart.getId());
        response.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        
        return response;
    }
}
