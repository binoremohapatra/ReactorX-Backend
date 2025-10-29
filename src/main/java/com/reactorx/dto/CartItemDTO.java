package com.reactorx.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long productId;
    private String productName;
    private String productPrice; // Keep as String for compatibility with frontend
    private String productImage;
    private int quantity;

    // Optional: add total price if needed later
    public double getTotalPrice() {
        try {
            return Double.parseDouble(productPrice) * quantity;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
