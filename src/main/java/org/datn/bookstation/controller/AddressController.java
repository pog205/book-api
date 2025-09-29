package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.AddressRequest;
import org.datn.bookstation.dto.response.AddressResponse;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@AllArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // Lấy toàn bộ địa chỉ của 1 user
    @GetMapping
    public ApiResponse<List<AddressResponse>> getAddressesByUser(@RequestParam Integer userId) {
        return addressService.getAddressesByUser(userId);
    }

    // Lấy địa chỉ theo id
    @GetMapping("/{id}")
    public ApiResponse<AddressResponse> getById(@PathVariable Integer id) {
        return addressService.getById(id);
    }

    // Tạo địa chỉ mới
    @PostMapping
    public ApiResponse<AddressResponse> create(@RequestBody AddressRequest request) {
        return addressService.create(request);
    }

    // Sửa địa chỉ theo id
    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> update(@RequestBody AddressRequest request, @PathVariable Integer id) {
        return addressService.update(request, id);
    }

    // Xóa địa chỉ theo id
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        return addressService.delete(id);
    }

    // Tắt trạng thái địa chỉ (disable, không bật lại được) update
    @PatchMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Integer id) {
        return addressService.disable(id);
    }
} 