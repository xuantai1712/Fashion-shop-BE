package com.example.Fashion_Shop.dto.SkuDTO;

import com.example.Fashion_Shop.dto.attribute_values.AttributeValueDTO;
import com.example.Fashion_Shop.dto.attribute_values.ColorDTO;
import com.example.Fashion_Shop.dto.attribute_values.SizeDTO;
import com.example.Fashion_Shop.model.AttributeValue;
import com.example.Fashion_Shop.model.SKU;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class SkuDTO {
    private Integer qtyInStock;
    private Double originalPrice;
    private Double salePrice;
    private ColorDTO color;
    private SizeDTO size;




    public SKU toEntity() {
        AttributeValue colorEntity = AttributeValue.builder()
                .id(color.getId())
                .valueImg(color.getValue_img())
                .valueName(color.getName())
                .build();


        AttributeValue sizeEntity = AttributeValue.builder()
                .id(size.getId())
                .valueName(color.getName())
                .build();

        return SKU.builder()
                .qtyInStock(this.qtyInStock)
                .originalPrice(this.originalPrice)
                .salePrice(this.salePrice)
                .color(colorEntity)
                .size(sizeEntity)
                .build();
    }
}

