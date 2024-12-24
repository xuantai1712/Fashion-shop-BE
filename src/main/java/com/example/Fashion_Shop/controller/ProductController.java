package com.example.Fashion_Shop.controller;

import com.example.Fashion_Shop.dto.Product.CreateProductDTO;
import com.example.Fashion_Shop.model.Category;
import com.example.Fashion_Shop.model.Product;
import com.example.Fashion_Shop.response.product.ProductDetailResponse;
import com.example.Fashion_Shop.response.product.ProductListResponse;
import com.example.Fashion_Shop.response.product.ProductResponse;
import com.example.Fashion_Shop.service.SkuService;
import com.example.Fashion_Shop.service.product.ProductService;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/products")
@AllArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final SkuService skuService;
    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(required = false) Long categoryId, // Không bắt buộc
            @RequestParam(defaultValue = "") String keyword,  //keyword
            @RequestParam(defaultValue = "0") int page,  // Mặc định page là 0
            @RequestParam(defaultValue = "8") int size,     // Mặc định page size là 8
            @RequestParam(defaultValue = "createAt") String sortBy, // Mặc định sắp xếp theo ngày tạo product
            @RequestParam(defaultValue = "desc") String sortDirection // Mặc định sắp xếp giảm dần
    ) {
        try {
            Page<ProductResponse> productPage = productService.getProducts(categoryId, keyword, page, size, sortBy, sortDirection);

            ProductListResponse response = ProductListResponse.builder()
                    .products(productPage.getContent()) // Danh sách sản phẩm
                    .totalPages(productPage.getTotalPages()) // Tổng số trang
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetails(
            @PathVariable Long productId,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) Long sizeId) {

        ProductDetailResponse response = productService.getProductDetails(productId, colorId, sizeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addProduct")
    public ResponseEntity<?> createProduct(@RequestBody CreateProductDTO createProductDTO) {
        try {
            // Tạo sản phẩm từ DTO mà không trả về sản phẩm
            productService.createProduct(createProductDTO);

            // Tạo phản hồi thành công mà không trả về sản phẩm
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product created successfully!");

            // Trả về phản hồi với status CREATED (201)
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            // Xử lý lỗi và trả về phản hồi chi tiết
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error creating product.");
            errorResponse.put("error", e.getMessage()); // Trả về thông báo lỗi chi tiết

            // Trả về phản hồi với status INTERNAL_SERVER_ERROR (500)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/AllProducts")
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(required = false) Long categoryId, // Tìm theo danh mục
            @RequestParam(defaultValue = "") String keyword,  // Tìm theo tên sản phẩm hoặc từ khóa
            @RequestParam(defaultValue = "0") int page,  // Mặc định trang là 0
            @RequestParam(defaultValue = "8") int size,     // Mặc định số lượng sản phẩm mỗi trang là 8
            @RequestParam(defaultValue = "createAt") String sortBy, // Sắp xếp theo ngày tạo
            @RequestParam(defaultValue = "desc") String sortDirection, // Sắp xếp giảm dần
            @RequestParam(required = false) Double minPrice,  // Tìm theo giá (từ giá)
            @RequestParam(required = false) Double maxPrice   // Tìm theo giá (đến giá)
    ) {
        try {
            // Gọi service để lấy sản phẩm dựa trên các tham số tìm kiếm
            Page<ProductResponse> productPage = productService.getAllProducts(categoryId, keyword, page, size, sortBy, sortDirection, minPrice, maxPrice);
            ProductListResponse response = ProductListResponse.builder()
                    .products(productPage.getContent()) // Danh sách sản phẩm
                    .totalPages(productPage.getTotalPages()) // Tổng số trang
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{productId}/skus/{skuId}")
    public ResponseEntity<String> deleteSku(@PathVariable Long productId, @PathVariable Long skuId) {
        boolean isDeleted = productService.deleteSku(productId, skuId);
        if (isDeleted) {
            return ResponseEntity.ok("SKU deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SKU not found");
        }
    }

    @GetMapping("{productId}/sku/{skuId}")
    public ResponseEntity<ProductResponse> getSkuDetails(@PathVariable Long productId, @PathVariable Long skuId) {
        ProductResponse productResponse = skuService.getSkuDetails(productId, skuId);
        return ResponseEntity.ok(productResponse);
    }

    @PutMapping("/update/{productId}/{skuId}")
    public ResponseEntity<String> updateProduct(@PathVariable Long productId,
                                                @PathVariable Long skuId,
                                                @RequestBody CreateProductDTO createProductDTO
    ) {System.out.println("Received update request for Product ID: " + productId + " and SKU ID: " + skuId);
        try {
            // Cập nhật sản phẩm
            productService.updateProduct(productId, skuId, createProductDTO);
            return ResponseEntity.ok("successfully");
        } catch (RuntimeException e) {
            // Xử lý các lỗi nếu có
            System.out.println("Error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("error to update");
        }
    }






}
