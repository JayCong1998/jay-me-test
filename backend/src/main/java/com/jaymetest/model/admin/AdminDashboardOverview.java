package com.jaymetest.model.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminDashboardOverview {
    private long totalQuestions;
    private long totalUsers;
    private long totalRecords;
    private long todayRecords;
    private double averageCorrectCount;
    private Map<String, Long> modeDistribution;
}
