package com.example.Fashion_Shop.response.orderQR;

import com.example.Fashion_Shop.response.attribute_values.ColorResponse;
import com.example.Fashion_Shop.response.attribute_values.SizeResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailItemQRResponse {
    private String productName;
    private int quantity;
    private double price;
    private ColorResponse color;
    private SizeResponse size;
    private String productImage;

}
