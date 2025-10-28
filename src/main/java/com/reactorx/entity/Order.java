package com.reactorx.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Data @Table(name = "orders")
public class Order {
     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     private String orderNumber; // e.g., KR-12345
     private LocalDateTime orderDate;
     private BigDecimal totalAmount;
     private String status; // PROCESSING, SHIPPED, DELIVERED

     @ManyToOne
     @JoinColumn(name = "user_id")
     private User user;

     @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
     private List<OrderItem> items;

     // Shipping address details...
     private String shippingName;
     private String shippingAddress;
     private String shippingCity;
     // ...
}

