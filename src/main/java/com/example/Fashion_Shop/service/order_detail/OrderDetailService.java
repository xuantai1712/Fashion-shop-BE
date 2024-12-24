package com.example.Fashion_Shop.service.order_detail;


import com.example.Fashion_Shop.dto.OrderDetailDTO;
import com.example.Fashion_Shop.model.*;
import com.example.Fashion_Shop.model.OrderDetail;
import com.example.Fashion_Shop.model.Product;
import com.example.Fashion_Shop.model.SKU;
import com.example.Fashion_Shop.repository.OrderDetailRepository;
import com.example.Fashion_Shop.response.SKU.SkuResponse;
import com.example.Fashion_Shop.response.orders.OrderdetailsResponse;
import com.example.Fashion_Shop.response.orders.ProfileOrderDetailResponse;
import com.example.Fashion_Shop.response.orders.ProfileProductReponse;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;


    public OrderDetail saveOrderDetail(OrderDetail orderDetail) {

        if (orderDetail.getOrder() == null || orderDetail.getOrder().getId() == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }


        OrderDetail order = orderDetailRepository.findById(Long.valueOf(orderDetail.getOrder().getId()))
                .orElseThrow(() -> new RuntimeException("Order not found"));


        orderDetail.setOrder(order.getOrder());


        return orderDetailRepository.save(orderDetail);
    }


    public List<OrderDetail> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }


    public Optional<OrderDetail> getOrderDetailById(Long id) {
        return orderDetailRepository.findById(id);
    }


//    @Transactional
//    public List<OrderDetail> getOrderDetailsByOrderId(Integer orderId) {
//        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
//        if (orderDetails.isEmpty()) {
//            System.out.println("No order details found for orderId: " + orderId);
//        }
//        return orderDetails;
//    }
//@Transactional
//public List<OrderDetailDTO> getOrderDetailsByOrderId(Integer orderId) {
//    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderIdWithSku(orderId);
//    return orderDetails.stream().map(orderDetail -> {
//        OrderDetailDTO dto = new OrderDetailDTO();
//        dto.setId(orderDetail.getId());
//        dto.setQuantity(orderDetail.getQuantity());
//        dto.setPrice(orderDetail.getPrice());
//        dto.setTotalMoney(orderDetail.getTotalMoney());
//
//
//        if (orderDetail.getSku() != null) {
//            dto.setSkuId(orderDetail.getSku().getId());
//        } else {
//            dto.setSkuId(null);
//        }
//
//        return dto;
//    }).collect(Collectors.toList());
//}





    @Transactional
    public ProfileOrderDetailResponse getOrderDetailsByOrderId(Integer orderId) {
        // Lấy danh sách OrderDetail từ cơ sở dữ liệu
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderIdWithSku(orderId);

        // Khởi tạo đối tượng phản hồi và danh sách chi tiết đơn hàng
        ProfileOrderDetailResponse response = new ProfileOrderDetailResponse();
        List<OrderdetailsResponse> orderdetailsResponses = new ArrayList<>();

        if (!orderDetails.isEmpty()) {
            Order order = orderDetails.get(0).getOrder();

            // Thiết lập thông tin chung của đơn hàng
            response.setOrderId(order.getId().toString());
            response.setShippingAddress(order.getShippingAddress());
            response.setStatus(order.getStatus());
            response.setTotalPrice(order.getTotalMoney().doubleValue());
            response.setPaymentMethod(order.getPaymentMethod());
            response.setShippingMethod(order.getShippingMethod());

            // Thiết lập thông tin tạo và cập nhật thời gian
            response.setCreateAt(order.getCreateAt());
            response.setUpdateAt(order.getUpdateAt());

            double totalPrice = 0;  // Tổng giá trị đơn hàng
            int totalQuantity = 0;  // Tổng số lượng sản phẩm

            // Duyệt qua từng OrderDetail để map dữ liệu
            for (OrderDetail orderDetail : orderDetails) {
                SKU sku = orderDetail.getSku();
                if (sku != null) {
                    Product product = sku.getProduct();

                    // Cập nhật thông tin sản phẩm chính vào response nếu chưa có
                    if (response.getProfileProductReponse() == null) {
                        response.setProfileProductReponse(ProfileProductReponse.from(product));
                    }

                    // Chuyển đổi SKU sang SkuResponse
                    SkuResponse skuResponse = SkuResponse.fromSKU(sku);

                    // Tạo OrderdetailsResponse cho mỗi chi tiết đơn hàng
                    OrderdetailsResponse orderdetailsResponse = OrderdetailsResponse.builder()
                            .skus(Collections.singletonList(skuResponse))  // Đưa sku vào danh sách
                            .quantity(orderDetail.getQuantity())
                            .price(sku.getSalePrice())  // Giá của SKU
                            .totalMoney(orderDetail.getTotalMoney()) // Tổng tiền của mục này
                            .build();

                    // Thêm vào danh sách chi tiết đơn hàng
                    orderdetailsResponses.add(orderdetailsResponse);

                    // Cập nhật tên sản phẩm và hình ảnh sản phẩm cho chi tiết đơn hàng
                    if (orderdetailsResponse.getProductName() == null) {
                        orderdetailsResponse.setProductName(product.getName());

                        response.setProductImage(product.getProductImages().stream()
                                .filter(image -> image.getColor().getId().equals(sku.getColor().getId()))
                                .findFirst()
                                .map(ProductImage::getImageUrl)
                                .orElse(null));
                    }
                }

                // Tính tổng số lượng và giá trị đơn hàng
                totalQuantity += orderDetail.getQuantity();
                totalPrice += orderDetail.getTotalMoney();
            }

            // Cập nhật thông tin tổng hợp vào phản hồi
            response.setOrderdetailsResponses(orderdetailsResponses);
            response.setQuantity(totalQuantity);
        }

        return response;
    }







    public void deleteOrderDetail(Long id) {
        orderDetailRepository.deleteById(id);
    }




}
