package com.example.Fashion_Shop.service;

import com.example.Fashion_Shop.dto.SkuDTO.SkuDTO;
import com.example.Fashion_Shop.model.Product;
import com.example.Fashion_Shop.model.SKU;
import com.example.Fashion_Shop.repository.ProductRepository;
import com.example.Fashion_Shop.repository.SkuRepository;
import com.example.Fashion_Shop.response.SKU.SkuResponse;
import com.example.Fashion_Shop.response.product.ProductResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SkuService {
    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    @Transactional
    public ProductResponse getSkuDetails(Long productId, Long skuId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + productId));


        SKU sku = (SKU) skuRepository.findByIdAndProductId(skuId, productId)
                .orElseThrow(() -> new RuntimeException("SKU not found with id " + skuId + " for product " + productId));

        // Tạo SKUResponse cho SKU duy nhất
        SkuResponse skuResponse = SkuResponse.fromSKU(sku);

        // Tạo ProductResponse chỉ chứa SKU này
        ProductResponse productResponse = ProductResponse.fromProduct(product);
        productResponse.setSkus(List.of(skuResponse));

        return productResponse;
    }
}
