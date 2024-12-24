package com.example.Fashion_Shop.dto.attribute_values;

import com.example.Fashion_Shop.model.AttributeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ColorDTO {
    private Long id;
    private String name;
    private String value_img;

    public ColorDTO(Long id) {
    }

    public static ColorDTO fromColor(AttributeValue color) {
        return ColorDTO.builder()
                .id(color.getId())               // Lấy ID từ AttributeValue
                .name(color.getValueName())      // Lấy tên từ AttributeValue
                .value_img(color.getValueImg())  // Lấy hình ảnh từ AttributeValue
                .build();
    }



}
