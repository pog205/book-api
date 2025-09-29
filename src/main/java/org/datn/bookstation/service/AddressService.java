package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.AddressRequest;
import org.datn.bookstation.dto.response.AddressResponse;
import org.datn.bookstation.dto.response.ApiResponse;

import java.util.List;

public interface AddressService {
    ApiResponse<List<AddressResponse>> getAddressesByUser(Integer userId);
    ApiResponse<AddressResponse> getById(Integer id);
    ApiResponse<AddressResponse> create(AddressRequest request);
    ApiResponse<AddressResponse> update(AddressRequest request, Integer id);
    ApiResponse<Void> delete(Integer id);
    ApiResponse<Void> disable(Integer id);
} 