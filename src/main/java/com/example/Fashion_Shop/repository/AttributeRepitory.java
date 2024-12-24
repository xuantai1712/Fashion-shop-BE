package com.example.Fashion_Shop.repository;

import com.example.Fashion_Shop.model.Attribute;
import com.example.Fashion_Shop.model.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepitory  extends JpaRepository<Attribute, Integer> {
    Attribute findByAttributeName(String color);
}
