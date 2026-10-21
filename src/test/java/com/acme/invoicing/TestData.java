package com.acme.invoicing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;

import com.acme.invoicing.model.Customer;
import com.acme.invoicing.model.Order;
import com.acme.invoicing.model.OrderLine;

public final class TestData {
    private TestData() {
    }

    public static Order order(String id, String country, String currency, OrderLine... lines) {
        return new Order(id, LocalDate.of(2024, 1, 15), new Customer("C-1", "Test Customer"), country,
                Currency.getInstance(currency), Arrays.asList(lines));
    }

    public static OrderLine line(String sku, int quantity, String price) {
        return new OrderLine(sku, "Item " + sku, quantity, new BigDecimal(price));
    }
}
