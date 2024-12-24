package com.example.Fashion_Shop.response.orders;

import com.example.Fashion_Shop.response.BaseResponse;
import com.example.Fashion_Shop.response.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseAdmin extends BaseResponse {
    private Integer id;
    private String shippingAddress;
    private String phoneNumber;
    private BigDecimal totalMoney;
    private String status;
    private Long userId;
    private String shippingMethod;
    private String paymentMethod;
}
