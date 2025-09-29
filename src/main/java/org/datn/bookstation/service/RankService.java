package org.datn.bookstation.service;

import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.dto.request.RankRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.RankResponse;

import java.util.List;

public interface RankService {
    List<Rank> getAll();
    List<Rank> getAllActiveRanks();
    Rank getById(Integer id);
    ApiResponse<Rank> add(RankRequest rankRequest);
    Rank update(Rank rank, Integer id);
    void delete(Integer id);
    PaginationResponse<RankResponse> getAllWithPagination(int page, int size, String name, Byte status);
    ApiResponse<Rank> toggleStatus(Integer id);
}
