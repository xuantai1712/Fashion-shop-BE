package com.example.Fashion_Shop.response.orders;

import com.example.Fashion_Shop.response.SKU.SkuResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderdetailsResponse {
    private List<SkuResponse> skus;
    private Integer quantity;
    private Double price;
    private Double totalMoney;
    private String productName;

}
