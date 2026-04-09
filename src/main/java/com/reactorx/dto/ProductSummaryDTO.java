package com.reactorx.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ProductSummaryDTO {
    private Long id;
    private String name;
    private String price; // Keep as String like frontend? Or use BigDecimal?
    private String mrp;
    private Integer discountPercentage;
    private Double rating;
    private String categorySlug;
    private MediaDTO primaryMedia;



}

