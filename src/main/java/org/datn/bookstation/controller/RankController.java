package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.RankRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.RankResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.service.RankService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
@RequestMapping("/api/ranks")
public class RankController {
    private final RankService rankService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<RankResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte status) {
        PaginationResponse<RankResponse> ranks = rankService.getAllWithPagination(page, size, name, status);
        ApiResponse<PaginationResponse<RankResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", ranks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Rank>> getById(@PathVariable Integer id) {
        Rank rank = rankService.getById(id);
        if (rank == null) {
            ApiResponse<Rank> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<Rank> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", rank);
        return ResponseEntity.ok(response);
    }

    @PostMapping    
    public ResponseEntity<ApiResponse<Rank>> add(@RequestBody RankRequest rankRequest) {
        ApiResponse<Rank> response = rankService.add(rankRequest);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, "Tên hạng đã tồn tại", null));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo mới thành công", response.getData()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Rank>> update(@PathVariable Integer id, @RequestBody Rank rank) {
        Rank updated = rankService.update(rank, id);
        ApiResponse<Rank> response = new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật thành công", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        rankService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Rank>> toggleStatus(@PathVariable Integer id) {
        ApiResponse<Rank> response = rankService.toggleStatus(id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", response.getData()));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownRanks() {
        List<DropdownOptionResponse> dropdown = rankService.getAllActiveRanks().stream()
            .map(rank -> new DropdownOptionResponse(rank.getId(), rank.getRankName()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách hạng thành công", dropdown);
        return ResponseEntity.ok(response);
    }
}
