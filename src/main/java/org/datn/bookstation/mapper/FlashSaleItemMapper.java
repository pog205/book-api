package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.FlashSaleItemRequest;
import org.datn.bookstation.dto.response.FlashSaleItemResponse;
import org.datn.bookstation.entity.FlashSaleItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FlashSaleItemMapper {

    /* Entity → Response */
    @Mapping(source = "flashSale.id", target = "flashSaleId")
    @Mapping(source = "flashSale.name", target = "flashSaleName")
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.bookName", target = "bookName")
    FlashSaleItemResponse toResponse(FlashSaleItem item);

    /* Request → Entity */
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flashSale", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "createdAt", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "updatedAt", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    FlashSaleItem toEntity(FlashSaleItemRequest request);

    @AfterMapping
    default void setStatus(@MappingTarget FlashSaleItem item, FlashSaleItemRequest request) {
        if (request.getStatus() == null) {
            item.setStatus((byte) 1);
        } else {
            item.setStatus(request.getStatus());
        }
    }
} 