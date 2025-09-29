package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.RankResponse;
import org.datn.bookstation.entity.Rank;
import org.springframework.stereotype.Component;

@Component
public class RankResponseMapper {
    public RankResponse toResponse(Rank rank) {
        if (rank == null) return null;
        RankResponse response = new RankResponse();
        response.setId(rank.getId());
        response.setName(rank.getRankName());
        response.setMinSpent(rank.getMinSpent());
        response.setPointMultiplier(rank.getPointMultiplier());
        response.setStatus(rank.getStatus());
        return response;
    }
}
