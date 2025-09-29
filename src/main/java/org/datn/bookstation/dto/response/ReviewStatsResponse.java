package org.datn.bookstation.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewStatsResponse {
    private long total;
    private long approved; 
    private long edited;    
    private long hidden;  
}


