package com.example.Fashion_Shop.repository;

import com.example.Fashion_Shop.model.Order;
import com.example.Fashion_Shop.response.statistic.MonthlyRevenueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUser_Id(Integer userId);

    Optional<Order> findById(Long id);
    @Query("SELECT o FROM Order o JOIN FETCH o.orderDetails WHERE o.id = :id")
    Optional<Order> findOrderByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT new com.example.Fashion_Shop.response.statistic.MonthlyRevenueResponse(
            MONTH(o.createAt),
            SUM(oi.quantity * oi.price)
        )
        FROM OrderDetail oi
        JOIN oi.sku sku
        JOIN sku.product p
        JOIN oi.order o
        WHERE o.status = 'Delivered'
        AND YEAR(o.createAt) = :year
        GROUP BY MONTH(o.createAt)
        ORDER BY MONTH(o.createAt)
    """)
    List<MonthlyRevenueResponse> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT o FROM Order o WHERE MONTH(o.createAt) = :month AND YEAR(o.createAt) = :year")
    List<Order> findByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
