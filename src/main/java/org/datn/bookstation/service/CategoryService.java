package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;

import java.util.List;

public interface CategoryService {
    ApiResponse<List<Category>> getAll();

    ApiResponse<Category> add(Category category);

    ApiResponse<Category> getById(Integer id);

    ApiResponse<Category> update(Category category, Integer id);

    ApiResponse<Category> delete(Integer id);

    ApiResponse<List<Category>> getActiveCategories(); // For dropdown

    ApiResponse<List<Category>> getAllExceptById(Integer id); // localhost:8080/api/categories/except/1

    ApiResponse<PaginationResponse<ParentCategoryResponse>> getAllCategoryPagination(Integer page, Integer size,
                                                                                     String name, Byte status);

    ApiResponse<List<ParentCategoryResponse>> getAllCategoryPagination();

    ApiResponse<Category> toggleStatus(Integer id);

    ApiResponse<List<Category>> getAllByParentIsNull();

    ApiResponse<List<Category>> getAllByParenId(Integer id);

    ApiResponse<List<Category>> getAllByParentIsNotNull();

    ApiResponse<List<Category>> getAllExceptByIdNotNull(Integer id);
}
