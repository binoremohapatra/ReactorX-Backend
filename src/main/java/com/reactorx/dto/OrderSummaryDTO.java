package com.reactorx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {
    private String id; // The OrderNumber (e.g., "KR-12345")
    private String date;
    private String total; // Formatted total as a String
    private String status;
    private List<OrderItemDTO> items;
}