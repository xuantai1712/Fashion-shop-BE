package com.example.Fashion_Shop.service.orders;

import com.example.Fashion_Shop.dto.OrderDTO;
import com.example.Fashion_Shop.dto.OrderDetailDTO;
import com.example.Fashion_Shop.model.*;
import com.example.Fashion_Shop.repository.*;
import com.example.Fashion_Shop.response.attribute_values.ColorResponse;
import com.example.Fashion_Shop.response.attribute_values.SizeResponse;
import com.example.Fashion_Shop.response.orderQR.OrderDetailItemQRResponse;
import com.example.Fashion_Shop.response.orderQR.OrderQRResponse;
import com.example.Fashion_Shop.response.user.UserResponse;
import com.example.Fashion_Shop.response.orders.OrderResponseAdmin;
import com.example.Fashion_Shop.response.user.UserResponse;
import com.example.Fashion_Shop.service.EmailService;
import com.example.Fashion_Shop.service.QRCodeService;
import com.example.Fashion_Shop.service.cart.CartService;

import com.google.zxing.WriterException;
import jakarta.mail.MessagingException;
import com.example.Fashion_Shop.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class OrderService {

    private final EmailService mailerService;

    private final SkuRepository skuRepository;

    private final CartRepository cartRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    private CartService cartService;

    private final AddressRepository addressRepository;

    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;

    private final QRCodeService qrCodeService;


    @Transactional
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(order -> convertToDTO(order)).collect(Collectors.toList());
    }

    @Transactional
    public List<OrderDTO> getOrdersByUserId(Integer userId) {
        List<Order> orders = orderRepository.findByUser_Id(userId);

        return orders.stream()
                .sorted(Comparator.comparing(Order::getCreateAt).reversed())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }




    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }


    public void deleteOrder(Integer id) {
        Order deleteOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        deleteOrder.setIsActive(false);
        orderRepository.save(deleteOrder);
    }


    @Transactional
    public Order createOrderFromCart(Long userId) throws MessagingException {

        List<Cart> cartItems = cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        // Tạo một đối tượng Order mới
        Order order = new Order();
        order.setUser(user);
        order.setStatus("Pending");

        Address defaultAddresses = addressRepository.findByUserIdAndIsDefaultTrue(userId);
        if (defaultAddresses == null) {
            throw new RuntimeException("Người dùng chưa thiết lập địa chỉ mặc định.");
        }
        order.setShippingAddress(defaultAddresses.getStreet() + ", "
                + defaultAddresses.getWard() + ", "
                + defaultAddresses.getCity());
        order.setPhoneNumber(user.getPhone());


        double totalMoney = cartItems.stream()
                .mapToDouble(cartItem -> cartItem.getSku().getSalePrice() * cartItem.getQuantity())
                .sum();

        order.setTotalMoney(BigDecimal.valueOf(totalMoney));


        if (order.getShippingMethod() == null || order.getShippingMethod().isEmpty()) {
//            throw new RuntimeException("Bạn chưa chọn phương thức giao hàng");
            order.setShippingMethod("Nhận tại cửa hàng");
        } else {
            order.setShippingMethod(order.getShippingMethod());
        }

        if (order.getPaymentMethod() == null || order.getPaymentMethod().isEmpty()) {
//            throw new RuntimeException("Bạn chưa chọn phương thức thanh toán");
            order.setPaymentMethod("Thanh toán khi nhận hàng");
        } else {

            order.setPaymentMethod(order.getPaymentMethod());
        }


        // Chuyển đổi từng CartItem thành OrderDetail
        List<OrderDetail> orderDetails = cartItems.stream().map(cartItem -> {
            SKU sku = cartItem.getSku();
            int quantity = cartItem.getQuantity();
            double price = sku.getSalePrice();

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSku(sku);
            orderDetail.setQuantity(quantity);
            orderDetail.setPrice(price);
            orderDetail.setTotalMoney(price * quantity);
            orderDetail.setOrder(order);

            System.out.println("OrderDetail chuẩn bị lưu, ID trước khi lưu: " + orderDetail.getId()); // Kiểm tra ID

            return orderDetail;

        }).collect(Collectors.toList());

        order.setOrderDetails(orderDetails);


        Order savedOrder = orderRepository.save(order);
        System.out.println("Order đã lưu với ID: " + savedOrder.getId());
        savedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));

        for (OrderDetail detail : savedOrder.getOrderDetails()) {
            System.out.println("OrderDetail đã lưu với ID: " + detail.getId()); // In ID sau khi lưu
        }

        System.out.println(order.getShippingMethod());
        if ("Nhận tại cửa hàng".equalsIgnoreCase(order.getShippingMethod())) {
            try {
                String qrCodePath = qrCodeService.generateQRCode(
                        savedOrder.getId().toString(),
                        300, 300
                );
                sendOrderConfirmationEmailWithQRCode(savedOrder, qrCodePath);

                Path path = FileSystems.getDefault().getPath(qrCodePath);
                String fileName = path.getFileName().toString();
                order.setQrCode(fileName);
            } catch (IOException | WriterException e) {
                throw new RuntimeException("Lỗi khi tạo hoặc gửi mã QR: " + e.getMessage());
            }
        } else {
            sendOrderConfirmationEmail(savedOrder);
        }

        cartService.deleteAllCart(userId);

        return savedOrder;
    }


    private void sendOrderConfirmationEmail(Order savedOrder) throws MessagingException {
        String recipientEmail = savedOrder.getUser().getEmail();
        String subject = "Xác Nhận Đơn Hàng - #" + savedOrder.getId();
        StringBuilder body = new StringBuilder();

        body.append("Chào ").append(savedOrder.getUser().getName()).append(",<br><br>");
        body.append("Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi. Đây là thông tin đơn hàng của bạn:<br>");
        body.append("<b>Đơn hàng ID: </b>").append(savedOrder.getId()).append("<br>");
        body.append("<b>Địa chỉ giao hàng: </b>").append(savedOrder.getShippingAddress()).append("<br>");
        body.append("<b>Tổng tiền: </b>").append(savedOrder.getTotalMoney()).append("<br><br>");
        body.append("<b>Chi tiết đơn hàng:</b><br>");

        savedOrder.getOrderDetails().forEach(orderDetail -> {
            body.append("Sản phẩm: ").append(orderDetail.getSku().getProduct().getName())
                    .append(" - Số lượng: ").append(orderDetail.getQuantity())
                    .append(" - Màu: ").append(orderDetail.getSku().getColor().getValueName())
                    .append(" - Size: ").append(orderDetail.getSku().getSize().getValueName())
                    .append(" - Giá: ").append(orderDetail.getPrice()).append("<br>");
        });

        body.append("<br>Cảm ơn bạn đã mua sắm tại cửa hàng của chúng tôi!");

        // Gửi email
        mailerService.sendEmail(recipientEmail, subject, body.toString());
    }

    private void sendOrderConfirmationEmailWithQRCode(Order savedOrder, String qrCodeUrl) throws MessagingException {
        String recipientEmail = savedOrder.getUser().getEmail();
        String subject = "Xác Nhận Đơn Hàng - #" + savedOrder.getId();
        StringBuilder body = new StringBuilder();

        body.append("Chào ").append(savedOrder.getUser().getName()).append(",<br><br>");
        body.append("Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi. Đây là thông tin đơn hàng của bạn:<br>");
        body.append("<b>Đơn hàng ID: </b>").append(savedOrder.getId()).append("<br>");
        body.append("<b>Phương thức giao hàng: </b>").append(savedOrder.getShippingMethod()).append("<br>");
        body.append("<b>Tổng tiền: </b>").append(savedOrder.getTotalMoney()).append("<br><br>");
        body.append("<b>Mã QR của bạn:</b><br>");
        body.append("<img src='cid:qrCode' alt='QR Code'/><br><br>");
        savedOrder.getOrderDetails().forEach(orderDetail -> {
            body.append("Sản phẩm: ")
                    .append(orderDetail.getSku().getProduct().getName())
                    .append(" - Số lượng: ").append(orderDetail.getQuantity())
                    .append(" - Màu: ").append(orderDetail.getSku().getColor().getValueName())
                    .append(" - Size: ").append(orderDetail.getSku().getSize().getValueName())
                    .append(" - Giá: ").append(orderDetail.getPrice()).append("<br>");
        });
        body.append("Cảm ơn bạn đã mua sắm tại cửa hàng của chúng tôi!");

       mailerService.sendEmailWithAttachment(recipientEmail, subject, body.toString(), qrCodeUrl);
    }


    public Order updateOrder(Long id, Order updatedOrder) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
                    existingOrder.setTotalMoney(updatedOrder.getTotalMoney());
                    existingOrder.setStatus(updatedOrder.getStatus());
                    existingOrder.setUser(updatedOrder.getUser());
                    return orderRepository.save(existingOrder);
                }).orElseThrow(() -> new RuntimeException("Order not found with id " + id));
    }


