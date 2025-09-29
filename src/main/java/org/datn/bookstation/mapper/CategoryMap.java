package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.ParentCategoryResponse;
import org.datn.bookstation.entity.Category;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CategoryMap {

    public List<ParentCategoryResponse> mapToCategoryTreeList(List<Category> categories) {
        // Lấy tất cả các ID có trong danh sách
        Set<Integer> existingIds = categories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        // Lấy danh sách các danh mục gốc (root categories)
        // Bao gồm cả những danh mục có parentCategory nhưng cha không tồn tại trong
        // list
        List<Category> rootCategories = categories.stream()
                .filter(category -> category.getParentCategory() == null ||
                        !existingIds.contains(category.getParentCategory().getId()))
                .toList();

        // Xây dựng cây danh mục
        List<ParentCategoryResponse> tree = new ArrayList<>();
        for (Category rootCategory : rootCategories) {
            tree.add(buildCategoryTree(rootCategory, categories));
        }

        return tree;
    }

    private ParentCategoryResponse buildCategoryTree(Category category, List<Category> allCategories) {
        ParentCategoryResponse dto = new ParentCategoryResponse();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setDescription(category.getDescription());
        dto.setStatus(category.getStatus());

        // Khởi tạo danh sách con nếu chưa có
        if (dto.getParentCategory() == null) {
            dto.setParentCategory(new ArrayList<>());
        }

        // Tìm danh mục con
        List<Category> childCategories = allCategories.stream()
                .filter(c -> c.getParentCategory() != null &&
                        c.getParentCategory().getId().equals(category.getId()))
                .toList();

        // Đệ quy cho từng danh mục con
        for (Category childCategory : childCategories) {
            dto.getParentCategory().add(buildCategoryTree(childCategory, allCategories));
        }

        return dto;
    }
}
