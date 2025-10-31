package com.reactorx.dto;

import lombok.Builder; // Import Builder
import lombok.Data;
import lombok.NoArgsConstructor; // Add NoArgsConstructor
import lombok.AllArgsConstructor; // Add AllArgsConstructor

@Data
@Builder // 🌟 FIX for "Cannot resolve method 'builder'"
@NoArgsConstructor // Required by Lombok when using @Builder
@AllArgsConstructor // Required by Lombok when using @Builder
public class CartItemDTO {
    private Long productId;
    private String productName;
    private String productPrice;
    private String productImage;
    private int quantity;

    public double getTotalPrice() {
        try {
            return Double.parseDouble(productPrice) * quantity;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
