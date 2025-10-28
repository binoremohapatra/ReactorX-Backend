package com.reactorx.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Entity @Data
public class Product {
    @Id
    private Long id; // Use the frontend ID

    private String name;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer discountPercentage;
    private Double rating;
    private Integer reviewCount;
    @Lob // Use @Lob for potentially long text descriptions
    @Column(columnDefinition = "TEXT")
    private String info;
    private String categorySlug; // Reference category by slug/id
    private String soldCount; // e.g., "5K+" - Keep as String for flexibility

    @ElementCollection // Simple list of strings
    private List<String> statusTags;

    // --- Handling Complex Fields (Choose ONE strategy per field) ---

    // Option 1: Store as JSON String in a TEXT/JSON/JSONB column
    @Lob @Column(columnDefinition = "TEXT") // Or use JSONB with converter
    private String mediaJson; // Store the List<Map<String, String>> as JSON

    @Lob @Column(columnDefinition = "TEXT")
    private String featureIconGridJson;

    @Lob @Column(columnDefinition = "TEXT")
    private String heroVideoJson; // Store Map<String, String>

    @Lob @Column(columnDefinition = "TEXT")
    private String featureStatsJson; // Store List<Map<String, String>>

    @Lob @Column(columnDefinition = "TEXT")
    private String featureSectionsJson;

    @Lob @Column(columnDefinition = "TEXT")
    private String specsV2Json; // Store Map<String, List<Map<String, String>>>

    @Lob @Column(columnDefinition = "TEXT")
    private String featureBannerTextJson;

     @Lob @Column(columnDefinition = "TEXT")
    private String featureBannerImageJson;

    @Lob @Column(columnDefinition = "TEXT")
    private String galleryBannersJson;

     @Lob @Column(columnDefinition = "TEXT")
    private String switchOptionsJson;

     @Lob @Column(columnDefinition = "TEXT")
    private String colorsJson;

    // Option 2: Separate Entities (More relational, better for querying)
    // @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Media> media;
    // @OneToMany(...)
    // private List<FeatureSection> featureSections;
    // ... etc. (Requires defining Media, FeatureSection entities)

    // Option 3: Use a library like hypersistence-utils for JSON mapping
    // @Type(JsonType.class)
    // @Column(columnDefinition = "jsonb")
    // private List<Map<String, Object>> media; // Map directly

}

