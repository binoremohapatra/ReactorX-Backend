package com.reactorx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactorx.dto.MediaDTO;
import com.reactorx.dto.ProductDetailDTO;
import com.reactorx.dto.ProductSummaryDTO;
import com.reactorx.entity.Product;
import com.reactorx.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public List<ProductSummaryDTO> getAllProductsSummary() {
        return productRepository.findAll().stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    public List<ProductSummaryDTO> getProductsByCategory(String categorySlug) {
        // FIX: The repository call is correct, but returning an empty list
        // will cause the frontend to say 'not found'. This is the correct method call.
        return productRepository.findByCategorySlug(categorySlug).stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProductDetailDTO> getProductDetails(Long id) {
        return productRepository.findById(id).map(this::mapToDetailDTO);
    }

    public List<ProductSummaryDTO> searchProducts(String searchTerm) {
        return productRepository.findByNameContainingIgnoreCase(searchTerm).stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }


    /**
     * Converts a Product Entity to a ProductSummaryDTO.
     */
    public ProductSummaryDTO mapToSummaryDTO(Product product) {
        ProductSummaryDTO dto = new ProductSummaryDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());

        if(product.getPrice() != null) {
            dto.setPrice(product.getPrice().toString());
        }
        if(product.getMrp() != null) {
            dto.setMrp(product.getMrp().toString());
        }

        dto.setDiscountPercentage(product.getDiscountPercentage());
        dto.setRating(product.getRating());
        dto.setCategorySlug(product.getCategorySlug());

        // --- JSON Parsing for Primary Media ---
        try {
            if (product.getMediaJson() != null && !product.getMediaJson().isBlank()) {
                List<Map<String, String>> mediaList = objectMapper.readValue(
                        product.getMediaJson(),
                        new TypeReference<>() {}
                );

                Optional<Map<String, String>> firstImageMap = mediaList.stream()
                        .filter(media -> "image".equalsIgnoreCase(media.get("type")))
                        .findFirst();

                if (firstImageMap.isPresent()) {
                    Map<String, String> mediaMap = firstImageMap.get();
                    dto.setPrimaryMedia(new MediaDTO(mediaMap.get("type"), mediaMap.get("src")));
                }
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error parsing media JSON for product summary ID " + product.getId() + ": " + e.getMessage());
        }

        return dto;
    }

    /**
     * Converts a Product Entity to a ProductDetailDTO.
     */
    private ProductDetailDTO mapToDetailDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();

        // Map basic fields...
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice() != null ? product.getPrice().toString() : null);
        dto.setMrp(product.getMrp() != null ? product.getMrp().toString() : null);
        dto.setRating(product.getRating());
        dto.setReviewCount(product.getReviewCount());
        dto.setInfo(product.getInfo());
        dto.setCategorySlug(product.getCategorySlug());
        dto.setSoldCount(product.getSoldCount());
        dto.setStatusTags(product.getStatusTags());
        dto.setDiscountPercentage(product.getDiscountPercentage());

        // Map complex JSON fields
        try {
            dto.setMedia(fromJson(product.getMediaJson(), new TypeReference<List<MediaDTO>>() {}));
            dto.setColors(fromJson(product.getColorsJson(), new TypeReference<List<Map<String, String>>>() {}));
            dto.setSwitchOptions(fromJson(product.getSwitchOptionsJson(), new TypeReference<List<Map<String, String>>>() {}));
            dto.setFeatureIconGrid(fromJson(product.getFeatureIconGridJson(), new TypeReference<List<Map<String, String>>>() {}));
            dto.setHeroVideo(fromJson(product.getHeroVideoJson(), new TypeReference<Map<String, String>>() {}));
            dto.setFeatureStats(fromJson(product.getFeatureStatsJson(), new TypeReference<List<Map<String, String>>>() {}));
            dto.setFeatureBannerText(fromJson(product.getFeatureBannerTextJson(), new TypeReference<List<Map<String, String>>>() {}));
            dto.setFeatureBannerImage(fromJson(product.getFeatureBannerImageJson(), new TypeReference<Map<String, String>>() {}));
            dto.setFeatureSections(fromJson(product.getFeatureSectionsJson(), new TypeReference<List<Map<String, Object>>>() {}));
            dto.setGalleryBanners(fromJson(product.getGalleryBannersJson(), new TypeReference<List<Map<String, String>>>() {}));
            dto.setSpecsV2(fromJson(product.getSpecsV2Json(), new TypeReference<Map<String, List<Map<String, String>>>>() {}));

        } catch (IOException e) {
            System.err.println("Error parsing JSON for product details ID " + product.getId() + ": " + e.getMessage());
        }

        return dto;
    }

    // Helper to parse JSON with type reference
    private <T> T fromJson(String json, TypeReference<T> typeReference) throws IOException {
        if (json == null || json.isBlank()) {
            return null;
        }
        return objectMapper.readValue(json, typeReference);
    }
}