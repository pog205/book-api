package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.QuickActionRequest;
import org.datn.bookstation.dto.response.*;

/**
 * Service interface for Advanced Business Intelligence Analytics
 * Provides AI-powered dashboard insights and automated actions
 */
public interface AdvancedAnalyticsService {

    /**
     * Get survival KPIs - critical business health metrics
     * @return SurvivalKpiResponse with daily/weekly trends and instant actions
     */
    SurvivalKpiResponse getSurvivalKpis();

    /**
     * Get opportunity radar - business opportunities detection
     * @return OpportunityRadarResponse with hot books, trending items, VIP customers, and abandoned carts
     */
    OpportunityRadarResponse getOpportunityRadar();

    /**
     * Get order health map - regional and shipping analysis
     * @return OrderHealthMapResponse with region health, shipping performance, and risk orders
     */
    OrderHealthMapResponse getOrderHealthMap();

    /**
     * Get profit optimizer insights - profit maximization suggestions
     * @return ProfitOptimizerResponse with high-margin low-sale books and pricing opportunities
     */
    // ProfitOptimizerResponse getProfitOptimizer(); // TODO: Implement in next phase

    /**
     * Get real-time alerts - critical system alerts
     * @return RealTimeAlertsResponse with categorized alerts and statistics
     */
    RealTimeAlertsResponse getRealTimeAlerts();

    /**
     * Execute quick action - automated business actions
     * @param request QuickActionRequest with action type and parameters
     * @return QuickActionResponse with execution result
     */
    QuickActionResponse executeQuickAction(QuickActionRequest request);
}
