package com.example.Fashion_Shop.response.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyRevenueResponse {
    private int month;
    private double totalRevenue;
}
