package com.example.Fashion_Shop.controller;

import com.example.Fashion_Shop.dto.OrderDTO;
import com.example.Fashion_Shop.dto.OrderStatus;
import com.example.Fashion_Shop.model.Order;
import com.example.Fashion_Shop.response.orders.OrderResponseAdmin;
import com.example.Fashion_Shop.response.orderQR.OrderQRResponse;
import com.example.Fashion_Shop.service.orders.OrderService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;


@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")

public class OrderController {


    private final OrderService orderService;


    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@PathVariable Integer userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);

        if (orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(orders);
    }

//    @GetMapping
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public List<OrderDTO> getAllOrders() {
//        return orderService.getAllOrders();
//    }


//    @PostMapping
//    public ResponseEntity<OrderDTO> createOrUpdateOrder(@RequestBody Order order) {
//        Order savedOrder = orderService.saveOrder(order);
//        OrderDTO orderDTO = orderService.convertToDTO(savedOrder);
//        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
//    }

    @PostMapping("/create-from-cart/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<OrderDTO> createOrderFromCart(@PathVariable Long userId)  throws MessagingException {

        Order savedOrder = orderService.createOrderFromCart(userId);

        OrderDTO orderDTO = orderService.convertToDTO(savedOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Integer id, @RequestBody OrderDTO updateDTO) {
//        try {
//            OrderDTO updatedOrder = orderService.updateOrder(id, updateDTO);
//            return ResponseEntity.ok(updatedOrder);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isPresent()) {
            OrderDTO orderDTO = orderService.convertToDTO(orderOpt.get());
            return ResponseEntity.ok(orderDTO);
        }
        return ResponseEntity.notFound().build();
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/QR/{orderId}")
    public OrderQRResponse getOrderDetails(@PathVariable("orderId") Long orderId) {
        return orderService.getOrderDetailsQR(orderId);
    }

    @PutMapping("/QR/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderQRResponse updatedOrder) {
        try {
            // Cập nhật trạng thái đơn hàng
           orderService.updateOrderQRStatus(orderId, updatedOrder);

            // Trả về thông điệp thành công
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Trả về thông điệp lỗi nếu có lỗi xảy ra
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating order status: " + e.getMessage());
        }
    }


    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OrderResponseAdmin> updateStatusOrder(
            @PathVariable Long orderId,
            @RequestParam String newStatus) {
        try {
            OrderResponseAdmin updatedOrder = orderService.updateStatusOrder(orderId, newStatus);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<OrderResponseAdmin> getOrderResponseAdmin() {
        return orderService.getOrderResponseAdmin().stream().toList();
    }

    @GetMapping("/admin/by-month-year")
    public List<OrderResponseAdmin> getOrdersByMonthAndYear(
            @RequestParam("month") int month,
            @RequestParam("year") int year) {
        return orderService.getOrderResponseAdmin(month, year);
    }


}
