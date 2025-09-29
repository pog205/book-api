package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Author;
import org.datn.bookstation.repository.AuthorRepository;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.service.AuthorService;
import org.datn.bookstation.specification.AuthorSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public ApiResponse<List<Author>> getAll() {
        try {
            List<Author> authors = authorRepository.findAll();
            return new ApiResponse<>(200, "Lấy danh sách tác giả thành công", authors);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách tác giả: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> getById(Integer id) {
        try {
            Author author = authorRepository.findById(id).orElse(null);
            if (author == null) {
                return new ApiResponse<>(404, "Không tìm thấy tác giả với ID: " + id, null);
            }
            return new ApiResponse<>(200, "Lấy thông tin tác giả thành công", author);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy thông tin tác giả: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> add(Author author) {
        try {
            // Validate tên tác giả không được null hoặc rỗng
            if (author.getAuthorName() == null || author.getAuthorName().trim().isEmpty()) {
                return new ApiResponse<>(400, "Tên tác giả không được để trống", null);
            }

            // Validate tên tác giả đã tồn tại
            String trimmedName = author.getAuthorName().trim();
            if (authorRepository.existsByAuthorNameIgnoreCase(trimmedName)) {
                return new ApiResponse<>(400, "Tên tác giả đã tồn tại", null);
            }

            // Validate độ dài tên tác giả (max 100 ký tự theo entity)
            if (trimmedName.length() > 100) {
                return new ApiResponse<>(400, "Tên tác giả không được vượt quá 100 ký tự", null);
            }

            // Validate biography length (nếu có)
            if (author.getBiography() != null && author.getBiography().length() > 1000) {
                return new ApiResponse<>(400, "Tiểu sử không được vượt quá 1000 ký tự", null);
            }

            // Validate birth date (không được trong tương lai và phải trên 18 tuổi)
            if (author.getBirthDate() != null) {
                LocalDate today = LocalDate.now();

                // Kiểm tra không được trong tương lai
                if (author.getBirthDate().isAfter(today)) {
                    return new ApiResponse<>(400, "Ngày sinh không được trong tương lai", null);
                }

                // Kiểm tra tuổi phải trên 18
                LocalDate eighteenYearsAgo = today.minusYears(18);
                if (author.getBirthDate().isAfter(eighteenYearsAgo)) {
                    return new ApiResponse<>(400, "Tác giả phải từ 18 tuổi trở lên", null);
                }
            }

            // Reset ID và set default values
            author.setId(null);
            author.setAuthorName(trimmedName);
            author.setCreatedBy(1);

            // Set default status nếu chưa có
            if (author.getStatus() == null) {
                author.setStatus((byte) 1); // Active by default
            }

            // Validate status
            if (author.getStatus() != null && author.getStatus() != 0 && author.getStatus() != 1) {
                return new ApiResponse<>(400, "Trạng thái chỉ được là 0 hoặc 1", null);
            }

            Author savedAuthor = authorRepository.save(author);
            return new ApiResponse<>(201, "Thêm tác giả thành công", savedAuthor);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Thêm tác giả thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> update(Author author, Integer id) {
        try {
            // Validate ID
            if (id == null || id <= 0) {
                return new ApiResponse<>(400, "ID tác giả không hợp lệ", null);
            }

            // Tìm tác giả cần update
            Author authorToUpdate = authorRepository.findById(id).orElse(null);
            if (authorToUpdate == null) {
                return new ApiResponse<>(404, "Không tìm thấy tác giả với ID: " + id, null);
            }

            // Validate tên tác giả không được null hoặc rỗng
            if (author.getAuthorName() == null || author.getAuthorName().trim().isEmpty()) {
                return new ApiResponse<>(400, "Tên tác giả không được để trống", null);
            }

            // Validate độ dài tên tác giả
            String trimmedName = author.getAuthorName().trim();
            if (trimmedName.length() > 100) {
                return new ApiResponse<>(400, "Tên tác giả không được vượt quá 100 ký tự", null);
            }

            // Kiểm tra tên tác giả trùng (loại trừ chính nó)
            Author existingAuthor = authorRepository.findByAuthorNameIgnoreCase(trimmedName);
            if (existingAuthor != null && !existingAuthor.getId().equals(id)) {
                return new ApiResponse<>(400, "Tên tác giả đã tồn tại", null);
            }

            // Validate biography length
            if (author.getBiography() != null && author.getBiography().length() > 1000) {
                return new ApiResponse<>(400, "Tiểu sử không được vượt quá 1000 ký tự", null);
            }

            // Validate birth date (không được trong tương lai và phải trên 18 tuổi)
            if (author.getBirthDate() != null) {
                LocalDate today = LocalDate.now();

                // Kiểm tra không được trong tương lai
                if (author.getBirthDate().isAfter(today)) {
                    return new ApiResponse<>(400, "Ngày sinh không được trong tương lai", null);
                }

                // Kiểm tra tuổi phải trên 18
                LocalDate eighteenYearsAgo = today.minusYears(18);
                if (author.getBirthDate().isAfter(eighteenYearsAgo)) {
                    return new ApiResponse<>(400, "Tác giả phải từ 18 tuổi trở lên", null);
                }
            }

            // Validate status
            if (author.getStatus() != null && author.getStatus() != 0 && author.getStatus() != 1) {
                return new ApiResponse<>(400, "Trạng thái chỉ được là 0 hoặc 1", null);
            }

            // Update các trường
            // Update các trường
            authorToUpdate.setAuthorName(trimmedName);
            authorToUpdate.setBiography(author.getBiography());
            authorToUpdate.setBirthDate(author.getBirthDate());
            authorToUpdate.setStatus(author.getStatus());
            authorToUpdate.setUpdatedBy(1);
            authorToUpdate.setUpdatedAt(System.currentTimeMillis());

            Author updatedAuthor = authorRepository.save(authorToUpdate);
            return new ApiResponse<>(200, "Cập nhật tác giả thành công", updatedAuthor);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Cập nhật tác giả thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> delete(Integer id) {
        try {
            Author author = authorRepository.findById(id).orElse(null);
            if (author == null) {
                return new ApiResponse<>(404, "Không tìm thấy tác giả với ID: " + id, null);
            }

            // Không cho phép xóa nếu có sách thuộc tác giả này
            boolean hasBook = bookRepository.existsByAuthorBooks_Author_Id(id); // hoặc existsByAuthorBooks_Author_Id(id)
            if (hasBook) {
                return new ApiResponse<>(400, "Không thể xóa vì có sách thuộc tác giả này!", null);
            }

            authorRepository.deleteById(id);
            return new ApiResponse<>(200, "Xóa tác giả thành công", author);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Xóa tác giả thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Author> toggleStatus(Integer id) {
        try {
            Author author = authorRepository.findById(id).orElse(null);
            if (author == null) {
                return new ApiResponse<>(404, "Không tìm thấy tác giả với ID: " + id, null);
            }

            if (author.getStatus() == null) {
                author.setStatus((byte) 1);
            } else {
                author.setStatus((byte) (author.getStatus() == 1 ? 0 : 1));
            }
            author.setUpdatedBy(1); // Hoặc lấy từ user hiện tại

            Author updatedAuthor = authorRepository.save(author);
            return new ApiResponse<>(200, "Cập nhật trạng thái tác giả thành công", updatedAuthor);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Cập nhật trạng thái thất bại: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<PaginationResponse<Author>> getAllAuthorPagination(Integer page, Integer size, String name,
            Byte status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Specification<Author> spec = AuthorSpecification.filterBy(name, status);
            Page<Author> authorPage = authorRepository.findAll(spec, pageable);

            List<Author> authors = authorPage.getContent();

            PaginationResponse<Author> paginationResponse = PaginationResponse.<Author>builder()
                    .content(authors)
                    .pageNumber(authorPage.getNumber())
                    .pageSize(authorPage.getSize())
                    .totalElements(authorPage.getTotalElements())
                    .totalPages(authorPage.getTotalPages())
                    .build();

            return new ApiResponse<>(200, "Lấy danh sách tác giả phân trang thành công", paginationResponse);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách phân trang: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<Author>> getActiveAuthors() {
        try {
            List<Author> activeAuthors = authorRepository.findByStatus((byte) 1);
            return new ApiResponse<>(200, "Lấy danh sách tác giả đang hoạt động thành công", activeAuthors);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách tác giả hoạt động: " + e.getMessage(), null);
        }
    }
}
