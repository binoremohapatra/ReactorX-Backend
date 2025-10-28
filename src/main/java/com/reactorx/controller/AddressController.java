package com.reactorx.controller;

import com.reactorx.dto.AddressDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // CRITICAL
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);
    private static final String ADDRESS_SESSION_KEY = "shippingAddress";

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> saveAddressToSession(@Valid @RequestBody AddressDTO address, HttpSession session) {
        session.setAttribute(ADDRESS_SESSION_KEY, address);
        logger.info("Shipping address saved to session {} for user", session.getId());
        return ResponseEntity.ok().build(); // Just return 200 OK
    }
}