package com.example.Fashion_Shop.dto.Product;

import com.example.Fashion_Shop.dto.BaseDTO.BaseDTO;
import com.example.Fashion_Shop.dto.SkuDTO.SkuDTO;
import com.example.Fashion_Shop.dto.product_image.ProductImageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductDTO extends BaseDTO {

    private String name;
    private String Catelory_id;
    private String description;
    private List<SkuDTO> skus;
    private List<ProductImageDTO> productImages;

    public static class ProductDTOBuilder {
        private Long id;
        private String category_id;
        private String name;
        private String description;
        private List<SkuDTO> skus;
        private List<ProductImageDTO> productImages;

        public ProductDTOBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public ProductDTOBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ProductDTOBuilder
        setSkus(List<SkuDTO> skus) {
            this.skus = skus;
            return this;
        }

        public ProductDTOBuilder setProductImages(List<ProductImageDTO> productImages) {
            this.productImages = productImages;
            return this;
        }

        public ProductDTO build() {
            ProductDTO productDTO = new ProductDTO();

            productDTO.setName(name);
            productDTO.setSkus(skus);

            productDTO.setProductImages(productImages);
            return productDTO;
        }
    }

    public static ProductDTOBuilder builder() {
        return new ProductDTOBuilder();
    }

}
