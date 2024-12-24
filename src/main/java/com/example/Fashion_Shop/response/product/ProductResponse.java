package com.example.Fashion_Shop.response.product;

import com.example.Fashion_Shop.model.Category;
import com.example.Fashion_Shop.model.Product;
import com.example.Fashion_Shop.model.Review;
import com.example.Fashion_Shop.response.BaseResponse;
import com.example.Fashion_Shop.response.SKU.SkuResponse;
import com.example.Fashion_Shop.response.product_images.ProductImageResponse;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse extends BaseResponse{
    private Long id;
    private String name;
    private Long categoryId;
    private String description;
    private List<SkuResponse> skus;
    private List<ProductImageResponse> productImages;
    private Double avgRating;
    private long countReviews;

    public static ProductResponse fromProduct(Product product) {
        List<SkuResponse> skuResponses = product.getSku().stream()
                .map(SkuResponse::fromSKU)
                .collect(Collectors.toList());

        List<ProductImageResponse> productImageResponses = product.getProductImages().stream()
                .map(ProductImageResponse::fromProductImage)
                .collect(Collectors.toList());

        double avgRating = product.getSku().stream()
                .flatMap(sku -> sku.getReview().stream())
                .mapToDouble(Review::getRatingValue)
                .average()
                .orElse(0.0);

        long countReview = product.getSku().stream()
                .flatMap(sku -> sku.getReview().stream())
                .count();

        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .name(product.getName())
                .skus(skuResponses)
                .productImages(productImageResponses)
                .avgRating(avgRating)
                .countReviews(countReview)
                .build();
        productResponse.setCreateAt(product.getCreateAt());
        productResponse.setUpdateAt(product.getUpdateAt());

        return productResponse;
    }

}

