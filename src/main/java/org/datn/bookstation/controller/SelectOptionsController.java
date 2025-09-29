package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.SelectOptions;
import org.datn.bookstation.service.SelectOptionsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/select")
@RequiredArgsConstructor
public class SelectOptionsController {

    private final SelectOptionsService selectOptionsService;
    
    @GetMapping("/books")
    public ApiResponse<List<SelectOptions.BookOption>> getBookOptions() {
        return selectOptionsService.getBookOptions();
    }
    
    @GetMapping("/users")
    public ApiResponse<List<SelectOptions.UserOption>> getUserOptions() {
        return selectOptionsService.getUserOptions();
    }

    @GetMapping("/users/customers")
    public ApiResponse<List<SelectOptions.UserOption>> getCustomers() {
        return selectOptionsService.getCustomerOptions();
    }

    @GetMapping("/users/admins")
    public ApiResponse<List<SelectOptions.UserOption>> getAdmins() {
        return selectOptionsService.getAdminOptions();
    }

    @GetMapping("/users/staffs")
    public ApiResponse<List<SelectOptions.UserOption>> getStaffs() {
        return selectOptionsService.getStaffOptions();
    }

}
