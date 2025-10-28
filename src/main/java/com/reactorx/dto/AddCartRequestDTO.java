// src/main/java/com/reactorx/dto/AddCartRequestDTO.java
package com.reactorx.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartRequestDTO {

    @NotNull
    private Long productId;

    @Min(1) // Must add at least one item
    private int quantity;
}