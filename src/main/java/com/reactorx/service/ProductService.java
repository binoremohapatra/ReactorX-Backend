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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public List<ProductSummaryDTO> getAllProductsSummary() {
        log.debug("Fetching all products summary");
        return productRepository.findAll().stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    public List<ProductSummaryDTO> getProductsByCategory(String categorySlug) {
        log.debug("Fetching products by category: {}", categorySlug);
        return productRepository.findByCategorySlug(categorySlug).stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProductDetailDTO> getProductDetails(Long id) {
        log.debug("Fetching product details for ID: {}", id);
        return productRepository.findById(id).map(this::mapToDetailDTO);
    }

    public List<ProductSummaryDTO> searchProducts(String searchTerm) {
        log.debug("Searching products with term: {}", searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return productRepository.findByNameContainingIgnoreCase(searchTerm.trim()).stream()
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
            log.warn("Error parsing media JSON for product summary ID {}: {}", product.getId(), e.getMessage());
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
        
                
        dto.setDiscountPercentage(product.getDiscountPercentage());

        // Map complex JSON fields with individual error handling
        try {
            if (product.getMediaJson() != null && !product.getMediaJson().isBlank()) {
                dto.setMedia(fromJson(product.getMediaJson(), new TypeReference<List<MediaDTO>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing media JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getColorsJson() != null && !product.getColorsJson().isBlank()) {
                dto.setColors(fromJson(product.getColorsJson(), new TypeReference<List<Map<String, String>>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing colors JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getSwitchOptionsJson() != null && !product.getSwitchOptionsJson().isBlank()) {
                dto.setSwitchOptions(fromJson(product.getSwitchOptionsJson(), new TypeReference<List<Map<String, String>>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing switch options JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getFeatureIconGridJson() != null && !product.getFeatureIconGridJson().isBlank()) {
                dto.setFeatureIconGrid(fromJson(product.getFeatureIconGridJson(), new TypeReference<List<Map<String, String>>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing feature icon grid JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getHeroVideoJson() != null && !product.getHeroVideoJson().isBlank()) {
                dto.setHeroVideo(fromJson(product.getHeroVideoJson(), new TypeReference<Map<String, String>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing hero video JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getFeatureStatsJson() != null && !product.getFeatureStatsJson().isBlank()) {
                dto.setFeatureStats(fromJson(product.getFeatureStatsJson(), new TypeReference<List<Map<String, String>>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing feature stats JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getFeatureBannerTextJson() != null && !product.getFeatureBannerTextJson().isBlank()) {
                dto.setFeatureBannerText(fromJson(product.getFeatureBannerTextJson(), new TypeReference<List<Map<String, String>>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing feature banner text JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getFeatureBannerImageJson() != null && !product.getFeatureBannerImageJson().isBlank()) {
                dto.setFeatureBannerImage(fromJson(product.getFeatureBannerImageJson(), new TypeReference<Map<String, String>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing feature banner image JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getFeatureSectionsJson() != null && !product.getFeatureSectionsJson().isBlank()) {
                dto.setFeatureSections(fromJson(product.getFeatureSectionsJson(), new TypeReference<List<Map<String, Object>>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing feature sections JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getGalleryBannersJson() != null && !product.getGalleryBannersJson().isBlank()) {
                dto.setGalleryBanners(fromJson(product.getGalleryBannersJson(), new TypeReference<List<Map<String, String>>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing gallery banners JSON for product ID {}: {}", product.getId(), e.getMessage());
        }

        try {
            if (product.getSpecsV2Json() != null && !product.getSpecsV2Json().isBlank()) {
                dto.setSpecsV2(fromJson(product.getSpecsV2Json(), new TypeReference<Map<String, List<Map<String, String>>>>() {}));
            }
        } catch (Exception e) {
            log.warn("Error parsing specs V2 JSON for product ID {}: {}", product.getId(), e.getMessage());
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