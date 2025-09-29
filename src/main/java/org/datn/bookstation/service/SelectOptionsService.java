package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.SelectOptions;
import java.util.List;

public interface SelectOptionsService {
    ApiResponse<List<SelectOptions.BookOption>> getBookOptions();
    ApiResponse<List<SelectOptions.UserOption>> getUserOptions();

    ApiResponse<List<SelectOptions.UserOption>> getCustomerOptions();
    ApiResponse<List<SelectOptions.UserOption>> getAdminOptions();
    ApiResponse<List<SelectOptions.UserOption>> getStaffOptions();
}
