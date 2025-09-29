package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.RankRequest;
import org.datn.bookstation.entity.Rank;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RankMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rank toRank(RankRequest request);
}
