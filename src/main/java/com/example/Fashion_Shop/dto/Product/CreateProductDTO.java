package com.example.Fashion_Shop.dto.Product;

import com.example.Fashion_Shop.dto.BaseDTO.BaseDTO;
import com.example.Fashion_Shop.dto.SkuDTO.SkuDTO;
import com.example.Fashion_Shop.dto.product_image.ProductImageDTO;
import com.example.Fashion_Shop.model.*;
import com.example.Fashion_Shop.repository.AttributeValueRepository;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Data
@Getter
@Setter
@NoArgsConstructor
@Builder
public class CreateProductDTO extends BaseDTO {
    private String name;
    private String description;
    private List<ProductImageDTO> imgs;
    private long category_id;
    private List<SkuDTO> skus;

    public Product toEntity(AttributeValueRepository attributeValueRepository) {
        Product product = Product.builder()
                .name(this.name)
                .description(this.description)
                .category(Category.builder().id(this.category_id).build())
                .build();

        // Mapping images
        if (imgs != null && !imgs.isEmpty()) {
            List<ProductImage> images = this.imgs.stream()
                    .map(dto -> dto.toEntity(product, attributeValueRepository))
                    .collect(Collectors.toList());
            product.setProductImages(images);
        }

        // Mapping SKUs
        if (skus != null) {
            List<SKU> skuList = this.skus.stream()
                    .map(SkuDTO::toEntity)
                    .peek(sku -> sku.setProduct(product)) // Gắn product vào mỗi SKU
                    .collect(Collectors.toList());
            product.setSku(skuList);
        }

        return product;
    }




}
