package com.example.Fashion_Shop.service.Size;

import com.example.Fashion_Shop.dto.attribute_values.ColorDTO;
import com.example.Fashion_Shop.dto.attribute_values.SizeDTO;
import com.example.Fashion_Shop.model.Attribute;
import com.example.Fashion_Shop.model.AttributeValue;
import com.example.Fashion_Shop.repository.AttributeRepitory;
import com.example.Fashion_Shop.repository.AttributeValueRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SizeService {
    private AttributeValueRepository attributeValueRepository;
    private AttributeRepitory attributeRepitory;
    public List<SizeDTO> getAllSizes() {
        // Tìm attribute có tên là "Color"
        Attribute sizeAttribute = attributeRepitory.findByAttributeName("Size");

        if (sizeAttribute == null) {
            return List.of(); // Nếu không có attribute "Color", trả về danh sách rỗng
        }

        // Lấy các giá trị thuộc tính (AttributeValue) với attributeId tương ứng với màu sắc
        List<AttributeValue> sizeValue = attributeValueRepository.findByAttributeId(sizeAttribute.getId());

        // Chuyển đổi từ AttributeValue sang ColorDTO
        return sizeValue.stream()
                .map(SizeDTO::fromSize) // Chuyển mỗi AttributeValue thành ColorDTO
                .collect(Collectors.toList()); // Trả về danh sách ColorDTO
    }

}
