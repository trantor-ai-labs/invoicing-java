package com.acme.invoicing.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Invoice {
    String number;
    LocalDate issueDate;
    Customer customer;
    String country;
    Currency currency;
    List<InvoiceLine> lines;
    BigDecimal subtotal;
    BigDecimal discountRate;
    BigDecimal discount;
    BigDecimal net;
    BigDecimal vatRate;
    BigDecimal vat;
    BigDecimal total;
    String checksum;
}
