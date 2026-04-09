package com.reactorx.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactorx.entity.Product;
import com.reactorx.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run AFTER CategoryInitializer (which is @Order(1))
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== DataInitializer starting ===");

        long count = productRepository.count();
        log.info("Current product count in DB: {}", count);

        if (count > 0) {
            log.info("Products already exist. Skipping seed. Count: {}", count);
            return;
        }

        log.info("DB is empty. Loading products from products.json...");

        ClassPathResource resource = new ClassPathResource("products.json");
        if (!resource.exists()) {
            log.error("CRITICAL: products.json not found in src/main/resources/. Products will NOT be loaded.");
            return;
        }

        try (InputStream is = resource.getInputStream()) {
            List<JsonNode> rawProducts = objectMapper.readValue(is, new TypeReference<>() {});
            log.info("Parsed {} products from products.json", rawProducts.size());

            List<Product> products = new ArrayList<>();
            int skipped = 0;

            for (JsonNode node : rawProducts) {
                try {
                    Product p = mapJsonToProduct(node);
                    products.add(p);
                } catch (Exception e) {
                    log.warn("Skipping product id={} due to mapping error: {}", 
                        node.path("id").asText("?"), e.getMessage());
                    skipped++;
                }
            }

            if (!products.isEmpty()) {
                productRepository.saveAll(products);
                log.info("Successfully inserted {} products. Skipped: {}", products.size(), skipped);
            } else {
                log.error("No products were mapped successfully. Check mapping logic.");
            }

        } catch (Exception e) {
            log.error("Failed to load products.json: {}", e.getMessage(), e);
        }
    }

    private Product mapJsonToProduct(JsonNode node) {
        Product p = new Product();

        // --- Required fields ---
        p.setId(node.path("id").asLong());
        p.setName(node.path("name").asText(null));
        p.setCategorySlug(node.path("category").asText(null));

        // --- Price fields: stored as "1,299" strings - strip commas ---
        String priceStr = node.path("price").asText("0").replace(",", "").trim();
        String mrpStr = node.path("mrp").asText("0").replace(",", "").trim();
        try {
            p.setPrice(new BigDecimal(priceStr));
        } catch (NumberFormatException e) {
            log.warn("Invalid price '{}' for product {}, defaulting to 0", priceStr, p.getId());
            p.setPrice(BigDecimal.ZERO);
        }
        try {
            p.setMrp(new BigDecimal(mrpStr));
        } catch (NumberFormatException e) {
            p.setMrp(BigDecimal.ZERO);
        }

        // --- Simple fields ---
        if (node.hasNonNull("discountPercentage")) p.setDiscountPercentage(node.path("discountPercentage").asInt());
        if (node.hasNonNull("rating"))             p.setRating(node.path("rating").asDouble());
        if (node.hasNonNull("reviewCount"))        p.setReviewCount(node.path("reviewCount").asInt());
        if (node.hasNonNull("info"))               p.setInfo(node.path("info").asText(null));
        if (node.hasNonNull("soldCount"))          p.setSoldCount(node.path("soldCount").asText(null));

        // --- Extract primary image URL for quick access ---
        JsonNode mediaArray = node.path("media");
        if (mediaArray.isArray() && !mediaArray.isEmpty()) {
            for (JsonNode media : mediaArray) {
                if ("image".equals(media.path("type").asText())) {
                    p.setImageUrl(media.path("src").asText(null));
                    break;
                }
            }
            // fallback: if no image type found, use first media item
            if (p.getImageUrl() == null) {
                p.setImageUrl(mediaArray.get(0).path("src").asText(null));
            }
        }

        // --- JSON blob fields (store as raw JSON strings) ---
        p.setMediaJson(toJsonString(node.path("media")));
        p.setFeatureIconGridJson(toJsonString(node.path("featureIconGrid")));
        p.setHeroVideoJson(toJsonString(node.path("heroVideo")));
        p.setFeatureStatsJson(toJsonString(node.path("featureStats")));
        p.setFeatureSectionsJson(toJsonString(node.path("featureSections")));
        p.setSpecsV2Json(toJsonString(node.path("specsV2")));
        p.setFeatureBannerTextJson(toJsonString(node.path("featureBannerText")));
        p.setFeatureBannerImageJson(toJsonString(node.path("featureBannerImage")));
        p.setGalleryBannersJson(toJsonString(node.path("galleryBanners")));
        p.setSwitchOptionsJson(toJsonString(node.path("switchOptions")));
        p.setColorsJson(toJsonString(node.path("colors")));

        // --- statusTags as list ---
        JsonNode tagsNode = node.path("statusTags");
        if (tagsNode.isArray()) {
            List<String> tags = new ArrayList<>();
            tagsNode.forEach(t -> tags.add(t.asText()));
            p.setStatusTags(tags);
        }

        return p;
    }

    /**
     * Converts a JsonNode to its JSON string representation.
     * Returns null if the node is missing or null (avoids storing "null" strings).
     */
    private String toJsonString(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        // Don't store empty arrays/objects as blobs
        if (node.isArray() && node.isEmpty()) return null;
        if (node.isObject() && node.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            log.warn("Failed to serialize JSON node: {}", e.getMessage());
            return null;
        }
    }
}
