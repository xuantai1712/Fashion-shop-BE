package com.example.Fashion_Shop.dto.attribute_values;

import com.example.Fashion_Shop.model.AttributeValue;
import com.example.Fashion_Shop.response.attribute_values.ColorResponse;
import com.example.Fashion_Shop.response.attribute_values.SizeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SizeDTO {
    private long id;
    private String name;
    private Long attribute_id;

    public static SizeDTO fromSize(AttributeValue size) {
        return SizeDTO.builder()
                .id(size.getId())               // Lấy ID từ AttributeValue
                .name(size.getValueName())      // Lấy tên từ AttributeValue
                .build();
    }



}
