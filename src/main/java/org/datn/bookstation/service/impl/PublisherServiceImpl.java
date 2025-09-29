package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.PublisherRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Publisher;
import org.datn.bookstation.repository.PublisherRepository;
import org.datn.bookstation.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublisherServiceImpl implements PublisherService {
    @Autowired
    private PublisherRepository publisherRepository;

    @Override
    public ApiResponse<PaginationResponse<PublisherRequest>> getAllWithPagination(int page, int size, String publisherName, String email, String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Specification<Publisher> spec = null;

            if (publisherName != null && !publisherName.isEmpty()) {
                Specification<Publisher> nameSpec = (root, query, cb) -> cb.like(cb.lower(root.get("publisherName")), "%" + publisherName.toLowerCase() + "%");
                spec = spec == null ? nameSpec : spec.and(nameSpec);
            }
            if (email != null && !email.isEmpty()) {
                Specification<Publisher> emailSpec = (root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
                spec = spec == null ? emailSpec : spec.and(emailSpec);
            }
            if (status != null && !status.isEmpty()) {
                Specification<Publisher> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), status);
                spec = spec == null ? statusSpec : spec.and(statusSpec);
            }

            Page<Publisher> publisherPage = publisherRepository.findAll(spec, pageable);

            List<PublisherRequest> responses = publisherPage.getContent().stream().map(publisher -> {
                PublisherRequest dto = new PublisherRequest();
                dto.setId(publisher.getId());
                dto.setPublisherName(publisher.getPublisherName());
                dto.setPhoneNumber(publisher.getPhoneNumber());
                dto.setEmail(publisher.getEmail());
                dto.setAddress(publisher.getAddress());
                dto.setWebsite(publisher.getWebsite());
                dto.setEstablishedYear(publisher.getEstablishedYear());
                dto.setDescription(publisher.getDescription());
                dto.setStatus(publisher.getStatus() != null ? publisher.getStatus().toString() : "1");
                dto.setCreatedBy(publisher.getCreatedBy() != null ? publisher.getCreatedBy().toString() : "1");
                dto.setUpdatedBy(publisher.getUpdatedBy() != null ? publisher.getUpdatedBy().toString() : "1");
                return dto;
            }).collect(Collectors.toList());

            PaginationResponse<PublisherRequest> pagination = new PaginationResponse<>(
                    responses,
                    publisherPage.getNumber(),
                    publisherPage.getSize(),
                    publisherPage.getTotalElements(),
                    publisherPage.getTotalPages()
            );

            return new ApiResponse<>(200, "Thành công", pagination);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi hệ thống", null);
        }
    }

    @Override
    public ApiResponse<Object> addPublisher(PublisherRequest request) {
        try {
            // Validate email không trùng
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (publisherRepository.existsByEmail(request.getEmail())) {
                    return new ApiResponse<>(409, "Email '" + request.getEmail() + "' đã tồn tại, vui lòng sử dụng email khác", null);
                }
            }
            
            // Validate số điện thoại không trùng
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                if (publisherRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    return new ApiResponse<>(409, "Số điện thoại '" + request.getPhoneNumber() + "' đã tồn tại, vui lòng sử dụng số điện thoại khác", null);
                }
            }
            
            Publisher publisher = new Publisher();
            publisher.setPublisherName(request.getPublisherName());
            publisher.setPhoneNumber(request.getPhoneNumber());
            publisher.setEmail(request.getEmail());
            publisher.setAddress(request.getAddress());
            publisher.setWebsite(request.getWebsite()); // Có thể null
            publisher.setEstablishedYear(request.getEstablishedYear());
            publisher.setDescription(request.getDescription()); // Có thể null
            publisher.setStatus((byte) 1); // Trạng thái luôn là 1

            // Tự động gán createBy và updateBy
            publisher.setCreatedBy(1); // Default system user
            publisher.setUpdatedBy(1);

            publisherRepository.save(publisher);
            return new ApiResponse<>(201, "Tạo nhà xuất bản thành công", null);
        } catch (DataIntegrityViolationException e) {
            return new ApiResponse<>(409, "Tên nhà xuất bản đã tồn tại", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi hệ thống", null);
        }
    }

    @Override
    public ApiResponse<Object> editPublisher(PublisherRequest request, Integer id) {
        try {
            Publisher publisher = publisherRepository.findById(id)
                    .orElse(null);
            
            if (publisher == null) {
                return new ApiResponse<>(404, "Nhà xuất bản không tồn tại", null);
            }

            // Validate email không trùng (loại trừ chính nó)
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (publisherRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                    return new ApiResponse<>(409, "Email '" + request.getEmail() + "' đã tồn tại, vui lòng sử dụng email khác", null);
                }
            }
            
            // Validate số điện thoại không trùng (loại trừ chính nó)
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                if (publisherRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
                    return new ApiResponse<>(409, "Số điện thoại '" + request.getPhoneNumber() + "' đã tồn tại, vui lòng sử dụng số điện thoại khác", null);
                }
            }

            publisher.setPublisherName(request.getPublisherName());
            publisher.setPhoneNumber(request.getPhoneNumber());
            publisher.setEmail(request.getEmail());
            publisher.setAddress(request.getAddress());
            publisher.setWebsite(request.getWebsite()); // Có thể null
            publisher.setEstablishedYear(request.getEstablishedYear());
            publisher.setDescription(request.getDescription()); // Có thể null
            publisher.setStatus((byte) 1); // Trạng thái luôn là 1

            // Tự động gán updateBy
            publisher.setUpdatedBy(1);

            publisherRepository.save(publisher);
            return new ApiResponse<>(200, "Cập nhật nhà xuất bản thành công", null);
        } catch (DataIntegrityViolationException e) {
            return new ApiResponse<>(409, "Tên nhà xuất bản đã tồn tại", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi hệ thống", null);
        }
    }

    @Override
    public ApiResponse<Object> deletePublisher(Integer id) {
        try {
            Publisher publisher = publisherRepository.findById(id)
                    .orElse(null);
            
            if (publisher == null) {
                return new ApiResponse<>(404, "Nhà xuất bản không tồn tại", null);
            }
            
            publisherRepository.delete(publisher);
            return new ApiResponse<>(200, "Xóa nhà xuất bản thành công", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi hệ thống", null);
        }
    }

    @Override
    public ApiResponse<Object> updateStatus(Integer id, byte status, String updatedBy) {
        try {
            Publisher publisher = publisherRepository.findById(id)
                    .orElse(null);
            
            if (publisher == null) {
                return new ApiResponse<>(404, "Nhà xuất bản không tồn tại", null);
            }
            
            publisher.setStatus(status);
            publisher.setUpdatedBy(1); // Tự động gán updateBy
            publisherRepository.save(publisher);
            return new ApiResponse<>(200, "Cập nhật trạng thái thành công", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi hệ thống", null);
        }
    }

    @Override
    public ApiResponse<List<DropdownOptionResponse>> getActivePublishers() {
        try {
            List<Publisher> publishers = publisherRepository.findByStatus((byte) 1);
            List<DropdownOptionResponse> dropdown = publishers.stream()
                    .map(publisher -> new DropdownOptionResponse(publisher.getId(), publisher.getPublisherName()))
                    .collect(Collectors.toList());
            return new ApiResponse<>(200, "Lấy danh sách nhà xuất bản thành công", dropdown);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi hệ thống", null);
        }
    }

    @Override
    public ApiResponse<List<Publisher>> getAllPublisher() {
        try {
            List<Publisher> publishers = publisherRepository.findAll();
            return new ApiResponse<>(200, "Lấy danh sách nhà xuất bản thành công", publishers);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi hệ thống", null);
        }
    }
}