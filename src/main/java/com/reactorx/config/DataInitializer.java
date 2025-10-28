package com.reactorx.config; // Or your actual package for this class

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactorx.entity.Product; // Make sure this import matches your entity package
import com.reactorx.repository.ProductRepository; // Make sure this import matches your repository package
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource; // <<<--- Import needed
import org.springframework.stereotype.Component;
// Removed ResourceUtils import
// Removed File import
// Removed Files import
import java.io.IOException;
import java.io.InputStream; // <<<--- Import needed
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets; // <<<--- Import needed
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper; // Make sure ObjectMapper is configured as a Bean

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) { // Only load if DB is empty
            System.out.println("Loading initial product data...");
            try {
                // --- VVV UPDATED CODE TO READ FROM CLASSPATH VVV ---
                ClassPathResource resource = new ClassPathResource("products.json");
                InputStream inputStream = resource.getInputStream();
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                inputStream.close(); // Close the stream after reading
                // --- ^^^ UPDATED CODE TO READ FROM CLASSPATH ^^^ ---

                // Deserialize the JSON array into a List of Maps initially
                List<Map<String, Object>> productMaps = objectMapper.readValue(content, new TypeReference<>() {});

                // Convert Maps to Product entities
                List<Product> products = productMaps.stream()
                        .map(this::mapToProductEntity)
                        .toList();

                productRepository.saveAll(products);
                System.out.println("Loaded " + products.size() + " products.");

            } catch (IOException e) {
                System.err.println("Failed to load initial product data: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for better debugging
            }
        } else {
            System.out.println("Database already contains product data. Skipping initialization.");
        }
        // TODO: Load Categories similarly if needed
    }

    // Helper to map the raw Map (from JSON) to a Product entity
    // This needs careful handling based on your Product entity structure
    private Product mapToProductEntity(Map<String, Object> map) {
        Product product = new Product();

        // --- Basic Fields ---
        product.setId(((Number) map.get("id")).longValue());
        product.setName((String) map.get("name"));
        // Use BigDecimal for price/mrp, handle comma removal
        product.setPrice(new BigDecimal(map.get("price").toString().replace(",", "")));
        product.setMrp(new BigDecimal(map.get("mrp").toString().replace(",", "")));
        product.setDiscountPercentage((Integer) map.get("discountPercentage"));
        // Handle potential Number format issues for rating
        Object ratingObj = map.get("rating");
        if (ratingObj instanceof Number) {
            product.setRating(((Number)ratingObj).doubleValue());
        } else if (ratingObj != null){
            try { product.setRating(Double.parseDouble(ratingObj.toString())); } catch (NumberFormatException e) { product.setRating(0.0); /* default or log */ }
        } else {
            product.setRating(0.0); // Default if null
        }
        product.setReviewCount((Integer) map.get("reviewCount"));
        product.setInfo((String) map.get("info"));
        product.setCategorySlug((String) map.get("category")); // Assuming 'category' field holds the slug
        product.setSoldCount((String) map.get("soldCount"));
        product.setStatusTags((List<String>) map.get("statusTags"));


        // --- Serialize complex fields back to JSON strings for @Lob TEXT columns ---
        try {
            product.setMediaJson(safeWriteValueAsString(map.get("media")));
            product.setFeatureIconGridJson(safeWriteValueAsString(map.get("featureIconGrid")));
            product.setHeroVideoJson(safeWriteValueAsString(map.get("heroVideo")));
            product.setFeatureStatsJson(safeWriteValueAsString(map.get("featureStats")));
            product.setFeatureSectionsJson(safeWriteValueAsString(map.get("featureSections")));
            product.setSpecsV2Json(safeWriteValueAsString(map.get("specsV2"))); // Note: specsV2 key in JSON
            product.setFeatureBannerTextJson(safeWriteValueAsString(map.get("featureBannerText")));
            product.setFeatureBannerImageJson(safeWriteValueAsString(map.get("featureBannerImage")));
            product.setGalleryBannersJson(safeWriteValueAsString(map.get("galleryBanners")));
            product.setSwitchOptionsJson(safeWriteValueAsString(map.get("switchOptions")));
            product.setColorsJson(safeWriteValueAsString(map.get("colors")));
            // Add any other complex fields here
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing JSON fields for product " + product.getId() + ": " + e.getMessage());
            // Consider setting fields to null or "{}" as fallback depending on DB constraints
        }

        return product;
    }

    // Helper method to safely convert objects to JSON string, returning null if input is null
    private String safeWriteValueAsString(Object obj) throws JsonProcessingException {
        if (obj == null) {
            return null; // Return null if the object itself is null in the map
        }
        return objectMapper.writeValueAsString(obj);
    }
}
