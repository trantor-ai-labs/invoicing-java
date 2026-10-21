package com.acme.invoicing.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import lombok.Getter;

@Getter
public class Order {
    private final String id;
    private final LocalDate date;
    private final Customer customer;
    private final String country;
    private final Currency currency;
    private final List<OrderLine> lines;

    public Order(String id, LocalDate date, Customer customer, String country, Currency currency,
                 List<OrderLine> lines) {
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.country = country;
        this.currency = currency;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public int unitCount() {
        return lines.stream().mapToInt(OrderLine::getQuantity).sum();
    }
}