//    public void updateOrderPayment(Long orderId, String transactionId, String paymentResponse, BigDecimal totalAmount) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        // Cập nhật trạng thái đơn hàng
//        order.setStatus("Paid");
//        orderRepository.save(order);
//
//        // Tạo bản ghi thanh toán mới
//        OrderPayment payment = new OrderPayment();
//        payment.setOrder(order);
//        payment.setTransactionId(transactionId);
//        payment.setTransactionDate(new Date());
//        payment.setPaymentGatewayResponse(paymentResponse);
//        payment.setAmount(totalAmount);
//        payment.setStatus("Success");
//
//        orderPaymentRepository.save(payment);
//    }


//    public OrderDTO convertToDTO(Order order) {
//        List<OrderDetailDTO> orderDetailDTOs = order.getOrderDetails().stream()
//                .map(detail -> {
//                    // Kiểm tra nếu SKU không null
//                    SKU sku = detail.getSku();
//                    Long skuId = (sku != null) ? sku.getId() : null;
//                    return OrderDetailDTO.builder()
//                            .id(detail.getId())
//                            .skuId(Math.toIntExact(skuId))
//                            .quantity(detail.getQuantity())
//                            .price(detail.getPrice())
//                            .totalMoney(detail.getTotalMoney())
//                            .build();
//                })
//                .collect(Collectors.toList());
//
//        return OrderDTO.builder()
//                .orderId(order.getId())
//                .shippingAddress(order.getShippingAddress())
//                .phoneNumber(order.getPhoneNumber())
//                .totalMoney(order.getTotalMoney())
//                .status(order.getStatus())
//                .qrCode(order.getQrCode())
//                .paymentMethod(order.getPaymentMethod())
//                .shippingMethod(order.getShippingMethod())
//                .orderDetails(order.getOrderDetails())
//                .build();
//
//    }


    public OrderDTO convertToDTO(Order order) {
        List<OrderDetailDTO> orderDetailDTOs = order.getOrderDetails().stream()
                .map(detail -> {
                    System.out.println("OrderDetail ID: " + detail.getId());
                    SKU sku = detail.getSku();
                    System.out.println("SKU: " + sku);
                    Long skuId = (sku != null) ? sku.getId() : null;
                    if (skuId != null && skuId > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException("SKU ID vượt quá giới hạn ");
                    }
                    return OrderDetailDTO.builder()
                            .id(detail.getId())
                            .skuId(detail.getSku().getId())
                            .quantity(detail.getQuantity())
                            .price(detail.getPrice())
                            .totalMoney(detail.getTotalMoney())
                            .build();


                })
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(Math.toIntExact(order.getId()))
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber() != null ? order.getPhoneNumber() : "")
                .totalMoney(order.getTotalMoney() != null ? order.getTotalMoney() : BigDecimal.ZERO)
                .status(order.getStatus())
                .qrCode(order.getQrCode() != null ? order.getQrCode() : "")
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "Thanh toán khi nhận hàng")
                .shippingMethod(order.getShippingMethod() != null ? order.getShippingMethod() : "Giao hàng nhanh")
                .orderDetails(orderDetailDTOs)
                .build();
    }


