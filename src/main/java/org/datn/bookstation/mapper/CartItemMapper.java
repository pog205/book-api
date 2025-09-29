package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.CartItemRequest;
import org.datn.bookstation.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "flashSaleItem", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", expression = "java((byte) 1)")
    @Mapping(target = "selected", expression = "java(true)")
    CartItem toEntity(CartItemRequest request);
}
