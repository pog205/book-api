package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.SelectOptions;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.SelectOptionsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.datn.bookstation.entity.enums.RoleName;

@Service
@RequiredArgsConstructor
public class SelectOptionsServiceImpl implements SelectOptionsService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<List<SelectOptions.BookOption>> getBookOptions() {
        List<Book> books = bookRepository.findAll();
        
        List<SelectOptions.BookOption> options = books.stream()
            .map(book -> new SelectOptions.BookOption(
                book.getId(),
                book.getBookName(),
                book.getIsbn(),
                book.getBookCode()
            ))
            .collect(Collectors.toList());
            
        return new ApiResponse<>(200, "Lấy danh sách sách thành công", options);
    }

    @Override
    public ApiResponse<List<SelectOptions.UserOption>> getUserOptions() {
        List<User> users = userRepository.findAll();
        
        List<SelectOptions.UserOption> options = users.stream()
            .map(user -> new SelectOptions.UserOption(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFullName()
            ))
            .collect(Collectors.toList());
            
        return new ApiResponse<>(200, "Lấy danh sách người dùng thành công", options);
    }

    @Override
    public ApiResponse<List<SelectOptions.UserOption>> getCustomerOptions() {
        List<User> users = userRepository.findByRole_RoleName(RoleName.CUSTOMER);
        List<SelectOptions.UserOption> options = users.stream()
            .map(user -> new SelectOptions.UserOption(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFullName()
            ))
            .collect(Collectors.toList());
        return new ApiResponse<>(200, "Lấy danh sách khách hàng thành công", options);
    }

    @Override
    public ApiResponse<List<SelectOptions.UserOption>> getAdminOptions() {
        List<User> users = userRepository.findByRole_RoleName(RoleName.ADMIN);
        List<SelectOptions.UserOption> options = users.stream()
            .map(user -> new SelectOptions.UserOption(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFullName()
            ))
            .collect(Collectors.toList());
        return new ApiResponse<>(200, "Lấy danh sách admin thành công", options);
    }

    @Override
    public ApiResponse<List<SelectOptions.UserOption>> getStaffOptions() {
        List<User> users = userRepository.findByRole_RoleName(RoleName.STAFF);
        List<SelectOptions.UserOption> options = users.stream()
            .map(user -> new SelectOptions.UserOption(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFullName()
            ))
            .collect(Collectors.toList());
        return new ApiResponse<>(200, "Lấy danh sách nhân viên thành công", options);
    }
}
