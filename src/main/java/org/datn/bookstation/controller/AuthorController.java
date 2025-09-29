package org.datn.bookstation.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/authors")
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    public ApiResponse<List<Author>> getAll() {
        return authorService.getAll();
    }


    @GetMapping("/page")
    public ApiResponse<PaginationResponse<Author>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte status) {
        return authorService.getAllAuthorPagination(page, size, name, status);
    }

    @GetMapping("/{id}")
    public ApiResponse<Author> getById(@PathVariable Integer id) {
        return authorService.getById(id);
    }

    // ✅ Sửa ADD với validation đầy đủ
    @PostMapping
    public ResponseEntity<ApiResponse<Author>> add( @RequestBody Author author) {
        ApiResponse<Author> response = authorService.add(author);

        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, response.getMessage(), null));
        }

        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, response.getMessage(), null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Tạo tác giả thành công", response.getData()));
    }

    // ✅ Sửa UPDATE với validation đầy đủ
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Author>> update(@PathVariable Integer id,  @RequestBody Author author) {
        ApiResponse<Author> response = authorService.update(author, id);

        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, response.getMessage(), null));
        }

        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, response.getMessage(), null));
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "Cập nhật tác giả thành công", response.getData()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Author> delete(@PathVariable Integer id) {
        return authorService.delete(id);
    }

    @PatchMapping("/{id}/toggle-status")
    public ApiResponse<Author> toggleStatus(@PathVariable Integer id) {
        return authorService.toggleStatus(id);
    }

    @GetMapping("/dropdown")
    public ApiResponse<List<DropdownOptionResponse>> getDropdownAuthors() {
        ApiResponse<List<Author>> authorsResponse = authorService.getActiveAuthors();

        if (authorsResponse.getStatus() != 200) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách tác giả", null);
        }

        List<DropdownOptionResponse> dropdown = authorsResponse.getData().stream()
                .map(author -> new DropdownOptionResponse(author.getId(), author.getAuthorName()))
                .collect(Collectors.toList());

        return new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách tác giả thành công", dropdown);
    }
}
