package com.example.Fashion_Shop.repository;

import com.example.Fashion_Shop.model.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    List<AttributeValue> findByAttributeId(Integer id);
}
