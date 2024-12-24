package com.example.Fashion_Shop.response.orders;

import com.example.Fashion_Shop.model.Product;
import com.example.Fashion_Shop.model.Review;
import com.example.Fashion_Shop.response.SKU.SkuResponse;
import com.example.Fashion_Shop.response.product.ProductResponse;
import com.example.Fashion_Shop.response.product_images.ProductImageResponse;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileProductReponse {
    private Long id;
    private String name;
    private List<ProductImageResponse> productImages;

    public static ProfileProductReponse from(Product product) {

        List<ProductImageResponse> productImageResponses = product.getProductImages().stream()
                .map(ProductImageResponse::fromProductImage)
                .collect(Collectors.toList());

        ProfileProductReponse profileProductReponse = ProfileProductReponse.builder()
                .id(product.getId())
                .name(product.getName())
                .productImages(productImageResponses)
                .build();

        return profileProductReponse;
    }
}
