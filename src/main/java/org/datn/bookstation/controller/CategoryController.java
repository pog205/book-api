package org.datn.bookstation.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@AllArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<Category>> getAll() {
        return categoryService.getAll();
    }

    @GetMapping("/parentcategories")
    public ApiResponse<PaginationResponse<ParentCategoryResponse>> getAllWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte status) {
        return categoryService.getAllCategoryPagination(page, size, name, status);
    }

    @GetMapping("/{id}")
    public ApiResponse<Category> getById(@PathVariable Integer id) {
        return categoryService.getById(id);
    }

    @GetMapping("/except/{id}")
    public ApiResponse<List<Category>> getAllExceptById(@PathVariable Integer id) {
        return categoryService.getAllExceptById(id);
    }

    @GetMapping("/dropdown")
    public ApiResponse<List<DropdownOptionResponse>> getDropdownCategories() {
        ApiResponse<List<Category>> categoriesResponse = categoryService.getActiveCategories();

        if (categoriesResponse.getStatus() != 200) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách danh mục", null);
        }

        List<DropdownOptionResponse> dropdown = categoriesResponse.getData().stream()
                .map(category -> new DropdownOptionResponse(category.getId(), category.getCategoryName()))
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Lấy danh sách danh mục thành công", dropdown);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> add(@RequestBody Category category) {
        System.out.println(category.toString());

        ApiResponse<Category> response = categoryService.add(category);

        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, response.getMessage(), null));
        }

        if (response.getStatus() == 400) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)

                    .body(new ApiResponse<>(400, response.getMessage(), null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Tạo danh mục thành công", response.getData()));
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Integer id, @RequestBody Category category) {
        return categoryService.update(category, id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Category> delete(@PathVariable Integer id) {
        return categoryService.delete(id);
    }

    @PatchMapping("/{id}/toggle-status")
    public ApiResponse<Category> toggleStatus(@PathVariable Integer id) {
        return categoryService.toggleStatus(id);
    }

    @GetMapping("/fiter")
    public ApiResponse<List<ParentCategoryResponse>> getAllCategoriesForUser() {
        return categoryService.getAllCategoryPagination();
    }

    @GetMapping("/parent-null")
    public ApiResponse<List<Category>> getAllByParentIsNull() {
        return categoryService.getAllByParentIsNull();
    }

    @GetMapping("/parent/{id}")
    public ApiResponse<List<Category>> getAllByParenId(@PathVariable Integer id) {
        return categoryService.getAllByParenId(id);
    }

    @GetMapping("/parent-not-null")
    public ApiResponse<List<Category>> getAllByParentIsNotNull() {
        System.out.println(categoryService.getAllByParentIsNotNull());
        return categoryService.getAllByParentIsNotNull();
    }

    @GetMapping("/parent-excep-not-null/{id}")
    public ApiResponse<List<Category>> getAllExceptByIdNotNull(@PathVariable int id) {
        return categoryService.getAllExceptByIdNotNull(id);
    }

}
