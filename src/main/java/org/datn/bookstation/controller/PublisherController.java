package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.PublisherRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.entity.Publisher;
import org.datn.bookstation.service.PublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller để quản lý Nhà xuất bản
 */
@RestController
@RequestMapping("/api/publishers")
@AllArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<PublisherRequest>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status) {
        ApiResponse<PaginationResponse<PublisherRequest>> response = publisherService.getAllWithPagination(page, size, name, email, status);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> add(@Valid @RequestBody PublisherRequest request) {
        ApiResponse<Object> response = publisherService.addPublisher(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> update(@PathVariable Integer id,
            @Valid @RequestBody PublisherRequest request) {
        ApiResponse<Object> response = publisherService.editPublisher(request, id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Integer id) {
        ApiResponse<Object> response = publisherService.deletePublisher(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Object>> updateStatus(@PathVariable Integer id,
            @RequestParam byte status,
            @RequestParam(defaultValue = "1") String updatedBy) {
        ApiResponse<Object> response = publisherService.updateStatus(id, status, updatedBy);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownPublishers() {
        ApiResponse<List<DropdownOptionResponse>> response = publisherService.getActivePublishers();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<Publisher>>> getAll() {
        ApiResponse<List<Publisher>> response = publisherService.getAllPublisher();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}