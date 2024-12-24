package com.example.Fashion_Shop.service.product;

import com.example.Fashion_Shop.dto.Product.CreateProductDTO;
import com.example.Fashion_Shop.dto.SkuDTO.SkuDTO;
import com.example.Fashion_Shop.model.Category;
import com.example.Fashion_Shop.model.Product;
import com.example.Fashion_Shop.model.ProductImage;
import com.example.Fashion_Shop.model.SKU;
import com.example.Fashion_Shop.repository.*;
import com.example.Fashion_Shop.response.product.ProductDetailResponse;
import com.example.Fashion_Shop.response.product.ProductResponse;
import com.example.Fashion_Shop.service.SkuService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final SkuRepository skuRepository;
    private final SkuService skuService;
    private final ProductImageRepository productImageRepository;
    public Page<ProductResponse> getProducts(
            Long categoryId, String keyword, int page, int size, String sortBy, String sortDirection) {
        // code cũ
//        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
//                ? Sort.Direction.ASC : Sort.Direction.DESC;
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
//        Page<Product> products = productRepository.findAllWithVariantsAndCategory(categoryId, pageable);
        // pagerequest trả về size với page
        Pageable pageable = PageRequest.of(page, size);

        // dựa theo sortDirection asc hay desc mà chạy query sort
        // do price nằm ở SKU còn name nằm ở product
        Page<Product> products = "asc".equalsIgnoreCase(sortDirection)
                ? productRepository.findAllWithSortAndKeywordASC(categoryId,keyword, sortBy, pageable)
                : productRepository.findAllWithSortAndKeywordDESC(categoryId,keyword, sortBy, pageable);

        return products.map(ProductResponse::fromProduct);
    }

    public ProductDetailResponse getProductDetails(Long productId, Long colorId, Long sizeId) {
        // Lấy sản phẩm từ repository
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Tìm SKU được chọn dựa trên colorId và sizeId
        SKU selectedSku = product.getSku().stream()
                .filter(sku ->
                        sku.getColor().getId().equals(colorId) &&
                        sku.getSize().getId().equals(sizeId))
                .findFirst()
                .orElse(null);

        if (selectedSku == null) {
            throw new RuntimeException("No SKU found matching the given colorId and sizeId");
        }

        // Tìm ảnh được chọn từ SKU
        ProductImage selectedImage = selectedSku != null
                ? product.getProductImages().stream()
                .filter(image -> image.getColor().getId().equals(colorId))
                .findFirst()
                .orElse(null)
                : null;

        // Xây dựng response bằng cách gọi fromProduct
        return ProductDetailResponse.fromProduct(product, selectedSku, selectedImage);
    }


    @Transactional
    public Product createProduct(CreateProductDTO createProductDTO) {
        // Truyền repository vào DTO khi chuyển đổi sang Entity
        Product product = createProductDTO.toEntity(attributeValueRepository);

        // Gắn Product vào SKU và ProductImage
        if (product.getSku() != null) {
            product.getSku().forEach(sku -> sku.setProduct(product));
        }
        if (product.getProductImages() != null) {
            product.getProductImages().forEach(image -> image.setProduct(product));
        }

        return productRepository.save(product);
    }

    @Transactional
    public void updateProduct(Long productId, Long skuId, CreateProductDTO createProductDTO) {
        // 1. Lấy sản phẩm từ cơ sở dữ liệu theo productId
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // 2. Cập nhật các trường cần thiết của sản phẩm
        existingProduct.setName(createProductDTO.getName());
        existingProduct.setDescription(createProductDTO.getDescription());

        // Nếu category_id hợp lệ, cập nhật category cho sản phẩm
        if (createProductDTO.getCategory_id() > 0) {
            Category category = categoryRepository.findById(createProductDTO.getCategory_id())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + createProductDTO.getCategory_id()));
            existingProduct.setCategory(category);
        }
        List<ProductImage> imagesToDelete = existingProduct.getProductImages().stream()
                .filter(existingImage -> createProductDTO.getImgs().stream()
                        .noneMatch(newImage -> newImage.getImageUrl().equals(existingImage.getImageUrl())))
                .collect(Collectors.toList());

        // 2. Xóa ảnh cần xóa khỏi cơ sở dữ liệu và danh sách trong sản phẩm
        productImageRepository.deleteAll(imagesToDelete);
        existingProduct.getProductImages().removeAll(imagesToDelete);

        // 3. Thêm ảnh mới từ DTO
        List<ProductImage> imagesToAdd = createProductDTO.getImgs().stream()
                .filter(newImage -> existingProduct.getProductImages().stream()
                        .noneMatch(existingImage -> existingImage.getImageUrl().equals(newImage.getImageUrl())))
                .map(dto -> dto.toEntity(existingProduct, attributeValueRepository))
                .collect(Collectors.toList());
        existingProduct.getProductImages().addAll(imagesToAdd);

        // 4. Sửa ảnh hiện có
        existingProduct.getProductImages().forEach(existingImage ->
                createProductDTO.getImgs().stream()
                        .filter(newImage -> newImage.getImageUrl().equals(existingImage.getImageUrl()))
                        .forEach(newImage -> {

                            existingImage.setColor(attributeValueRepository.findById(newImage.getColor().getId())
                                    .orElseThrow(() -> new RuntimeException("Color not found")));
                        })
        );


        // 3. Tìm SKU theo skuId trong sản phẩm vừa lấy
        if (createProductDTO.getSkus() != null && !createProductDTO.getSkus().isEmpty()) {
            // Lặp qua danh sách SKU trong DTO và cập nhật thông tin SKU trong sản phẩm
            for (SkuDTO skuDTO : createProductDTO.getSkus()) {
                // Tìm SKU cần cập nhật trong sản phẩm hiện tại
                SKU existingSku = existingProduct.getSku().stream()
                        .filter(sku -> sku.getId().equals(skuId))  // Tìm SKU theo ID
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("SKU not found with id: " + skuId));

                // Cập nhật các thuộc tính của SKU
                existingSku.setQtyInStock(skuDTO.getQtyInStock());
                existingSku.setOriginalPrice(skuDTO.getOriginalPrice());
                existingSku.setSalePrice(skuDTO.getSalePrice());
                existingSku.setSize(attributeValueRepository.findById(skuDTO.getSize().getId())
                        .orElseThrow(() -> new RuntimeException("Size not found with id: " + skuDTO.getSize().getId())));
                existingSku.setColor(attributeValueRepository.findById(skuDTO.getColor().getId())
                        .orElseThrow(() -> new RuntimeException("Color not found with id: " + skuDTO.getColor().getId())));
            }
        }
        // Lưu SKU đã cập nhật vào cơ sở dữ liệu
        skuRepository.saveAll(existingProduct.getSku());

        productRepository.save(existingProduct);
    }


    public Page<ProductResponse> getAllProducts(Long categoryId, String keyword, int page, int size, String sortBy, String sortDirection, Double minPrice, Double maxPrice) {
        // Xác định Sort.Direction từ sortDirection
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Tạo Pageable với thông tin sắp xếp
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Logic tìm kiếm
        // Logic tìm kiếm
        Page<Product> productPage;

        if (keyword != null && !keyword.isEmpty()) {
            // Tìm theo tên sản phẩm
            productPage = productRepository.findByNameContaining(keyword, pageable);
        } else if (minPrice != null && maxPrice != null) {
            // Tìm theo khoảng giá trong cả salePrice và originalPrice
            // Sử dụng skuRepository để tìm kiếm giá trong khoảng
            Page<SKU> skuPage = skuRepository.findBySalePriceBetweenOrOriginalPriceBetween(minPrice, maxPrice, minPrice, maxPrice, pageable);

            // Bạn cần chuyển đổi từ Page<SKU> thành Page<Product>
            // Giả sử mỗi SKU liên kết với một Product, có thể sử dụng dữ liệu từ SKU để lấy Product
            productPage = skuPage.map(sku -> sku.getProduct()); // Giả sử SKU có phương thức `getProduct()`
        } else {
            // Trả về tất cả sản phẩm nếu không có tiêu chí tìm kiếm
            productPage = productRepository.findAll(pageable);
        }


        // Chuyển đổi Page<Product> thành Page<ProductResponse>
        return productPage.map(ProductResponse::fromProduct);
    }


    public boolean deleteSku(Long productId, Long skuId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            Product prod = product.get();
            List<SKU> skus = prod.getSku();
            // Tìm SKU và xóa
            Optional<SKU> skuToDelete = skus.stream().filter(sku -> sku.getId().equals(skuId)).findFirst();
            if (skuToDelete.isPresent()) {
                skus.remove(skuToDelete.get());
                skuRepository.delete(skuToDelete.get());  // Xóa SKU khỏi cơ sở dữ liệu
                // Kiểm tra nếu không còn SKU nào
                if (skus.isEmpty()) {
                    // Nếu không còn SKU, xóa sản phẩm
                    productRepository.delete(prod);
                } else {
                    productRepository.save(prod);  // Lưu lại thay đổi nếu còn SKU
                }
                return true;
            }
        }
        return false;
    }





}