package com.example.Fashion_Shop.response.orders;

import com.example.Fashion_Shop.response.BaseResponse;
import com.example.Fashion_Shop.response.SKU.SkuResponse;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileOrderDetailResponse extends BaseResponse {
    private String orderId;
    private ProfileProductReponse profileProductReponse;
    private List<OrderdetailsResponse> orderdetailsResponses;
    private String shippingAddress;
    private String status;
    private String paymentMethod;
    private String shippingMethod;
    private Double totalPrice;
    private String productImage;
    private Integer quantity;

}
