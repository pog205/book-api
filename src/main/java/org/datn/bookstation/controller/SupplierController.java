package org.datn.bookstation.controller;

import org.datn.bookstation.dto.request.SupplierRepuest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public PaginationResponse<SupplierRepuest> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status
    ) {
        return supplierService.getAllWithPagination(page, size, supplierName, contactName, email, status);
    }

    @PostMapping
    public void addSupplier(@RequestBody SupplierRepuest request) {
        supplierService.addSupplier(request);
    }

    @PutMapping
    public void editSupplier(@RequestBody SupplierRepuest request) {
        supplierService.editSupplier(request);
    }

    @DeleteMapping("/{id}")
    public void deleteSupplier(@PathVariable Integer id) {
        supplierService.deleteSupplier(id);
    }

    @PatchMapping("/status")
    public void upStatus(
            @RequestParam Integer id,
            @RequestParam byte status,
            @RequestParam String updatedBy
    ) {
        supplierService.upStatus(id, status, updatedBy);
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownSuppliers() {
        List<DropdownOptionResponse> dropdown = supplierService.getActiveSuppliers().stream()
            .map(supplier -> new DropdownOptionResponse(supplier.getId(), supplier.getSupplierName()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách nhà cung cấp thành công", dropdown);
        return ResponseEntity.ok(response);
    }
}
