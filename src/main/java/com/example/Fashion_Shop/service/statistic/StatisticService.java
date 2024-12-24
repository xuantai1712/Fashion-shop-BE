package com.example.Fashion_Shop.service.statistic;

import com.example.Fashion_Shop.repository.OrderRepository;
import com.example.Fashion_Shop.repository.StatisticRepository;
import com.example.Fashion_Shop.response.statistic.MonthlyRevenueResponse;
import com.example.Fashion_Shop.response.statistic.TopSellingResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;


import java.util.List;

@Service
@AllArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticsRepository;
    private final OrderRepository orderRepository;

    public List<TopSellingResponse> getTopSellingSKU(int month, int year) {
        Pageable topFive = PageRequest.of(0, 10); // Chỉ lấy 5 kết quả đầu tiên
        return statisticsRepository.getTopSellingSKU(month, year, topFive);
    }

    public List<MonthlyRevenueResponse> getMonthlyRevenue(int year) {
        return orderRepository.getMonthlyRevenue(year);
    }
}
