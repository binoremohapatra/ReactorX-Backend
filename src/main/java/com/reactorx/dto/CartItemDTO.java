package com.reactorx.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long productId;
    private String productName;
    private String productPrice;
    private String productImage;
    private int quantity;
}

