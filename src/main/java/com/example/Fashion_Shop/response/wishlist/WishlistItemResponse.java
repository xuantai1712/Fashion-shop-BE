package com.example.Fashion_Shop.response.wishlist;

import com.example.Fashion_Shop.model.ProductImage;
import com.example.Fashion_Shop.model.Wishlist;
import com.example.Fashion_Shop.response.SKU.SkuResponse;
import com.example.Fashion_Shop.service.productImage.ProductImageService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class WishlistItemResponse {

    Long id;
    Long productId;
    String productName;
    Double price;
    String productImage;
    SkuResponse skuResponse;


    public static WishlistItemResponse fromWishlist(@NotNull Wishlist wishlist) {

        WishlistItemResponse wishlistItemResponse = WishlistItemResponse.builder()
                .id(wishlist.getId()) // ID của mục giỏ hàng
                .productId(wishlist.getSku().getProduct().getId()) // ID sản phẩm từ SKU
                .productName(wishlist.getSku().getProduct().getName()) // Tên sản phẩm từ SKU
                .price(wishlist.getSku().getSalePrice()) // Giá bán từ SKU
                .productImage(wishlist.getSku().getProduct().getProductImages().stream()
                        .filter(image -> image.getProduct().getId().equals(wishlist.getSku().getProduct().getId()) &&
                                image.getColor().getId().equals(wishlist.getSku().getColor().getId()))
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElse(null)) // Ensure null is returned if no match
                .skuResponse(SkuResponse.fromSKU(wishlist.getSku())) // Chuyển đổi SKU thành SkuResponse
                .build();
        return wishlistItemResponse;
    }
}