//    public OrderDTO updateOrder(Integer orderId, OrderDTO updateDTO) {
//        // Tìm Order hiện tại
//        Order existingOrder = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        // Cập nhật thông tin Order
//        existingOrder.setShippingAddress(updateDTO.getShippingAddress());
//        existingOrder.setPhoneNumber(updateDTO.getPhoneNumber());
//        existingOrder.setTotalMoney(updateDTO.getTotalMoney());
//        existingOrder.setStatus(updateDTO.getStatus());
//
//
//        existingOrder.getOrderDetails().clear();  // Xóa các chi tiết hiện tại
//        List<OrderDetail> updatedDetails = updateDTO.getOrderDetails().stream()
//                .map(dto -> {
//                    OrderDetail detail = new OrderDetail();
//                    detail.setSku(new SKU(Math.toIntExact(dto.getSkuId())));  // Đặt SKU từ ID
//                    detail.setQuantity(dto.getQuantity());
//                    detail.setPrice(dto.getPrice());
//                    detail.setTotalMoney(dto.getTotalMoney());
//                    detail.setOrder(existingOrder);  // Liên kết với Order
//                    return detail;
//                }).collect(Collectors.toList());
//
//        existingOrder.setOrderDetails(updatedDetails);
//
//        // Lưu Order và các OrderDetails mới
//        Order updatedOrder = orderRepository.save(existingOrder);
//        return convertToDTO(updatedOrder);  // Chuyển về DTO để trả về
//    }


    @Transactional
    public void updateInventory(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            SKU sku = skuRepository.findById(detail.getSku().getId())
                    .orElseThrow(() -> new RuntimeException("SKU not found with ID: " + detail.getSku().getId()));

            // Giảm số lượng tồn kho
            int remainingQty = sku.getQtyInStock() - detail.getQuantity();
            if (remainingQty < 0) {
                throw new RuntimeException("Not enough stock for product: " + sku.getProduct().getName());
            }
            sku.setQtyInStock(remainingQty);
            skuRepository.save(sku);  // Lưu cập nhật vào cơ sở dữ liệu
        }
    }


    @Transactional
    public void cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Cập nhật lại tồn kho
        restockInventory(order);

        // Đặt trạng thái đơn hàng thành "Canceled"
        order.setStatus("Canceled");
        orderRepository.save(order);
    }


    @Transactional
    public void restockInventory(Order order) {
        for (OrderDetail detail : order.getOrderDetails()) {
            SKU sku = skuRepository.findById(detail.getSku().getId())
                    .orElseThrow(() -> new RuntimeException("SKU not found with ID: " + detail.getSku().getId()));

            sku.setQtyInStock(sku.getQtyInStock() + detail.getQuantity());
            skuRepository.save(sku);  // Cập nhật kho
        }
    }


    private void checkAndUpdateInventory(SKU sku, int quantity) {
        // Kiểm tra số lượng tồn kho
        if (sku.getQtyInStock() < quantity) {
            throw new IllegalArgumentException("Không đủ hàng trong kho cho SKU: " + sku.getId());
        }

        // Giảm số lượng tồn kho
        sku.setQtyInStock(sku.getQtyInStock() - quantity);

        // Lưu cập nhật vào database nếu cần thiết
        skuRepository.save(sku);
    }

    public OrderQRResponse getOrderDetailsQR(Long orderId) {
        // Lấy thông tin đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));

        // Lấy danh sách OrderDetail cho đơn hàng
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);

        // Chuyển đổi thành OrderQRResponse
        OrderQRResponse response = OrderQRResponse.builder()
                .id(order.getId())
                .user(UserResponse.fromUser(order.getUser()))
                .shippingAddress(order.getShippingAddress())
                .totalMoney(order.getTotalMoney())
                .shippingMethod(order.getShippingMethod())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createAt(order.getCreateAt())
                .updateAt(order.getUpdateAt())
                .orderDetailItems(orderDetails.stream().map(orderDetail -> {
                    SKU sku = orderDetail.getSku();
                    Product product = sku.getProduct();
                    AttributeValue color = sku.getColor();
                    AttributeValue size = sku.getSize();

                    return OrderDetailItemQRResponse.builder()
                            .productName(product.getName())
                            .quantity(orderDetail.getQuantity())
                            .price(orderDetail.getPrice())
                            .color(ColorResponse.builder()
                                    .id(color.getId())
                                    .name(color.getValueName())
                                    .build())
                            .size(SizeResponse.builder()
                                    .id(size.getId())
                                    .name(size.getValueName())
                                    .build())
                            .productImage(getProductImageForVariant(product, color, size))
                            .build();
                }).collect(Collectors.toList()))
                .build();

        return response;
    }

    public String getProductImageForVariant(Product product, AttributeValue color, AttributeValue size) {
        return product.getProductImages().stream()
                .filter(image -> image.getProduct().getId().equals(product.getId()) &&
                        image.getColor().getId().equals(color.getId())) // Thêm điều kiện kích thước
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null); // Nếu không tìm thấy, trả về null hoặc ảnh mặc định
    }

    public Order updateOrderQRStatus(Long orderId, OrderQRResponse updatedOrder) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(updatedOrder.getStatus());  // Cập nhật trạng thái
        return orderRepository.save(order);  // Lưu lại đơn hàng đã cập nhật
    }
