package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.PublisherRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Publisher;

import java.util.List;

public interface PublisherService {
    ApiResponse<PaginationResponse<PublisherRequest>> getAllWithPagination(int page, int size, String publisherName, String email, String status);

    ApiResponse<Object> addPublisher(PublisherRequest request);

    ApiResponse<Object> editPublisher(PublisherRequest request, Integer id);

    ApiResponse<Object> deletePublisher(Integer id);

    ApiResponse<Object> updateStatus(Integer id, byte status, String updatedBy);
    
    ApiResponse<List<DropdownOptionResponse>> getActivePublishers(); // For dropdown

    ApiResponse<List<Publisher>> getAllPublisher();

}
