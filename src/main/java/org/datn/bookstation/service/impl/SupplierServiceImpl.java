package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.SupplierRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Supplier;
import org.datn.bookstation.repository.SupplierRepository;
import org.datn.bookstation.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl implements SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public PaginationResponse<SupplierRepuest> getAllWithPagination(int page, int size, String supplierName, String contactName, String email, String status) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Supplier> spec = Specification.where(null);

        if (supplierName != null && !supplierName.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("supplierName")), "%" + supplierName.toLowerCase() + "%"));
        }
        if (contactName != null && !contactName.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("contactName")), "%" + contactName.toLowerCase() + "%"));
        }
        if (email != null && !email.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Supplier> supplierPage = supplierRepository.findAll(spec, pageable);

        List<SupplierRepuest> responses = supplierPage.getContent().stream().map(supplier -> {
            SupplierRepuest dto = new SupplierRepuest();
            dto.setId(supplier.getId());
            dto.setSupplierName(supplier.getSupplierName());
            dto.setContactName(supplier.getContactName());
            dto.setPhoneNumber(supplier.getPhoneNumber());
            dto.setEmail(supplier.getEmail());
            dto.setAddress(supplier.getAddress());
            dto.setStatus(supplier.getStatus());
            dto.setCreatedBy(supplier.getCreatedBy());
            dto.setUpdatedBy(supplier.getUpdatedBy());
            dto.setCreatedAt(supplier.getCreatedAt() != null ? supplier.getCreatedAt() : System.currentTimeMillis());
            dto.setUpdatedAt(supplier.getUpdatedAt() != null ? supplier.getUpdatedAt() : null);
            return dto;
        }).collect(Collectors.toList());

        return new PaginationResponse<>(
                responses,
                supplierPage.getNumber(),
                supplierPage.getSize(),
                supplierPage.getTotalElements(),
                supplierPage.getTotalPages()
        );
    }

    @Override
    public void addSupplier(SupplierRepuest request) {
        // Validate email không trùng
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (supplierRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email '" + request.getEmail() + "' đã tồn tại, vui lòng sử dụng email khác");
            }
        }
        
        // Validate số điện thoại không trùng
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (supplierRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new RuntimeException("Số điện thoại '" + request.getPhoneNumber() + "' đã tồn tại, vui lòng sử dụng số điện thoại khác");
            }
        }
        
        Supplier supplier = new Supplier();
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactName(request.getContactName());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setStatus(request.getStatus() != null ? request.getStatus() : (byte) 1);
        supplier.setCreatedBy(request.getCreatedBy());
        supplier.setUpdatedBy(request.getUpdatedBy());
        supplierRepository.save(supplier);
    }

    @Override
    public void editSupplier(SupplierRepuest request) {
        Supplier supplier = supplierRepository.findById(request.getId())
            .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        // Validate email không trùng (loại trừ chính nó)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (supplierRepository.existsByEmailAndIdNot(request.getEmail(), request.getId())) {
                throw new RuntimeException("Email '" + request.getEmail() + "' đã tồn tại, vui lòng sử dụng email khác");
            }
        }
        
        // Validate số điện thoại không trùng (loại trừ chính nó)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (supplierRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), request.getId())) {
                throw new RuntimeException("Số điện thoại '" + request.getPhoneNumber() + "' đã tồn tại, vui lòng sử dụng số điện thoại khác");
            }
        }
        
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactName(request.getContactName());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            supplier.setStatus(request.getStatus());
        }
        supplier.setUpdatedBy(request.getUpdatedBy());
        supplierRepository.save(supplier);
    }

    @Override
    public void deleteSupplier(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        supplierRepository.delete(supplier);
    }

    @Override
    public void upStatus(Integer id, byte status, String updatedBy) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        supplier.setStatus(status);
        // supplier.setUpdatedAt(Instant.now());
        supplier.setUpdatedBy(updatedBy);
        supplierRepository.save(supplier);
    }

    @Override
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByStatus((byte) 1);
    }
}
