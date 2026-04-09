package com.reactorx.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
public class Product {

    @Id
    private Long id; // Use the frontend ID

    private String name;
    private BigDecimal price;
    private BigDecimal mrp;
    private Integer discountPercentage;
    private Double rating;
    private Integer reviewCount;
    @Column(name = "image_url")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String info;

    private String categorySlug;
    private String soldCount;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> statusTags;

    @Column(columnDefinition = "TEXT")
    private String mediaJson;

    @Column(columnDefinition = "TEXT")
    private String featureIconGridJson;

    @Column(columnDefinition = "TEXT")
    private String heroVideoJson;

    @Column(columnDefinition = "TEXT")
    private String featureStatsJson;

    @Column(columnDefinition = "TEXT")
    private String featureSectionsJson;

    @Column(columnDefinition = "TEXT")
    private String specsV2Json;

    @Column(columnDefinition = "TEXT")
    private String featureBannerTextJson;

    @Column(columnDefinition = "TEXT")
    private String featureBannerImageJson;

    @Column(columnDefinition = "TEXT")
    private String galleryBannersJson;

    @Column(columnDefinition = "TEXT")
    private String switchOptionsJson;

    @Column(columnDefinition = "TEXT")
    private String colorsJson;
}
