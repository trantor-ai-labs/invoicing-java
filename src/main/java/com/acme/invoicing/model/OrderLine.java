package com.acme.invoicing.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Value;

@Value
public class OrderLine {
    String sku;
    String description;
    int quantity;
    BigDecimal unitPrice;

    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
