package com.reactorx.repository;

import com.reactorx.entity.CartItem;
import com.reactorx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c JOIN FETCH c.product WHERE c.user = :user")
    List<CartItem> findByUser(@Param("user") User user);

    Optional<CartItem> findByUserAndProductId(User user, Long productId);

    void deleteByUser(User user);
}
