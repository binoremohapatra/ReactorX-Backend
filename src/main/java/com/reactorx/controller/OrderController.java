package com.reactorx.controller;

import com.reactorx.dto.AddressDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.OrderSummaryDTO;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.UserRepository;
import com.reactorx.service.CartService;
import com.reactorx.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final CartService cartService; // ✅ added to fetch DB-based cart

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    /**
     * GET /api/orders - fetch user’s past orders
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderSummaryDTO>> getOrders(Authentication authentication) {
        logger.info("Fetching orders for user: {}", authentication.getName());
        try {
            List<OrderSummaryDTO> orders = orderService.getOrdersForCurrentUser();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching orders for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/orders - create order from DB cart
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(@RequestBody(required = false) AddressDTO address,
                                         Authentication authentication) {
        try {
            String email = authentication.getName();
            logger.info("Order creation request from user: {}", email);

            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

            // ✅ Fetch user's cart directly from database
            List<CartItemDTO> userCart = cartService.getCartForUser(email);

            if (userCart == null || userCart.isEmpty()) {
                logger.warn("Cart is empty for user: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cart is empty.");
            }

            if (address == null) {
                logger.warn("Shipping address is missing for user: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Shipping address is missing.");
            }

            // ✅ Create the order using the service
            OrderSummaryDTO newOrder = orderService.createOrderFromCart(userCart, address, currentUser);

            logger.info("✅ Order successfully created for user {}", email);

            // Optionally clear the user's cart in DB after successful order
            cartService.removeAllItemsForUser(email);

            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Internal error during order creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error.");
        }
    }
}
