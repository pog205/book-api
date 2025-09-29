package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.PointRequest;
import org.datn.bookstation.dto.response.PointResponse;
import org.datn.bookstation.entity.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PointMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) 
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Point toPoint(PointRequest request);
    
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "order.code", target = "orderCode")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    PointResponse toPointResponse(Point point);
}
