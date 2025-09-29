package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.PointResponse;
import org.datn.bookstation.entity.Point;
import org.datn.bookstation.dto.request.PointRequest;
import org.datn.bookstation.dto.response.ApiResponse;

import java.util.List;

public interface PointService {
    PaginationResponse<PointResponse> getAllWithPagination(int page, int size, String orderCode, String email, Byte status, Integer pointSpent);
    List<Point> getAll();
    Point getById(Integer id);
    ApiResponse<Point> add(PointRequest pointRequest);
    ApiResponse<Point> update(PointRequest pointRequest, Integer id);
    void delete(Integer id);
}
