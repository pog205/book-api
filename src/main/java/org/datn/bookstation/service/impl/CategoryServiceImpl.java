package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;
import org.datn.bookstation.mapper.CategoryMap;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.CategoryRepository;
import org.datn.bookstation.service.CategoryService;
import org.datn.bookstation.specification.CategorySpecification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private CategoryRepository categoryRepository;
    private CategoryMap categoryMap;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public ApiResponse<List<Category>> getAll() {
        try {
            List<Category> categories = categoryRepository.findAll();
            return new ApiResponse<>(200, "Lấy danh sách danh mục thành công", categories);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách danh mục: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Category> add(Category category) {
        try {
            //  Validate tên danh mục
            if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
                return new ApiResponse<>(400, "Tên danh mục không được để trống", null);
            }

            //  Trim và kiểm tra tên trùng
            String trimmedName = category.getCategoryName().trim();
            if (categoryRepository.existsByCategoryNameIgnoreCase(trimmedName)) {
                return new ApiResponse<>(400, "Tên danh mục đã tồn tại", null);
            }

            //  Validate description length
            if (category.getDescription() != null && category.getDescription().length() > 500) {
                return new ApiResponse<>(400, "Mô tả không được vượt quá 500 ký tự", null);
            }

            //  Validate parent category
            if (category.getParentCategory() != null && category.getParentCategory().getId() != null) {
                Category parentCategory = categoryRepository.findById(category.getParentCategory().getId())
                        .orElse(null);

                if (parentCategory == null) {
                    return new ApiResponse<>(404, "Danh mục cha không tồn tại", null);
                }

                category.setParentCategory(parentCategory);
            } else {
                category.setParentCategory(null);
            }
            System.out.println(category.getId());
            //  Set default values
            category.setId(null);
            category.setCategoryName(trimmedName);
            category.setCreatedBy(1);

            //  Set default status nếu chưa có
            if (category.getStatus() == null) {
                category.setStatus((byte) 1);
            }

            Category savedCategory = categoryRepository.save(category);
            return new ApiResponse<>(201, "Thêm danh mục thành công", savedCategory);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Thêm danh mục thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Category> getById(Integer id) {
        try {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null) {
                return new ApiResponse<>(404, "Không tìm thấy danh mục với ID: " + id, null);
            }
            return new ApiResponse<>(200, "Lấy thông tin danh mục thành công", category);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy thông tin danh mục: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Category> update(Category category, Integer id) {
        try {
            //  Kiểm tra tên trùng (loại trừ chính nó)
            String trimmedName = category.getCategoryName().trim();
            Category existingCategory = categoryRepository.findByCategoryNameIgnoreCase(trimmedName);
            if (existingCategory != null && !existingCategory.getId().equals(id)) {
                return new ApiResponse<>(400, "Tên danh mục đã tồn tại", null);
            }

            //  Validate ID
            if (id == null || id <= 0) {
                return new ApiResponse<>(400, "ID danh mục không hợp lệ", null);
            }
            Category categoryById = categoryRepository.findById(id).orElse(null);
            if (categoryById == null) {
                return new ApiResponse<>(404, "Không tìm thấy danh mục với ID: " + id, null);
            }
            //  Validate tên danh mục
            if (category.getParentCategory() != null && category.getParentCategory().getId() != null) {
                // Không thể tự làm cha của chính mình
                if (category.getParentCategory().getId().equals(id)) {
                    return new ApiResponse<>(400, "Danh mục không thể làm cha của chính nó", null);
                }

                // Kiểm tra parent category có tồn tại
                Category parentCategory = categoryRepository.findById(category.getParentCategory().getId())
                        .orElse(null);
                if (parentCategory == null) {
                    return new ApiResponse<>(404, "Danh mục cha không tồn tại", null);
                }

                categoryById.setParentCategory(parentCategory);
            } else {
                categoryById.setParentCategory(null);
            }
            categoryById.setCategoryName(category.getCategoryName());
            categoryById.setDescription(category.getDescription());
            categoryById.setStatus(category.getStatus());
            categoryById.setUpdatedBy(1);

            //  Không cho phép sửa nếu id này là cha của danh mục khác
            boolean isParentOfAny = categoryRepository.existsByParentCategoryId(id);
            if (isParentOfAny) {
                return new ApiResponse<>(400, "Không thể sửa vì danh mục này đang là cha của danh mục khác!", null);
            }

            categoryById.setId(id);
            Category updatedCategory = categoryRepository.save(categoryById);
            return new ApiResponse<>(200, "Cập nhật danh mục thành công", updatedCategory);
        } catch (Exception e) {
            return new ApiResponse<>(400, "Cập nhật danh mục thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Category> delete(Integer id) {
        try {
            Category categoryById = categoryRepository.findById(id).orElse(null);
            if (categoryById == null) {
                return new ApiResponse<>(404, "Không tìm thấy danh mục với ID: " + id, null);
            }

            //  Không cho phép xóa nếu id này là cha của danh mục khác
            boolean isParentOfAny = categoryRepository.existsByParentCategoryId(id);
            if (isParentOfAny) {
                return new ApiResponse<>(400, "Không thể xóa vì danh mục này đang là cha của danh mục khác!", null);
            }

            //  Không cho phép xóa nếu có sách thuộc danh mục này
            boolean hasBook = bookRepository.existsByCategoryId(id);
            if (hasBook) {
                return new ApiResponse<>(400, "Không thể xóa vì có sách thuộc danh mục này!", null);
            }

            categoryRepository.delete(categoryById);
            return new ApiResponse<>(200, "Xóa danh mục thành công", categoryById);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Xóa danh mục thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<Category>> getActiveCategories() {
        try {
            List<Category> activeCategories = categoryRepository.findByStatus((byte) 1);
            return new ApiResponse<>(200, "Lấy danh sách danh mục hoạt động thành công", activeCategories);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách danh mục hoạt động: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<Category>> getAllExceptById(Integer id) {
        try {
            List<Category> categories = categoryRepository.getAllExceptByID(id);
            return new ApiResponse<>(200, "Lấy danh sách danh mục (trừ ID: " + id + ") thành công", categories);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách danh mục: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<PaginationResponse<ParentCategoryResponse>> getAllCategoryPagination(Integer page, Integer size,
            String name, Byte status) {
        try {
            Specification<Category> spec = CategorySpecification.filterBy(name, status);
            List<Category> categoriesSpec = categoryRepository.findAll(spec);

            // Tạo cây danh mục
            List<ParentCategoryResponse> parentCategoryResponseList = categoryMap.mapToCategoryTreeList(categoriesSpec);

            // Phân trang thủ công
            int totalElements = parentCategoryResponseList.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            // Tính toán vị trí bắt đầu và kết thúc
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            // Lấy phần tử theo trang
            List<ParentCategoryResponse> pagedCategories;
            if (startIndex >= totalElements) {
                pagedCategories = new ArrayList<>();
            } else {
                pagedCategories = parentCategoryResponseList.subList(startIndex, endIndex);
            }

            // Tạo PaginationResponse
            PaginationResponse<ParentCategoryResponse> paginationResponse = PaginationResponse
                    .<ParentCategoryResponse>builder()
                    .content(pagedCategories)
                    .pageNumber(page)
                    .pageSize(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .build();

            return new ApiResponse<>(200, "Lấy danh sách danh mục phân trang thành công", paginationResponse);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách phân trang: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<ParentCategoryResponse>> getAllCategoryPagination() {
        try {
            List<Category> getAll = categoryRepository.findAll();
            List<ParentCategoryResponse> parentCategoryResponseList = categoryMap.mapToCategoryTreeList(getAll);

            return new ApiResponse<>(200, "Lấy danh sách danh mục phân trang thành công", parentCategoryResponseList);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Category> toggleStatus(Integer id) {
        try {
            Category category = categoryRepository.findById(id).orElse(null);

            if (category == null) {
                return new ApiResponse<>(404, "Category không tồn tại", null);
            }

            if (category.getStatus() == null) {
                category.setStatus((byte) 1);
            } else {
                category.setStatus((byte) (category.getStatus() == 1 ? 0 : 1));
            }

            category.setUpdatedBy(1);
            Category updatedCategory = categoryRepository.save(category);

            return new ApiResponse<>(200, "Cập nhật trạng thái category thành công", updatedCategory);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Cập nhật trạng thái thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<Category>> getAllByParentIsNull() {
        try {
            List<Category> categories = categoryRepository.getAllByParentIsNull();
            return new ApiResponse<>(200, "Lấy danh sách danh mục cha thành công", categories);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh mục cha: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<Category>> getAllByParenId(Integer id) {
        try {
            List<Category> categories = categoryRepository.getALlByParentId(id);
            return new ApiResponse<>(200, "Lấy danh sách danh mục con thành công", categories);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh mục con: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<Category>> getAllByParentIsNotNull() {
        return new ApiResponse<>(200, "Lấy danh mục con thành công", categoryRepository.getAllByParentIsNotNull());
    }

    @Override
    public ApiResponse<List<Category>> getAllExceptByIdNotNull(Integer id) {
        return new ApiResponse<>(200, "Lấy danh mục con thành công", categoryRepository.getAllExceptByIdNotNull(id));

    }
}
