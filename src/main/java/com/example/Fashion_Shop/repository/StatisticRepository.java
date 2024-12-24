package com.example.Fashion_Shop.repository;

import com.example.Fashion_Shop.model.Product;
import com.example.Fashion_Shop.response.statistic.TopSellingResponse;
import io.micrometer.core.instrument.Statistic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticRepository extends JpaRepository<Product, Long> {
    @Query("""
    SELECT new com.example.Fashion_Shop.response.statistic.TopSellingResponse(
        p.id,
        p.name,
        sku.id,
        c.valueName,
        s.valueName,
        pi.imageUrl,
        SUM(oi.quantity)
    )
    FROM OrderDetail oi
    JOIN oi.sku sku
    JOIN sku.product p
    JOIN sku.color c
    JOIN sku.size s
    LEFT JOIN ProductImage pi ON pi.product.id = p.id AND pi.color.id = c.id
    JOIN oi.order o
    WHERE o.status = 'Delivered'
    AND MONTH(o.createAt) = :month
    AND YEAR(o.createAt) = :year
    GROUP BY p.id, p.name, sku.id, c.valueName, s.valueName, pi.imageUrl
    ORDER BY SUM(oi.quantity) DESC
""")
    List<TopSellingResponse> getTopSellingSKU(@Param("month") int month, @Param("year") int year, Pageable pageable);
}
