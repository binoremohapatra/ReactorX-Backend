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

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "TEXT")
    private String info;

    private String categorySlug;
    private String soldCount;

    @ElementCollection
    private List<String> statusTags;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String mediaJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String featureIconGridJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String heroVideoJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String featureStatsJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String featureSectionsJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String specsV2Json;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String featureBannerTextJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String featureBannerImageJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String galleryBannersJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String switchOptionsJson;

    @Lob @Basic(fetch = FetchType.EAGER) @Column(columnDefinition = "TEXT")
    private String colorsJson;
}
