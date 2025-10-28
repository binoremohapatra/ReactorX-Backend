package com.reactorx.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddressDTO {
    @NotEmpty
    private String shippingName;
    @NotEmpty
    private String shippingAddress;
    @NotEmpty
    private String shippingCity;
    @NotEmpty
    private String shippingState;
    @NotEmpty
    private String shippingZipCode;
    @NotEmpty
    private String shippingPhone;
}