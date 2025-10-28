package com.reactorx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactorx.dto.*; // Import all DTOs
import com.reactorx.entity.Order;
import com.reactorx.entity.OrderItem;
import com.reactorx.entity.Product;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.OrderRepository;
import com.reactorx.repository.ProductRepository;
import com.reactorx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;    // Assumes you have this
    private final ProductRepository productRepository; // Assumes you have this

    // ObjectMapper is thread-safe, so a single instance is fine
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final DateTimeFormatter DTO_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Creates and saves an order from a cart and shipping address.
     * This is called by PaymentController *after* payment is verified.
     */
    @Transactional
    public OrderSummaryDTO createOrderFromCart(List<CartItemDTO> userCart, AddressDTO shippingAddress, User currentUser) {

        logger.info("Creating order for user: {}", currentUser.getEmail());

        // 1. Create the Order entity
        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus("PROCESSING"); // Default status

        // 2. Set Shipping Address from the DTO
        newOrder.setShippingName(shippingAddress.getShippingName());
        newOrder.setShippingAddress(shippingAddress.getShippingAddress());
        newOrder.setShippingCity(shippingAddress.getShippingCity());
        // ... (Uncomment and add fields to your Order entity as needed)
        // newOrder.setShippingState(shippingAddress.getShippingState());
        // newOrder.setShippingZipCode(shippingAddress.getShippingZipCode());
        // newOrder.setShippingPhone(shippingAddress.getShippingPhone());

        // 3. Create OrderItem entities and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO cartItem : userCart) {
            BigDecimal itemPrice = new BigDecimal(cartItem.getProductPrice());
            int quantity = cartItem.getQuantity();

            // Find the full Product entity to link it
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found during order creation: " + cartItem.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(newOrder); // Link item to the order
            orderItem.setProduct(product); // Link the full product
            orderItem.setQuantity(quantity);
            orderItem.setPriceAtPurchase(itemPrice); // Store price at time of purchase

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemPrice.multiply(new BigDecimal(quantity)));
        }

        newOrder.setItems(orderItems); // Set the list on the order
        newOrder.setTotalAmount(totalAmount);

        // 4. Save the Order (This will also save OrderItems due to CascadeType.ALL on the Order entity)
        Order savedOrder = orderRepository.save(newOrder);

        // 5. Generate and set the OrderNumber (using the generated ID)
        savedOrder.setOrderNumber("KR-" + savedOrder.getId());
        savedOrder = orderRepository.save(savedOrder); // Save again to update the order number

        logger.info("Order {} created successfully for user {}", savedOrder.getOrderNumber(), currentUser.getEmail());

        // 6. Convert to DTO to return to frontend
        return mapOrderToSummaryDTO(savedOrder);
    }

    /**
     * Retrieves all orders for the currently logged-in user.
     */
    @Transactional(readOnly = true) // Use readOnly for GET methods
    public List<OrderSummaryDTO> getOrdersForCurrentUser() {
        logger.debug("Fetching orders for current user...");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        logger.debug("Found user {}, fetching orders.", currentUser.getEmail());
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(currentUser);

        return orders.stream()
                .map(this::mapOrderToSummaryDTO) // Use the helper method
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total price of a cart.
     * Used by PaymentController to create the Razorpay order.
     */
    public BigDecimal getCartTotal(List<CartItemDTO> userCart) {
        return userCart.stream()
                .map(item -> {
                    // Safety check for null or invalid price
                    try {
                        return new BigDecimal(item.getProductPrice()).multiply(new BigDecimal(item.getQuantity()));
                    } catch (NumberFormatException | NullPointerException e) {
                        logger.warn("Invalid price or quantity for product ID {} in cart, skipping.", item.getProductId());
                        return BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Helper to convert an Order entity to the OrderSummaryDTO.
     * This is now safer against potential NullPointerExceptions.
     */
    private OrderSummaryDTO mapOrderToSummaryDTO(Order order) {

        List<OrderItemDTO> itemDTOs;

        // ✅ SAFETY CHECK: Handle if an order has no items or the list is null
        if (order.getItems() == null || order.getItems().isEmpty()) {
            logger.warn("Order {} has no items or items list is null.", order.getId());
            itemDTOs = new ArrayList<>(); // Use an empty list
        } else {
            itemDTOs = order.getItems().stream()
                    .map(item -> {
                        // ✅ SAFETY CHECK: Handle if an order item is missing its product link
                        if (item.getProduct() == null) {
                            logger.error("CRITICAL: OrderItem {} (Order ID: {}) is missing its Product link. Skipping item.", item.getId(), order.getId());
                            return null; // Return null to filter it out
                        }

                        // ✅ SAFETY CHECK: Handle null price at purchase
                        BigDecimal price = item.getPriceAtPurchase() != null ? item.getPriceAtPurchase() : BigDecimal.ZERO;

                        return new OrderItemDTO(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                price, // Use the safe price
                                getProductImageFromMediaJson(item.getProduct())
                        );
                    })
                    .filter(Objects::nonNull) // Filter out any items that failed validation
                    .collect(Collectors.toList());
        }

        // ✅ SAFETY CHECKS: Provide defaults for potentially null fields
        String orderNum = order.getOrderNumber() != null ? order.getOrderNumber() : "KR-" + order.getId();
        String date = order.getOrderDate() != null ? order.getOrderDate().format(DTO_DATE_FORMATTER) : "N/A";
        String total = order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0.00";
        String status = order.getStatus() != null ? order.getStatus() : "UNKNOWN";

        return new OrderSummaryDTO(
                orderNum,
                date,
                total,
                status,
                itemDTOs
        );
    }

    /**
     * Helper to parse mediaJson from a Product and get the first image.
     */
    private String getProductImageFromMediaJson(Product product) {
        String defaultImage = "https://via.placeholder.com/100"; // Fallback image

        // ✅ SAFETY CHECK: Handle null product
        if (product == null) {
            return defaultImage;
        }

        String mediaJsonString = product.getMediaJson();
        if (mediaJsonString == null || mediaJsonString.isBlank()) {
            return defaultImage;
        }

        try {
            // Assuming MediaDTO is in com.reactorx.dto
            List<MediaDTO> mediaList = objectMapper.readValue(mediaJsonString, new TypeReference<List<MediaDTO>>() {});

            return mediaList.stream()
                    .filter(m -> m != null && "image".equalsIgnoreCase(m.getType()) && m.getSrc() != null && !m.getSrc().isBlank())
                    .map(MediaDTO::getSrc)
                    .findFirst()
                    .orElse(defaultImage);
        } catch (Exception e) {
            logger.error("Error parsing mediaJson for product ID {}: {}. JSON: '{}'", product.getId(), e.getMessage(), mediaJsonString);
            return defaultImage; // Return default if JSON is malformed
        }
    }
}