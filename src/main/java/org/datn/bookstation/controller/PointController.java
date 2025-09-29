package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.PointRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.PointResponse;
import org.datn.bookstation.entity.Point;
import org.datn.bookstation.service.PointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/points")
public class PointController {
    private final PointService pointService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<PointResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Integer pointSpent) {
        PaginationResponse<PointResponse> points = pointService.getAllWithPagination(page, size, orderCode, email, status, pointSpent);
        ApiResponse<PaginationResponse<PointResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", points);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Point>> getById(@PathVariable Integer id) {
        Point point = pointService.getById(id);
        if (point == null) {
            ApiResponse<Point> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<Point> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", point);
        return ResponseEntity.ok(response);
    }    @PostMapping
    public ResponseEntity<ApiResponse<Point>> add(@RequestBody PointRequest pointRequest) {
        ApiResponse<Point> response = pointService.add(pointRequest);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo mới thành công", response.getData()));
    } 
       @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Point>> update(@PathVariable Integer id, @RequestBody PointRequest pointRequest) {
        ApiResponse<Point> response = pointService.update(pointRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, "Dữ liệu không hợp lệ", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", response.getData()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        pointService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
