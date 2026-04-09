package com.reactorx.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug; // Use slug for frontend ID
    private String imageUrl;
    private long count; // Add product count
}

