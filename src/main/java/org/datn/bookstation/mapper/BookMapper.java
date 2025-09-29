package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.BookRequest;
import org.datn.bookstation.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {
    
    default String mapImagesToString(java.util.List<String> images) {
        if (images == null || images.isEmpty()) return null;
        return String.join(",", images);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "authorBooks", ignore = true)
    @Mapping(target = "bookFormat", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    @Mapping(target = "images", expression = "java(mapImagesToString(request.getImages()))")
    @Mapping(target = "discountValue", source = "discountValue")
    @Mapping(target = "discountPercent", source = "discountPercent")
    @Mapping(target = "discountActive", source = "discountActive")
    Book toEntity(BookRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "authorBooks", ignore = true)
    @Mapping(target = "bookFormat", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    @Mapping(target = "images", expression = "java(mapImagesToString(request.getImages()))")
    @Mapping(target = "discountValue", source = "discountValue")
    @Mapping(target = "discountPercent", source = "discountPercent")
    @Mapping(target = "discountActive", source = "discountActive")
    void updateEntity(BookRequest request, @MappingTarget Book book);

}
