package com.reactorx.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactorx.entity.Product; // Assuming you have this entity
import io.jsonwebtoken.io.IOException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ProductDetailDTO {

    // --- Basic Info ---
    private Long id;
    private String name;
    private String price; // Using String to match frontend
    private String mrp;   // Using String to match frontend
    private Double rating;
    private Integer reviewCount;
    private String info;
    private String categorySlug;
    private String soldCount;
    private Integer discountPercentage;
    private List<String> statusTags; // From Product entity

    // --- JSON Mapped Fields (These were missing) ---
    private List<MediaDTO> media; // Use MediaDTO for consistency
    private List<Map<String, String>> colors;
    private List<Map<String, String>> switchOptions;
    private List<Map<String, String>> featureIconGrid;
    private Map<String, String> heroVideo;
    private List<Map<String, String>> featureStats;
    private List<Map<String, String>> featureBannerText;
    private Map<String, String> featureBannerImage;
    private List<Map<String, Object>> featureSections; // Use Object for mixed-type sections
    private List<Map<String, String>> galleryBanners;
    private Map<String, List<Map<String, String>>> specsV2;

    // --- Optional: Constructor to map from Entity ---
    // You can keep your mapping logic in the service, this is just an alternative
    public ProductDetailDTO(Product product, ObjectMapper objectMapper) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice() != null ? product.getPrice().toString() : null;
        this.mrp = product.getMrp() != null ? product.getMrp().toString() : null;
        this.rating = product.getRating();
        this.reviewCount = product.getReviewCount();
        this.info = product.getInfo();
        this.categorySlug = product.getCategorySlug();
        this.soldCount = product.getSoldCount();
        this.discountPercentage = product.getDiscountPercentage();
        this.statusTags = product.getStatusTags();

        // --- JSON Parsing Logic ---
        // This is safer to do in the service, but shown here for completeness
        try {
            this.media = fromJson(objectMapper, product.getMediaJson(), new TypeReference<>() {});
            this.colors = fromJson(objectMapper, product.getColorsJson(), new TypeReference<>() {});
            this.switchOptions = fromJson(objectMapper, product.getSwitchOptionsJson(), new TypeReference<>() {});
            this.featureIconGrid = fromJson(objectMapper, product.getFeatureIconGridJson(), new TypeReference<>() {});
            this.heroVideo = fromJson(objectMapper, product.getHeroVideoJson(), new TypeReference<>() {});
            this.featureStats = fromJson(objectMapper, product.getFeatureStatsJson(), new TypeReference<>() {});
            this.featureBannerText = fromJson(objectMapper, product.getFeatureBannerTextJson(), new TypeReference<>() {});
            this.featureBannerImage = fromJson(objectMapper, product.getFeatureBannerImageJson(), new TypeReference<>() {});
            this.featureSections = fromJson(objectMapper, product.getFeatureSectionsJson(), new TypeReference<>() {});
            this.galleryBanners = fromJson(objectMapper, product.getGalleryBannersJson(), new TypeReference<>() {});
            this.specsV2 = fromJson(objectMapper, product.getSpecsV2Json(), new TypeReference<>() {});
        } catch (IOException e) {
            System.err.println("Error parsing JSON for product ID " + product.getId() + ": " + e.getMessage());
            // Handle error, maybe set fields to null or empty lists
        }
    }

    // Helper method for JSON parsing
    private <T> T fromJson(ObjectMapper objectMapper, String json, TypeReference<T> typeReference) throws IOException {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}