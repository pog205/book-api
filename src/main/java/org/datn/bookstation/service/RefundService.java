package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.RefundRequestCreate;
import org.datn.bookstation.dto.request.RefundApprovalRequest;
import org.datn.bookstation.dto.response.RefundRequestResponse;
import org.datn.bookstation.entity.RefundRequest;

import java.util.List;

public interface RefundService {
    
    /**
     *  Tạo yêu cầu hoàn trả từ customer
     */
    RefundRequestResponse createRefundRequest(RefundRequestCreate request, Integer userId);
    
    /**
     *  Admin phê duyệt/từ chối yêu cầu hoàn trả
     */
    RefundRequestResponse approveRefundRequest(Integer refundRequestId, RefundApprovalRequest approval, Integer adminId);
    
    /**
     *  Admin từ chối yêu cầu hoàn trả
     */
    RefundRequestResponse rejectRefundRequest(Integer refundRequestId, RefundApprovalRequest rejection, Integer adminId);
    
    /**
     *  Xử lý hoàn trả sau khi được phê duyệt
     */
    RefundRequestResponse processRefund(Integer refundRequestId, Integer adminId);
    
    /**
     *  Lấy danh sách yêu cầu hoàn trả theo user
     */
    List<RefundRequestResponse> getRefundRequestsByUser(Integer userId);
    
    /**
     *  Admin: Lấy tất cả yêu cầu hoàn trả chờ phê duyệt
     */
    List<RefundRequestResponse> getPendingRefundRequests();
    
    /**
     *  Lấy chi tiết yêu cầu hoàn trả
     */
    RefundRequestResponse getRefundRequestById(Integer refundRequestId);

    /**
     *  Lấy tất cả yêu cầu hoàn trả (Admin, có phân trang, sort)
     */
    List<RefundRequestResponse> getAllRefundRequests(int page, int size, String sortBy, String sortDir);
    
    /**
     *  Validate yêu cầu hoàn trả
     */
    String validateRefundRequest(Integer orderId, Integer userId);
}
