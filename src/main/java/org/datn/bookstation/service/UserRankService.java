package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.UserRankRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserRankResponse;
import org.datn.bookstation.dto.response.UserRankSimpleResponse;
import org.datn.bookstation.entity.UserRank;
import java.util.List;

public interface UserRankService {
    PaginationResponse<UserRankResponse> getAllWithPagination(int page, int size, Integer userId, Integer rankId, Byte status, String userEmail, String rankName);
    List<UserRank> getAll();
    UserRank getById(Integer id);
    ApiResponse<UserRank> add(UserRankRequest request);
    ApiResponse<UserRank> update(UserRankRequest request, Integer id);
    void delete(Integer id);
    ApiResponse<UserRank> toggleStatus(Integer id);
    List<UserRankSimpleResponse> getByRankId(Integer rankId);
    PaginationResponse<UserRankSimpleResponse> getByRankIdWithFilter(int page, int size, Integer rankId, String email, String userName);
}
