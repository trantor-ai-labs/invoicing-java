package com.acme.invoicing.io;

import java.util.List;
import java.util.Optional;

import com.acme.invoicing.model.Order;

public interface OrderRepository {
    void save(Order order);

    Optional<Order> findById(String id);

    List<Order> findAll();

    String describe();
}
