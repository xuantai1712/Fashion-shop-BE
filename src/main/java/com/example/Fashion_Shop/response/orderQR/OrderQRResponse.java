package com.example.Fashion_Shop.response.orderQR;

import com.example.Fashion_Shop.model.Order;
import com.example.Fashion_Shop.model.User;
import com.example.Fashion_Shop.response.attribute_values.ColorResponse;
import com.example.Fashion_Shop.response.user.UserResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderQRResponse {
    private Long id;
    private UserResponse user;
    private String shippingAddress;
    private BigDecimal totalMoney;
    private String shippingMethod; // Phương thức giao hàng
    private String paymentMethod;  // Phương thức thanh toán
    private String status;
    private List<OrderDetailItemQRResponse> orderDetailItems;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

}
