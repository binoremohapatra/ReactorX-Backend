package com.reactorx.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDTO {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
     private String name; // Optional for registration
}

