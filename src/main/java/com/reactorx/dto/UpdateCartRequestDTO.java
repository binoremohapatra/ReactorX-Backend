package com.reactorx.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartRequestDTO {
     @Min(0) // Allow 0 to remove item
    private int quantity;
}

