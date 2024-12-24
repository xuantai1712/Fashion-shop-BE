package com.example.Fashion_Shop.repository;

import com.example.Fashion_Shop.model.SKU;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkuRepository extends JpaRepository<SKU, Long> {
    Page<SKU> findBySalePriceBetweenOrOriginalPriceBetween(Double minPrice, Double maxPrice, Double minPrice1, Double maxPrice1, Pageable pageable);

    Optional<Object> findByIdAndProductId(Long skuId, Long productId);
}
