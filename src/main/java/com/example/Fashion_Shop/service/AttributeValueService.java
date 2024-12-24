package com.example.Fashion_Shop.service;

import com.example.Fashion_Shop.model.AttributeValue;
import com.example.Fashion_Shop.repository.AttributeValueRepository;
import org.springframework.stereotype.Service;

@Service
public class AttributeValueService {
    private AttributeValueRepository attributeValueRepository;
    public AttributeValue findColorById(Long colorId) {
        return attributeValueRepository.findById(colorId).get();

    }




}
