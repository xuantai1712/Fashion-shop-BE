package com.example.Fashion_Shop.response.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopSellingResponse {
    private Long productId;
    private String productName;
    private Long skuId;
    private String colorName;
    private String size;
    private String productImage;
    private Long totalSold;
}