///////////////////////ADMIN/////////////////////////////
public OrderResponseAdmin updateStatusOrder(Long id, String newStatus) {
    // Danh sách trạng thái hợp lệ
    List<String> validStatuses = Arrays.asList("pending", "processing", "shipped", "delivered", "cancelled");

    // Kiểm tra trạng thái hợp lệ
    if (!validStatuses.contains(newStatus)) {
        throw new IllegalArgumentException("Invalid order status: " + newStatus);
    }

    // Tìm đơn hàng theo ID
    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order không tồn tại"));

    // Cập nhật trạng thái
    order.setStatus(newStatus);
    order.setUpdateAt(LocalDateTime.now());

    // Nếu trạng thái là "cancelled", đặt is_active thành false
    if ("cancelled".equalsIgnoreCase(newStatus)) {
        order.setIsActive(false);
    }

    // Lưu thay đổi vào cơ sở dữ liệu
    Order updatedOrder = orderRepository.save(order);

    // Chuyển đổi Order sang OrderResponseAdmin và trả về
    return convertToOrderResponseAdmin(updatedOrder);
}


    public List<OrderResponseAdmin> getOrderResponseAdmin() {
        // Lấy danh sách tất cả các đơn hàng
        List<Order> orders = orderRepository.findAll();

        // Lọc danh sách để chỉ lấy các đơn hàng có is_active = true
        return orders.stream()
                .map(this::convertToOrderResponseAdmin) // Chuyển đổi sang OrderResponseAdmin
                .collect(Collectors.toList());
    }

    public List<OrderResponseAdmin> getOrderResponseAdmin(int month, int year) {
        // Lấy danh sách đơn hàng theo tháng và năm
        List<Order> orders = orderRepository.findByMonthAndYear(month, year);

        // Chuyển đổi danh sách Order sang OrderResponseAdmin
        return orders.stream()
                .map(this::convertToOrderResponseAdmin) // Chuyển đổi sang DTO
                .collect(Collectors.toList());
    }

    public OrderResponseAdmin getOrderResponseByIdAdmin(Long orderId) {
        // Tìm đơn hàng theo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        // Chuyển đổi Order sang OrderResponseAdmin
        return convertToOrderResponseAdmin(order);
    }

    private OrderResponseAdmin convertToOrderResponseAdmin(Order order) {
        OrderResponseAdmin response = OrderResponseAdmin.builder()
                .id(Math.toIntExact(order.getId()))
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber())
                .totalMoney(order.getTotalMoney())
                .status(order.getStatus())
                .userId(order.getUser().getId())
                .shippingMethod(order.getShippingMethod())
                .paymentMethod(order.getPaymentMethod())
                .build();

        // Gán các trường từ BaseResponse
        response.setCreateAt(order.getCreateAt());
        response.setUpdateAt(order.getUpdateAt());

        return response;
    }
}
