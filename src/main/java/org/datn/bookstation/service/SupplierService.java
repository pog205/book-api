package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.SupplierRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Supplier;

import java.util.List;

public interface SupplierService {
    PaginationResponse<SupplierRepuest> getAllWithPagination(int page, int size, String supplierName, String contactName, String email, String status);

    void addSupplier(SupplierRepuest request);

    void editSupplier(SupplierRepuest request);

    void deleteSupplier(Integer id);

    void upStatus(Integer id, byte status, String updatedBy);
    
    List<Supplier> getActiveSuppliers(); // For dropdown
}
