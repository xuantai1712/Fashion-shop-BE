package com.example.Fashion_Shop.dto.attribute_values;

import com.example.Fashion_Shop.model.AttributeValue;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class AttributeValueDTO {

    private String attributeName;
    private String value;
    private Long attributeId;

    public AttributeValue toEntity() {
        return AttributeValue.builder()
                .valueName(this.attributeName)
                .valueImg(this.value)
                .build();
    }

}
