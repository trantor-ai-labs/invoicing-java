package com.acme.invoicing.model;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class InvoiceLine {
    String sku;
    String description;
    int quantity;
    BigDecimal unitPrice;
    BigDecimal total;
}
