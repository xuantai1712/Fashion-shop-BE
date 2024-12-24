package com.example.Fashion_Shop.dto.product_image;

import com.example.Fashion_Shop.dto.attribute_values.ColorDTO;
import com.example.Fashion_Shop.dto.attribute_values.SizeDTO;
import com.example.Fashion_Shop.model.AttributeValue;
import com.example.Fashion_Shop.model.Product;
import com.example.Fashion_Shop.model.ProductImage;
import com.example.Fashion_Shop.repository.AttributeValueRepository;
import lombok.*;

import static com.example.Fashion_Shop.dto.attribute_values.ColorDTO.fromColor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class ProductImageDTO {

    private String imageUrl;
    private boolean isThumbnail;
    private ColorDTO color;

    public ProductImage toEntity(Product product, AttributeValueRepository attributeValueRepository) {
        // Lấy color từ cơ sở dữ liệu
        AttributeValue colorEntity = null;
        if (color != null && color.getId() != null) {
            colorEntity = attributeValueRepository.findById(color.getId())
                    .orElseThrow(() -> new RuntimeException("Color not found with ID: " + color.getId()));
        }

        return ProductImage.builder()
                .imageUrl(this.imageUrl)
                .isThumbnail(this.isThumbnail)
                .product(product) // Gán Product vào ảnh
                .color(colorEntity) // Gán màu đã lấy từ DB
                .build();
    }
}
