package com.acme.invoicing.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.acme.invoicing.model.Order;
import com.acme.invoicing.support.ListDiagnostics;

public class InMemoryOrderRepository implements OrderRepository {
    private final ArrayList<Order> orders = new ArrayList<>();

    @Override
    public void save(Order order) {
        orders.removeIf(existing -> existing.getId().equals(order.getId()));
        orders.add(order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return orders.stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    @Override
    public List<Order> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    @Override
    public String describe() {
        return "orders: " + ListDiagnostics.describe(orders);
    }
}
