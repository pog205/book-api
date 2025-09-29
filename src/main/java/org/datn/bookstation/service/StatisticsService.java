package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.statistics.*;

public interface StatisticsService {
    UserStatisticsResponse getUserStatistics();
    RankStatisticsResponse getRankStatistics();
    PointStatisticsResponse getPointStatistics();
    PublisherStatisticsResponse getPublisherStatistics();
    SupplierStatisticsResponse getSupplierStatistics();
}
